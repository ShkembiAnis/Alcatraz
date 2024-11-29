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
    private boolean isAvailable = true;            //MM20241124: needed in order to allow adding and removing players
    private static final long MINSIZE = 2;
    private static final long MAXSIZE = 4;

    public Lobby(long id, String ownerId, String secretToken) {
        this.id = id;
        this.players = new HashMap<>();
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

    public void addPlayer(Player player) throws RemoteException {       //MM20241124: analyse need for availability check!
        if (!this.isAvailable) {
            throw new LobbyLockedException("Cannot add player " + player.getClientName() + " to lobby " + this.id + ".");
        }

        if (this.players.size() >= MAXSIZE) {
            throw new LobbyFullException(this.id);
        }

        this.players.put(player.getClientName(), player);
    }

    public boolean checkSecret(String secret) {     //MM20241127: keep silent?
        return this.secret.equals(secret);
    }

    //MM20241127: simulate default function parameters
    public void removePlayer(String clientName) throws LobbyLockedException {
        removePlayer(clientName, false);
    }

    public void removePlayer(String clientName, boolean force) throws LobbyLockedException {
        if (!this.isAvailable && !force) {
            System.out.println("Player " + "'" + clientName + "' cannot leave lobby " + this.id + ".");
            throw new LobbyLockedException("Cannot remove player from lobby.");
        }
        this.players.remove(clientName);
    }

    public void setUnavailable() { this.isAvailable = false; }

    public void setAvailable() { this.isAvailable = true; }

    public boolean isAvailable() { return this.isAvailable; }

    public boolean isFull() { return this.players.size() == MAXSIZE; }

    public boolean canBePlayed(String secret) { return this.isAvailable()
                                                        && this.checkSecret(secret)
                                                        && this.players.size() <= MAXSIZE
                                                        && this.players.size() >= MINSIZE; }     //MM20241124: look up game size!
}
