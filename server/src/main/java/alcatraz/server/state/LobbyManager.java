package alcatraz.server.state;

import java.util.HashMap;
import java.util.UUID;

import alcatraz.shared.Lobby;
import alcatraz.shared.LockedLobby;
import alcatraz.shared.Player;

public class LobbyManager {
    private HashMap<Long, Lobby> lobbies;
    private long lobbyIdCounter = 0;


    public LobbyManager() {
        this.lobbies = new HashMap<>();
    }

    public LockedLobby createLobby(String ownerName) {
        while (lobbies.containsKey(lobbyIdCounter)) {
            ++lobbyIdCounter;
        }

        final String secretToken = UUID.randomUUID().toString();
        Lobby newLobby = new Lobby(lobbyIdCounter, secretToken, ownerName);
        lobbies.put(lobbyIdCounter, newLobby);

        return new LockedLobby(lobbyIdCounter, secretToken);
    }

    public void removeLobby(long lobbyId) {
        lobbies.remove(lobbyId);
    }
    public void setLobbies(HashMap<Long, Lobby> lobbies) {
        this.lobbies = lobbies; // added for updating local state when primary updates backups
    }
    public HashMap<Long, Lobby> getLobbies() {
        return lobbies;
    }
    public Lobby getLobbyById(long lobbyId){
        return lobbies.get(lobbyId);
    }
    public Boolean addPlayerToLobby(long lobbyId, Player player){
        // TODO: it needs to be checked of there is still space for another player, maximal 4 players
        return lobbies.get(lobbyId).addPlayer(player); // vielleicht kann das ein void sein mit exception?
    }
}
