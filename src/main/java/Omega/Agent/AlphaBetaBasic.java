package Omega.Agent;

import Omega.Library.Enum.Color;
import Omega.Graphics.Hexagon.HexBoard;
import Omega.Graphics.Hexagon.HexTile;
import Omega.Library.Model.Move;
import Omega.Library.Model.Player;

import java.util.List;

public class AlphaBetaBasic implements Agent {

    private final int initialDepth = 3;

    private HexBoard board;
    private Player parent;
    private Color[] tilesToPlace;

    public void GetMove(Player parent, HexBoard board, Color[] tilesToPlace) {
        this.board = board;
        this.parent = parent;
        this.tilesToPlace = tilesToPlace;

        Move best = AlphaBeta(board.getGameState(), initialDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
        System.out.println("AlphaBetaBasic found score: " + best.score);

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
     * Returns the best possible board configuration (according to minmax with alpha beta pruning)
     */
    private Move AlphaBeta(HexTile[][] node, int depth, double alpha, double beta, boolean isMaximizingPlayer) {
        // Check for leaf nodes
        boolean terminal = false;
        List<HexTile[][]> children = null;
        if (depth <= 0) terminal = true;
        else {
            children = this.GenerateChildren(node, tilesToPlace, depth, isMaximizingPlayer);
        }
        int minChildrenRequired = (parent.getColor() == Color.WHITE ? 4*3 : 2); // Calculate end game requirement for 2 players
        if (terminal || children.size() <= minChildrenRequired) {
            return new Move(this.EvaluateNode(node, board, parent), node);
        }

        if (isMaximizingPlayer) {
            Move value = new Move(Integer.MIN_VALUE, null);
            for (HexTile[][] child : children) {
                Move value_child = AlphaBeta(child, depth - 1, alpha, beta, false);
                if (value_child.score > value.score) {
                    value = value_child;
                }
                alpha = Math.max(alpha, value.score);
                if (alpha >= beta) {
                    break;
                }
            }
            return value;
        } else {
            Move value = new Move(Integer.MAX_VALUE, null);
            for (HexTile[][] child : children) {
                Move value_child = AlphaBeta(child, depth - 1, alpha, beta, true);
                if (value_child.score < value.score) {
                    value = value_child;
                }
                beta = Math.min(beta, value.score);
                if (alpha >= beta) {
                    break;
                }
            }
            return value;
        }
    }
}
