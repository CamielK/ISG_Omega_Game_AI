package Graphics.Hexagon;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;

/**
 * Inspired by: https://gist.github.com/Akjir/5721503, https://www.redblobgames.com/grids/hexagons/
 */
public class HexBoard extends Canvas {

    private int width;
    private int height;
    private int radius;
    private int size;
    private HexMetrics metrics;
    private HexGraphic hexGraphic = new HexGraphic(getGraphicsContext2D());

    private int offsetLimit;
    private int axialSize;
    private HexTile[][] hexTiles;

    public HexBoard(int size) {
        this.size = size;
        this.width = size*2-1;
        this.height = size*2-1;

        // Initialize flat axial representation of game board
        // The board is defined in the array [q,r] where the cells at index (q+r < offsetLimit) are unused
        offsetLimit = size-1;
        axialSize = size*2-1;
        hexTiles = new HexTile[axialSize][axialSize];

        // Initialize board pieces
        for (int q=0; q<axialSize; q++) {
            for (int r = 0; r < axialSize; r++) {
                if (q+r >= offsetLimit && q+r <= (axialSize-1)*2-offsetLimit) {
                    hexTiles[q][r] = new HexTile(q, r);
                } else {
                    // The tile at this index is unused
                    hexTiles[q][r] = null;
                }
            }
        }

        setOnMouseClicked(this::handleMouseClick);
    }

    private Polygon hexCellHandler = null;
    public Polygon getHexCellHandler() {return hexCellHandler;}
    public void setHexCellHandler(Polygon handler) {hexCellHandler = handler;}

    /**
     * Update board dimensions to best fit the container
     */
    public void setDimensions(int d_width, int d_height) {
        // Check which dimension is the constraining factor
        double radius = (d_width * .6) / (size*2);
        double computed_height = (height*.75+.25) * (2*radius);
        if (computed_height > d_height-20 && d_height != 0) {
            radius = ((d_height-20) / (height*.75+.25)) / 2;
            computed_height = (height*.75+.25) * (2*radius);
        }

        // Apply new radius
        this.radius = (int) radius;
        this.metrics = new HexMetrics(radius);
        this.setWidth(metrics.width * width + 10);
        this.setHeight(computed_height + 10);
        repaint();
    }

    private void handleMouseClick(MouseEvent event) {
        // Check clicked tile
        for (int q=0; q<axialSize; q++) {
            for (int r = 0; r < axialSize; r++) {
                if (hexTiles[q][r] != null && metrics.isBoundingHex(hexTiles[q][r].getCornersX(), hexTiles[q][r].getCornersY(), (int) event.getX(), (int) event.getY())) {
                    if (hexTiles[q][r].getValue() == 2) {
                        hexTiles[q][r].setValue(0);
                    } else {
                        hexTiles[q][r].setValue(2);
                    }
                    repaint();
                    r = axialSize; q = axialSize;
                }
            }
        }
    }

    public void repaint() {
        getGraphicsContext2D().clearRect(0,0,this.getWidth(),this.getHeight());
        for (int q=0; q<axialSize; q++) {
            for (int r=0; r<axialSize; r++) {
                // Check hexagon boundaries
                if (hexTiles[q][r] != null) {
                    repaintCell(q, r);
                }
            }
        }
    }
    private void repaintCell(int q, int r) {
        if (hexCellHandler != null) {
            hexCellHandler.refresh(q, r, hexTiles, hexGraphic);
        }
        ArrayList<double[]> corners = metrics.computeCorners(q, r);
        hexGraphic.draw(corners.get(0), corners.get(1));
        hexTiles[q][r].setCornersX(corners.get(0));
        hexTiles[q][r].setCornersY(corners.get(1));
    }
}