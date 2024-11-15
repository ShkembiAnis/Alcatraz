package alcatraz.server;

import shared.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Server extends UnicastRemoteObject implements ServerInterface {

    private static long lobbyIdCounter = 0;
    private Map<String, Player> players = new HashMap<>();
    private LobbyManager lobbyManager;

    protected Server(LobbyManager lobbyManager) throws RemoteException {
        this.lobbyManager = lobbyManager;
    }

    @Override
    public boolean registerPlayer(String playerName, ClientInterface client) throws RemoteException {
        if(checkIfUsernameExists(playerName)) {
            return false;
        }else{
            players.put(playerName, new Player(client, playerName,"ip_address", "port"));
            System.out.println(playerName + " registered");
            System.out.println("Updated Players list: ");
            players.forEach((key, value) -> System.out.println(value.getClientName()));
            return true;
        }
    }

    @Override
    public Lobby createLobby(String clientName) throws RemoteException {
        lobbyIdCounter++;
        long lobbyId = lobbyIdCounter;
        Map<String, Player> lobbyPlayers =  new HashMap<>();
        lobbyPlayers.put(clientName,players.get(clientName));
        Lobby lobby = new Lobby(lobbyId, lobbyPlayers, clientName);
        lobbyManager.createLobby(lobbyId, lobby);
        System.out.println("Lobby created with ID: " + lobbyId + " by Player: " + clientName);
        return lobby;
    }

    @Override
    public boolean joinLobby(String clientName, Long lobbyId) throws RemoteException {
        if (lobbyManager.getLobbies().containsKey(lobbyId) && lobbyManager.getLobbyById(lobbyId).getPlayers().size() < 4) {
            lobbyManager.addPlayerToLobby(lobbyId,players.get(clientName));
            System.out.println("Player " + clientName + " joined lobby " + lobbyId);
            return true;
        }
        return false;
    }

    @Override
    public Map<Long, Lobby> getLobbies() throws RemoteException {
        return lobbyManager.getLobbies();
    }


    @Override
    public void initializeGameStart(long lobbyId) throws RemoteException {
        Lobby lobby = lobbyManager.getLobbyById(lobbyId);
        lobby.getPlayers().forEach((key, value) -> {
            try {
                System.out.println("Starting game for client:" + value.getClientName()+ " in lobby" + lobby.getId()) ;
                value.getClient().startGame(lobby);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

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
}
