package alcatraz.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;

import alcatraz.shared.interfaces.ClientInterface;
import alcatraz.shared.utils.Player;
import alcatraz.shared.interfaces.ServerInterface;
import at.falb.games.alcatraz.api.Alcatraz;
import at.falb.games.alcatraz.api.IllegalMoveException;
import at.falb.games.alcatraz.api.MoveListener;
import at.falb.games.alcatraz.api.Prisoner;

public class Client extends UnicastRemoteObject implements ClientInterface {
    private String clientId;
    private String clientName;
    private String client_ip_address;
    private ArrayList<Player> lobbyPlayers;
    private Alcatraz alcatraz = new Alcatraz();


    private ServerInterface server;

    protected Client(ServerInterface server, String clientName) throws RemoteException {
        this.server = server;
        this.clientName = clientName;
    }
    protected Client() throws RemoteException {
    }

    @Override
    public void doMove(at.falb.games.alcatraz.api.Player player, Prisoner prisoner, int rowOrCol, int row, int col) throws RemoteException {
        for(Player playerEntry : lobbyPlayers){
            if(!playerEntry.getClientName().equals(clientName)){
                playerEntry.getClient().receiveMove(player.getId(), prisoner.getId(), rowOrCol, row, col);
            }
        }
    }

    @Override
    public void getMessage(String message) throws RemoteException {
        System.out.println("Received Message: " + message);
    }

    @Override
    public void startGame(ArrayList<Player> players, int myLobbyPlayerId) throws RemoteException {
        this.lobbyPlayers = players;
        this.alcatraz.init(players.size(), myLobbyPlayerId);
        this.alcatraz.addMoveListener(new MoveListener() {
            @Override
            public void moveDone(at.falb.games.alcatraz.api.Player player, Prisoner prisoner, int rowOrCol, int row, int col) {
                System.out.println(player.getName() + " moved prisoner " + prisoner +
                        " to position: " + row + ", " + col);

                // Broadcast the move to all clients
                try {
                    doMove(player, prisoner, rowOrCol, row, col);
                } catch (RemoteException e) {
                    System.out.println("Could not reach ");
                }
            }

            @Override
            public void gameWon(at.falb.games.alcatraz.api.Player winner) {
                System.out.println("Game won by: " + winner.getName());
            }
        });
        this.alcatraz.showWindow();
        this.alcatraz.start();
        System.out.println(alcatraz.getPlayer(myLobbyPlayerId).toString());
        System.out.println("Game started");
    }

    private void startTurn() throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Your move: ");
        String move = scanner.nextLine();
        broadcastMove(move);
    }

    @Override
    public void receiveMove(int playerId, int prisonerId, int rowOrCol, int row, int col) throws RemoteException {
        System.out.println("Move received from Player " + playerId + ": " + "Prisoner " + prisonerId + " to (" + row + ", " + col + ")");
        at.falb.games.alcatraz.api.Player player = alcatraz.getPlayer(playerId);
        Prisoner prisoner = alcatraz.getPrisoner(prisonerId);

        try{
            alcatraz.doMove(player, prisoner, rowOrCol, row, col);
        }catch (IllegalMoveException e) {
            throw new RuntimeException(e);
        }
    }

    private void broadcastMove(String move) throws RemoteException {
        System.out.println("Broadcasting move: " + move);
        return;
    }
}
