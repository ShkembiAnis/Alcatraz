package alcatraz.client;

import alcatraz.shared.ClientInterface;
import alcatraz.shared.Lobby;
import alcatraz.shared.ServerInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Scanner;
import alcatraz.client.GameGUI;

public class Main {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ServerInterface server = (ServerInterface) registry.lookup("Alcatraz");

            Scanner scanner = new Scanner(System.in);
            String clientName = register_GetClientName(server);

            while (true) {
                System.out.println("\nMain Menu:");
                System.out.println("(1) Show Available Lobbies");
                System.out.println("(2) Create Lobby");
                System.out.println("(0) Exit");
                System.out.print("Enter your choice: ");
                String menu1Input = scanner.nextLine();

                switch (menu1Input) {
                    case "1":
                        showAvailableLobbies(server, scanner, clientName);
                        break;
                    case "2":
                        createLobby(server, scanner, clientName);
                        break;
                    case "0":
                        System.out.println("Exiting...");
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showAvailableLobbies(ServerInterface server, Scanner scanner, String clientName) throws Exception {
        System.out.println("Show available Lobbies...\nListing lobbies:");
        Map<Long, Lobby> lobbies = server.getLobbies();
        for (Map.Entry<Long, Lobby> entry : lobbies.entrySet()) {
            Lobby lobbyEntry = entry.getValue();
            System.out.println("Lobby: " + lobbyEntry.getId() + " Players: " + lobbyEntry.getPlayers().size() + " ownerId:" + lobbyEntry.getOwner());
        }
        System.out.println("Enter a lobbyID to join a lobby (enter 0 to exit Lobby-List):");
        String joinLobbyIdInput = scanner.nextLine();
        if (!joinLobbyIdInput.equals("0") && server.joinLobby(clientName, Long.valueOf(joinLobbyIdInput))) {
            System.out.println("Lobby " + lobbies.get(Long.valueOf(joinLobbyIdInput)).getId() + " joined.");
            System.out.println("Waiting For Game Start...");
            guestMenu(scanner);
        } else {
            System.out.println("Could not join lobby.");
        }
    }

    private static void createLobby(ServerInterface server, Scanner scanner, String clientName) throws Exception {
        System.out.println("Creating Lobby...");
        Lobby lobby = server.createLobby(clientName);
        System.out.println("Lobby created with id: " + lobby.getId());

        while (true) {
            System.out.println("\nLobby Menu:");
            System.out.println("(1) Start Game");
            System.out.println("(0) Exit Lobby");
            System.out.print("Enter your choice: ");
            String ownerLobbyMenuInput = scanner.nextLine();

            switch (ownerLobbyMenuInput) {
                case "1":
                    server.initializeGameStart(lobby.getId());
                    System.out.println("Game started in Lobby " + lobby.getId());
                    GameGUI.main(new String[]{});
                    return;
                case "0":
                    server.removeLobby(lobby.getId());
                    System.out.println("Exited Lobby " + lobby.getId());
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void guestMenu(Scanner scanner) {
        while (true) {
            System.out.println("Enter 0 to exit:");
            String wantsExitsQuestionmark = scanner.nextLine();
            if (wantsExitsQuestionmark.equals("0")) {
                System.exit(0);
            }
        }
    }

    private static String register_GetClientName(ServerInterface server) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String clientName = "";
        while (true) {
            System.out.print("Enter your name to register: ");
            clientName = scanner.nextLine();
            ClientInterface client = new Client(server, clientName);
            if (server.registerPlayer(clientName, client)) {
                System.out.println(clientName + " registered successfully.");
                return clientName;
            } else {
                System.out.println("Registration failed. Try a different name.");
            }
        }
    }
}