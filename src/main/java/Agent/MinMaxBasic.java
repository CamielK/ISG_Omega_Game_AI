package Agent;

import Enum.Color;
import Agent.Agent;
import Graphics.Hexagon.HexBoard;

public class MinMaxBasic implements Agent {

    public void GetMove(HexBoard board, Color[] tilesToPlace) {
        // TODO: implement minmax basic
        Random rand = new Random();
        rand.GetMove(board, tilesToPlace);
    }
}
