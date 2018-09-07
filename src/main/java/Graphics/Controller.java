package Graphics;

import Agent.Agent;
import Enum.Color;
import Graphics.Hexagon.HexBoard;
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

    private Player[] players;
    private HexBoard board;

    private final int WIDTH_PLAYERS = 260;

    // containers
    @FXML public BorderPane root;
    @FXML public VBox contentContainer;
    @FXML public BorderPane playerContainer;
    @FXML public VBox startContainer;
    @FXML public VBox boardContainer;
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
        initPlayerSelection(2);

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
            board.setDimensions((int) (root.getWidth()-sidebarWidth), (int) root.getHeight());
        }
    }

    private void reloadScoreboard() {
        playersBox.getChildren().clear();
        for (Player player : players) {
            HBox title = new HBox();
            title.setAlignment(Pos.CENTER);
            Label label = new Label("Player " + Integer.toString(player.getNumber()));
            label.getStyleClass().add("player-label");
            Polygon poly = Graphics.Hexagon.Polygon.getPolygon(player.getColor());
            Separator sep = new Separator(Orientation.VERTICAL);
            sep.setStyle("-fx-padding: 20");
            title.getChildren().addAll(poly, sep, label);
            Label label3 = new Label("<" + player.getAgent().getClass().getName() + ">");
            playersBox.getChildren().addAll(title, label3);
            if (player.getId() < players.length-1) {
                playersBox.getChildren().add(new Separator());
            }
        }
    }

    private final Color[] colors = new Color[]{Color.WHITE, Color.BLACK, Color.RED, Color.BLUE};
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

            // Player selection item
            VBox playerBox = new VBox();
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

            playerBox.getChildren().addAll(label, jfxCombo);
            playerSelect.getChildren().add(playerBox);
        }
        reloadScoreboard();
    }

    private void SetPlayerContainerVisible(boolean visible) {
        playerContainer.setVisible(visible);
        playerContainer.setManaged(visible);
    }

    private void SetBoardContainerVisible(boolean visible) {
        boardContainer.setVisible(visible);
        boardContainer.setManaged(visible);
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
        initPlayerSelection(2);
    }
    private void resetBoard(int size) {
        board = new HexBoard(size);
        board.setHexCellHandler(new Graphics.Hexagon.Polygon());
        board.setDimensions((int) (root.getWidth()-Math.max(playerContainer.getWidth(), WIDTH_PLAYERS)), (int) root.getHeight());
        boardContainer.getChildren().clear();
        boardContainer.getChildren().add(board);
    }
}
