package alcatraz.server.state;

import alcatraz.shared.utils.Player;

import java.util.HashMap;
import java.io.Serializable;
import java.util.Map;

public class SharedState implements Serializable {

    public Map<String, Player> players = new HashMap<>();

    public LobbyManager lobbyManager = new LobbyManager();

    public SharedState() {}
}
