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

            /**
             * Start of User interaction
             */

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your name: ");
            String clientName = register_GetClientName(server);
            ClientInterface client = new Client(server, clientName);

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
                        System.out.println("Show available Lobbies...");
                        System.out.println("Listing lobbies:");
                        Map<Long, Lobby> lobbies = server.getLobbies();
                        for (Map.Entry<Long, Lobby> entry : lobbies.entrySet()) {
                            Lobby lobbyEntry = entry.getValue(); // The value (List<String>)
                            System.out.println("Lobby: " + lobbyEntry.getId() + " Players: " + lobbyEntry.getPlayers().size() + " ownerId:" + lobbyEntry.getOwner());
                        }
                        System.out.println("Enter a lobbyID, to join a lobby (enter 0 to exit Lobby-List)");
                        String joinLobbyIdInput = scanner.nextLine();
                        try {
                            server.joinLobby( clientName, Long.valueOf(joinLobbyIdInput));
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
                        } catch (RemoteException e) {
                            System.out.println("Could not join lobby.");
                        }

                        break;
                    case "2":
                        System.out.println("Creating Lobby...");
                        LobbyKey lobby = server.createLobby(clientName);
                        System.out.println("Lobby created with id: " + lobby.lobbyId);
                        /**
                         * Owner LobbyMenu
                         * responsible for the start of the game
                         */

                        System.out.println("Lobby Menu: (1) start game (0 exit Lobby)");
                        String ownerLobbyMenuInput = scanner.nextLine();
                        switch (ownerLobbyMenuInput){
                            case "1":
                                /* Start Game */
                                ArrayList<Player> lobbyPlayers = server.initializeGameStart(lobby.lobbyId, lobby.secret);
                                for(int i = 0; i < lobbyPlayers.size(); i++){
                                    try {
                                        if(!lobbyPlayers.get(i).getClientName().equals(clientName)){
                                            lobbyPlayers.get(i).getClient().startGame(lobbyPlayers, i);
                                        }else{
                                            lobbyPlayers.get(i).getClient().startGame(lobbyPlayers, 0);
                                        }
                                    } catch (RemoteException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
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
        while (true) {
            clientName = scanner.nextLine();
            ClientInterface client = new Client(server, clientName);
            try {
                server.registerPlayer(clientName, client);
                System.out.println(clientName + " registered");
                break;
            } catch (Exception e) {
                System.out.println("The Username " + clientName + " already exits.");
                System.out.println("Enter your name: ");
            }
        }
        return clientName;
    }
}
