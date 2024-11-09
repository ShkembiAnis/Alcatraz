package org.example;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ClientInterface extends Remote {

    void startGame(Lobby lobby) throws RemoteException;

    void receiveMove(String move) throws RemoteException;

    void notifyStart(String lobbyId, List<ClientInterface> players) throws RemoteException;

    boolean doMove(Player player, String Move) throws RemoteException;

    void getMessage(String message) throws RemoteException;

    void nextTurn() throws RemoteException;

    // additonal methods
    /*
     * void nextTurn() throws RemoteException; // to notify the next player to start
     * their turn
     * extend doMove() to include broadcasting the move to all players // send the
     * move to all players
     * 
     */
}
