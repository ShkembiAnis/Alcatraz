package alcatraz.shared.exceptions;

import java.rmi.RemoteException;

public class PlayerNotRegisteredException extends RemoteException {
    public PlayerNotRegisteredException(String clientName) {
        super("Player " + clientName + " has not yet been registered.");
        System.out.println("Player " + clientName + " not registered.");
    }
}
