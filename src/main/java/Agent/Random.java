package Agent;

import Enum.Color;
import Graphics.Hexagon.HexBoard;
import Graphics.Hexagon.HexTile;
import Library.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Random implements Agent {

    public void GetMove(Player parent, HexBoard board, Color[] tilesToPlace) {
//        HexTile[][] state = board.getGameStateDeepCopy();
        HexTile[][] state = board.getGameState();

        // Check possible tiles
        List<HexTile> possibleMoves = new ArrayList<>();
        for (int q=0; q<state.length; q++) {
            for (int r = 0; r < state.length; r++) {
                HexTile tile = state[q][r];
                if (tile != null && tile.getColor() == Color.EMPTY) {
                    possibleMoves.add(tile);
                }
            }
        }

        // Pick a random set
        Collections.shuffle(possibleMoves);
        possibleMoves = possibleMoves.subList(0, tilesToPlace.length);
        for (int i = 0; i<tilesToPlace.length; i++) {
            possibleMoves.get(i).setColor(tilesToPlace[i]);
        }
    }
}
