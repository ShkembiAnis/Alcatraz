package alcatraz.shared.exceptions;

import java.rmi.RemoteException;

public class LobbyFullException extends RemoteException {
    public LobbyFullException(Long lobbyId) {
        super("Lobby " + lobbyId + " cannot take any more players");
    }
}
