package alcatraz.server;

import java.util.HashMap;
import java.util.Map;

import alcatraz.shared.Lobby;
import alcatraz.shared.Player;

public class LobbyManager {
    private Map<Long, Lobby> lobbies;

    public LobbyManager() {
        this.lobbies = new HashMap<>();
    }

    public void createLobby(long lobbyId,Lobby lobby) {
        lobbies.put(lobbyId, lobby);
    }
    public void removeLobby(long lobbyId) {
        lobbies.remove(lobbyId);
    }
    public void setLobbies(Map<Long, Lobby> lobbies) {
        this.lobbies = lobbies; // added for updating local state when primary updates backups
    }
    public Map<Long, Lobby> getLobbies() {
        return lobbies;
    }
    public Lobby getLobbyById(long lobbyId){
        return lobbies.get(lobbyId);
    }
    public Boolean addPlayerToLobby(long lobbyId, Player player){
        return lobbies.get(lobbyId).addPlayer(player); // vielleicht kann das ein void sein mit exception?
    }
}
