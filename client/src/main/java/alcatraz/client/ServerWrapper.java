package alcatraz.client;

import alcatraz.shared.interfaces.ServerInterface;

import java.rmi.RemoteException;

public class ServerWrapper {
    private final ServerInterface[] servers;

    public ServerWrapper(ServerInterface... servers) {
        this.servers = servers;
    }

    public <T> T execute(ServerOperation<T> operation) throws RemoteException {
        RemoteException lastException = null;
        for (ServerInterface server : servers) {
            try {
                return operation.execute(server);
            } catch (RemoteException e) {
                System.err.println("Server operation failed, trying next server...");
                lastException = e;
            }
        }
        throw new RemoteException("All servers failed.", lastException);
    }

    @FunctionalInterface
    public interface ServerOperation<T> {
        T execute(ServerInterface server) throws RemoteException;
    }
}
