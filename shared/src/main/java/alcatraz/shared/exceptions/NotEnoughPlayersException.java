package alcatraz.shared.exceptions;

import java.rmi.RemoteException;

public class NotEnoughPlayersException extends RemoteException {
    public NotEnoughPlayersException(Long lobbyId) {
        super("Cannot start game for Lobby " + lobbyId);
        System.out.println("Cannot start game for Lobby " + lobbyId);
        System.out.println("Not enough players in lobby " + lobbyId);
    }
}
