package Library;

import Graphics.Hexagon.HexTile;

public class Move {

    public double score; // Evaluation result
    public HexTile[][] board; // Board that produced the result

    public Move(double score, HexTile[][] board) {
        this.score = score;
        this.board = board;
    }
}
