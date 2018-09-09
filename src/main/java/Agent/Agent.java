package Agent;

import Enum.Color;
import Graphics.Hexagon.HexBoard;

public interface Agent {
    /**
     * Method places the tilesToPlace on the board specific to its class implementation
     */
    public void GetMove(HexBoard board, Color[] tilesToPlace);
}
