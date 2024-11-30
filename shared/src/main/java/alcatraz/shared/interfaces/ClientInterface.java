package alcatraz.shared.interfaces;

import at.falb.games.alcatraz.api.Prisoner;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ClientInterface extends Remote {

    void startGame(ArrayList<alcatraz.shared.utils.Player> players, int myLobbyPlayerId) throws RemoteException;

    void receiveMove(int playerId, int prisonerId, int rowOrCol, int row, int col) throws RemoteException;

    void doMove(at.falb.games.alcatraz.api.Player player, Prisoner prisoner, int rowOrCol, int row, int col)throws RemoteException;

    void getMessage(String message) throws  RemoteException;
}
