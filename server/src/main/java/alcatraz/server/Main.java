package alcatraz.server;

import alcatraz.server.rmi.RMIManager;
import alcatraz.server.spread.Spread;
import alcatraz.server.replication.Replication;
import alcatraz.server.state.SharedState;
import alcatraz.server.rmi.RMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // TODO: it is missleading when everything is in one huge try catch block
        try {
            int serverId = 1; // Default ID

            // TODO: it could be just an IP address of the server
            //  not only spread since we need it for RMI too
            Spread spread = new Spread("localhost", 4803, "ServerGroup");
            RMI rmi = new RMI(spread.host, 1099);

            boolean verbose = false;

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
                    case "-p":
                    case "--rmi-port":
                        if (i + 1 < args.length) {
                            rmi.port = Integer.parseInt(args[++i]);
                        } else {
                            System.err.println("Error: Missing value for RMI port.");
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
                    case "-v":
                    case "--verbose":
                        verbose = true;
                        break;
                    default:
                        System.err.println("Unknown argument: " + args[i]);
                        return;
                }
            }

            // TODO: it should not be localhost, but a received IP address
            // the servers have to know which IP addresses they have
            // but it is not new since we have to know the IP address of the spread server
            Map<Integer, RMI> serverIdToPortMap = new HashMap<>();
            serverIdToPortMap.put(1, new RMI(spread.host, 1099));
            serverIdToPortMap.put(2, new RMI(spread.host, 1100));
            serverIdToPortMap.put(3, new RMI(spread.host, 1101));


            // Create the server with the parsed parameters
            SharedState sharedState = new SharedState();
            RMIManager rmiManager = new RMIManager(serverIdToPortMap);

            Replication replication = new Replication(sharedState, rmiManager, spread, serverId);
            Server server = new Server(replication);

            // Start the RMI registry
            Registry registry = LocateRegistry.createRegistry(rmi.port);
            registry.bind("Alcatraz", server);
            System.out.println("Server " + serverId + " started on RMI port " + rmi.port + ". Waiting for clients...");

            replication.joinServerGroup(serverId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


