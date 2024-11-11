package alcatraz.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Scanner;

import shared.ClientInterface;
import shared.Lobby;
import shared.Player;
import shared.ServerInterface;

public class Client extends UnicastRemoteObject implements ClientInterface {
    private String clientId;
    private String clientName;
    private String client_ip_address;
    private List<ClientInterface> players;

    private ServerInterface server;

    protected Client(ServerInterface server, String clientName) throws RemoteException {
        this.server = server;
        this.clientName = clientName;
    }
    protected Client() throws RemoteException {
    }

    @Override
    public void notifyStart(String lobbyId, List<ClientInterface> players) throws RemoteException {
        this.players = players;
        System.out.println("Game started in lobby " + lobbyId);
        startTurn();
    }

    @Override
    public boolean doMove(Player player, String Move) throws RemoteException {
        return false;
    }

    @Override
    public void getMessage(String message) throws RemoteException {
        System.out.println("Received Message: " + message);
    }

    @Override
    public void startGame(Lobby lobby) throws RemoteException {
        System.out.println("Game started " + lobby.getId());
        Game game = new Game(lobby);
        game.start();


    }

    private void startTurn() throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Your move: ");
        String move = scanner.nextLine();
        broadcastMove(move);
    }

    @Override
    public void receiveMove(String move) throws RemoteException {
        System.out.println("Received move: " + move);
    }

    private void broadcastMove(String move) throws RemoteException {
        for (ClientInterface player : players) {
            player.receiveMove(move);
        }
    }
}
