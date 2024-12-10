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
    private String clientId;
    private String clientName;
    private String client_ip_address;
    private String client_port;
    private ArrayList<Player> lobbyPlayers;
    private Alcatraz alcatraz;
    private AlcatrazMoveListener alcatrazMoveListener;

    private ServerInterface server;

    public Client(ServerInterface server, String clientName) throws RemoteException {
        this.server = server;
        this.clientName = clientName;
    }



    @Override
        public synchronized void broadcastMove(at.falb.games.alcatraz.api.Player player, Prisoner prisoner, int rowOrCol, int row, int col) throws RemoteException {
            for(Player playerEntry : lobbyPlayers){
                if(!playerEntry.getClientName().equals(clientName)){
                    boolean moveDeliverd = false;
                    while(!moveDeliverd){
                        try {
                            playerEntry.getClient().doMove(player, prisoner, rowOrCol, row, col);
                            moveDeliverd = true;
                            break;
                        }catch (RemoteException e){
                            System.out.println(playerEntry.getClientName() + " could not be reached. Game paused. Retrying in 5 seconds.");
                            try{
                                Thread.sleep(5000);
                            }catch (InterruptedException ex) {
                                System.out.println("Unexpected error");
                                Thread.currentThread().interrupt(); // Restore the interrupted status
                                break;
                            }
                        }
                    }
                }
            }
        }

    @Override
    public void isPresent() throws RemoteException {
        System.out.println("Client " + this.clientName + " is present");
    }

    @Override
    public void endGame() throws RemoteException{
        try {
            Thread.sleep(5000);

        }catch (InterruptedException e) {
            System.err.println("The delay was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
        this.alcatraz.closeWindow();
        this.alcatraz.disposeWindow();
        this.alcatraz.removeMoveListener(this.alcatrazMoveListener);
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
    public void doMove(at.falb.games.alcatraz.api.Player player, Prisoner prisoner, int rowOrCol, int row, int col) throws RemoteException {
        System.out.println("Move received from Player " + player.getName() + ": " + "Prisoner " + prisoner.getId() + " to (" + row + ", " + col + ")");
        try{
            this.alcatraz.doMove(player, prisoner, rowOrCol, row, col);
        }catch (IllegalMoveException e) {
            throw new RuntimeException(e);
        }
    }

}
