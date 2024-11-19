package alcatraz.server.state;

import alcatraz.shared.Player;

import java.util.HashMap;
import java.io.Serializable;

public class SharedState implements Serializable {

    public HashMap<String, Player> players = new HashMap<>();
    public LobbyManager lobbyManager = new LobbyManager();

    // TODO: should it not be shared too? all the servers will start from 0...
    public static long lobbyIdCounter = 0;

    public SharedState() {}
}
