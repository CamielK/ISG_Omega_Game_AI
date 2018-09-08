package Graphics;

import Agent.Agent;
import Agent.Human;
import Enum.Color;
import Graphics.Hexagon.HexBoard;
import Library.Config;
import Library.Player;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSlider;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private final int WIDTH_PLAYERS = 260;
    private final int NUM_PLAYERS = 2;
    private final Color[] colors = new Color[]{Color.WHITE, Color.BLACK, Color.RED, Color.BLUE};

    private Player[] players;
    private HexBoard board;
    private int currentTurnPlayerId = 0;
    private int currentTurnTilesLeft = NUM_PLAYERS;

    // containers
    @FXML public BorderPane root;
    @FXML public VBox contentContainer;
    @FXML public BorderPane playerContainer;
    @FXML public VBox startContainer;
    @FXML public VBox boardContainer;
    @FXML public HBox boardArea;
    @FXML public HBox currentPlayerArea;
    @FXML public HBox playerSelect;
    @FXML public VBox playersBox;

    @FXML public Rectangle startRect;
    @FXML public Polygon startPoly;

    // labels
    @FXML public Label labelHexSize;

    // inputs
    @FXML public JFXSlider sliderHexSize;
    @FXML public JFXButton btnStartGame;
    @FXML public JFXButton btnResetGame;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sliderHexSize.valueProperty().addListener((observable, oldValue, newValue) -> {
            int value = newValue.intValue();
            labelHexSize.setText(Integer.toString(value));
        });
        initPlayerSelection(NUM_PLAYERS);

        // Add listener to window size
        root.widthProperty().addListener((obs, oldVal, newVal) -> updateBoardDimensions());
        root.heightProperty().addListener((obs, oldVal, newVal) -> updateBoardDimensions());

        // Bind managed state to visibility
        startContainer.managedProperty().bind(startContainer.visibleProperty());

        // Animations
        ScaleTransition st = new ScaleTransition(Duration.millis(2000), startRect);
        st.setByX(.6f);
        st.setCycleCount(Integer.MAX_VALUE);
        st.setAutoReverse(true);
        st.play();
    }
    private void updateBoardDimensions() {
        if (board != null) {
            double sidebarWidth = Math.max(WIDTH_PLAYERS, root.getWidth() * 0.25);
            playerContainer.setMinWidth(sidebarWidth);
            board.setDimensions((int) (root.getWidth()-sidebarWidth), (int) (root.getHeight() - currentPlayerArea.getHeight()));
        }
    }

    public void reloadScoreboard() {
        playersBox.getChildren().clear();
        for (Player player : players) {
            HBox title = new HBox();
            title.setAlignment(Pos.CENTER);
            Label label = new Label("Player " + Integer.toString(player.getNumber()));
            label.getStyleClass().add("player-label");
            Polygon poly = Graphics.Hexagon.Polygon.getPolygon(player.getColor());
            Separator sep = new Separator(Orientation.VERTICAL);
            sep.setStyle("-fx-padding: 20");

            // Indicate current player
            if (currentTurnPlayerId == player.getId()) {
                // animate player poly
                ScaleTransition st = new ScaleTransition(Duration.millis(2000), poly);
                st.setByX(.5f);
                st.setByY(.5f);
                st.setCycleCount(Integer.MAX_VALUE);
                st.setAutoReverse(true);
                st.play();

                // draw current player info
                Label labelCurrent = new Label("Current turn: Player " + player.getNumber());
                labelCurrent.getStyleClass().add("player-label");

                // Show tiles left in this turn
                VBox tilesContainer = new VBox();
                tilesContainer.setAlignment(Pos.CENTER_LEFT);
                HBox tilesCollection = new HBox(20);
                tilesCollection.setAlignment(Pos.CENTER);
                for (int i=0; i < currentTurnTilesLeft; i++) {
                    Polygon tile = Graphics.Hexagon.Polygon.getPolygon(colors[i], 1.3);
                    tilesCollection.getChildren().add(tile);
                }
                tilesContainer.getChildren().addAll(new Label("Tiles left in this turn: " + currentTurnTilesLeft), tilesCollection);

                // End turn button
                JFXButton btnUndo = new JFXButton("Undo turn");
                btnUndo.getStyleClass().add("btn-reset");
                btnUndo.setOnAction(event -> undoTurn());
                JFXButton btnEnd = new JFXButton("End turn");
                btnEnd.getStyleClass().add("btn-error");
                btnEnd.setOnAction(event -> endTurn());

                currentPlayerArea.getChildren().clear();
                currentPlayerArea.getChildren().addAll(labelCurrent, tilesContainer, btnUndo, btnEnd);
            }

            title.getChildren().addAll(poly, sep, label);
            Label label3 = new Label("<" + player.getAgent().getClass().getName() + ">");
            Label label4 = new Label("Score: " + player.getScore());
            playersBox.getChildren().addAll(title, label3, label4);
            if (player.getId() < players.length-1) {
                playersBox.getChildren().add(new Separator());
            }
        }
    }

    private void undoTurn() {
        int numUndo = NUM_PLAYERS-currentTurnTilesLeft;
        currentTurnTilesLeft = NUM_PLAYERS;
        board.undoMoves(numUndo);
    }

    private void endTurn() {
        if (currentTurnTilesLeft == 0) {
            currentTurnTilesLeft = NUM_PLAYERS;
            currentTurnPlayerId++;
            if (currentTurnPlayerId > NUM_PLAYERS-1) {
                currentTurnPlayerId = 0;
            }
            reloadScoreboard();
        }
    }

    private void initPlayerSelection(int numPlayers) {
        playerSelect.getChildren().clear();
        players = new Player[numPlayers];

        // Shuffle color assignment
        Color[] used_colors = Arrays.copyOfRange(colors, 0, numPlayers);
        List<Color> list = Arrays.asList(used_colors);
        Collections.shuffle(list);
        Object[] used_colors_shuffled = list.toArray();

        // Init players
        for (int i = 0; i < numPlayers; i++) {
            // Player object
            Player player = new Player(i);
            player.setColor((Color) used_colors_shuffled[i]);
            players[i] = player;

            // White player starts the game!
            if (used_colors_shuffled[i] == Color.WHITE) {
                currentTurnPlayerId = i;
            }

            // Player selection item
            VBox playerBox = new VBox(30);
            playerBox.setAlignment(Pos.CENTER);

            // Draw player selection
            Label label = new Label("Player " + Integer.toString(player.getNumber()));
            label.getStyleClass().add("player-label");
            JFXComboBox<Label> jfxCombo = new JFXComboBox<Label>();
            jfxCombo.getItems().add(new Label("Human"));
            jfxCombo.getItems().add(new Label("MinMaxBasic"));
            jfxCombo.setPromptText("Human");
            jfxCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    Agent agent = (Agent) Class.forName("Agent." + newVal.getText()).newInstance();
                    player.setAgent(agent);
                    reloadScoreboard();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            Polygon poly = Graphics.Hexagon.Polygon.getPolygon(player.getColor(), 1.5);

            playerBox.getChildren().addAll(label, jfxCombo, poly);
            playerSelect.getChildren().add(playerBox);
        }
        reloadScoreboard();
    }

    public Player[] getPlayers() {
        return players;
    }
    public boolean currentPlayerIsHuman() {
        return players[currentTurnPlayerId].getAgent() instanceof Human;
    }

    /**
     * Places the current tile and returns its color value
     */
    public Color placeTile() {
        if (currentTurnTilesLeft > 0) {
            Color placed = colors[currentTurnTilesLeft-1];
            currentTurnTilesLeft--;
            return placed;
        } else {
            return null;
        }
    }

    private void SetPlayerContainerVisible(boolean visible) {
        playerContainer.setVisible(visible);
        playerContainer.setManaged(visible);
    }

    private void SetBoardContainerVisible(boolean visible) {
        boardContainer.setVisible(visible);
        boardContainer.setManaged(visible);
    }

    @FXML protected void ToggleGroup(ActionEvent event) {
        Config.GFX_GROUP_ENABLED = !Config.GFX_GROUP_ENABLED;
        board.repaint();
    }
    @FXML protected void ToggleAxes(ActionEvent event) {
        Config.GFX_AXES_ENABLED = !Config.GFX_AXES_ENABLED;
        board.repaint();
    }

    @FXML protected void StartGame(ActionEvent event) {
        resetBoard((int) sliderHexSize.getValue());
        startContainer.setVisible(false);
        SetBoardContainerVisible(true);
        SetPlayerContainerVisible(true);
    }

    @FXML protected void ResetGame(ActionEvent event) {
        SetBoardContainerVisible(false);
        SetPlayerContainerVisible(false);
        startContainer.setVisible(true);
        currentTurnTilesLeft = NUM_PLAYERS;
        initPlayerSelection(NUM_PLAYERS);
    }
    private void resetBoard(int size) {
        board = new HexBoard(size, this);
        board.setHexCellHandler(new Graphics.Hexagon.Polygon());
        board.setDimensions(
                (int) (root.getWidth()-Math.max(playerContainer.getWidth(), WIDTH_PLAYERS)),
                (int) (currentPlayerArea.getHeight() > 0 ? root.getHeight() - currentPlayerArea.getHeight() : root.getHeight()));
        boardArea.getChildren().clear();
        boardArea.getChildren().add(board);
    }
}
