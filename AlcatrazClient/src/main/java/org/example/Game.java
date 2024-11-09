package org.example;

import java.rmi.RemoteException;

public class Game {
    Lobby lobby;

    public Game(Lobby lobby){
        this.lobby = lobby;
    }

    public void start(){
        int counter = 0;
        lobby.getPlayers().forEach((key, value) -> {
            System.out.println(key + ": " + value.getClientName());
        });
    }


}
