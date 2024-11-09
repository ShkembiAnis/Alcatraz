package org.example;

import java.rmi.RemoteException;

public class Game {
    Lobby lobby;
    Gameboard gameboard;

    public Game(Lobby lobby) {
        this.lobby = lobby;
        this.gameboard = new Gameboard();
    }

    public void start() {
        showGameboard();
        int counter = 0;
        lobby.getPlayers().forEach((key, value) -> {
            System.out.println(key + ": " + value.getClientName());
        });
    }

    public void showGameboard() {
        gameboard.displayBoard();

    }

    // additional methods for moving figures and determining the winner
    /*
     * public void moveFigure(int player, int figure, int steps) { // Move figure on
     * the gameboard }
     * nextPlayer() { // Switch to next player }
     * 
     * 
     * 
     */
}
