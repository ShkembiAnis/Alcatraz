package alcatraz.shared.exceptions;

import java.rmi.RemoteException;

public class LobbyLockedException extends RemoteException {
    public LobbyLockedException(String additionalMessage) {
        super("Lobby is locked for changes. " + additionalMessage);
    }
}
