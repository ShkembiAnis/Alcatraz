package alcatraz.shared;

import java.rmi.RemoteException;

public class TooManyLobbiesException extends RemoteException {
    public TooManyLobbiesException() {
        super("Too many lobbies");
        System.out.println("Too many lobbies");
    }
}
