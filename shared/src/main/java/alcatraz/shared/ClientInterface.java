package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ClientInterface extends Remote {

    void startGame(Lobby lobby) throws RemoteException;

    void receiveMove(String move) throws RemoteException;

    void notifyStart(String lobbyId, List<ClientInterface> players) throws RemoteException;

    boolean doMove(Player player, String Move)throws RemoteException;

    void getMessage(String message) throws  RemoteException;
}
