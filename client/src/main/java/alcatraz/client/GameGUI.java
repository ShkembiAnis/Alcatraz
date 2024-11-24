package alcatraz.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GameGUI extends Application {

    private static final int GRID_SIZE = 9;
    private static final String PRISONER_GREEN = "G";
    private static final String PRISONER_BLUE = "B";
    private static final String GUARD = "R";
    private static final String SHIP = "S";
    private static final String EMPTY = "";
    private static final String EMPTY_RED = "ER";

    private String[][] board = {
        {EMPTY, EMPTY, EMPTY_RED, EMPTY_RED, GUARD, EMPTY_RED, EMPTY_RED, EMPTY, EMPTY},
        {EMPTY, EMPTY, EMPTY, GUARD, EMPTY_RED, EMPTY_RED, EMPTY, EMPTY, EMPTY},
        {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY_RED, EMPTY, GUARD, EMPTY, EMPTY},
        {EMPTY, EMPTY, GUARD, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
        {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, GUARD, EMPTY},
        {EMPTY, GUARD, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
        {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, GUARD},
        {GUARD, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
        {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY}
    };

    private String[] prisoners = {
        PRISONER_GREEN, PRISONER_GREEN, PRISONER_GREEN, PRISONER_GREEN, PRISONER_BLUE, PRISONER_BLUE, PRISONER_BLUE, PRISONER_BLUE
    };

    private static boolean isApplicationLaunched = false;

    private Image guardImage;
    private Image prisonerGreenImage;
    private Image prisonerBlueImage;
    private Image shipImage;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Alcatraz Game");

        // Load images
        guardImage = new Image(getClass().getResourceAsStream("/gamers/Guard.png"), 40, 40, true, true);
        prisonerGreenImage = new Image(getClass().getResourceAsStream("/gamers/Prison_green.png"), 40, 40, true, true);
        prisonerBlueImage = new Image(getClass().getResourceAsStream("/gamers/Prison_blue.png"), 40, 40, true, true);
        shipImage = new Image(getClass().getResourceAsStream("/gamers/Ship.png"), 40, 40, true, true);

        BorderPane root = new BorderPane();
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setStyle("-fx-background-color: #D2B48C;");

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Button cell = new Button();
                cell.setMinSize(50, 50);
                updateCell(cell, board[row][col]);
                int finalRow = row;
                int finalCol = col;
                cell.setOnAction(e -> handleCellClick(finalRow, finalCol));
                grid.add(cell, col, row);
            }
        }
        // Add ship above the grid in one row
        HBox shipBox = new HBox();
        shipBox.setAlignment(Pos.CENTER);
        shipBox.setSpacing(5);
        shipBox.setStyle("-fx-background-color: darkblue;");
        for (int col = 0; col < GRID_SIZE; col++) {
            Button cell = new Button();
            cell.setMinSize(50, 50);
            if (col == 4) {
                ImageView shipImageView = new ImageView(shipImage);
                shipImageView.setFitWidth(60); // Set the width of the ship icon
                shipImageView.setFitHeight(60); // Set the height of the ship icon
                cell.setGraphic(shipImageView);
            } else {
                cell.setStyle("-fx-background-color: darkblue; -fx-border-color: darkblue;");
            }
            shipBox.getChildren().add(cell);
        }

        // Add prisoners below the grid
        HBox prisonerBox = new HBox();
        prisonerBox.setAlignment(Pos.CENTER);
        prisonerBox.setSpacing(5);
        for (int col = 0; col < prisoners.length; col++) {
            Button cell = new Button();
            cell.setMinSize(50, 50);
            updateCell(cell, prisoners[col]);
            int finalCol = col;
            cell.setOnAction(e -> handlePrisonerClick(finalCol));
            prisonerBox.getChildren().add(cell);
        }

        // Add close button
        Button closeButton = new Button("X");
        closeButton.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        HBox closeButtonBox = new HBox(closeButton);
        closeButtonBox.setAlignment(Pos.TOP_RIGHT);
        closeButtonBox.setStyle("-fx-padding: 10px;");

        Label statusLabel = new Label("Game Status: Waiting for players...");
        HBox statusBox = new HBox(statusLabel);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setStyle("-fx-padding: 10px; -fx-background-color: #f0f0f0;");

        VBox layout = new VBox();
        layout.getChildren().addAll(closeButtonBox, shipBox, grid, prisonerBox, statusBox);

        Scene scene = new Scene(layout, 500, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            isApplicationLaunched = false;
            System.out.println("Warning: The game window was closed. Returning to the main menu.");
            Platform.runLater(() -> Main.showMainMenu(false));
        });
    }

    private void updateCell(Button cell, String value) {
        switch (value) {
            case PRISONER_GREEN:
                cell.setGraphic(new ImageView(prisonerGreenImage));
                cell.setStyle("-fx-border-color: black; -fx-border-width: 2px;");
                break;
            case PRISONER_BLUE:
                cell.setGraphic(new ImageView(prisonerBlueImage));
                cell.setStyle("-fx-border-color: black; -fx-border-width: 2px;");
                break;
            case GUARD:
                cell.setGraphic(new ImageView(guardImage));
                cell.setStyle("-fx-border-color: black; -fx-border-width: 2px;");
                break;
                case SHIP:
                ImageView shipImageView = new ImageView(shipImage);
                shipImageView.setFitWidth(60); // Set the width of the ship icon
                shipImageView.setFitHeight(60); // Set the height of the ship icon
                cell.setGraphic(shipImageView);
                cell.setStyle("-fx-border-color: darkblue; -fx-border-width: 2px;");
                break;
            case EMPTY_RED:
                cell.setStyle("-fx-background-color: red; -fx-border-color: black; -fx-border-width: 2px;"); // Set TOMATO color for empty cells with black border
                break;
            default:
                cell.setStyle("-fx-background-color: #BA55D3; -fx-border-color: black; -fx-border-width: 2px;"); // Set MEDIUMORCHID color for empty cells with black border
                break;
        }
    }

    private void handleCellClick(int row, int col) {
        // Implement the logic for handling cell clicks
        // For example, move the player or guard to the clicked cell
        System.out.println("Cell clicked: (" + row + ", " + col + ")");
    }

    private void handlePrisonerClick(int col) {
        // Implement the logic for handling prisoner clicks
        System.out.println("Prisoner clicked: (" + col + ")");
    }

    public static void startGame() {
        if (!isApplicationLaunched) {
            isApplicationLaunched = true;
            new Thread(() -> Application.launch(GameGUI.class)).start();
        } else {
            Platform.runLater(() -> {
                try {
                    new GameGUI().start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void main(String[] args) {
        startGame();
    }
}