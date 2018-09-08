package Graphics.Hexagon;

import Graphics.Controller;
import Library.Player;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Enum.Color;

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
    private Controller parent;
    private ArrayList<HexTile> moveHistory = new ArrayList<>();

    public HexBoard(int size, Controller parent) {
        this.parent = parent;
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

    private void evaluatePlayerScores(Player[] players) {
        for (Player player : players) {
            Color color = player.getColor();

            // compute score
            Map<Integer, Integer> groupScores = new HashMap<>();
            for (int q=0; q<axialSize; q++) {
                for (int r = 0; r < axialSize; r++) {
                    // Evaluate this tiles group
                    HexTile tile = hexTiles[q][r];
                    if (tile != null && tile.getColor() == player.getColor()) {
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
            player.setScore(score);
        }
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
    private int maxGroupId = 1;
    private void handleMouseClick(MouseEvent event) {
        if (parent.currentPlayerIsHuman()) {
            for (int q=0; q<axialSize; q++) {
                for (int r = 0; r < axialSize; r++) {
                    if (hexTiles[q][r] != null
                            && hexTiles[q][r].getColor() == Color.EMPTY
                            && metrics.isBoundingHex(hexTiles[q][r].getCornersX(), hexTiles[q][r].getCornersY(), (int) event.getX(), (int) event.getY())) {
                        Color placed = parent.placeTile();
                        if (placed != null) {
                            moveHistory.add(hexTiles[q][r]);
                            hexTiles[q][r].setColor(placed);

                            // Check neighbours for group scoring
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
                                if (tile != null && tile.getColor() == placed) {

                                    if (hasGroupingNeighbour > 0) {
                                        // This tile joins 2 groups together > update that group
                                        joinTileGroupsWithColor(placed, hasGroupingNeighbour, tile.getGroup());
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

    private void updateAll() {
        repaint();
        evaluatePlayerScores(parent.getPlayers());
        parent.reloadScoreboard();
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
        metrics.computeCorners(hexTiles[q][r]);
        hexGraphic.draw(hexTiles[q][r]);
    }
}