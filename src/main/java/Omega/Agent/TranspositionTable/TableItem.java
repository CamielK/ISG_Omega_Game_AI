package Omega.Agent.TranspositionTable;

import Omega.Graphics.Hexagon.HexTile;
import Omega.Library.Enum.Flag;

public class TableItem {
    public int value;
    public short depth;
    public HexTile[][] bestMove;
    public Flag flag;
    public TableItem(int value, HexTile[][] bestMove, short depth, Flag flag) {
        this.value = value;
        this.bestMove = bestMove;
        this.depth = depth;
        this.flag = flag;
    }
}
