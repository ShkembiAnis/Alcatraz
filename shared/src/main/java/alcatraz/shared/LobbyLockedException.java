package alcatraz.shared;

import java.rmi.RemoteException;

public class LobbyLockedException extends RemoteException {
    LobbyLockedException(String additionalMessage) {
        super("Lobby is locked for changes. " + additionalMessage);
    }
}
