package alcatraz.server.replication.dto;

import alcatraz.shared.Lobby;
import alcatraz.shared.Player;

import java.io.Serializable;
import java.util.HashMap;

public class ReplicationDTO implements Serializable {
    private HashMap<Long, Lobby> lobbies = new HashMap<>();
    private HashMap<String, Player> players = new HashMap<>();

    public ReplicationDTO(HashMap<Long, Lobby> lobbies, HashMap<String, Player> players) {
        this.lobbies = lobbies;
        this.players = players;
    }

    public HashMap<String, Player> getPlayers() {
        return players;
    }

    public HashMap<Long, Lobby> getLobbies() {
        return lobbies;
    }
}
