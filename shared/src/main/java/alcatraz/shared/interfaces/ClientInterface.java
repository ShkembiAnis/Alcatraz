package alcatraz.shared.interfaces;

import at.falb.games.alcatraz.api.Player;
import at.falb.games.alcatraz.api.Prisoner;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ClientInterface extends Remote {
    void startGame(ArrayList<alcatraz.shared.utils.Player> players, int myLobbyPlayerId) throws RemoteException;
    void doMove(Player playerId, Prisoner prisoner, int rowOrCol, int row, int col) throws RemoteException;
    void broadcastMove(at.falb.games.alcatraz.api.Player player, Prisoner prisoner, int rowOrCol, int row, int col) throws RemoteException;
    void isPresent() throws RemoteException;
}
