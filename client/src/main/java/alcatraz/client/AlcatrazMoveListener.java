package alcatraz.client;

import alcatraz.shared.interfaces.ClientInterface;
import at.falb.games.alcatraz.api.MoveListener;
import at.falb.games.alcatraz.api.Player;
import at.falb.games.alcatraz.api.Prisoner;

import javax.swing.*;
import java.rmi.RemoteException;

public class AlcatrazMoveListener implements MoveListener {
    private Client client;

    AlcatrazMoveListener(Client client){
        this.client = client;
    }

    @Override
    public void moveDone(at.falb.games.alcatraz.api.Player player, Prisoner prisoner, int rowOrCol, int row, int col) {
        System.out.println(player.getName() + " moved prisoner " + prisoner + " to position: " + row + ", " + col);
        try {
            client.broadcastMove(player, prisoner, rowOrCol, row, col);
        }catch (RemoteException e){
            System.out.println("Unexpected error.");
        }
    }

    @Override
    public void gameWon(at.falb.games.alcatraz.api.Player winner) {
        System.out.println("Game won by: " + winner.getName());
    }
}
