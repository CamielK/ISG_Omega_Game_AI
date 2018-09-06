package Graphics.Hexagon;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


/**
 * Inspired by: https://gist.github.com/Akjir/5721503
 */
public class HexGraphic {

    public Color fill = Color.rgb(118, 254, 118);
    public Color border = Color.WHITE;
    private GraphicsContext context;

    public HexGraphic(GraphicsContext context) {
        this.context = context;
    }

    public void draw(double[] cX, double[] cY) {
        context.setFill(fill);
        context.fillPolygon(cX, cY, 6);
        context.setStroke(border);
        context.setLineWidth(2);
        context.strokePolygon(cX, cY, 6);
    }
}
