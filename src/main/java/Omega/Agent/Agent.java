package Omega.Agent;

import Omega.Library.Enum.Color;
import Omega.Graphics.Hexagon.HexBoard;
import Omega.Graphics.Hexagon.HexTile;
import Omega.Library.Helper;
import Omega.Library.Model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Agent {

    /**
     * Method places the tilesToPlace on the board specific to its class implementation
     */
    public void GetMove(Player parent, HexBoard board, Color[] tilesToPlace);

    default int EvaluateNode(HexTile[][] node, HexBoard board, Player parent) {
        // Get game evaluation
        Map<Color, Integer[]> colorScores = board.evaluatePlayerScores(node, false);
        Integer[] player1Scores = colorScores.getOrDefault(parent.getColor(), new Integer[]{0,0});
        Integer[] player2Scores = colorScores.getOrDefault((parent.getColor()==Color.WHITE?Color.BLACK:Color.WHITE), new Integer[]{0,0});

        // 1. Return score of parent as eval result
//        return player1Scores[0]; // game eval score for AI player
//        return player1Scores[0] * player1Scores[1]; // game eval score * num disjoint groups

        // 2. Function of own score (higher is better) and opponent score (lower is better)
//        int ownScore = colorScores.getOrDefault(parent.getColor(), 0);
//        int oppScore = colorScores.getOrDefault((parent.getColor()==Color.WHITE?Color.BLACK:Color.WHITE), 0);
//        return ownScore+(ownScore-oppScore);

        // 3. Num disjoint groups
        int ownScore = player1Scores[0] * player1Scores[1];
        int oppScore = player2Scores[0] * player2Scores[1];
//        return ownScore+(ownScore-oppScore); // own score + diff
        return ownScore * (10-player2Scores[1]); // emphasize low number of opponent groups

        //TODO: expand eval func
        // - num disjoint groups (higher is better)
        // - avg distance to other tiles (higher is better)
    }

    /**
     * Returns a list of free (empty) tiles based on the given board and increments their turn id
     * @param board Board representation
     * @return list of free tiles
     */
    default List<HexTile> GetPossibleMoves(HexTile[][] board) {
        List<HexTile> possibleMoves = new ArrayList<>();
        for (int q = 0; q < board.length; q++) {
            for (int r = 0; r < board.length; r++) {
                HexTile tile = board[q][r];
                if (tile != null) {
                    if (tile.getColor() == Color.EMPTY) {
                        possibleMoves.add(tile);
                    }
                }
            }
        }
        return possibleMoves;
    }

    /**
     * Generates a list of all legal children for the given game state
     * @param node Board representation
     * @param tilesToPlace List of colors to be placed
     * @return list of all legal children (size = n*(n-1), where n is the number of empty tiles in the given node)
     */
    default List<HexTile[][]> GenerateChildren(HexTile[][] node, Color[] tilesToPlace, int depth, boolean isMaximizing) {
        List<HexTile[][]> children = new ArrayList<>();

        // Get all legal positions for this node
        List<HexTile> possibleMoves = GetPossibleMoves(node);

        // Return each combination of possible positions
        // A node will generate n*(n-1) children where n is the number of empty tiles in the node
        for (int x = 0; x < possibleMoves.size(); x++) {
            for (int y = 0; y < possibleMoves.size(); y++) {
                HexTile tile = possibleMoves.get(x);
                HexTile tile2 = possibleMoves.get(y);
                if (!(tile.getQ() == tile2.getQ() && tile.getR() == tile2.getR())) {
                    HexTile[][] board = Helper.getGameStateDeepCopy(node, false);
                    // Place first tile
                    board[tile.getQ()][tile.getR()].setColor(tilesToPlace[0]);
                    board[tile.getQ()][tile.getR()].setPlacedId(depth);
                    board[tile.getQ()][tile.getR()].setPlacedBy(isMaximizing?"MAX":"MIN");
                    // Place second tile
                    board[tile2.getQ()][tile2.getR()].setColor(tilesToPlace[1]);
                    board[tile2.getQ()][tile2.getR()].setPlacedId(depth);
                    board[tile2.getQ()][tile2.getR()].setPlacedBy(isMaximizing?"MAX":"MIN");
                    children.add(board);
                }
            }
        }
        return children;
    }

    /**
     * Returns the max depth a legal leaf node can be at (terminal game nodes are always at the same depth; given by the number of available tiles)
     */
    default int GetMaxGameDepth(HexBoard board, Player parent) {
        int emptyTiles = board.numEmptySpaces();
        if (parent.getColor() != Color.WHITE) emptyTiles -= 2;
        int maxDepth = (int) Math.floor(emptyTiles/2);
        return maxDepth;
    }
}
