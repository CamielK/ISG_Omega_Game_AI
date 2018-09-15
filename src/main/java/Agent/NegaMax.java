package Agent;

import Graphics.Hexagon.HexBoard;
import Graphics.Hexagon.HexTile;
import Library.Enum.Color;
import Library.Model.Move;
import Library.Model.Player;

import java.util.List;

public class NegaMax implements Agent {

    private final int initialDepth = 3;

    private HexBoard board;
    private Player parent;
    private Color[] tilesToPlace;

    public void GetMove(Player parent, HexBoard board, Color[] tilesToPlace) {
        this.board = board;
        this.parent = parent;
        this.tilesToPlace = tilesToPlace;

        Move best = NegaMax(board.getGameState(), initialDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
        System.out.println("NegaMax found score: " + best.score);

        // Get the first 2 tiles placed by the minmax algorithm
        for (int q = 0; q < best.board.length; q++) {
            for (int r = 0; r < best.board.length; r++) {
                HexTile tile = best.board[q][r];
                if (tile != null && tile.getPlacedId() == initialDepth) {
                    board.getGameState()[q][r].setColor(tile.getColor());
                }
            }
        }

        // Store best board for debugging
        board.bestBoard = best.board;
    }

    /**
     * Returns the best possible board configuration (according to negamax with alpha-beta pruning)
     */
    private Move NegaMax(HexTile[][] node, int depth, double alpha, double beta, int color) {
        // Check for leaf nodes
        boolean terminal = false;
        List<HexTile[][]> children = null;
        if (depth <= 0) terminal = true;
        else {
            children = this.GenerateChildren(node, tilesToPlace, depth, color == 1);
        }
        int minChildrenRequired = (parent.getColor() == Color.WHITE ? 4*3 : 2); // Calculate end game requirement for 2 players
        if (terminal || children.size() <= minChildrenRequired) {
            return new Move(color * this.EvaluateNode(node, board, parent), node);
        }

        Move value = new Move(Integer.MIN_VALUE, null);
        for (HexTile[][] child : children) {
            Move value_child = NegaMax(child, depth - 1, -beta, -alpha, -color); // Swap alpha/beta and negate
            value_child.score = -value_child.score; // Negate
            if (value_child.score > value.score) value = value_child;
            if (value_child.score > alpha) alpha = value.score;
            if (alpha >= beta) break;
        }
        return value;
    }
}
