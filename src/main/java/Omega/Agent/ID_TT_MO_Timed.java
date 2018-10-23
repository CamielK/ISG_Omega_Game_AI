package Omega.Agent;

import Omega.Agent.TranspositionTable.TableItem;
import Omega.Agent.TranspositionTable.TranspositionTable;
import Omega.Graphics.Hexagon.HexBoard;
import Omega.Graphics.Hexagon.HexTile;
import Omega.Library.Enum.Color;
import Omega.Library.Enum.Flag;
import Omega.Library.Helper;
import Omega.Library.Model.Move;
import Omega.Library.Model.Player;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ID: Iterative deepening using negamax formulation and alpha-beta pruning
 * TT: Using TranspositionTable to store and retrieve known nodes
 * MO: Uses move ordering when generating children
 * Timed: Optimizes search time management
 */
public class ID_TT_MO_Timed implements Agent {

    private static int countNodes = 0;
    private static int countNodesEvaluated = 0;
    private static int countTTUsed = 0;
    private static int countTTStored = 0;
    private static int countPruned = 0;
    private HexBoard board;
    private Player parent;
    private Color[] tilesToPlace;
    private TranspositionTable tt = null;

    public void GetMove(Player parent, HexBoard board, Color[] tilesToPlace) {
        this.board = board;
        this.parent = parent;
        this.tilesToPlace = tilesToPlace;
        if (tt == null) tt = new TranspositionTable(board.getGameState());

        // Time management
        long startTime = System.nanoTime();
        int maxDepth = GetMaxGameDepth(board, parent);
        int maxSearchTime = parent.getTotalTimeLeft() / parent.getTotalTurnsLeft();

        if (parent.getTotalTurnsLeft() == board.getMaxTurns() && parent.getColor() == Color.WHITE) {
            // First move of game is played randomly to save time
            new Random().GetMove(parent, board, tilesToPlace);
        }

        final int[] maxDepthReached = {0};
        final Move[] best = {new Move(Integer.MIN_VALUE, null)};
        long secondsUsed = 0;
        long depthStartTime = maxSearchTime;
        boolean done = false;
        System.out.println("\nStarting ID search with a max possible depth of " + maxDepth + " and a timeout of " + maxSearchTime + " seconds.");
        for (int depth = 1; depth <= maxDepth; depth++) {
            countNodes = 0;
            countNodesEvaluated = 0;
            countTTUsed = 0;
            countTTStored = 0;
            countPruned = 0;
            try {
                if (done) {
                    break;
                }
                long secondsLeft = maxSearchTime - ((System.nanoTime() - startTime) / 1000000000);
                secondsUsed = depthStartTime - secondsLeft;
                depthStartTime = secondsLeft;
                if (secondsLeft <= 0) {
                    // Ran out of time
                    done = true;
                    break;
                } else if (secondsUsed*2 > secondsLeft) {
                    System.out.println("Skipping depth " + depth + ". " + secondsLeft + " seconds is not expected to complete this depth.");
                    // Avoid wasted search (depth+1 is not expected to be finished in time)
                    done = true;
                    break;
                } else {
                    System.out.println("Searching depth " + depth + ", time left: " + secondsLeft + " seconds");
                }

                int finalDepth = depth;
                CountDownLatch latch = new CountDownLatch(1);
                Service<Void> service = new Service<Void>() {
                    @Override
                    protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call(){
                        try {
                            Move move = ID(board.getGameState(), finalDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, finalDepth);
                            best[0] = move;
                            maxDepthReached[0] = finalDepth;
                            System.out.println("Nodes: " + countNodes + ", Nodes evaluated: " + countNodesEvaluated + ", RetrievedTT: " + countTTUsed + ", StoredTT: " + countTTStored + ", Pruned: " + countPruned + ", Tt size: " + tt.getSize());
                            System.out.println("Finished depth " + finalDepth + " with score " + best[0].score);
                        } catch (InterruptedException e) {
                            System.out.println("ID timeout at depth " + finalDepth);
                            maxDepthReached[0] = finalDepth-1;
                            this.cancel();
                            return null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            latch.countDown();
                            return null;
                        }
                        }
                    };
                    }
                };
                service.start();
                latch.await(secondsLeft, TimeUnit.SECONDS);
                Platform.runLater(service::cancel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Wait for iterative deepening to reach its max depth or until a timeout is called by the latch countdown
        while (maxDepthReached[0] <= 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("IterativeDeepening found score: " + best[0].score + ", max depth reached: " + maxDepthReached[0]);

        // Get the first 2 unplaced tiles placed by the algorithm and place them on the real board
        HexTile[][] realBoard = board.getGameState();
        boolean placedWhite = false;
        boolean placedBlack = false;
        for (int i = 0; i < maxDepth; i++) {
            for (int q = 0; q < best[0].board.length; q++) {
                for (int r = 0; r < best[0].board.length; r++) {
                    HexTile tile = best[0].board[q][r];
                    if (tile != null && tile.getPlacedId() == i && realBoard[q][r].getColor() == Color.EMPTY) {
                        if (tile.getColor() == Color.WHITE) {
                            if (!placedWhite) realBoard[q][r].setColor(tile.getColor());
                            placedWhite = true;
                        } else {
                            if (!placedBlack) realBoard[q][r].setColor(tile.getColor());
                            placedBlack = true;
                        }
                        if (placedBlack && placedWhite) {
                            i = maxDepth;
                        }
                    }
                }
            }
        }

        // Update used time
        long secondsUsedTotal = (System.nanoTime() - startTime) / 1000000000;
        parent.setTotalTimeLeft((int) (parent.getTotalTimeLeft()-secondsUsedTotal));
        parent.setTotalTurnsLeft(parent.getTotalTurnsLeft()-1);

        // Store best board for debugging
        board.bestBoard = best[0].board;
    }

    /**
     * Returns the best possible board configuration (according to negamax with alpha-beta pruning)
     */
    private Move ID(HexTile[][] node, int depth, double alpha, double beta, int color, int maxDepth) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        // Check transposition table
        double olda = alpha;
        boolean useBestMove = false;
        TableItem n = tt.get(node);
        if (n != null) {
            countTTUsed++;
            if (n.depth >= depth) {
                // Check flag and value of retrieved node
                if (n.flag == Flag.EXACT) {
                    return new Move(n.value, n.bestMove);
                } else if (n.flag == Flag.LOWER_BOUND) {
                    alpha = Math.max(alpha, n.value);
                } else if (n.flag == Flag.UPPER_BOUND) {
                    beta = Math.min(beta, n.value);
                }
                if (alpha >= beta) {
                    return new Move(n.value, n.bestMove);
                }
            } else {
                // Current depth > retrieved depth: we should explore the node deeper
                // Apply move ordering with the retrieved best move to start exploring deeper
                useBestMove = true;
            }
        }

        // Check for leaf nodes
        boolean terminal = false;
        List<HexTile[][]> children = null;
        if (depth <= 0) terminal = true;
        else {
//            long startTime2 = System.nanoTime();
//            children = this.GenerateChildren(node, tilesToPlace, depth, color == 1, maxDepth);
            children = this.GenerateChildrenWithMoveOrdering(board, parent.getColor(), node, tilesToPlace, depth, color == 1, maxDepth);
//            System.out.println("children generation time: " + Integer.toString((int)(((System.nanoTime() - startTime2) / 1000))));
        }
        int minChildrenRequired = (parent.getColor() == Color.WHITE ? 4*3 : 2); // Calculate end game requirement for 2 players
        if (terminal || children.size() <= minChildrenRequired) {
            countNodesEvaluated++;
            return new Move(color * this.EvaluateNode(node, board, parent, true), node);
        }

        // Iterate children
        countNodes++;
        if (useBestMove)  {
            children.add(0, Helper.getGameStateDeepCopy(n.bestMove, false, false)); // Apply move ordering when possible
        }
        Move value = new Move(Integer.MIN_VALUE, null);
        for (int i = 0; i < children.size(); i++) {
            HexTile[][] child = children.get(i);
            Move value_child = ID(child, depth - 1, -beta, -alpha, -color, maxDepth); // Swap alpha/beta and negate
            value_child.score = -value_child.score; // Negate
            if (value_child.score > value.score) {
                value = value_child;
                if (value.score > alpha) alpha = value.score;
                if (value.score >= beta) {
                    countPruned++;
                    break;
                }
            }
        }

        // Transposition table storing
        TableItem item = new TableItem((int) value.score, value.board, (short) depth, null);
        if (value.score <= olda) {
            item.flag = Flag.UPPER_BOUND;
        } else if (value.score >= beta) {
            item.flag = Flag.LOWER_BOUND;
        } else {
            item.flag = Flag.EXACT;
        }
        tt.store(node, item);
        countTTStored++;

        return value;
    }
}
