package Graphics.Hexagon;

import Graphics.Controller;
import Library.Model.Player;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Library.Enum.Color;

/**
 * Inspired by: https://gist.github.com/Akjir/5721503, https://www.redblobgames.com/grids/hexagons/
 */
public class HexBoard extends Canvas {

    private int width;
    private int height;
    private int radius;
    private int size;
    private double canvasHeightOffset;
    private HexMetrics metrics;
    private HexGraphic hexGraphic = new HexGraphic(getGraphicsContext2D());

    private int axialSize;
    private HexTile[][] hexTiles;
    private Controller parent;
    private ArrayList<HexTile> moveHistory = new ArrayList<>();

    // AI debug vars
    public HexTile[][] bestBoard = null;

    public HexBoard(int size, Controller parent) {
        this.parent = parent;
        this.size = size;
        this.width = size*2-1;
        this.height = size*2-1;

        // Initialize flat axial representation of game board
        // The board is defined in the array [q,r] where the cells at index (q+r < offsetLimit) are unused
        axialSize = size*2-1;
        hexTiles = new HexTile[axialSize][axialSize];

        // Initialize board pieces
        int offsetLimit = size - 1;
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
            radius = ((d_height-40) / (height*.75+.25)) / 2;
            computed_height = (height*.75+.25) * (2*radius);
        }

        // Apply new radius
        this.radius = (int) radius;
        this.metrics = new HexMetrics(radius);
        this.setWidth(metrics.width * width + 10);
        canvasHeightOffset = (d_height - computed_height) / 2;
        this.setHeight(d_height);
        repaint();
    }

    /**
     * Returns the game evaluation for the given board
     * Scores are returned for each color
     * @param hexTiles board representation
     * @return Map
     */
    public Map<Color, Integer> evaluatePlayerScores(HexTile[][] hexTiles) {
        int axialSize = hexTiles.length;

        // Reset groups
        for (int q=0; q<axialSize; q++) {
            for (int r = 0; r < axialSize; r++) {
                if (hexTiles[q][r] != null) hexTiles[q][r].setGroup(0);
            }
        }

        // Update groups for all tiles
        int maxGroupId = 1;
        ArrayList<Color> colors = new ArrayList<Color>();
        for (int q=0; q<axialSize; q++) {
            for (int r = 0; r < axialSize; r++) {
                if (hexTiles[q][r] != null && hexTiles[q][r].getColor() != Color.EMPTY && hexTiles[q][r].getGroup() <= 0) {// Check neighbours for group scoring
                    if (!colors.contains(hexTiles[q][r].getColor())) {
                        colors.add(hexTiles[q][r].getColor());
                    }

                    HexTile[] neighbours = new HexTile[]{
                            (q+1<axialSize              ? hexTiles[q+1][r]  : null),
                            (q-1>=0                     ? hexTiles[q-1][r]  : null),
                            (q+1<axialSize && r-1>=0    ? hexTiles[q+1][r-1]: null),
                            (r+1<axialSize && q-1>=0    ? hexTiles[q-1][r+1]: null),
                            (r+1<axialSize              ? hexTiles[q][r+1]  : null),
                            (r-1>=0                     ? hexTiles[q][r-1]  : null),
                    };

                    // Analyze neighbours
                    int hasGroupingNeighbour = 0;
                    for (HexTile tile : neighbours) {
                        if (tile != null && tile.getColor() == hexTiles[q][r].getColor() && tile.getGroup() > 0) {

                            if (hasGroupingNeighbour > 0) {
                                // This tile joins 2 groups together > update that group
                                joinTileGroupsWithColor(tile.getColor(), hasGroupingNeighbour, tile.getGroup());
                                break; // no need to compare any further
                            } else {
                                hexTiles[q][r].setGroup(tile.getGroup());
                                hasGroupingNeighbour = tile.getGroup();
                            }

                        }
                    }
                    if (hasGroupingNeighbour == 0) {
                        hexTiles[q][r].setGroup(maxGroupId);
                        maxGroupId++;
                    }
                }
            }
        }

        // Check player scores
        Map<Color, Integer> colorScores = new HashMap<Color, Integer>();
        for (Color color : colors) {

            // compute score
            Map<Integer, Integer> groupScores = new HashMap<>();
            for (int q=0; q < axialSize; q++) {
                for (int r = 0; r < axialSize; r++) {
                    // Evaluate this tiles group
                    HexTile tile = hexTiles[q][r];
                    if (tile != null && tile.getColor() == color) {
                        if (groupScores.containsKey(tile.getGroup())) {
                            groupScores.put(tile.getGroup(), (groupScores.get(tile.getGroup())+1));
                        } else {
                            groupScores.put(tile.getGroup(), 1);
                        }
                    }
                }
            }
            int score = 0;
            for (Map.Entry<Integer, Integer> groupScore : groupScores.entrySet()) {
                if (score == 0) {
                    score = groupScore.getValue();
                } else {
                    score = score * groupScore.getValue();
                }
            }

            colorScores.put(color, score);
        }

        return colorScores;
    }

    public int numEmptySpaces() {
        int empty = 0;
        for (int q=0; q<axialSize; q++) {
            for (int r = 0; r < axialSize; r++) {
                if (hexTiles[q][r] != null && hexTiles[q][r].getColor() == Color.EMPTY) {
                    empty++;
                }
            }
        }
        return empty;
    }

    public void undoMoves(int numMoves) {
        for (int i = moveHistory.size()-1; i >= moveHistory.size()-numMoves; i--) {
            moveHistory.get(i).setColor(Color.EMPTY);
            moveHistory.get(i).setGroup(0);
        }
        updateAll();
    }

    /**
     * Find the tile that was clicked and handle its update if applicable
     */
    private void handleMouseClick(MouseEvent event) {
        if (parent.currentPlayerIsHuman()) {
            for (int q=0; q<axialSize; q++) {
                for (int r = 0; r < axialSize; r++) {
                    if (hexTiles[q][r] != null
                            && hexTiles[q][r].getColor() == Color.EMPTY
                            && metrics.isBoundingHex(hexTiles[q][r].getCornersX(), hexTiles[q][r].getCornersY(), (int) event.getX(), (int) event.getY())) {
                        Color placed = parent.placeTile();
                        if (placed != null) {
                            hexTiles[q][r].setColor(placed);
                            moveHistory.add(hexTiles[q][r]);
                            updateAll();
                            r = axialSize; q = axialSize;
                        }
                    }
                }
            }
        }
    }

    /**
     * Joins group2 into group1 of the same color
     */
    private void joinTileGroupsWithColor(Color color, int group1, int group2) {
        for (int q=0; q<axialSize; q++) {
            for (int r = 0; r < axialSize; r++) {
                if (hexTiles[q][r] != null && hexTiles[q][r].getColor() == color) {
                    if (hexTiles[q][r].getGroup() == group2) {
                        hexTiles[q][r].setGroup(group1);
                    }
                }
            }
        }
    }

    public void updateAll() {
        Map<Color, Integer> colorScores = evaluatePlayerScores(hexTiles);
        for (Player player : parent.players) {
            player.setScore(colorScores.getOrDefault(player.getColor(), 0));
        }
        parent.reloadScoreboard();
        repaint();
    }

    /**
     * Draw best board from last AI move for debugging
     */
    public void repaintBestBoard() {
        if (bestBoard != null) {
            getGraphicsContext2D().clearRect(0,0,this.getWidth(),this.getHeight());
            for (int q=0; q<axialSize; q++) {
                for (int r=0; r<axialSize; r++) {
                    // Check hexagon boundaries
                    if (bestBoard[q][r] != null) {
                        repaintCell(bestBoard, q, r);
                    }
                }
            }
        }
    }

    /**
     * Draw regular hexagon board grid
     */
    public void repaint() {
        getGraphicsContext2D().clearRect(0,0,this.getWidth(),this.getHeight());
        for (int q=0; q<axialSize; q++) {
            for (int r=0; r<axialSize; r++) {
                // Check hexagon boundaries
                if (hexTiles[q][r] != null) {
                    repaintCell(hexTiles, q, r);
                }
            }
        }
    }

    private void repaintCell(HexTile[][] hexTiles, int q, int r) {
        if (hexCellHandler != null) {
            hexCellHandler.refresh(q, r, hexTiles, hexGraphic);
        }
        metrics.computeCorners(hexTiles[q][r], canvasHeightOffset);
        hexGraphic.draw(hexTiles[q][r]);
    }

    public HexTile[][] getGameState() {
        return hexTiles;
    }

}