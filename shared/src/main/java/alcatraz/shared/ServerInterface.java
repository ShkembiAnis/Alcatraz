package alcatraz.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface ServerInterface extends Remote {
    void registerPlayer(String playerName, ClientInterface client) throws RemoteException;
    LobbyKey createLobby(Player owner) throws RemoteException;
    void joinLobby(Player client, Long lobbyId) throws RemoteException;
    void leaveLobby(Player client) throws RemoteException;
    Map<Long, Lobby> getLobbies() throws RemoteException;
    void initializeGameStart(long lobbyId, String secret) throws RemoteException;
    // TODO: leave lobby, remove lobby
}
