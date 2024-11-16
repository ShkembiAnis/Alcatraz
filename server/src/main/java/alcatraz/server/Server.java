package alcatraz.server;

import alcatraz.shared.Player;
import alcatraz.shared.ClientInterface;
import alcatraz.shared.ServerInterface;
import alcatraz.shared.Lobby;
import spread.*;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Server extends UnicastRemoteObject implements ServerInterface, ReplicationInterface{

    private static long lobbyIdCounter = 0;
    private final Map<String, Player> players = new HashMap<>();
    private final LobbyManager lobbyManager;

    private final SpreadConnection connection;
    private SpreadGroup group;
    private String serverName;
    private boolean isPrimary;

    protected Server(LobbyManager lobbyManager) throws RemoteException {
        super();
        this.lobbyManager = lobbyManager;
        this.serverName = serverName;
        this.isPrimary = false;
        this.connection = new SpreadConnection();
        joinServerGroup();
    }

    @Override
    public boolean registerPlayer(String playerName, ClientInterface client) throws RemoteException {
        if (isPrimary) {
            if (players.containsKey(playerName)) {
                return false;
            } else {
                // Store the Player with ClientInterface
                players.put(playerName, new Player(client, playerName, "ip_address", "port"));
                System.out.println(playerName + " registered");

                return true;
            }
        } else {
            // Forward the request to the primary server
            forwardToPrimary(new RegistrationRequest("registerPlayer", playerName, client));
            return false;
        }
    }

    @Override
    public Lobby createLobby(String clientName) throws RemoteException {
        if (isPrimary) {
            lobbyIdCounter++;
            long lobbyId = lobbyIdCounter;
            Map<String, Player> lobbyPlayers = new HashMap<>();
            lobbyPlayers.put(clientName, players.get(clientName));
            Lobby lobby = new Lobby(lobbyId, lobbyPlayers, clientName);
            lobbyManager.createLobby(lobbyId, lobby);
            System.out.println("Lobby created with ID: " + lobbyId + " by Player: " + clientName);

            // Replication logic: Update backups with the new lobbies state
            update(lobbyManager.getLobbies());

            return lobby;
        } else {
            // Forward the request to the primary server
            forwardToPrimary(new RegistrationRequest("createLobby", clientName));
            return null;
        }
    }

    @Override
    public boolean joinLobby(String clientName, Long lobbyId) throws RemoteException {
        if (isPrimary) {
            if (lobbyManager.getLobbies().containsKey(lobbyId) &&
                    lobbyManager.getLobbyById(lobbyId).getPlayers().size() < 4) {

                lobbyManager.addPlayerToLobby(lobbyId, players.get(clientName));
                System.out.println("Player " + clientName + " joined lobby " + lobbyId);

                // Replication logic: Update backups with the new lobbies state
                update(lobbyManager.getLobbies());

                return true;
            }
        } else {
            // Forward the request to the primary server
            forwardToPrimary(new RegistrationRequest("joinLobby", clientName, lobbyId));
        }
        return false;
    }

    @Override
    public Map<Long, Lobby> getLobbies() throws RemoteException {
        return lobbyManager.getLobbies();
    }


    @Override
    public void initializeGameStart(long lobbyId) throws RemoteException {
        if (isPrimary) {
            Lobby lobby = lobbyManager.getLobbyById(lobbyId);
            lobby.getPlayers().forEach((key, value) -> {
                try {
                    ClientInterface client = value.getClient();
                    if (client != null) {
                        System.out.println("Starting game for client: " + value.getClientName() + " in lobby " + lobby.getId());
                        client.startGame(lobby);
                    } else {
                        System.out.println("ClientInterface not found for player: " + value.getClientName());
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });
            // No need to replicate here if lobbies haven't changed
        } else {
            // Forward the request to the primary server
            forwardToPrimary(new RegistrationRequest("initializeGameStart", lobbyId));
        }

    }



    private Boolean checkIfUsernameExists(String playerName){
        for (String key : players.keySet()) {
            if(playerName.equals(key)){
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param lobbies
     * @param playerId
     * @return true if player is not in a lobby, else false
     */
    private  boolean isPlayerInAnyLobby(Map<Long, List<String>> lobbies, String playerId){
        for (List<String> players : lobbies.values()) {
            if (players.contains(playerId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void joinServerGroup() {

    }

    @Override
    public void forwardToPrimary(Object request) {

    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {

    }

    @Override
    public void electNewPrimary(MembershipInfo info) {
        SpreadGroup[] members = info.getMembers();
        String primaryName = serverName;
        for (SpreadGroup member : members) {
            String memberName = member.toString();
            if (memberName.compareTo(primaryName) < 0) {
                primaryName = memberName;
            }
        }
        if (serverName.equals(primaryName)) {
            isPrimary = true;
            System.out.println(serverName + " is now the primary server.");

            // As the new primary, update backup servers with the current lobbies
            update(lobbyManager.getLobbies());

        } else {
            isPrimary = false;
            System.out.println(serverName + " is a backup server.");
        }
    }

    @Override
    public void update(Object state) {
        try {
            SpreadMessage msg = new SpreadMessage();
            msg.setReliable();
            msg.addGroup("ServerGroup");
            msg.setObject((Serializable) state);
            connection.multicast(msg);
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void regularMessageReceived(SpreadMessage message) {
        try {
            Object receivedObject = message.getObject();

            if (receivedObject instanceof Map) {
                // Received lobbies update from primary
                Map<Long, Lobby> updatedLobbies = (Map<Long, Lobby>) receivedObject;
                lobbyManager.setLobbies(updatedLobbies);
                System.out.println(serverName + " updated lobbies from primary server.");

            } else if (receivedObject instanceof RegistrationRequest) {
                // Handle requests forwarded by backup servers (if this server is primary)
                if (isPrimary) {
                    RegistrationRequest request = (RegistrationRequest) receivedObject;
                    handleForwardedRequest(request);
                }
            }
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    private void handleForwardedRequest(RegistrationRequest request) {
        try {
            switch (request.getMethodName()) {
                case "registerPlayer":
                    registerPlayer(request.getClientName(), request.getClient());
                    break;
                case "createLobby":
                    createLobby(request.getClientName());
                    break;
                case "joinLobby":
                    joinLobby(request.getClientName(), request.getLobbyId());
                    break;
                case "initializeGameStart":
                    initializeGameStart(request.getLobbyId());
                    break;
                default:
                    System.out.println("Unknown request method: " + request.getMethodName());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
