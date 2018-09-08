package Graphics.Hexagon;

import Library.Config;
import javafx.scene.canvas.GraphicsContext;
import Enum.Color;


/**
 * Inspired by: https://gist.github.com/Akjir/5721503
 */
public class HexGraphic {

    public javafx.scene.paint.Color fill = javafx.scene.paint.Color.rgb(118, 254, 118);
    public javafx.scene.paint.Color border = javafx.scene.paint.Color.WHITE;
    private GraphicsContext context;

    private int shadow_increase = 25;
    private int shadow_width = 8;

    public HexGraphic(GraphicsContext context) {
        this.context = context;
    }

    public void draw(HexTile tile) {
        double[] cX = tile.getCornersX();
        double[] cY = tile.getCornersY();

        // Draw shadow
        for (int j = shadow_width; j > 0; j--) {
            double[] cX_shadow = new double[6];
            double[] cY_shadow = new double[6];
            for (int i = 0; i < cX.length; i++) {
                cX_shadow[i] = cX[i] + j; // x offset
                cY_shadow[i] = cY[i] + j/Math.sqrt(3); // y offset
            }
            context.setFill(javafx.scene.paint.Color.rgb(Math.max(0,236 - (shadow_width-j)*shadow_increase), Math.max(0,240 - (shadow_width-j)*shadow_increase), Math.max(0,241 - (shadow_width-j)*shadow_increase)));
            context.fillPolygon(cX_shadow, cY_shadow, 6);
        }

        // Draw hexagon
        context.setFill(fill);
        context.fillPolygon(cX, cY, 6);

        // Draw border
        context.setStroke(border);
        context.setLineWidth(2);
        context.strokePolygon(cX, cY, 6);

        // Draw debug info if enabled
        context.setFill(Polygon.BLACK);
        if (tile.getColor() == Color.BLACK) {
            context.setFill(Polygon.WHITE);
        }
        if (Config.GFX_GROUP_ENABLED) context.fillText(Integer.toString(tile.getGroup()), cX[5]+10, cY[5]+15);
        if (Config.GFX_AXES_ENABLED) context.fillText(Integer.toString(tile.getQ()) + "," + Integer.toString(tile.getR()), cX[5]+10, cY[5]+30);
    }
}
