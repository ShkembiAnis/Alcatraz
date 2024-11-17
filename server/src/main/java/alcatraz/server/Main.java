package alcatraz.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {
    public static void main(String[] args) {
        try {
            // Default values
            String serverName = "Server1";
            int serverId = 1; // Default ID
            int rmiPort = 1099;
            String spreadHost = "localhost";
            int spreadPort = 4803;
            String groupName = "ServerGroup";
            boolean verbose = false;

            // Simple argument parsing
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-n":
                    case "--name":
                        if (i + 1 < args.length) {
                            serverName = args[++i];
                        } else {
                            System.err.println("Error: Missing value for server name.");
                            return;
                        }
                        break;
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
                            rmiPort = Integer.parseInt(args[++i]);
                        } else {
                            System.err.println("Error: Missing value for RMI port.");
                            return;
                        }
                        break;
                    case "-s":
                    case "--spread-host":
                        if (i + 1 < args.length) {
                            spreadHost = args[++i];
                        } else {
                            System.err.println("Error: Missing value for Spread host.");
                            return;
                        }
                        break;
                    case "-sp":
                    case "--spread-port":
                        if (i + 1 < args.length) {
                            spreadPort = Integer.parseInt(args[++i]);
                        } else {
                            System.err.println("Error: Missing value for Spread port.");
                            return;
                        }
                        break;
                    case "-g":
                    case "--group":
                        if (i + 1 < args.length) {
                            groupName = args[++i];
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

            // Create the server with the parsed parameters
            LobbyManager lobbyManager = new LobbyManager();
            Server server = new Server(serverName, serverId, lobbyManager, spreadHost, spreadPort, groupName, verbose);

            // Start the RMI registry
            Registry registry = LocateRegistry.createRegistry(rmiPort);
            registry.bind("Alcatraz", server);
            System.out.println(serverName + " started on RMI port " + rmiPort + ". Waiting for clients...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


