package Omega.Agent.TranspositionTable;

import Omega.Graphics.Hexagon.HexTile;
import Omega.Library.Config;
import Omega.Library.Model.Move;

import java.util.HashMap;
import java.util.Map;

public class TranspositionTable {

    private static Map<Long, TableItem> hashTable = new HashMap<>();
    private static long[][][] encodings;
    private static int boardSize = 0;

    /**
     * Initialize the transposition table
     */
    public TranspositionTable(HexTile[][] board) {
        if (boardSize != board.length) {
            // Detect a new game with a different board size: reset all
            encodings = null;
            hashTable = new HashMap<>();
            boardSize = board.length;
        }

        // Randomly initialize the encodings for each tile and state
        if (encodings == null) {
            encodings = new long[boardSize][boardSize][Config.COLORS_IN_PLAY.length];
            for (int q = 0; q < boardSize; q++) {
                for (int r = 0; r < boardSize; r++) {
                    if (board[q][r] != null) {
                        for (int c = 0; c < Config.COLORS_IN_PLAY.length; c++) {
                            encodings[q][r][c] = new java.util.Random().nextLong() & Long.MAX_VALUE; // duplicate random generation is ignored
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the stored score for the given board or null if the board is unknown
     */
    public TableItem get(HexTile[][] board) {
        long hash = zobristHash(board);
        return hashTable.getOrDefault(hash, null);
    }

    /**
     * Stores the given move into the transposition table
     */
    public void store(HexTile[][] board, TableItem item) {
        long hash = zobristHash(board);
        TableItem old = hashTable.put(hash, item);
        int t=2;
    }

    /**
     * Stores the given move into the transposition table using the parent board hash (this reduces the number of XOR operations required to generate the zobrist hash)
     */
    public void storeIncremental(Move move, long parentHash) {
        // TODO: implement
    }

    /**
     * Use Zobrist hashing to encode the position
     */
    private long zobristHash(HexTile[][] board) {
        long hash = 0;

        for (int q = 0; q < board.length; q++) {
            for (int r = 0; r < board.length; r++) {
                HexTile tile = board[q][r];
                if (tile != null) {
                    hash ^= getPositionEncoding(tile);
                }
            }
        }

        return hash;
    }

    public long getSize() {
        return hashTable.size();
    }

    /**
     * Returns the randomly generated encoding for this specific tile (tile state is determined by its coordinates q and r, as well as the current color of the tile)
     */
    private long getPositionEncoding(HexTile tile) {
        int index = 2;
        switch (tile.getColor()) {
            case WHITE:
                index = 0;
                break;
            case BLACK:
                index = 1;
                break;
        }
        return encodings[tile.getQ()][tile.getR()][index];
    }
}

