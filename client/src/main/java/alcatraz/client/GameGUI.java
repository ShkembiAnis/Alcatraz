package alcatraz.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
//new class
public class GameGUI extends Application {

    private static final int GRID_SIZE = 9;
    private static final String PRISONER = "P";
    private static final String GUARD = "G";
    private static final String EMPTY = "";

    private String[][] board = {
            {EMPTY, EMPTY, EMPTY, EMPTY, PRISONER, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, PRISONER, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, PRISONER, EMPTY, EMPTY},
            {EMPTY, EMPTY, PRISONER, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, PRISONER, EMPTY},
            {EMPTY, PRISONER, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, PRISONER},
            {PRISONER, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY}
    };

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Alcatraz Game");

        GridPane grid = new GridPane();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Button cell = new Button(board[row][col]);
                cell.setMinSize(50, 50);
                grid.add(cell, col, row);
            }
        }

        Scene scene = new Scene(grid, 450, 450);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}