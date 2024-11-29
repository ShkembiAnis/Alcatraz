package alcatraz.shared;

import java.rmi.RemoteException;

public class DuplicateNameException extends RemoteException {
    public DuplicateNameException(String clientName) {
        super(clientName + " already exists.");
        System.out.println(clientName + " will not be registered.");
    }
}
