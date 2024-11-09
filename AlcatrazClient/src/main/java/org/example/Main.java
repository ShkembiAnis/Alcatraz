package org.example;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ServerInterface server = (ServerInterface) registry.lookup("Alcatraz");
            //Lobby currentlobby = null;

            /**
             * Start of User interaction
             */

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your name: ");
            String clientName = register_GetClientName(server);

            /**
             * Start of Menu: 2 Options
             * 1. showAvailableLobbies
             * 2. createLobby
             */

            while(true){
                System.out.println("Menu1:  (1)showAvailableLobbies (2)createLobby");
                String menu1Input = scanner.nextLine();
                switch (menu1Input){
                    case "1":
                        System.out.println("Show available Lobbies...\nListing lobbies:");
                        Map<Long, Lobby> lobbies = server.getLobbies();
                        for (Map.Entry<Long, Lobby> entry : lobbies.entrySet()) {
                            Lobby lobbyEntry = entry.getValue(); // The value (List<String>)
                            System.out.println("Lobby: " + lobbyEntry.getId() + " Players: " + lobbyEntry.getPlayers().size() + " ownerId:" + lobbyEntry.getOwner());
                        }
                        System.out.println("Enter a lobbyID, to join a lobby (enter 0 to exit Lobby-List)");
                        String joinLobbyIdInput = scanner.nextLine();
                        if(server.joinLobby(clientName, Long.valueOf(joinLobbyIdInput))){
                            System.out.println("Lobby" + lobbies.get(Long.valueOf(joinLobbyIdInput)).getId() + " joined.");
                            System.out.println("Waiting For Game Start...");

                            /**
                             * Guest Menu
                             */

                            System.out.println("Players:" + (lobbies.get(Long.valueOf(joinLobbyIdInput)).getPlayers().size()+1) + " LobbyId:" + lobbies.get(Long.valueOf(joinLobbyIdInput)).getId());
                            System.out.println(" enter 0 exit");
                            while (true){
                                String wantsExitsQuestionmark = scanner.nextLine();
                                if(wantsExitsQuestionmark.equals("0")){
                                    break;
                                }
                            }
                        }else{
                            System.out.println("Could not join lobby.");
                        }

                        break;
                    case "2":
                        System.out.println("Creating Lobby...");
                        Lobby lobby = server.createLobby(clientName);
                        System.out.println("Lobby created with id: " + lobby.getId());
                        /**
                         * Owner LobbyMenu
                         * responsible for the start of the game
                         */

                        System.out.println("Lobby Menu: (1) start game (0 exit Lobby)");
                        String ownerLobbyMenuInput = scanner.nextLine();
                        switch (ownerLobbyMenuInput){
                            case "1":
                                server.initializeGameStart(lobby.getId());
                                break;
                            case "0":
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String register_GetClientName(ServerInterface server) throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        String clientName = "";
        while(true){
            clientName = scanner.nextLine();
            ClientInterface client = new Client(server, clientName);
            if(server.registerPlayer(clientName, client)){
                System.out.println(clientName + " registered");
                break;
            }else{
                System.out.println("The Username " + clientName + " already exits.\nEnter your name:");
            }
        }
        return clientName;
    }
}
