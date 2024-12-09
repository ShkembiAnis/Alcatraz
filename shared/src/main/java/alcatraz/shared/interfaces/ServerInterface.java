package alcatraz.shared.interfaces;

import alcatraz.shared.utils.Lobby;
import alcatraz.shared.utils.LobbyKey;
import alcatraz.shared.utils.Player;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

public interface ServerInterface extends Remote {
    void registerPlayer(String playerName, ClientInterface client) throws RemoteException;
    LobbyKey createLobby(String ownerName) throws RemoteException;
    void joinLobby(String clientName, Long lobbyId) throws RemoteException;
    void leaveLobby(String clientName) throws RemoteException;
    Map<Long, Lobby> getLobbies(String clientName) throws RemoteException;

    ArrayList<Player> initializeGameStart(long lobbyId, String secret) throws RemoteException;
    // TODO: leave lobby, remove lobby
}
