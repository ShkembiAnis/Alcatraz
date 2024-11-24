package alcatraz.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface ServerInterface extends Remote {
    void registerPlayer(String playerName, ClientInterface client) throws RemoteException;
    LobbyKey createLobby(Player owner) throws RemoteException;
    void joinLobby(Player client, Long lobbyId) throws RemoteException;
    Map<Long, Lobby> getLobbies() throws RemoteException;
    void initializeGameStart(long lobbyId) throws RemoteException;
    // TODO: leave lobby, remove lobby
}
