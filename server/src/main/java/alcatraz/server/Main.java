package alcatraz.server;

import alcatraz.shared.rmi.RMIManager;
import alcatraz.server.spread.Spread;
import alcatraz.server.replication.Replication;
import alcatraz.server.state.SharedState;
import alcatraz.shared.rmi.RMI;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        // default settings that should be overridden by program arguments
        int serverId = 1;
        Spread spread = new Spread("localhost", 4803, "ServerGroup");

        // Simple argument parsing
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-id":
                case "--server-id":
                    if (i + 1 < args.length) {
                        serverId = Integer.parseInt(args[++i]);
                    } else {
                        System.err.println("Error: Missing value for server ID.");
                        return;
                    }
                    break;
                case "-s":
                case "--spread-host":
                    if (i + 1 < args.length) {
                        spread.host = args[++i];
                    } else {
                        System.err.println("Error: Missing value for Spread host.");
                        return;
                    }
                    break;
                case "-sp":
                case "--spread-port":
                    if (i + 1 < args.length) {
                        spread.port = Integer.parseInt(args[++i]);
                    } else {
                        System.err.println("Error: Missing value for Spread port.");
                        return;
                    }
                    break;
                case "-g":
                case "--group":
                    if (i + 1 < args.length) {
                        spread.groupName = args[++i];
                    } else {
                        System.err.println("Error: Missing value for group name.");
                        return;
                    }
                    break;
                default:
                    System.err.println("Unknown argument: " + args[i]);
                    return;
            }
        }

        Map<Integer, RMI> rmiServers = RMI.getRMISettings(System.getProperty("user.dir") + "/rmi.json");

        // Create the server with the parsed parameters
        RMI localRmi = rmiServers.get(serverId);
        RMIManager rmiManager = new RMIManager(rmiServers);

        SharedState sharedState = new SharedState();
        Replication replication = new Replication(sharedState, rmiManager, spread, serverId);

        Server server;
        try {
            server = new Server(replication);
        } catch (RemoteException e) {
            System.err.println("RemoteException while creating server instance: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Start the RMI registry
        Registry registry;
        try {
            registry = LocateRegistry.createRegistry(localRmi.port);
        } catch (RemoteException e) {
            System.err.println("Could not create RMI registry: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        try {
            registry.bind(RMI.remoteObjectName, server);
        } catch (Exception e) {
            System.err.println("Exception while binding server to RMI registry: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        System.out.println("Server " + serverId + " started on RMI port " + localRmi.port + ". Waiting for clients...");

        replication.joinServerGroup(serverId);
    }
}


