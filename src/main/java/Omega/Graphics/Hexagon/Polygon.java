package Omega.Graphics.Hexagon;

import Omega.Library.Enum.Color;
import javafx.scene.effect.DropShadow;

public class Polygon {

    public static javafx.scene.paint.Color WHITE    = javafx.scene.paint.Color.rgb(250, 250, 250);
    public static javafx.scene.paint.Color BLACK    = javafx.scene.paint.Color.rgb(35,  32,  29);
    public static javafx.scene.paint.Color RED      = javafx.scene.paint.Color.rgb(218, 97,  28);
    public static javafx.scene.paint.Color BLUE     = javafx.scene.paint.Color.rgb(0,   142, 219);
    public static javafx.scene.paint.Color EMPTY    = javafx.scene.paint.Color.rgb(118, 254, 118);

    public static javafx.scene.shape.Polygon getPolygon(Color color) { return getPolygon(color, 1); }
    public static javafx.scene.shape.Polygon getPolygon(Color color, double scale) {
        javafx.scene.shape.Polygon poly = new javafx.scene.shape.Polygon();
        int shadowRadius = 14;
        poly.getPoints().addAll(
                20.*scale,  0.,
                40.*scale,          (.25*50)*scale,
                40.*scale,          (.75*50)*scale,
                20.*scale,          50.*scale,
                0.,                 (.75*50)*scale,
                0.,                 (.25*50)*scale);
        switch (color){
            case WHITE:
                poly.setFill(WHITE);
//                poly.setStroke(javafx.scene.paint.Color.BLACK);
//                poly.setStrokeWidth(1);
                shadowRadius=6;
                break;
            case BLACK:
                poly.setFill(BLACK);
                break;
            case RED:
                poly.setFill(RED);
                break;
            case BLUE:
                poly.setFill(BLUE);
                break;
            default:
                poly.setFill(EMPTY);
        }
        poly.setEffect(new DropShadow(shadowRadius, javafx.scene.paint.Color.GREY));
        return poly;
    }

    public void refresh(int q, int r, HexTile[][] pieces, HexGraphic hg) {
        switch (pieces[q][r].getColor()) {
            case WHITE:
                hg.fill = Polygon.WHITE;
                break;
            case BLACK:
                hg.fill = Polygon.BLACK;
                break;
            case RED:
                hg.fill = Polygon.RED;
                break;
            case BLUE:
                hg.fill = Polygon.BLUE;
                break;
            default:
                hg.fill = Polygon.EMPTY;
        }
    }
}
