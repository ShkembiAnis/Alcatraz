package alcatraz.client;

import alcatraz.shared.interfaces.ClientInterface;
import at.falb.games.alcatraz.api.MoveListener;
import at.falb.games.alcatraz.api.Player;
import at.falb.games.alcatraz.api.Prisoner;

import javax.swing.*;
import java.rmi.RemoteException;

public class AlcatrazMoveListener implements MoveListener {
    private final Object lock = new Object();
    private ClientInterface client;

    AlcatrazMoveListener( ClientInterface client){
        this.client = client;
    }

    @Override
    public void moveDone(at.falb.games.alcatraz.api.Player player, Prisoner prisoner, int rowOrCol, int row, int col) {
        System.out.println(player.getId() + " moved prisoner " + prisoner + " to position: " + row + ", " + col);
        synchronized (lock) {
            SwingUtilities.invokeLater(() -> {
                try {
                    client.broadcastMove(player, prisoner, rowOrCol, row, col);
                } catch (RemoteException e) {
                    System.out.println("Could not reach clients");
                }
            });
        }
    }

    @Override
    public void gameWon(Player winner) {
        // return to Menu/Lobby ?
        System.out.println("Game won by: " + winner.getId());
    }
}
