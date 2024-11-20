package alcatraz.server;

import alcatraz.server.replication.ReplicationInterface;
import alcatraz.server.state.SharedState;
import alcatraz.shared.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Server extends UnicastRemoteObject implements ServerInterface {

    private final SharedState state;
    private final ReplicationInterface replication;

    public Server(ReplicationInterface replication) throws RemoteException {
        super();
        this.replication = replication;
        this.state = replication.getSharedState();
    }

    // TODO: instead of return boolean, throw an exception if player already exists
    @Override
    public boolean registerPlayer(String clientName, ClientInterface client) throws RemoteException {

        if (this.replication.isPrimary()) {
            if (this.state.players.containsKey(clientName)) {
                // TODO: throw an exception instead of returning false
                return false;
            }
            // Store the Player with ClientInterface
            this.state.players.put(clientName, new Player(client, clientName, "ip_address", "port"));
            System.out.println(clientName + " registered");
            this.replication.replicatePrimaryState();
            return true;
            // TODO: there should be an early return instead of else
        } else {
            // Forward the request to the primary server
            return this.replication.getPrimaryServer().registerPlayer(clientName, client);
        }
    }

    @Override
    public LockedLobby createLobby(String clientName) throws RemoteException {
        if (this.replication.isPrimary()) {
            LockedLobby newLobby = this.state.lobbyManager.createLobby(clientName);
            System.out.println("Lobby created with ID: " + newLobby.id + " by Player: " + clientName);

            // Replication logic: Update backups with the new lobbies state
            this.replication.replicatePrimaryState();

            return newLobby;
        } else {
            // Forward the request to the primary server
            return this.replication.getPrimaryServer().createLobby(clientName);
        }
    }

    // TODO: refactor nested if statements
    @Override
    public boolean joinLobby(String clientName, Long lobbyId) throws RemoteException {
        if (this.replication.isPrimary()) {
            if (this.state.lobbyManager.getLobbies().containsKey(lobbyId) &&
                    this.state.lobbyManager.getLobbyById(lobbyId).getPlayers().size() < 4) {

                this.state.lobbyManager.addPlayerToLobby(lobbyId, this.state.players.get(clientName));
                System.out.println("Player " + clientName + " joined lobby " + lobbyId);

                // Replication logic: Update backups with the new lobbies state
                this.replication.replicatePrimaryState();

                return true;
            }
        } else {
            // Forward the request to the primary server
            return this.replication.getPrimaryServer().joinLobby(clientName, lobbyId);
        }

        return false;
    }

    @Override
    public Map<Long, Lobby> getLobbies() throws RemoteException {
        return this.state.lobbyManager.getLobbies();
    }


    @Override
    public void initializeGameStart(long lobbyId) throws RemoteException {
        if (this.replication.isPrimary()) {
            Lobby lobby = this.state.lobbyManager.getLobbyById(lobbyId);
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
            this.replication.getPrimaryServer().initializeGameStart(lobbyId);
        }

    }


    // TODO: method not used
    private Boolean checkIfUsernameExists(String playerName) {
        for (String key : this.state.players.keySet()) {
            if (playerName.equals(key)) {
                return true;
            }
        }
        return false;
    }


    // TODO: method not used

    /**
     * @param lobbies
     * @param playerId
     * @return true if player is not in a lobby, else false
     */
    private boolean isPlayerInAnyLobby(Map<Long, List<String>> lobbies, String playerId) {
        for (List<String> players : lobbies.values()) {
            if (players.contains(playerId)) {
                return true;
            }
        }
        return false;
    }
}