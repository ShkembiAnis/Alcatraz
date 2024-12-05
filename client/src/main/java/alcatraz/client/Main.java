package alcatraz.client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import alcatraz.shared.interfaces.ClientInterface;
import alcatraz.shared.utils.Lobby;
import alcatraz.shared.utils.LobbyKey;
import alcatraz.shared.interfaces.ServerInterface;
import alcatraz.shared.utils.Player;

public class Main {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ServerInterface server = (ServerInterface) registry.lookup("Alcatraz");

            Scanner scanner = new Scanner(System.in);
            String clientName = register_GetClientName(server);
            ClientInterface client = new Client(server, clientName); //

            // Main menu loop
            while (true) {
                showMainMenu(scanner, server, clientName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String register_GetClientName(ServerInterface server) throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        String clientName = "";
        System.out.print("Enter your name: ");
        while (true) {
            clientName = scanner.nextLine();
            ClientInterface client = new Client(server, clientName);
            try {
                server.registerPlayer(clientName, client);
                System.out.println(clientName + " registered");
                break;
            } catch (Exception e) {
                System.out.println("The Username " + clientName + " already exists.");
                System.out.print("Enter your name: ");
            }
        }
        return clientName;
    }

    private static void showMainMenu(Scanner scanner, ServerInterface server, String clientName) throws RemoteException {
        System.out.println("Main Menu: (1) Show Available Lobbies (2) Create Lobby (0) Exit");
        String menuInput = scanner.nextLine();

        switch (menuInput) {
            case "1":
                showAvailableLobbies(scanner, server, clientName);
                break;
            case "2":
                createLobby(scanner, server, clientName);
                break;
            case "0":
                System.out.println("Exiting...");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                break;
        }
    }

    private static void showAvailableLobbies(Scanner scanner, ServerInterface server, String clientName) throws RemoteException {
        System.out.println("Showing available lobbies...");
        Map<Long, Lobby> lobbies = server.getLobbies();

        if (lobbies.isEmpty()) {
            System.out.println("No available lobbies.");
            return;
        }

        for (Map.Entry<Long, Lobby> entry : lobbies.entrySet()) {
            Lobby lobby = entry.getValue();
            System.out.println("Lobby ID: " + lobby.getId() + " | Players: " + lobby.getPlayers().size() + " | Owner: " + lobby.getOwner());
        }

        System.out.print("Enter a Lobby ID to join (0 to cancel): ");
        String lobbyIdInput = scanner.nextLine();

        if (lobbyIdInput.equals("0")) {
            System.out.println("Returning to main menu...");
            return;
        }

        try {
            server.joinLobby(clientName, Long.valueOf(lobbyIdInput));
            System.out.println("Joined Lobby " + lobbyIdInput);
            guestLobbyMenu(scanner, server, Long.valueOf(lobbyIdInput));
        } catch (RemoteException e) {
            System.out.println("Failed to join lobby. Please try again.");
        }
    }

    private static void createLobby(Scanner scanner, ServerInterface server, String clientName) throws RemoteException {
        System.out.println("Creating a new lobby...");
        LobbyKey lobbyKey = server.createLobby(clientName);
        System.out.println("Lobby created with ID: " + lobbyKey.lobbyId);
        ownerLobbyMenu(scanner, server, lobbyKey);
    }

    private static void guestLobbyMenu(Scanner scanner, ServerInterface server, Long lobbyId) throws RemoteException {
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

    private static void ownerLobbyMenu(Scanner scanner, ServerInterface server, LobbyKey lobbyKey) throws RemoteException {
        System.out.println("Owner Lobby Menu: (1) Start Game (0) Exit Lobby");
        String input = scanner.nextLine();

        switch (input) {
            case "1":
                System.out.println("Starting game...");
                ArrayList<Player> players = server.initializeGameStart(lobbyKey.lobbyId, lobbyKey.secret);
                for (int i = 0; i < players.size(); i++) {
                    try {
                        players.get(i).getClient().startGame(players, i);
                    } catch (RemoteException e) {
                        System.out.println("Failed to start game for player: " + players.get(i).getClientName());
                        /*
                         * todo: retry until reached or abort because server provides clientInterfaces ?
                         *  how can we be sure that nobody reaches this section
                         */
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
    }
}
