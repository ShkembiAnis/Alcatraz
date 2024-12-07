package alcatraz.client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import alcatraz.shared.interfaces.ClientInterface;
import alcatraz.shared.interfaces.ServerInterface;
import alcatraz.shared.utils.Lobby;
import alcatraz.shared.utils.LobbyKey;
import alcatraz.shared.utils.Player;

public class Main {
    public static void main(String[] args) {
        try {
            // Set up servers
            ServerInterface server1 = (ServerInterface) LocateRegistry.getRegistry("localhost", 1099).lookup("Alcatraz");
            ServerInterface server2 = (ServerInterface) LocateRegistry.getRegistry("localhost", 1100).lookup("Alcatraz");
            ServerInterface server3 = (ServerInterface) LocateRegistry.getRegistry("localhost", 1101).lookup("Alcatraz");

            ServerWrapper serverWrapper = new ServerWrapper(server1, server2, server3);

            Scanner scanner = new Scanner(System.in);
            String clientName = register_GetClientName(serverWrapper);
            ClientInterface client = new Client(server1, clientName); // Using the first server for client initialization

            // Main menu loop
            while (true) {
                showMainMenu(scanner, serverWrapper, clientName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String register_GetClientName(ServerWrapper serverWrapper) {
        Scanner scanner = new Scanner(System.in);
        String clientName = "";
        System.out.print("Enter your name: ");
        while (true) {
            clientName = scanner.nextLine();
            try {
                String finalClientName = clientName;
                serverWrapper.execute(server -> {
                    ClientInterface client = new Client(server, finalClientName);
                    server.registerPlayer(finalClientName, client);
                    return null;
                });
                System.out.println(clientName + " registered");
                break;
            } catch (Exception e) {
                System.out.println("The Username " + clientName + " already exists or server failed.");
                System.out.print("Enter your name: ");
            }
        }
        return clientName;
    }

    private static void showMainMenu(Scanner scanner, ServerWrapper serverWrapper, String clientName) {
        try {
            System.out.println("Main Menu: (1) Show Available Lobbies (2) Create Lobby (0) Exit");
            String menuInput = scanner.nextLine();

            switch (menuInput) {
                case "1":
                    showAvailableLobbies(scanner, serverWrapper, clientName);
                    break;
                case "2":
                    createLobby(scanner, serverWrapper, clientName);
                    break;
                case "0":
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static void showAvailableLobbies(Scanner scanner, ServerWrapper serverWrapper, String clientName) {
        try {
            serverWrapper.execute(server -> {
                Map<Long, Lobby> lobbies = server.getLobbies();

                if (lobbies.isEmpty()) {
                    System.out.println("No available lobbies.");
                    return null;
                }

                for (Map.Entry<Long, Lobby> entry : lobbies.entrySet()) {
                    Lobby lobby = entry.getValue();
                    System.out.println("Lobby ID: " + lobby.getId() + " | Players: " + lobby.getPlayers().size() + " | Owner: " + lobby.getOwner());
                }

                System.out.print("Enter a Lobby ID to join (0 to cancel): ");
                String lobbyIdInput = scanner.nextLine();

                if (lobbyIdInput.equals("0")) {
                    System.out.println("Returning to main menu...");
                    return null;
                }

                server.joinLobby(clientName, Long.valueOf(lobbyIdInput));
                System.out.println("Joined Lobby " + lobbyIdInput);
                guestLobbyMenu(scanner, server, Long.valueOf(lobbyIdInput));
                return null;
            });
        } catch (Exception e) {
            System.out.println("Failed to show available lobbies: " + e.getMessage());
        }
    }

    private static void createLobby(Scanner scanner, ServerWrapper serverWrapper, String clientName) {
        try {
            serverWrapper.execute(server -> {
                LobbyKey lobbyKey = server.createLobby(clientName);
                System.out.println("Lobby created with ID: " + lobbyKey.lobbyId);
                ownerLobbyMenu(scanner, serverWrapper, lobbyKey);
                return null;
            });
        } catch (Exception e) {
            System.out.println("Failed to create lobby: " + e.getMessage());
        }
    }

    private static void guestLobbyMenu(Scanner scanner, ServerInterface server, Long lobbyId) {
        System.out.println("Guest Lobby Menu: (0) Exit Lobby");
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("0")) {
                System.out.println("Exiting lobby...");
                break;
            } else {
                System.out.println("Invalid option. Press 0 to exit.");
            }
        }
    }

    private static void ownerLobbyMenu(Scanner scanner, ServerWrapper serverWrapper, LobbyKey lobbyKey) {
        System.out.println("Owner Lobby Menu: (1) Start Game (0) Exit Lobby");
        String input = scanner.nextLine();

        try {
            switch (input) {
                case "1":
                    System.out.println("Starting game...");
                    ArrayList<Player> players = serverWrapper.execute(server -> server.initializeGameStart(lobbyKey.lobbyId, lobbyKey.secret));
                    for (int i = 0; i < players.size(); i++) {
                        try {
                            players.get(i).getClient().startGame(players, i);
                        } catch (RemoteException e) {
                            System.out.println("Failed to start game for player: " + players.get(i).getClientName());
                            // Optionally retry or log the failure
                        }
                    }
                    break;
                case "0":
                    System.out.println("Exiting lobby...");
                    break;
                default:
                    System.out.println("Invalid option. Returning to main menu...");
                    break;
            }
        } catch (RemoteException e) {
            System.out.println("An error occurred while starting the game: " + e.getMessage());
        }
    }
}
