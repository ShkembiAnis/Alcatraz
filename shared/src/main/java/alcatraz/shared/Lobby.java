//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package alcatraz.shared;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;

public class Lobby implements Serializable {
    public final long id;
    private HashMap<String, Player> players;
    public final String ownerId;
    private final String secret;
    private static final long MAXSIZE = 4;

    public Lobby(long id, String ownerId, String secretToken) {
        this.id = id;
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

    public void addPlayer(Player player) throws RemoteException {
        if (this.players.size() < MAXSIZE) {
            this.players.put(player.getClientName(), player);
        } else {
            throw new RemoteException();      //MM20241121: implement appropriate exception
        }
    }

    public boolean checkSecret(String secret) {
        return this.secret.equals(secret);
    }

    public void removePlayer(String clientName) {
        this.players.remove(clientName);
    }

    public long countPlayers() {
        return this.players.size();
    }
}
