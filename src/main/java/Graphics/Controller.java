package Graphics;

import Graphics.Hexagon.HexBoard;
import Graphics.Hexagon.HexCellHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML public Group boardGroup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resetBoard(4, 25);
    }

    private void resetBoard(int size, int radius) {
        HexBoard board = new HexBoard(size, radius);
        board.setHexCellHandler(new HexCellHandler());
        board.repaint();
        boardGroup.getChildren().add(board);
    }
}
