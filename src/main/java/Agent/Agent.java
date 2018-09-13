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

        // Return score of parent as eval result
        //TODO: add proper eval func
        return colorScores.getOrDefault(parent.getColor(), 0);
    }

    default List<HexTile[][]> GenerateChildren(HexTile[][] node, Color[] tilesToPlace) {
        List<HexTile[][]> children = new ArrayList<>();

        // Get all possible positions
        List<HexTile> possibleMoves = new ArrayList<>();
        for (int q = 0; q < node.length; q++) {
            for (int r = 0; r < node.length; r++) {
                HexTile tile = node[q][r];
                if (tile != null && tile.getColor() == Color.EMPTY) {
                    possibleMoves.add(tile);
                }
            }
        }

        // Return each combination of possible positions
        for (int x = 0; x < possibleMoves.size(); x++) {
            for (int y = x; y < possibleMoves.size(); y++) {
                HexTile tile = possibleMoves.get(x);
                HexTile tile2 = possibleMoves.get(y);
                if (tile.getQ() != tile2.getQ() && tile.getR() != tile2.getR()) {
                    for (int k = 0; k < tilesToPlace.length; k++) {
                        HexTile[][] board = Helper.getGameStateDeepCopy(node);
                        board[tile.getQ()][tile.getR()].setColor(tilesToPlace[0+k]);
                        board[tile2.getQ()][tile2.getR()].setColor(tilesToPlace[(k==0?1:0)]);
                        children.add(board);
                    }
                }
            }
        }
        return children;
    }
}
