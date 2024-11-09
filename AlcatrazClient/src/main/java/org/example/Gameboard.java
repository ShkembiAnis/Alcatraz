
package org.example;

import java.util.ArrayList;
import java.util.Random;

public class Gameboard {
    private ArrayList<ArrayList<String>> board;
    private final int ROWS = 10;
    private final int COLUMNS = 8;
    private final int PLAYER_COUNT = 2;
    private final int FIGURES_PER_PLAYER = 4;

    public Gameboard() {
        board = new ArrayList<>();
        initializeBoard();
        placePrisoners();
        placePlayerFigures();
    }

    private void initializeBoard() {
        for (int i = 0; i < ROWS; i++) {
            ArrayList<String> row = new ArrayList<>();
            for (int j = 0; j < COLUMNS; j++) {
                row.add("[  ]");
            }
            board.add(row);
        }
    }

    private void placePrisoners() {
        Random rand = new Random();
        for (int i = 0; i < ROWS; i++) {
            int prisonerCol = rand.nextInt(COLUMNS);
            board.get(i).set(prisonerCol, "[Pr]");
        }
    }

    private void placePlayerFigures() {
        for (int player = 1; player <= PLAYER_COUNT; player++) {
            for (int fig = 0; fig < FIGURES_PER_PLAYER; fig++) {
                board.get(ROWS - 1).set(fig + (player - 1) * FIGURES_PER_PLAYER, "[F" + player + "]");
            }
        }
    }

    public void displayBoard() {
        for (ArrayList<String> row : board) {
            for (String cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }

    // Additional methods for moving figures and determining the winner
    /*
     */
}