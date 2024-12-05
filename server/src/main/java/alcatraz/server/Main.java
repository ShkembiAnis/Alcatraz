package alcatraz.server;

import alcatraz.server.rmi.RMIManager;
import alcatraz.server.spread.Spread;
import alcatraz.server.replication.Replication;
import alcatraz.server.state.SharedState;
import alcatraz.server.rmi.RMI;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // TODO: it is missleading when everything is in one huge try catch block
        try {
            int serverId = 1; // Default ID

            // default settings of spread that should be overridden by program arguments
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
            Server server = new Server(replication);

            // Start the RMI registry
            Registry registry = LocateRegistry.createRegistry(localRmi.port);
            registry.bind(RMI.remoteObjectName, server);
            System.out.println("Server " + serverId + " started on RMI port " + localRmi.port + ". Waiting for clients...");

            replication.joinServerGroup(serverId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


