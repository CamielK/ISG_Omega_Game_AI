package Agent;

import Enum.Color;
import Graphics.Hexagon.HexBoard;
import Graphics.Hexagon.HexTile;
import Library.Helper;
import Library.Player;

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
        Map<Color, Integer> colorScores = board.evaluatePlayerScores(node);

        //TODO: add proper eval func

        // 1. Return score of parent as eval result
//        return colorScores.getOrDefault(parent.getColor(), 0);

        // 2. Return score difference in favor of parent
        int p1Score = colorScores.getOrDefault(parent.getColor(), 0);
        int p2Score = colorScores.getOrDefault((parent.getColor()==Color.WHITE?Color.BLACK:Color.WHITE), 0);
        return p1Score-p2Score;
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
    default List<HexTile[][]> GenerateChildren(HexTile[][] node, Color[] tilesToPlace, int depth) {
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
                    HexTile[][] board = Helper.getGameStateDeepCopy(node);
                    board[tile.getQ()][tile.getR()].setColor(tilesToPlace[0]);
                    board[tile.getQ()][tile.getR()].setPlacedId(depth);
                    board[tile2.getQ()][tile2.getR()].setColor(tilesToPlace[1]);
                    board[tile2.getQ()][tile2.getR()].setPlacedId(depth);
//                    if (tile.getPlacedId()==0 || tile2.getPlacedId()==0) {
//                        int test =1;
//                    }
                    children.add(board);
                }
            }
        }
        return children;
    }
}
