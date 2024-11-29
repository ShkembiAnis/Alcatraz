package alcatraz.server.state;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.UUID;

import alcatraz.shared.Lobby;
import alcatraz.shared.LobbyKey;
import alcatraz.shared.LobbyLockedException;
import alcatraz.shared.TooManyLobbiesException;
import alcatraz.shared.Player;

public class LobbyManager {
    private HashMap<Long, Lobby> lobbies = new HashMap<>();
    private HashMap<String, Long> lobbyByPlayer = new HashMap<>();
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
            throw new TooManyLobbiesException();
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

    public HashMap<Long, Lobby> getAllLobbies() { return lobbies; }

    public HashMap<Long, Lobby> getAvailableLobbies() {
        HashMap<Long, Lobby>    availableLobbies = new HashMap<>();

        for (Long lobbyId : lobbies.keySet()) {
            final Lobby currentLobby = lobbies.get(lobbyId);
            if (currentLobby.isAvailable() && !currentLobby.isFull()) {
                availableLobbies.put(lobbyId, currentLobby);
            }
        }

        return availableLobbies;
    }

    public Lobby getLobbyById(long lobbyId){
        return lobbies.get(lobbyId);
    }

    public void addPlayerToLobby(long lobbyId, Player player) throws RemoteException {
        //MM20241124: think about case, where player can already be found in respective lobby!
        if (lobbyByPlayer.containsKey(player.getClientName())) {
            removePlayerFromLobby(player.getClientName());
            System.out.println("Removed player " + player.getClientName() + " from lobby " + lobbyId + ".");
        }

        lobbies.get(lobbyId).addPlayer(player);
        lobbyByPlayer.put(player.getClientName(), lobbyId);

        System.out.println("Player '" + player.getClientName() + "' added to lobby " + lobbyId);
    }

    //MM20241127: not LobbyManager
    public void removePlayerFromLobby(String playerName) throws LobbyLockedException {
        final Long previousLobbyId = lobbyByPlayer.get(playerName);
        lobbies.get(previousLobbyId).removePlayer(playerName);
        if (lobbies.get(previousLobbyId).getPlayers().isEmpty()) {
            removeLobby(previousLobbyId);
        }
        lobbyByPlayer.remove(playerName);

        System.out.println("Player '" + playerName + "' removed from lobby " + previousLobbyId);
    }
}
