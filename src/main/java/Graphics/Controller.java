package Graphics;

import Agent.Agent;
import Agent.Human;
import Agent.MinMaxBasic;
import Agent.AlphaBetaBasic;
import Agent.NegaMax;
import Agent.Random;
import Library.Enum.Color;
import Graphics.Component.Scoreboard;
import Graphics.Component.TurnInformation;
import Graphics.Hexagon.HexBoard;
import Library.Config;
import Library.Model.Player;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSlider;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Controller implements Initializable {

    private final int WIDTH_PLAYERS = 260;
    public final Color[] colors = new Color[]{Color.WHITE, Color.BLACK, Color.RED, Color.BLUE};
    private Map<Color, Integer> colorSelectMap = new HashMap<Color, Integer>();

    private HexBoard board;
    public Player[] players;
    public int currentTurnPlayerId = 0;
    public int currentTurnTilesLeft = Config.NUM_PLAYERS;

    // containers
    @FXML public BorderPane root;
    @FXML public VBox contentContainer;
    @FXML public BorderPane playerContainer;
    @FXML public VBox startContainer;
    @FXML public VBox boardContainer;
    @FXML public HBox boardArea;
    @FXML public HBox currentPlayerArea;
    @FXML public VBox playerSelect;
    @FXML public VBox playersBox;
    @FXML public VBox settingsContainer;
    @FXML public HBox playerSelectP1;
    @FXML public HBox playerSelectP2;
    @FXML public VBox polyP1;
    @FXML public VBox polyP2;
    @FXML public HBox selectAgentP1;
    @FXML public HBox selectAgentP2;

    @FXML public Rectangle startRect;
    @FXML public Polygon startPoly;

    // labels
    @FXML public Label labelHexSize;

    // inputs
    @FXML public JFXComboBox selectColorP1;
    @FXML public JFXComboBox selectColorP2;
    @FXML public JFXSlider sliderHexSize;
    @FXML public JFXButton btnStartGame;
    @FXML public JFXButton btnResetGame;
    @FXML private JFXButton btnSettings;
    @FXML private JFXButton btnUndo;
    @FXML private JFXButton btnEnd;
    @FXML public JFXButton btnSwap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colorSelectMap.put(Color.WHITE, 0);
        colorSelectMap.put(Color.BLACK, 1);
        initPlayerSelect(Config.NUM_PLAYERS);
        sliderHexSize.valueProperty().addListener((observable, oldValue, newValue) -> {
            int value = newValue.intValue();
            labelHexSize.setText(Integer.toString(value));
        });

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
        AwesomeDude.setIcon(btnStartGame, AwesomeIcon.HAND_ALT_RIGHT, "28px", ContentDisplay.RIGHT);
        AwesomeDude.setIcon(btnSwap, AwesomeIcon.EXCHANGE, "32px", ContentDisplay.RIGHT);
    }

    private void initPlayerSelect(int numPlayers) {
        // Shuffle color assignment
        Color[] used_colors = Arrays.copyOfRange(colors, 0, numPlayers);
        List<Color> list = Arrays.asList(used_colors);
        Collections.shuffle(list);
        Object[] used_colors_shuffled = list.toArray();

        // Init players
        players = new Player[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            Player player = new Player(i);
            players[i] = player;
            players[i].setColor((Color) used_colors_shuffled[i]);

            // White player starts the game!
            if (used_colors_shuffled[i] == Color.WHITE) {
                currentTurnPlayerId = i;
            }

            JFXComboBox<Label> jfxCombo = new JFXComboBox<Label>();
            jfxCombo.getItems().add(new Label(Human.class.getName()));
            jfxCombo.getItems().add(new Label(NegaMax.class.getName()));
            jfxCombo.getItems().add(new Label(AlphaBetaBasic.class.getName()));
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
            if (i==0) selectAgentP1.getChildren().add(jfxCombo);
            else selectAgentP2.getChildren().add(jfxCombo);
        }

        // GFX
        updatePlayerSelectionGFX();
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
            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            //Background work
                            currentPlayer.GetMove(board, Arrays.copyOfRange(colors, 0, Config.NUM_PLAYERS));
                            currentTurnTilesLeft = 0;

                            final CountDownLatch latch = new CountDownLatch(1);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        //FX Stuff done here
                                        board.updateAll();
                                        endTurn();
                                    }finally{
                                        latch.countDown();
                                    }
                                }
                            });
                            latch.await();
                            //Keep with the background work
                            return null;
                        }
                    };
                }
            };
            service.start();
        } else {
            // Human turns are not handled > wait for user interaction instead
        }
    }

    public void undoTurn() {
        int numUndo = Config.NUM_PLAYERS-currentTurnTilesLeft;
        currentTurnTilesLeft = Config.NUM_PLAYERS;
        board.undoMoves(numUndo);
    }

    public void endTurn() {
        if (currentTurnTilesLeft == 0) {
            currentTurnTilesLeft = Config.NUM_PLAYERS;
            currentTurnPlayerId++;
            if (currentTurnPlayerId > Config.NUM_PLAYERS-1) {
                currentTurnPlayerId = 0;
            }
            reloadScoreboard();
        }

        // Check game termination
        if (players[currentTurnPlayerId].getColor() == Color.WHITE && board.numEmptySpaces() < Math.pow(Config.NUM_PLAYERS, Config.NUM_PLAYERS)) {
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

    private void updatePlayerSelectionGFX() {
        polyP1.getChildren().clear();
        polyP1.getChildren().add(Graphics.Hexagon.Polygon.getPolygon(players[0].getColor(), 1.5));
        polyP2.getChildren().clear();
        polyP2.getChildren().add(Graphics.Hexagon.Polygon.getPolygon(players[1].getColor(), 1.5));
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
    private boolean showBestBoard = false;
    @FXML protected void ToggleBestBoard(ActionEvent event) {
        showBestBoard = !showBestBoard;
        Config.GFX_PLACED_ENABLED = showBestBoard;
        if (!showBestBoard) board.repaint();
        else board.repaintBestBoard();
    }
    @FXML protected void SwapColors(ActionEvent event) {
        Color c = players[0].getColor();
        players[0].setColor(players[1].getColor());
        players[1].setColor(c);
        if (c == Color.WHITE) currentTurnPlayerId = 1;
        else currentTurnPlayerId = 0;
        updatePlayerSelectionGFX();
    }

    @FXML protected void StartGame(ActionEvent event) {
        resetBoard((int) sliderHexSize.getValue());
        startContainer.setVisible(false);
        SetBoardContainerVisible(true);
        SetPlayerContainerVisible(true);
        reloadScoreboard();
        handleTurn();
    }

    @FXML protected void ResetGame(ActionEvent event) {
        SetBoardContainerVisible(false);
        SetPlayerContainerVisible(false);
        startContainer.setVisible(true);
        currentTurnTilesLeft = Config.NUM_PLAYERS;
        for (Player player : players) {
            if (player.getColor() == Color.WHITE) {
                currentTurnPlayerId = player.getId();
            }
        }
//        initPlayerSelection(Config.NUM_PLAYERS);
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
