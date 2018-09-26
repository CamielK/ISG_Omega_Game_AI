package Omega.Agent;

import Omega.Graphics.Hexagon.HexBoard;
import Omega.Graphics.Hexagon.HexTile;
import Omega.Library.Enum.Color;
import Omega.Library.Model.Move;
import Omega.Library.Model.Player;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Iterative deepening using negamax formulation and alpha-beta pruning
 */
public class IterativeDeepening implements Agent {

    private final int maxSearchTime = 2*60;
//    private final int maxSearchTime = 1*30;

    private HexBoard board;
    private Player parent;
    private Color[] tilesToPlace;

    public void GetMove(Player parent, HexBoard board, Color[] tilesToPlace) {
        this.board = board;
        this.parent = parent;
        this.tilesToPlace = tilesToPlace;

        long startTime = System.nanoTime();
        int maxDepth = GetMaxGameDepth(board, parent);
        final int[] maxDepthReached = {0};
        final Move[] best = {new Move(Integer.MIN_VALUE, null)};
        System.out.println("Starting ID search with a max possible depth of " + maxDepth + " and a timeout of " + maxSearchTime + " seconds.");
        for (int depth = 1; depth <= maxDepth; depth++) {
            try {
                long secondsLeft = maxSearchTime - ((System.nanoTime() - startTime) / 1000000000);
                if (secondsLeft <= 0) {
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
                                    Move move = ID(board.getGameState(), finalDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
                                    best[0] = move;
                                    maxDepthReached[0] = finalDepth;
                                } catch (InterruptedException e) {
                                    System.out.println("ID timeout at depth " + finalDepth);
                                    maxDepthReached[0] = finalDepth-1;
                                    this.cancel();
                                    return null;
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
                System.out.println("Finished depth " + depth + " with score " + best[0].score);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        while (maxDepthReached[0] <= 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("IterativeDeepening found score: " + best[0].score + ", max depth reached: " + maxDepthReached[0]);

        // Get the first 2 tiles placed by the minmax algorithm
        for (int q = 0; q < best[0].board.length; q++) {
            for (int r = 0; r < best[0].board.length; r++) {
                HexTile tile = best[0].board[q][r];
                if (tile != null && tile.getPlacedId() == maxDepthReached[0]) {
                    board.getGameState()[q][r].setColor(tile.getColor());
                }
            }
        }

        // Store best board for debugging
        board.bestBoard = best[0].board;
    }

    /**
     * Returns the best possible board configuration (according to negamax with alpha-beta pruning)
     */
    private Move ID(HexTile[][] node, int depth, double alpha, double beta, int color) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        // Check for leaf nodes
        boolean terminal = false;
        List<HexTile[][]> children = null;
        if (depth <= 0) terminal = true;
        else {
            children = this.GenerateChildren(node, tilesToPlace, depth, color == 1);
        }
        int minChildrenRequired = (parent.getColor() == Color.WHITE ? 4*3 : 2); // Calculate end game requirement for 2 players
        if (terminal || children.size() <= minChildrenRequired) {
            return new Move(color * this.EvaluateNode(node, board, parent), node);
        }

        Move value = new Move(Integer.MIN_VALUE, null);
        for (HexTile[][] child : children) {
            Move value_child = ID(child, depth - 1, -beta, -alpha, -color); // Swap alpha/beta and negate
            value_child.score = -value_child.score; // Negate
            if (value_child.score > value.score) value = value_child;
            if (value_child.score > alpha) alpha = value.score;
            if (alpha >= beta) break;
        }
        return value;
    }
}
