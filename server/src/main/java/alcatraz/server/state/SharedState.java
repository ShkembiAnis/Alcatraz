package alcatraz.server.state;

import alcatraz.shared.Player;

import java.util.HashMap;
import java.io.Serializable;

public class SharedState implements Serializable {

    public HashMap<String, Player> players = new HashMap<>();
    public LobbyManager lobbyManager = new LobbyManager();

    public SharedState() {}
}
