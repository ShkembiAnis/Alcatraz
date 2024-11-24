package alcatraz.server.state;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.UUID;

import alcatraz.shared.Lobby;
import alcatraz.shared.LobbyKey;
import alcatraz.shared.Player;

public class LobbyManager {
    private HashMap<Long, Lobby> lobbies;
    private HashMap<String, Long> lobbyByPlayer;    //MM20241122: Each player can only be registered in one lobby.
    private long lobbyIdCounter;
    private static final long MAXSIZE = 100;

    public LobbyManager() {
        this.lobbyIdCounter = 0;
        this.lobbies = new HashMap<>();
        this.lobbyByPlayer = new HashMap<>();
    }

    /*
    * @brief: Construct a new lobby and assign it to the creator's name.
    *           Several lobbies can be created by the same owner but only the creator will get the secret.
    *           When an owner creates a new lobby and they have been inside a lobby before, they will be relocated to
    *               the newly created lobby.
     */
    public LobbyKey createLobby(Player owner) throws RemoteException {
        if (lobbies.size() == MAXSIZE) {
            throw new RemoteException();        //MM20241122: find better Exception! "MAXSIZE reached"
        }

        while (lobbies.containsKey(lobbyIdCounter)) {
            ++lobbyIdCounter;
        }
        //MM20241122: Each player can only be in one lobby.
        removePlayerFromLobby(owner.getClientName());

        //MM20241122: create lobby and register creator
        final String secretToken = UUID.randomUUID().toString();
        Lobby newLobby = new Lobby(lobbyIdCounter, secretToken, owner.getClientName());
        newLobby.addPlayer(owner);
        lobbies.put(lobbyIdCounter, newLobby);

        //MM20241122: register the most recently created lobby
        lobbyByPlayer.put(owner.getClientName(), lobbyIdCounter);

        return new LobbyKey(lobbyIdCounter, secretToken);
    }

    private void removeLobby(long lobbyId) {
        lobbies.remove(lobbyId);
    }

    public void setLobbies(HashMap<Long, Lobby> lobbies) {
        this.lobbies = lobbies; // added for updating local state when primary updates backups;
    }

    public HashMap<Long, Lobby> getLobbies() {
        return lobbies;
    }

    public Lobby getLobbyById(long lobbyId){
        return lobbies.get(lobbyId);
    }

    public void addPlayerToLobby(long lobbyId, Player player) throws RemoteException {
        lobbies.get(lobbyId).addPlayer(player);
    }

    public void removePlayerFromLobby(String playerName) throws RemoteException {
        lobbies.get(lobbyByPlayer.get(playerName)).removePlayer(playerName);
        if (lobbies.get(lobbyByPlayer.get(playerName)).getPlayers().isEmpty()) {
            removeLobby(lobbyByPlayer.get(playerName));
        }
    }
}
