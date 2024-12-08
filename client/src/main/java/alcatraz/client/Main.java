package alcatraz.client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import alcatraz.shared.exceptions.*;
import alcatraz.shared.rmi.RMI;
import alcatraz.shared.utils.Lobby;
import alcatraz.shared.utils.LobbyKey;
import alcatraz.shared.utils.Player;

public class Main {
    public static void main(String[] args) {
        try {
            // Set up servers
            Map<Integer, RMI> rmiList = RMI.getRMISettings(System.getProperty("user.dir") +  "/rmi.json");
            ServerWrapper serverWrapper = new ServerWrapper(new ArrayList<>(rmiList.values()));

            Scanner scanner = new Scanner(System.in);
            String clientName = register_GetClientName(serverWrapper);

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
                serverWrapper.registerPlayer(finalClientName);
                System.out.println(clientName + " registered");
                break;
            } catch (DuplicateNameException e) {
                System.out.println(e.getMessage());
            } catch (RemoteException e) {
                System.out.println("Unexpected Error: ");
                e.printStackTrace();
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
                    //todo: Hier sollte eigentlich der Spieler gelöscht werden.
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
        Map<Long, Lobby> lobbies = new HashMap<>();
        try{
            lobbies = serverWrapper.getLobbies();
        } catch (RemoteException e) {
            System.out.println("Unexpected Error");
            e.printStackTrace();
            return;
        }

        if (lobbies.isEmpty()) {
            System.out.println("No available lobbies.");
            return ;
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

        try{
            serverWrapper.joinLobby(clientName, Long.valueOf(lobbyIdInput));
            System.out.println("Joined Lobby " + lobbyIdInput);
        }catch(PlayerNotRegisteredException e){
            System.out.println(e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Unexpected Error");
        }
        guestLobbyMenu(scanner, serverWrapper, Long.valueOf(lobbyIdInput), clientName);
    }

    private static void createLobby(Scanner scanner, ServerWrapper serverWrapper, String clientName) {
        LobbyKey lobbyKey = null;
        try {
            lobbyKey = serverWrapper.createLobby(clientName);
        } catch (Exception e) {
            System.out.println("Failed to create lobby: " + e.getMessage());
        }
        ownerLobbyMenu(scanner, serverWrapper, lobbyKey, clientName);
    }

    //todo: handle Input from User
    private static void guestLobbyMenu(Scanner scanner, ServerWrapper serverWrapper, Long lobbyId, String clientName) {
        System.out.println("Guest Lobby Menu: (0) Exit Lobby");
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("0")) {
                System.out.println("Exiting lobby...");
                try{
                    serverWrapper.leaveLobby(clientName);
                }catch (LobbyLockedException e){
                    System.out.println(e.getMessage());
                } catch (RemoteException e) {
                    System.out.println(e.getCause().getMessage());
                }
                break;
            } else {
                System.out.println("Invalid option. Press 0 to exit.");
            }
        }
    }

    private static void ownerLobbyMenu(Scanner scanner, ServerWrapper serverWrapper, LobbyKey lobbyKey, String clientName) {
        System.out.println("Owner Lobby Menu: (1) Start Game (0) Exit Lobby");
        String input = scanner.nextLine();

        switch (input) {
            case "1":
                System.out.println("Starting game...");
                ArrayList<Player> players = new ArrayList<>();
                try{
                    players = serverWrapper.initializeGameStart(lobbyKey.lobbyId, lobbyKey.secret);
                }catch (LobbyFullException | LobbyKeyIncorrect e){
                    System.out.println(e.getMessage());
                    break;
                } catch (RemoteException e){
                    System.out.println("Unexpected Error");
                    break;
                }

                for (int i = 0; i < players.size(); i++) {
                    try {
                        players.get(i).getClient().isPresent();
                    } catch (RemoteException e) {
                        System.out.println("Unexpected Error");
                        break;
                    }
                }

                for (int i = 0; i < players.size(); i++) {
                    try {
                        players.get(i).getClient().startGame(players, i);
                    } catch (RemoteException e) {
                        System.out.println("Unexpected Error. Could not reach player: " + players.get(i).getClientName());
                    }
                }
                break;
            case "0":
                System.out.println("Exiting lobby...");
                try{
                    serverWrapper.leaveLobby(clientName);
                }catch (LobbyLockedException e){
                    System.out.println(e.getMessage());
                } catch (RemoteException e) {
                    System.out.println("Unexpected Error");
                }
                break;
            default:
                System.out.println("Invalid option.");
                break;
        }

    }
}
