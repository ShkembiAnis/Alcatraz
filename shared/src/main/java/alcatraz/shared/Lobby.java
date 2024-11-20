//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package alcatraz.shared;

import java.io.Serializable;
import java.util.HashMap;

public class Lobby implements Serializable {
    public final long id;
    private HashMap<String, Player> players;
    public final String ownerId;
    private final String secret;

    public Lobby(long id, String ownerId, String secretToken) {
        this.id = id;
        this.players = players;
        this.ownerId = ownerId;
        this.secret = secretToken;
    }

    public long getId() {
        return this.id;
    }

    public HashMap<String, Player> getPlayers() {
        return this.players;
    }

    public String getOwner() {
        return this.ownerId;
    }

    public boolean addPlayer(Player player) {
        if (this.players.size() < 4) {
            this.players.put(player.getClientName(), player);
            return true;
        } else {
            return false;
        }
    }

    public boolean checkSecret(String secret) {
        return this.secret.equals(secret);
    }

    public void removePlayer(String clientName) {
        this.players.remove(clientName);
    }
}
