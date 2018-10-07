package Omega.Agent;

import Omega.Library.Enum.Color;
import Omega.Graphics.Hexagon.HexBoard;
import Omega.Graphics.Hexagon.HexTile;
import Omega.Library.Helper;
import Omega.Library.Model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Agent {

    /**
     * Method places the tilesToPlace on the board specific to its class implementation
     */
    void GetMove(Player parent, HexBoard board, Color[] tilesToPlace);

    default int EvaluateNode(HexTile[][] node, HexBoard board, Player parent) {
        return EvaluateNode(node, board, parent, false);
    }
    default int EvaluateNode(HexTile[][] node, HexBoard board, Player parent, boolean resetGroupIds) {
        // Get game evaluation
        Map<Color, Integer[][]> colorScores = board.evaluatePlayerScores(node, resetGroupIds);
        /*
         * colorScores => (
         *  WHITE => (
         *      [totalScore, numDisjointGroups],
         *      list_of_all_group_sizes
         *  ),
         *  BLACK => (
         *      [totalScore, numDisjointGroups],
         *      list_of_all_group_sizes
         * )
         */
        Integer[][] player1Scores = colorScores.getOrDefault(parent.getColor(), new Integer[][]{{0,0},{}});
        Integer[][] player2Scores = colorScores.getOrDefault((parent.getColor()==Color.WHITE?Color.BLACK:Color.WHITE), new Integer[][]{{0,0},{}});

        // 1. Return score of parent as eval result
//        return player1Scores[0][0]; // game eval score for AI player
//        return player1Scores[0][0] * player1Scores[0][1]; // game eval score * num disjoint groups
        return player1Scores[0][0] * player1Scores[0][1]; // game eval score * num disjoint groups

        // 2. Function of own score (higher is better) and opponent score (lower is better)
//        int ownScore = player1Scores[0][0];
//        int oppScore = player2Scores[0][0];
//        return ownScore+(ownScore-oppScore);

        // 3. Num disjoint groups
//        int ownScore = player1Scores[0][0] * player1Scores[0][1];
//        int oppScore = player2Scores[0][0] * player2Scores[0][1];
//        return ownScore+(ownScore-oppScore); // own score + diff
//        return ownScore * (10-player2Scores[0][1]); // emphasize low number of opponent groups

        //TODO: expand eval func
        // - avg distance to other tiles (higher is better)
        // - try to make groups of 3
        // - try to make enemy groups >3
        // - try to join enemy groups
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
    default List<HexTile[][]> GenerateChildren(HexTile[][] node, Color[] tilesToPlace, int depth, boolean isMaximizing) {
        return GenerateChildren(node, tilesToPlace, depth, isMaximizing, Integer.MAX_VALUE);
    }
    default List<HexTile[][]> GenerateChildren(HexTile[][] node, Color[] tilesToPlace, int depth, boolean isMaximizing, int maxDepth) {
        List<HexTile[][]> children = new ArrayList<>();

        // Get all legal positions for this node
        List<HexTile> possibleMoves = GetPossibleMoves(node);

        // Check placement level
        int placed = 0;
        for (int q = 0; q < node.length; q++) {
            for (int r = 0; r < node.length; r++) {
                HexTile tile = node[q][r];
                if (tile != null && tile.getPlacedId() > placed) {
                    if (tile.getPlacedId() < maxDepth) {
                        placed = tile.getPlacedId();
                    } else {
                        tile.setPlacedBy("");
                        tile.setPlacedId(-1);
                        tile.setGroup(0);
                        tile.setColor(Color.EMPTY);
                    }
                }
            }
        }

        // Return each combination of possible positions
        // A node will generate n*(n-1) children where n is the number of empty tiles in the node
//        if (placed >= 6){
//            System.out.println(Integer.toString(placed) + " - " + Integer.toString(maxDepth));
//        }
        for (int x = 0; x < possibleMoves.size(); x++) {
            for (int y = 0; y < possibleMoves.size(); y++) {
                HexTile tile = possibleMoves.get(x);
                HexTile tile2 = possibleMoves.get(y);
                if (!(tile.getQ() == tile2.getQ() && tile.getR() == tile2.getR())) {
                    HexTile[][] board = Helper.getGameStateDeepCopy(node, false, false);
                    // Place first tile
                    board[tile.getQ()][tile.getR()].setColor(tilesToPlace[0]);
//                    board[tile.getQ()][tile.getR()].setPlacedId(depth);
                    board[tile.getQ()][tile.getR()].setPlacedId(placed+1);
                    board[tile.getQ()][tile.getR()].setPlacedBy(isMaximizing?"MAX":"MIN");
                    // Place second tile
                    board[tile2.getQ()][tile2.getR()].setColor(tilesToPlace[1]);
//                    board[tile2.getQ()][tile2.getR()].setPlacedId(depth);
                    board[tile2.getQ()][tile2.getR()].setPlacedId(placed+1);
                    board[tile2.getQ()][tile2.getR()].setPlacedBy(isMaximizing?"MAX":"MIN");
                    children.add(board);
                }
            }
        }
        return children;
    }

    default List<HexTile[][]> GenerateChildrenWithMoveOrdering(HexBoard board, Color parentColor, HexTile[][] node, Color[] tilesToPlace, int depth, boolean isMaximizing, int maxDepth) {
        List<HexTile[][]> children = new ArrayList<>();

        // Get all legal positions for this node
        List<HexTile> possibleMoves = GetPossibleMoves(node);

        // Check placement level
        int placed = 0;
        for (int q = 0; q < node.length; q++) {
            for (int r = 0; r < node.length; r++) {
                HexTile tile = node[q][r];
                if (tile != null) {
                    tile.setGroup(0); // Reset group
                    if (tile.getPlacedId() > placed) {
                        if (tile.getPlacedId() < maxDepth) {
                            placed = tile.getPlacedId();
                        } else {
                            tile.setPlacedBy("");
                            tile.setPlacedId(-1);
                            tile.setColor(Color.EMPTY);
                        }
                    }
                }
            }
        }

        // Return each combination of possible positions
        // A node will generate n*(n-1) children where n is the number of empty tiles in the node
        for (int x = 0; x < possibleMoves.size(); x++) {
            for (int y = 0; y < possibleMoves.size(); y++) {
                HexTile tile = possibleMoves.get(x);
                HexTile tile2 = possibleMoves.get(y);
                if (!(tile.getQ() == tile2.getQ() && tile.getR() == tile2.getR())) {
                    HexTile[][] stateDeepCopy = Helper.getGameStateDeepCopy(node, false, false);

                    // Place first tile
                    stateDeepCopy[tile.getQ()][tile.getR()].setColor(tilesToPlace[0]);
                    stateDeepCopy[tile.getQ()][tile.getR()].setPlacedId(placed+1);
                    stateDeepCopy[tile.getQ()][tile.getR()].setPlacedBy(isMaximizing?"MAX":"MIN");
                    stateDeepCopy[tile.getQ()][tile.getR()].setGroup(9999);

                    // Analyze result of first tile
                    int[] groupScores1 = board.exploreGroupRecursively(stateDeepCopy, tile.getQ(), tile.getR(), 9999, true);
                    int groupScore1 = groupScores1[0];
                    int isJoiningMove1 = groupScores1[1];
                    int isBarrierMove1 = groupScores1[2];

                    // Place second tile
                    stateDeepCopy[tile2.getQ()][tile2.getR()].setColor(tilesToPlace[1]);
                    stateDeepCopy[tile2.getQ()][tile2.getR()].setPlacedId(placed+1);
                    stateDeepCopy[tile2.getQ()][tile2.getR()].setPlacedBy(isMaximizing?"MAX":"MIN");
                    stateDeepCopy[tile2.getQ()][tile2.getR()].setGroup(9999);

                    // Analyze result of second tile
                    int[] groupScores2 = board.exploreGroupRecursively(stateDeepCopy, tile2.getQ(), tile2.getR(), 9999, true);
                    int groupScore2 = groupScores2[0];
                    int isJoiningMove2 = groupScores2[1];
                    int isBarrierMove2 = groupScores2[2];

                    // === Move Ordering:
                    // Apply move ordering rules to determine child placement
                    if ((isBarrierMove1==1 && parentColor == tilesToPlace[0]) || (isBarrierMove2==1 && parentColor == tilesToPlace[1])) {
                        // The player has placed an enemy color between 2 of his own groups: great move!
                        children.add(0, stateDeepCopy);
                    } else if ((isBarrierMove2==1 && parentColor == tilesToPlace[0]) || (isBarrierMove1==1 && parentColor == tilesToPlace[1])) {
                        // The player has placed his own color between 2 enemy groups: bad move!
                        children.add(0, stateDeepCopy);
                    } else if ((groupScore1 == 3 && parentColor == tilesToPlace[0]) || (groupScore2 == 3 && parentColor == tilesToPlace[1])) {
                        // the player has created a group with size 3 using its own color: good move!
                        children.add(0, stateDeepCopy);
                    } else if ((groupScore1 > 3 && parentColor == tilesToPlace[0]) || (groupScore2 > 3 && parentColor == tilesToPlace[1])) {
                        // the player has created a group with size >3 using its own color: bad move!
                        children.add(stateDeepCopy);
                    } else if ((groupScore2 == 3 && parentColor == tilesToPlace[0]) || (groupScore1 == 3 && parentColor == tilesToPlace[1])) {
                        // the player has created a group with size 3 for the enemy player: bad move!
                        children.add(stateDeepCopy);
                    } else if ((groupScore2 > 3 && parentColor == tilesToPlace[0]) || (groupScore1 > 3 && parentColor == tilesToPlace[1])) {
                        // the player has created a group with size >3 for the enemy player: good move!
                        children.add(0, stateDeepCopy);
                    } else if ((isJoiningMove1==1 && parentColor == tilesToPlace[0]) || (isJoiningMove2==1 && parentColor == tilesToPlace[1])) {
                        // the player has joined 2 or more groups using its own color: bad move!
                        children.add(stateDeepCopy);
                    } else if ((isJoiningMove2==1 && parentColor == tilesToPlace[0]) || (isJoiningMove1==1 && parentColor == tilesToPlace[1])) {
                        // the player has joined 2 or more groups using the enemy color: good move!
                        children.add(0, stateDeepCopy);
                    }
//                    else if ((isJoiningMove1==0 && parentColor == tilesToPlace[0]) || (isJoiningMove2==0 && parentColor == tilesToPlace[1])) {
//                        // the player has not joined 2 or more groups using its own color: good move!
//                        children.add(0, stateDeepCopy);
//                    } else if ((isJoiningMove2==0 && parentColor == tilesToPlace[0]) || (isJoiningMove1==0 && parentColor == tilesToPlace[1])) {
//                        // the player has not joined 2 or more groups using the enemy color: bad move!
//                        children.add(stateDeepCopy);
//                    }
                    else {
                        // No matching rules: add to middle of list
                        children.add(children.size()/2, stateDeepCopy);
                    }
                }
            }
        }

        return children;
    }

    /**
     * Returns the max depth a legal leaf node can be at (terminal game nodes are always at the same depth; given by the number of available tiles)
     */
    default int GetMaxGameDepth(HexBoard board, Player parent) {
        int emptyTiles = board.numEmptySpaces();
        if (parent.getColor() == Color.WHITE) emptyTiles -= 2;
        int maxDepth = (int) Math.floor(emptyTiles/2);
        return maxDepth;
    }
}
