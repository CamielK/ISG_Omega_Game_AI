package Agent;

import Enum.Color;
import Graphics.Hexagon.HexBoard;
import Graphics.Hexagon.HexTile;
import Library.Move;
import Library.Player;

import java.util.List;

public class MinMaxBasic implements Agent {

    private final int initialDepth = 2;

    private HexBoard board;
    private Player parent;
    private Color[] tilesToPlace;

    public void GetMove(Player parent, HexBoard board, Color[] tilesToPlace) {
        this.board = board;
        this.parent = parent;
        this.tilesToPlace = tilesToPlace;

        Move best = MinMax(board.getGameState(), initialDepth, true);

        // Get the first 2 tiles placed by the minmax algorithm
        for (int q = 0; q < best.board.length; q++) {
            for (int r = 0; r < best.board.length; r++) {
                HexTile tile = best.board[q][r];
                if (tile != null && tile.getPlacedId() == initialDepth) {
                    board.getGameState()[q][r].setColor(tile.getColor());
                }
            }
        }
    }

    /**
     * Returns the best possible board configuration (according to minmax) defined as an array of HexTiles
     */
    private Move MinMax(HexTile[][] node, int depth, boolean isMaximizingPlayer) {
        // Check for leaf nodes
        boolean terminal = false;
        List<HexTile[][]> children = null;
        if (depth <= 0) terminal = true;
        else {
            children = this.GenerateChildren(node, tilesToPlace, depth);
        }
        int minChildrenRequired = (parent.getColor() == Color.WHITE ? 4*3 : 2); // Calculate end game requirement for 2 players
        if (terminal || children.size() <= minChildrenRequired) {
            return new Move(this.EvaluateNode(node, board, parent), node);
        }

        if (isMaximizingPlayer) {
            Move value = new Move(Integer.MIN_VALUE, null);
            for (HexTile[][] child : children) {
                Move value_child = MinMax(child, depth - 1, false);
                if (value_child.score > value.score) {
                    value = value_child;
                }
            }
            return value;
        } else {
            Move value = new Move(Integer.MAX_VALUE, null);
            for (HexTile[][] child : children) {
                Move value_child = MinMax(child, depth - 1, true);
                if (value_child.score < value.score) {
                    value = value_child;
                }
            }
            return value;
        }
    }
}
