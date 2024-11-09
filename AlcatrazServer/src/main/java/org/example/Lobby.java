//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.example;

import java.io.Serializable;
import java.util.Map;

public class Lobby implements Serializable {
    private long id;
    private Map<String, Player> players;
    private String ownerId;

    public Lobby(long id, Map<String, Player> players, String ownerId) {
        this.id = id;
        this.players = players;
        this.ownerId = ownerId;
    }

    public long getId() {
        return this.id;
    }

    public Map<String, Player> getPlayers() {
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

    public boolean removePlayer(String clientName) {
        this.players.remove(clientName);
        if (this.players.get(clientName) == null) {
            return true;
        } else {
            return false;
        }
    }
}
