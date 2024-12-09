package alcatraz.shared.exceptions;

import java.rmi.RemoteException;

public class LobbyKeyIncorrect extends RemoteException {
    public LobbyKeyIncorrect() {
        super("Key to the lobby is incorrect");
    }
}
