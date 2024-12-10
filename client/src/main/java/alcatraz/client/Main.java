package alcatraz.client;

import java.rmi.RemoteException;
import java.rmi.ServerException;
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
        while (true) {
            System.out.print("Enter your name: ");
            clientName = scanner.nextLine();
            try {
                String finalClientName = clientName;
                serverWrapper.registerPlayer(finalClientName);
                System.out.println(clientName + " registered");
                break;
            } catch (ServerException e) {
                String errorMsg = HandleException.handleCauseException(
                        e.getCause(),
                        DuplicateNameException.class);

                System.out.println(errorMsg);
                continue;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            System.out.println("Unexpected Error!");
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
            lobbies = serverWrapper.getLobbies(clientName);
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
        String lobbyIdInput;

        while(true){
            lobbyIdInput = getNumberInput();
            if(lobbies.containsKey(Long.parseLong(lobbyIdInput))){
                break;
            }else{
                System.out.println("Lobby mit der ID " + lobbyIdInput + " existiert nicht.");
            }
        }

        if (lobbyIdInput.equals("0")) {
            System.out.println("Returning to main menu...");
            return;
        }

        try{
            serverWrapper.joinLobby(clientName, Long.valueOf(lobbyIdInput));
            System.out.println("Joined Lobby " + lobbyIdInput);
        } catch (RemoteException e) {
            String errorMsg = HandleException.handleCauseException(
                    e.getCause(),
                    PlayerNotRegisteredException.class,
                    LobbyLockedException.class,
                    LobbyFullException.class);

            System.out.println(errorMsg);
        }
        guestLobbyMenu(scanner, serverWrapper, Long.valueOf(lobbyIdInput), clientName);
    }

    private static void createLobby(Scanner scanner, ServerWrapper serverWrapper, String clientName) {
        LobbyKey lobbyKey = null;
        try {
            lobbyKey = serverWrapper.createLobby(clientName);
        } catch (RemoteException e) {
            String errorMsg = HandleException.handleCauseException(
                    e.getCause(),
                    TooManyLobbiesException.class,
                    LobbyLockedException.class);

            System.out.println(errorMsg);
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
                } catch (RemoteException e) {
                    String errorMsg = HandleException.handleCauseException(
                            e.getCause(),
                            LobbyLockedException.class);

                    System.out.println(errorMsg);
                }
                break;
            } else {
                System.out.println("Invalid option. Press 0 to exit.");
            }
        }
    }

    private static void ownerLobbyMenu(Scanner scanner, ServerWrapper serverWrapper, LobbyKey lobbyKey, String clientName) {

        boolean inLobby = true;
        while(inLobby){
            System.out.println("Owner Lobby Menu: (1) Start Game (0) Exit Lobby");
            String input = scanner.nextLine();
            switch (input) {
                case "1":
                    System.out.println("Starting game...");
                    ArrayList<Player> players = new ArrayList<>();
                    try{
                        players = serverWrapper.initializeGameStart(lobbyKey.lobbyId, lobbyKey.secret);
                    } catch (RemoteException e) {
                        String errorMsg = HandleException.handleCauseException(
                                e.getCause(),
                                LobbyKeyIncorrect.class,
                                NotEnoughPlayersException.class
                        );
                        System.out.println(errorMsg);
                        break;
                    }

                    // todo: shouldn't, the ckecing if clients are present, be done from server side?
                    // todo: at least that's how we discussed it before (look at the diagram)
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
                            break;
                        }
                    }
                    inLobby = false;
                    break;
                case "0":
                    System.out.println("Exiting lobby...");
                    try{
                        serverWrapper.leaveLobby(clientName);

                    } catch (RemoteException e) {
                        String errorMsg = HandleException.handleCauseException(
                                e.getCause(),
                                LobbyLockedException.class);
                        System.out.println(errorMsg);
                        break;
                    }
                    inLobby = false;
                    break;
                default:
                    System.out.println("Invalid option.");
                    break;
            }
        }


    }

    private static String getNumberInput(){
        Scanner scanner = new Scanner(System.in);
        while(true){
            try{
                long longInput = Long.parseLong(scanner.nextLine());
                return Long.toString(longInput);
            }catch(Exception e){
                System.out.println("Invalid input. Input has to be a number.");
            }
        }
    }
}

