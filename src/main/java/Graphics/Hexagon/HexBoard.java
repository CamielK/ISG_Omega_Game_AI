package Graphics.Hexagon;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;

import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Inspired by: https://gist.github.com/Akjir/5721503
 */
public class HexBoard extends Canvas {

    private int width;
    private int height;
    private int radius;
    private HexMetrics metrics;
    private HexGraphic hexGraphic = new HexGraphic(getGraphicsContext2D());
    private double[] cornersX = new double[6];
    private double[] cornersY = new double[6];

    private int offsetLimit;
    private int axialSize;
    private int[][] pieces;

    public HexBoard(int size, int radius) {
        this.width = size*2-1;
        this.height = size*2-1;
        this.radius = radius;
        this.metrics = new HexMetrics((double) radius);

        this.setWidth(metrics.width * width);
        this.setHeight(((width * radius * 3) / 2) + (radius / 2));

        offsetLimit = size-1;
        axialSize = size*2-1;
        pieces = new int[axialSize][axialSize];

        setOnMouseClicked(this::handleMouseClick);
    }

    private HexCellHandler hexCellHandler = null;
    public HexCellHandler getHexCellHandler() {return hexCellHandler;}
    public void setHexCellHandler(HexCellHandler handler) {hexCellHandler = handler;}

    private void handleMouseClick(MouseEvent event) {
        System.out.println("\nMouseclick:");
        System.out.println(event.getX());
        System.out.println(event.getY());
        double[] hex = metrics.pixel_to_hex(event.getX(), event.getY());
        System.out.println(hex[0]);
        System.out.println(hex[1]);

        if (pieces[(int) Math.round(hex[0])][(int) Math.round(hex[1])] == 2) {
            pieces[(int) Math.round(hex[0])][(int) Math.round(hex[1])] = 0;
        } else {
            pieces[(int) Math.round(hex[0])][(int) Math.round(hex[1])] = 2;
        }

        repaint();

//        for (int i = 0; i < hexCorners.size(); i++) {
//            if (metrics.isBoundingHex(hexCorners.get(i))) {
//                System.out.println(hexCorners.get(i).get(2)[0]);
//                System.out.println(hexCorners.get(i).get(2)[1]);
//            }
//        }
    }

    private ArrayList<ArrayList<double[]>> hexCorners;
    public void repaint() {
        hexCorners = new ArrayList<>();
        for (int q=0; q<axialSize; q++) {
            for (int r=0; r<axialSize; r++) {
                // Check hexagon boundaries
                if (q+r >= offsetLimit && q+r <= (axialSize-1)*2-offsetLimit) {
                    repaintCell(q, r);
                }
            }
        }
    }
    private void repaintCell(int q, int r) {
        if (hexCellHandler != null) {
            hexCellHandler.refresh(q, r, pieces, hexGraphic);
        }
        ArrayList<double[]> corners = metrics.computeCorners(q, r, cornersX, cornersY);
        hexGraphic.draw(corners.get(0), corners.get(1));
        corners.add(new double[]{q, r});
        hexCorners.add(corners);
    }
}