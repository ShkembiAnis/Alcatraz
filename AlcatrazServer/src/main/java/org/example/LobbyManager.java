package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LobbyManager {
    private Map<Long, Lobby> lobbies;

    public LobbyManager() {
        this.lobbies = new HashMap<>();
    }

    public void createLobby(long lobbyId, Lobby lobby) {
        lobbies.put(lobbyId, lobby);
    }

    public void removeLobby(long lobbyId) {
        lobbies.remove(lobbyId);
    }

    public Map<Long, Lobby> getLobbies() {
        return lobbies;
    }

    public Lobby getLobbyById(long lobbyId) {
        return lobbies.get(lobbyId);
    }

    public Boolean addPlayerToLobby(long lobbyId, Player player) {
        return lobbies.get(lobbyId).addPlayer(player);
    }

    public Boolean removePlayerFromLobby(long lobbyId, Player player) {
        return lobbies.get(lobbyId).removePlayer(player.getClientName());
    }
}
