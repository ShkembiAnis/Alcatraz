package alcatraz.shared;

import java.rmi.RemoteException;

public class LobbyFullException extends RemoteException {
    LobbyFullException(Long lobbyId) {
        super("Lobby " + lobbyId + " cannot take any more players");
    }
}
