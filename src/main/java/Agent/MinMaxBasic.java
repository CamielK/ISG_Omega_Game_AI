package Agent;

import Enum.Color;
import Graphics.Hexagon.HexBoard;
import Graphics.Hexagon.HexTile;
import Library.Helper;
import Library.Player;

import java.util.List;
import java.util.Map;

public class MinMaxBasic implements Agent {

    private HexBoard board;
    private Player parent;
    private Color[] tilesToPlace;

    public void GetMove(Player parent, HexBoard board, Color[] tilesToPlace) {
        this.board = board;
        this.parent = parent;
        this.tilesToPlace = tilesToPlace;

        // TODO: implement minmax basic
//        Random rand = new Random();
//        rand.GetMove(parent, board, tilesToPlace);

        int test = MinMax(board.getGameState(), 3, true);
        int test2 = 2;

    }

    /**
     * Returns the best possible board configuration (according to minmax) defined as an array of HexTiles
     */
    private int MinMax(HexTile[][] node, int depth, boolean isMaximizingPlayer) {
        List<HexTile[][]> children = this.GenerateChildren(node, tilesToPlace);
        if (depth <= 0 || children.size() <= 0) {
            return this.EvaluateNode(node, board, parent);
        }
        if (isMaximizingPlayer) {
            int value = Integer.MIN_VALUE;
            for (HexTile[][] child : children) {
                value = Math.max(value, MinMax(child, depth - 1, false));
            }
            return value;
        } else {
            int value = Integer.MAX_VALUE;
            for (HexTile[][] child : children) {
                value = Math.min(value, MinMax(child, depth - 1, true));
            }
            return value;
        }
    }
}
