package alcatraz.client;

import at.falb.games.alcatraz.api.Player;
import at.falb.games.alcatraz.api.Prisoner;

import java.io.Serializable;

public class Move implements Serializable {
    private final Player player;
    private final Prisoner prisoner;
    private final int rowOrCol;
    private final int row;
    private final int col;

    public Move(Player player, Prisoner prisoner, int rowOrCol, int row, int col) {
        this.player = player;
        this.prisoner = prisoner;
        this.rowOrCol = rowOrCol;
        this.row = row;
        this.col = col;
    }

    public Player getPlayer(){
        return this.player;
    }
    public Prisoner getPrisoner(){
        return this.prisoner;
    }
    public int getRowOrCol(){
        return this.rowOrCol;
    }
    public int getRow(){
        return this.row;
    }
    public int getCol(){
        return this.col;
    }
}
