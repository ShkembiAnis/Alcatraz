package alcatraz.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface ServerInterface extends Remote {
    boolean registerPlayer(String playerName, ClientInterface client) throws RemoteException;
    Lobby createLobby(String clientName) throws RemoteException;
    boolean joinLobby(String clientName, Long lobbyId) throws RemoteException;
    Map<Long, Lobby> getLobbies() throws RemoteException;
    void initializeGameStart(long lobbyId) throws RemoteException;

}
