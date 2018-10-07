package Omega.Graphics.Hexagon;

import Omega.Graphics.Controller;
import Omega.Library.Model.Player;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Omega.Library.Enum.Color;

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
     * @return Map (for each color: [[eval_score, num_disjoint_groups],list_of_all_group_sizes])
     */
    public Map<Color, Integer[][]> evaluatePlayerScores(HexTile[][] hexTiles, boolean resetGroupIds) {
        int axialSize = hexTiles.length;

        // Reset groups
        if (resetGroupIds) {
            for (int q=0; q<axialSize; q++) {
                for (int r = 0; r < axialSize; r++) {
                    if (hexTiles[q][r]!=null) hexTiles[q][r].setGroup(0);
                }
            }
        }

        // Update groups for all tiles
        int maxGroupId = 1;
        Map<Color, Integer[][]> colorScores = new HashMap<Color, Integer[][]>();
        for (int q=0; q<axialSize; q++) {
            for (int r = 0; r < axialSize; r++) {
                HexTile hexTile = hexTiles[q][r];
                if (hexTile != null && hexTile.getColor() != Color.EMPTY && hexTile.getGroup() == 0) {
                    hexTile.setGroup(maxGroupId);
                    int[] groupScores = exploreGroupRecursively(hexTiles, q, r, maxGroupId, false);
                    int groupScore = groupScores[0];

                    // Save scores and increment
                    Integer[][] scores = colorScores.getOrDefault(hexTile.getColor(), new Integer[][]{{1,0,0},{}});
                    Integer[] groups = new Integer[scores[1].length+1];
                    for (int i = 0; i < scores[1].length; i++) {
                        groups[i] = scores[1][i];
                    }
                    groups[scores[1].length] = groupScore;
                    colorScores.put(hexTile.getColor(), new Integer[][]{
                            new Integer[]{
                                    scores[0][0]*groupScore,   // Eval score
                                    scores[0][1]+1             // Num disjoint groups
                            },
                            groups // All individual groups
                    });
                    maxGroupId++;
                }
            }
        }

        return colorScores;
    }

    /**
     * Recursively finds all members of the given group, starting in hexTiles[q][r]
     * If checkMoveQuality is set to true, this method will also return some information about the properties of the center tile (used for move ordering).
     * @return int[] {groupScore, isJoiningMove, isBarrierMove}
     */
    public int[] exploreGroupRecursively(HexTile[][] hexTiles, int q, int r, int group, boolean checkMoveQuality) {
        HexTile[] neighbours = new HexTile[]{
                (q+1<axialSize              ? hexTiles[q+1][r]  : null),
                (q-1>=0                     ? hexTiles[q-1][r]  : null),
                (q+1<axialSize && r-1>=0    ? hexTiles[q+1][r-1]: null),
                (r+1<axialSize && q-1>=0    ? hexTiles[q-1][r+1]: null),
                (r+1<axialSize              ? hexTiles[q][r+1]  : null),
                (r-1>=0                     ? hexTiles[q][r-1]  : null),
        };

        // Optional: Analyze properties of the center tile
        int isJoiningMove = 0;
        int isBarrierMove = 0;
        if (checkMoveQuality) {
            for (HexTile source : neighbours) {
                if (source != null) {
                    for (HexTile target : neighbours) {
                        if (target != null && target != source && !isNeighbhour(source, target)) {
                            if (source.getColor() == target.getColor() && source.getColor() == hexTiles[q][r].getColor()) {
                                // player has joined 2 of his own groups by placing this tile: bad move!
                                isJoiningMove = 1;
                                break;
                            } else if (source.getColor() == target.getColor() && source.getColor() != hexTiles[q][r].getColor() && source.getColor() != Color.EMPTY) {
                                // player has blocked a joining position using the opponent color: good move! this creates a barrier between groups
                                isBarrierMove = 1;
                            }
                        }
                    }
                }
            }
        }

        // Recursively call all unexplored neighbours to find the total group size.
        int groupScore = 1;
        for (HexTile tile : neighbours) {
            if (tile != null && tile.getColor() == hexTiles[q][r].getColor() && tile.getGroup() <= 0) {
                tile.setGroup(group); // setGroup to mark this tile as explored
                groupScore += exploreGroupRecursively(hexTiles, tile.getQ(), tile.getR(), group, false)[0];
            }
        }

        // Result: [groupScore = total group size, isJoiningMove, isBarrierMove]
        return new int[]{groupScore, (checkMoveQuality && isJoiningMove==1 ? 1 : 0 ), (checkMoveQuality && isBarrierMove==1 ? 1 : 0 )};
    }

    /**
     * Returns true if the source and target tiles are direct neighbours
     */
    public boolean isNeighbhour(HexTile source, HexTile target) {
        int q = source.getQ(); int r = source.getR();
        HexTile[] neighbours = new HexTile[]{
                (q+1<axialSize              ? hexTiles[q+1][r]  : null),
                (q-1>=0                     ? hexTiles[q-1][r]  : null),
                (q+1<axialSize && r-1>=0    ? hexTiles[q+1][r-1]: null),
                (r+1<axialSize && q-1>=0    ? hexTiles[q-1][r+1]: null),
                (r+1<axialSize              ? hexTiles[q][r+1]  : null),
                (r-1>=0                     ? hexTiles[q][r-1]  : null),
        };
        for (HexTile tile : neighbours) {
            if (tile != null && tile.getQ() == target.getQ() && tile.getR() == target.getR()) {
                return true;
            }
        }
        return false;
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

    public void updateAll() {
        Map<Color, Integer[][]> colorScores = evaluatePlayerScores(hexTiles, true);
        for (Player player : parent.players) {
            Integer[][] playerScores = colorScores.getOrDefault(player.getColor(), new Integer[][]{{0,0},{}});
            player.setScore(playerScores[0][0]);
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