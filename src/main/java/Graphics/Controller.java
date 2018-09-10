package Graphics;

import Agent.Agent;
import Agent.Human;
import Agent.MinMaxBasic;
import Agent.Random;
import Enum.Color;
import Graphics.Component.Scoreboard;
import Graphics.Component.TurnInformation;
import Graphics.Hexagon.HexBoard;
import Library.Config;
import Library.Player;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSlider;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    public final int NUM_PLAYERS = 2;
    public final Color[] colors = new Color[]{Color.WHITE, Color.BLACK, Color.RED, Color.BLUE};

    private HexBoard board;
    public Player[] players;
    public int currentTurnPlayerId = 0;
    public int currentTurnTilesLeft = NUM_PLAYERS;

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
    @FXML public VBox settingsContainer;

    @FXML public Rectangle startRect;
    @FXML public Polygon startPoly;

    // labels
    @FXML public Label labelHexSize;

    // inputs
    @FXML public JFXSlider sliderHexSize;
    @FXML public JFXButton btnStartGame;
    @FXML public JFXButton btnResetGame;
    @FXML private JFXButton btnSettings;
    @FXML private JFXButton btnUndo;
    @FXML private JFXButton btnEnd;

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

        AwesomeDude.setIcon(btnResetGame, AwesomeIcon.UNDO, "24px", ContentDisplay.LEFT);
        AwesomeDude.setIcon(btnSettings, AwesomeIcon.COGS, "32px", ContentDisplay.LEFT);
//        AwesomeDude.setIcon(btnStartGame, AwesomeIcon.HAND_ALT_RIGHT, "28px", ContentDisplay.RIGHT);
    }
    private void updateBoardDimensions() {
        if (board != null) {
            double sidebarWidth = Math.max(WIDTH_PLAYERS, root.getWidth() * 0.25);
            playerContainer.setMinWidth(sidebarWidth);
            board.setDimensions((int) (root.getWidth()-sidebarWidth), (int) (root.getHeight() - currentPlayerArea.getHeight()));
        }
    }

    public void reloadScoreboard() {
        Scoreboard.getScoreboard(this);
        TurnInformation.getTurnInformation(this);
    }

    private void handleTurn() {
        Player currentPlayer = players[currentTurnPlayerId];
        if (!(currentPlayer.getAgent() instanceof Human)) {
            currentPlayer.getAgent().GetMove(board, Arrays.copyOfRange(colors, 0, NUM_PLAYERS));
            currentTurnTilesLeft = 0;
            board.updateAll();
            endTurn();
        } else {
            // Human turns are not handled > wait for user interaction instead
        }
    }

    public void undoTurn() {
        int numUndo = NUM_PLAYERS-currentTurnTilesLeft;
        currentTurnTilesLeft = NUM_PLAYERS;
        board.undoMoves(numUndo);
    }

    public void endTurn() {
        if (currentTurnTilesLeft == 0) {
            currentTurnTilesLeft = NUM_PLAYERS;
            currentTurnPlayerId++;
            if (currentTurnPlayerId > NUM_PLAYERS-1) {
                currentTurnPlayerId = 0;
            }
            reloadScoreboard();
        }

        // Check game termination
        if (players[currentTurnPlayerId].getColor() == Color.WHITE && board.numEmptySpaces() < Math.pow(NUM_PLAYERS, NUM_PLAYERS)) {
            showEndGameDialog();
        } else {
            handleTurn();
        }
    }

    private void showEndGameDialog() {
        Player winner = null;
        for (Player player : players) {
            if (winner == null || player.getScore() > winner.getScore()) {
                winner = player;
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Do you want to start a new game?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Game finished");
        alert.setHeaderText("Game has ended. Player " + winner.getNumber() + " was victorious!");
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            ResetGame(null);
        } else {
            Platform.exit();
        }
    }

    private void initPlayerSelection(int numPlayers) {
        initPlayerSelection(numPlayers, null);
    }
    private void initPlayerSelection(int numPlayers, Color p1Color) {
        playerSelect.getChildren().clear();
        boolean updateColorOnly = false;
        if (p1Color != null) {
            updateColorOnly = true;
            players[0].setColor(p1Color);
            if (p1Color == Color.WHITE) {
                players[1].setColor(Color.BLACK);
            } else {
                players[1].setColor(Color.WHITE);
            }
        } else {
            players = new Player[numPlayers];
        }

        // Shuffle color assignment
        Color[] used_colors = Arrays.copyOfRange(colors, 0, numPlayers);
        List<Color> list = Arrays.asList(used_colors);
        Collections.shuffle(list);
        Object[] used_colors_shuffled = list.toArray();

        // Init players
        for (int i = 0; i < numPlayers; i++) {
            // Player object
            if (!updateColorOnly) {
                Player player = new Player(i);
                players[i] = player;
                players[i].setColor((Color) used_colors_shuffled[i]);
            }

            // White player starts the game!
            if (used_colors_shuffled[i] == Color.WHITE) {
                currentTurnPlayerId = i;
            }

            // Player selection item
            VBox playerBox = new VBox(30);
            playerBox.setAlignment(Pos.CENTER);

            // Draw player selection
            Label label = new Label("Player " + Integer.toString(players[i].getNumber()));
            label.getStyleClass().add("player-label");
            JFXComboBox<Label> jfxCombo = new JFXComboBox<Label>();
            jfxCombo.getItems().add(new Label(Human.class.getName()));
            jfxCombo.getItems().add(new Label(MinMaxBasic.class.getName()));
            jfxCombo.getItems().add(new Label(Random.class.getName()));
            jfxCombo.setPromptText(players[i].getAgent().getClass().getName());
            int ic= i;
            jfxCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    Agent agent = (Agent) Class.forName(newVal.getText()).newInstance();
                    players[ic].setAgent(agent);
                    reloadScoreboard();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            Polygon poly = Graphics.Hexagon.Polygon.getPolygon(players[i].getColor(), 1.5);

            playerBox.getChildren().addAll(label, jfxCombo, poly);
            playerSelect.getChildren().add(playerBox);

            if (numPlayers == 2 && i==0) {
                System.out.println("swapping");
                JFXButton swap = new JFXButton("Swap color");
                swap.getStyleClass().add("btn-settings");
                swap.setOnAction(event -> {initPlayerSelection(numPlayers, players[1].getColor());});
                AwesomeDude.setIcon(swap, AwesomeIcon.EXCHANGE, "32px", ContentDisplay.TOP);
                playerSelect.getChildren().add(swap);
            }
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

    private void SetSettingsContainerVisible(boolean visible) {
        settingsContainer.setVisible(visible);
        settingsContainer.setManaged(visible);
    }

    private boolean showSettings = false;
    @FXML protected void ToggleSettings(ActionEvent event) {
        showSettings = !showSettings;
        SetSettingsContainerVisible(showSettings);
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
        handleTurn();
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
