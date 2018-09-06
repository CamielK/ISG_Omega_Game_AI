package Graphics;

import Graphics.Hexagon.HexBoard;
import Graphics.Hexagon.HexCellHandler;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSlider;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private int numPlayers = 2;
    private HexBoard board;

    // containers
    @FXML public BorderPane root;
    @FXML public VBox sideBarContainer;
    @FXML public VBox contentContainer;
    @FXML public VBox startContainer;
    @FXML public VBox boardContainer;
    @FXML public HBox playerSelect;

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
        initPlayerSelection(numPlayers);

        // Add listener to window size
        root.widthProperty().addListener((obs, oldVal, newVal) -> {
            updateBoardDimensions();
        });
        root.heightProperty().addListener((obs, oldVal, newVal) -> {
            updateBoardDimensions();
        });

        // Bind managed state to visibility
        startContainer.managedProperty().bind(startContainer.visibleProperty());
    }
    private void updateBoardDimensions() {
        if (board != null) {
            board.setDimensions((int) root.getWidth()-200, (int) root.getHeight());
        }
    }

    @FXML protected void StartGame(ActionEvent event) {
        resetBoard((int) sliderHexSize.getValue());
        boardContainer.setVisible(true);
        boardContainer.setManaged(true);
        startContainer.setVisible(false);
        btnResetGame.setDisable(false);
    }

    private void initPlayerSelection(int numPlayers) {
        playerSelect.getChildren().clear();
        for (int i = 0; i < numPlayers; i++) {
            VBox player = new VBox();
            player.setAlignment(Pos.CENTER);

            Label label = new Label("Player " + (i+1));
            JFXComboBox<Label> jfxCombo = new JFXComboBox<Label>();
            jfxCombo.getItems().add(new Label("Human"));
            jfxCombo.getItems().add(new Label("MinMaxBasic"));
            jfxCombo.setPromptText("Human");

            player.getChildren().addAll(label, jfxCombo);
            playerSelect.getChildren().add(player);
        }
    }

    @FXML protected void ResetGame(ActionEvent event) {
        boardContainer.setVisible(false);
        boardContainer.setManaged(false);
        startContainer.setVisible(true);
        btnResetGame.setDisable(true);
    }
    private void resetBoard(int size) {
        board = new HexBoard(size);
        board.setHexCellHandler(new HexCellHandler());
        board.setDimensions((int) boardContainer.getWidth(), (int) boardContainer.getHeight());
        boardContainer.getChildren().clear();
        boardContainer.getChildren().add(board);
    }
}
