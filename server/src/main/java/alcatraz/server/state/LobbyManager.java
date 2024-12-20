package alcatraz.server.state;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import alcatraz.shared.utils.Lobby;
import alcatraz.shared.utils.LobbyKey;
import alcatraz.shared.exceptions.LobbyLockedException;
import alcatraz.shared.exceptions.TooManyLobbiesException;
import alcatraz.shared.utils.Player;

public class LobbyManager implements Serializable {
    private Map<Long, Lobby> lobbies = Collections.synchronizedMap(new HashMap());
    private Map<String, Long> lobbyByPlayer = Collections.synchronizedMap(new HashMap());
    private long lobbyIdCounter = 0;        //MM20241205: 0 is a magic number for the client; however, will be checked
                                            //              upon calculation.
    private static final long MAXSIZE = 100;

    /*
    * @brief: ensure that we have no problems with the lobbyId's by taking the next free lobbyId and restarting the
    *           search at one, once the highest possible lobbyId (MAXSIZE) was found
     */
    private void determineFreeLobbyIdCounter() {
        while (lobbyIdCounter == 0 || lobbies.containsKey(lobbyIdCounter)) {
            lobbyIdCounter = (++lobbyIdCounter) % (MAXSIZE + 1);    //MM20241205: MAXSIZE is the highest lobbyId;
                                                                    //              (x % (MAXSIZE + 1)) == 0,
                                                                    //              when x == (MAXSIZE + 1)
        }
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

        determineFreeLobbyIdCounter();      //MM20241205: not entirely happy, because no return value, but should work

        //MM20241122: Each player can only be in one lobby.
        removePlayerFromLobby(owner.getClientName());

        //MM20241122: create lobby and register creator
        final String secretToken = UUID.randomUUID().toString();
        Lobby newLobby = new Lobby(this.lobbyIdCounter,owner.getClientName(), secretToken);
        lobbies.put(this.lobbyIdCounter, newLobby);

        addPlayerToLobby(newLobby.id, owner);

        return new LobbyKey(this.lobbyIdCounter, secretToken);
    }

    private void removeLobby(long lobbyId) {
        lobbies.remove(lobbyId);
    }

    public void setLobbies(Map<Long, Lobby> lobbies) {
        this.lobbies = lobbies; // added for updating local state when primary updates backups;
    }

    public Map<Long, Lobby> getAllLobbies() { return lobbies; }

    public Map<Long, Lobby> getAvailableLobbies() {
        Map<Long, Lobby>    availableLobbies = new HashMap<>();

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
        Long currentLobbyId = lobbyByPlayer.get(player.getClientName());
        if (currentLobbyId != null) {
            if (currentLobbyId == lobbyId) {
                return;
            }
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
        if(previousLobbyId == null){
            return;
        }
        lobbies.get(previousLobbyId).removePlayer(playerName);
        if (lobbies.get(previousLobbyId).getPlayers().isEmpty()) {
            removeLobby(previousLobbyId);
        }
        lobbyByPlayer.remove(playerName);
        System.out.println("Player '" + playerName + "' removed from lobby " + previousLobbyId);
    }
}
