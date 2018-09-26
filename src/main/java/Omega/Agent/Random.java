package Omega.Agent;

import Omega.Library.Enum.Color;
import Omega.Graphics.Hexagon.HexBoard;
import Omega.Graphics.Hexagon.HexTile;
import Omega.Library.Model.Player;

import java.util.Collections;
import java.util.List;

public class Random implements Agent {

    public void GetMove(Player parent, HexBoard board, Color[] tilesToPlace) {
        // Check possible tiles
        List<HexTile> possibleMoves = GetPossibleMoves(board.getGameState());

        GetMaxGameDepth(board, parent);

        // Pick a random set from the available moves
        Collections.shuffle(possibleMoves);
        possibleMoves = possibleMoves.subList(0, tilesToPlace.length);
        for (int i = 0; i<tilesToPlace.length; i++) {
            possibleMoves.get(i).setColor(tilesToPlace[i]);
        }
    }
}
