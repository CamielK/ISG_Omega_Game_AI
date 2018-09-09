package Graphics.Component;

import Graphics.Controller;
import Library.Player;
import com.jfoenix.controls.JFXButton;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

public class TurnInformation {

    public static void getTurnInformation(Controller parent) {
        // draw current player info
        VBox info = new VBox();
        Label labelCurrent = new Label("Player " + parent.players[parent.currentTurnPlayerId].getNumber() + " (" + parent.players[parent.currentTurnPlayerId].getColor().toString().toLowerCase() + ")");
        labelCurrent.getStyleClass().add("player-label");
        info.setAlignment(Pos.CENTER);
        info.getChildren().addAll(new Label("Current turn:"), labelCurrent);

        // Show tiles left in this turn
        VBox tilesContainer = new VBox();
        tilesContainer.setAlignment(Pos.CENTER_LEFT);
        HBox tilesCollection = new HBox(20);
        tilesCollection.setAlignment(Pos.CENTER);
        for (int i=0; i < parent.currentTurnTilesLeft; i++) {
            Polygon tile = Graphics.Hexagon.Polygon.getPolygon(parent.colors[i], 1.3);
            tilesCollection.getChildren().add(tile);
        }
        tilesContainer.getChildren().addAll(new Label("Tiles left in this turn: " + parent.currentTurnTilesLeft), tilesCollection);

        parent.currentPlayerArea.getChildren().clear();
        parent.currentPlayerArea.getChildren().addAll(info, new Separator(Orientation.VERTICAL), tilesContainer, new Separator(Orientation.VERTICAL));

        if (parent.currentPlayerIsHuman()) {
            // End turn button
            JFXButton btnUndo = new JFXButton("Undo turn");
            btnUndo.getStyleClass().add("btn-info");
            btnUndo.setOnAction(event -> parent.undoTurn());
            btnUndo.setDisable(true);
            AwesomeDude.setIcon(btnUndo, AwesomeIcon.CHEVRON_CIRCLE_LEFT, "24px");
            if (parent.currentTurnTilesLeft < parent.NUM_PLAYERS) {
                btnUndo.setDisable(false);
            }
            JFXButton btnEnd = new JFXButton("End turn");
            btnEnd.setDisable(true);
            AwesomeDude.setIcon(btnEnd, AwesomeIcon.CHEVRON_CIRCLE_RIGHT, "24px");
            if (parent.currentTurnTilesLeft == 0) {
                btnEnd.setDisable(false);
                PathTransition jump = new PathTransition();
                Arc path = new Arc(
                        btnEnd.getTranslateX() + 80,
                        btnEnd.getTranslateY() + 20,
                        0, 60, 360, 2
                );
                jump.setPath(path);
                jump.setNode(btnEnd);
                jump.setAutoReverse(true);
                jump.setInterpolator(Interpolator.EASE_BOTH);
                jump.setDuration(Duration.millis(300));
                jump.setCycleCount(Integer.MAX_VALUE);
                jump.play();
            }
            btnEnd.getStyleClass().add("btn-error");
            btnEnd.setOnAction(event -> parent.endTurn());
            parent.currentPlayerArea.getChildren().addAll(btnUndo, btnEnd);
        } else {
            Label labelAI = new Label("Waiting for AI move...");
            labelAI.getStyleClass().add("player-label");
            parent.currentPlayerArea.getChildren().addAll(labelAI);
        }
    }
}
