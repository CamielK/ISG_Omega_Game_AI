package Graphics.Component;

import Graphics.Controller;
import Library.Model.Player;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

public class Scoreboard {
    public static void getScoreboard(Controller parent) {
        parent.playersBox.getChildren().clear();
        for (Player player : parent.players) {
            HBox title = new HBox();
            title.setAlignment(Pos.CENTER);
            Label label = new Label("Player " + Integer.toString(player.getNumber()));
            label.getStyleClass().add("player-label");
            Polygon poly = Graphics.Hexagon.Polygon.getPolygon(player.getColor());
            Separator sep = new Separator(Orientation.VERTICAL);
            sep.setStyle("-fx-padding: 20");

            // Indicate current player
            if (parent.currentTurnPlayerId == player.getId()) {
                // animate player poly
                ScaleTransition st = new ScaleTransition(Duration.millis(1700), poly);
                st.setByX(.4f);
                st.setByY(.4f);
                st.setInterpolator(Interpolator.EASE_BOTH);
                st.setCycleCount(Integer.MAX_VALUE);
                st.setAutoReverse(true);
                st.play();
            }

            title.getChildren().addAll(poly, sep, label);
            Label label3 = new Label("<" + player.getAgent().getClass().getName() + ">");
            Label label4 = new Label("Score: " + player.getScore());
            parent.playersBox.getChildren().addAll(title, label3, label4);
            if (player.getId() < parent.players.length-1) {
                parent.playersBox.getChildren().add(new Separator());
            }
        }
    }
}
