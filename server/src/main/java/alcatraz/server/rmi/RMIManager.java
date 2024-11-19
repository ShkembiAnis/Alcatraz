package alcatraz.server.rmi;

import alcatraz.shared.ServerInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class RMIManager {
    private ServerInterface primaryServer;
    private Map<Integer, RMI> serverIdToPortMap;

    public RMIManager(Map<Integer, RMI> serverIdToPortMap) {
        this.serverIdToPortMap = serverIdToPortMap;
    }

    public void setPrimaryServer(int primaryId) {
        RMI rmiConnectionToNewPrimary = this.serverIdToPortMap.get(primaryId);

        try {
            // TODO: handle the errors more specifically than just Exception e
            Registry registry = LocateRegistry.getRegistry(rmiConnectionToNewPrimary.ip, rmiConnectionToNewPrimary.port);
            this.primaryServer = (ServerInterface) registry.lookup("Alcatraz");
        } catch (Exception e) {
            System.out.println("Error: Could not connect to the RMI registry of the new primary server.");
            e.printStackTrace();
        }
    }

    public ServerInterface getPrimaryServer() {
        return this.primaryServer;
    }
}
