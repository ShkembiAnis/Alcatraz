package alcatraz.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import alcatraz.shared.interfaces.ClientInterface;
import alcatraz.shared.utils.Player;
import alcatraz.shared.interfaces.ServerInterface;
import at.falb.games.alcatraz.api.Alcatraz;
import at.falb.games.alcatraz.api.IllegalMoveException;
import at.falb.games.alcatraz.api.Prisoner;

public class Client extends UnicastRemoteObject implements ClientInterface {
    private String clientName;
    private ArrayList<Player> lobbyPlayers;
    private Alcatraz alcatraz;
    private AlcatrazMoveListener alcatrazMoveListener;
    private ServerInterface server;
    private Move currentMove = null;

    public Client(ServerInterface server, String clientName) throws RemoteException {
        this.server = server;
        this.clientName = clientName;
    }



    @Override
    public synchronized void broadcastMove(at.falb.games.alcatraz.api.Player player, Prisoner prisoner, int rowOrCol, int row, int col) throws RemoteException {
        transmitMoveToAllPlayers(player, prisoner, rowOrCol, row, col);
        executeMoveByAllPlayers();
    }

    @Override
    public void isPresent() throws RemoteException {
        System.out.println("Client " + this.clientName + " is present");
    }

    @Override
    public void setMove(at.falb.games.alcatraz.api.Player player, Prisoner prisoner, int rowOrCol, int row, int col) throws RemoteException {
        this.currentMove = new Move(player,prisoner, rowOrCol, row, col);
    }

    @Override
    public void startGame(ArrayList<Player> players, int myLobbyPlayerId) throws RemoteException {
        this.alcatraz = new Alcatraz();
        this.alcatrazMoveListener = new AlcatrazMoveListener(this);
        this.lobbyPlayers = players;
        this.alcatraz.init(players.size(), myLobbyPlayerId);
        for (int i = 0; i < players.size(); i++) {
            this.alcatraz.getPlayer(i).setName(players.get(i).getClientName());
        }
        this.alcatraz.addMoveListener(this.alcatrazMoveListener);
        this.alcatraz.showWindow();
        this.alcatraz.start();
        System.out.println("Game started");
    }

    @Override
    public void doMove() throws RemoteException {
        if(currentMove != null){
            System.out.println("Move received from Player " + this.currentMove.getPlayer().getName() + ": " + "Prisoner " + this.currentMove.getPrisoner().getId() + " to (" + this.currentMove.getRow() + ", " + this.currentMove.getCol() + ")");
            try{
                this.alcatraz.doMove(this.currentMove.getPlayer(), this.currentMove.getPrisoner(), this.currentMove.getRowOrCol(), this.currentMove.getRow(), this.currentMove.getCol());
                this.currentMove = null;
            }catch (IllegalMoveException e) {
                throw new RuntimeException(e);
            }
        }else{
            System.out.println("Move is not set.");
        }
    }

    private void transmitMoveToAllPlayers(at.falb.games.alcatraz.api.Player player, Prisoner prisoner, int rowOrCol, int row, int col) {
        for (Player playerEntry : lobbyPlayers) {
            if (!playerEntry.getClientName().equals(clientName)) {
                while (true) {
                    try {
                        playerEntry.getClient().setMove(player, prisoner, rowOrCol, row, col);
                        break;
                    } catch (RemoteException e) {
                        System.out.println(playerEntry.getClientName() + " could not be reached. Game paused. Retrying in 2 seconds.");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ex) {
                            System.out.println("Unexpected error");
                            Thread.currentThread().interrupt(); // Restore the interrupted status
                            break;
                        }
                    }
                }
            }
        }
    }

    private void executeMoveByAllPlayers() {
        for (Player playerEntry : lobbyPlayers) {
            if (!playerEntry.getClientName().equals(clientName)) {
                while (true) {
                    try {
                        playerEntry.getClient().doMove();
                        break;
                    } catch (RemoteException e) {
                        System.out.println(playerEntry.getClientName() + " could not be reached. Game paused. Retrying in 2 seconds.");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ex) {
                            System.out.println("Unexpected error");
                            Thread.currentThread().interrupt(); // Restore the interrupted status
                            break;
                        }
                    }
                }
            }
        }
    }
}
