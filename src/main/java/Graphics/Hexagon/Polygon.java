package Graphics.Hexagon;

import Enum.Color;

public class Polygon {

    public static javafx.scene.paint.Color WHITE = javafx.scene.paint.Color.rgb(255, 255, 255);
    public static javafx.scene.paint.Color BLACK = javafx.scene.paint.Color.rgb(35, 32, 29);
    public static javafx.scene.paint.Color RED = javafx.scene.paint.Color.rgb(218, 97, 28);
    public static javafx.scene.paint.Color BLUE = javafx.scene.paint.Color.rgb(0, 142, 219);
    public static javafx.scene.paint.Color EMPTY = javafx.scene.paint.Color.rgb(118, 254, 118);

    public static javafx.scene.shape.Polygon getPolygon(Color color) {
        javafx.scene.shape.Polygon poly = new javafx.scene.shape.Polygon();
        poly.getPoints().addAll(
                20., 0.,
                40., .25*50,
                40., .75*50,
                20., 50.,
                0., .75*50,
                0., .25*50);
        switch (color){
            case WHITE: poly.setFill(WHITE); poly.setStyle("-fx-border-color: BLACK; -fx-border-width: 2;"); break;
            case BLACK: poly.setFill(BLACK); break;
            case RED: poly.setFill(RED); break;
            case BLUE: poly.setFill(BLUE); break;
            default: poly.setFill(EMPTY);
        }
        return poly;
    }

    public void refresh(int q, int r, HexTile[][] pieces, HexGraphic hg) {
        switch (pieces[q][r].getValue()) {
            case 0:
                hg.fill = Polygon.EMPTY;
                break;
            case 1:
                hg.fill = Polygon.WHITE;
                break;
            case 2:
                hg.fill = Polygon.BLACK;
                break;
            case 3:
                hg.fill = Polygon.RED;
                break;
            case 4:
                hg.fill = Polygon.BLUE;
                break;
            default:
                hg.fill = Polygon.EMPTY;
        }
    }
}
