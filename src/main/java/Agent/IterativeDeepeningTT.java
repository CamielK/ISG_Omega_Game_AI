package Agent;

import Graphics.Hexagon.HexBoard;
import Graphics.Hexagon.HexTile;
import Library.Enum.Color;
import Library.Enum.Flag;
import Library.Model.Move;
import Library.Model.Player;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Iterative deepening using negamax formulation and alpha-beta pruning
 */
public class IterativeDeepeningTT implements Agent {

    private final int maxSearchTime = 2*60;
//    private final int maxSearchTime = 1*30;

    private HexBoard board;
    private Player parent;
    private Color[] tilesToPlace;
    private TranspositionTable tt = null;

    public void GetMove(Player parent, HexBoard board, Color[] tilesToPlace) {
        this.board = board;
        this.parent = parent;
        this.tilesToPlace = tilesToPlace;
        if (tt == null) tt = new TranspositionTable(board.getGameState());

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

        // Check transposition table
        double olda = alpha;
        boolean useBestMove = false;
        TableItem n = tt.get(node);
        if (n != null && n.depth >= depth) {
            if (n.flag == Flag.EXACT) {
                return new Move(n.value, node);
            } else if (n.flag == Flag.LOWER_BOUND) {
                alpha = Math.max(alpha, n.value);
            } else if (n.flag == Flag.UPPER_BOUND) {
                beta = Math.min(beta, n.value);
            }
            if (alpha >= beta) {
                return new Move(n.value, node);
            }

            // move ordering: check best move first
            useBestMove = true;
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

        // Iterate children
        if (useBestMove) children.add(0, n.bestMove);
        Move value = new Move(Integer.MIN_VALUE, null);
        HexTile[][] bestMove = null;
        for (HexTile[][] child : children) {
            Move value_child = ID(child, depth - 1, -beta, -alpha, -color); // Swap alpha/beta and negate
            value_child.score = -value_child.score; // Negate
            if (value_child.score > value.score) {
                value = value_child;
                bestMove = child;
            }
            if (value_child.score > alpha) alpha = value.score;
            if (alpha >= beta) break;
        }

        // Transposition table storing
        TableItem item = new TableItem((int) value.score, bestMove, (short) depth, null);
        if (value.score <= olda) {
            item.flag = Flag.UPPER_BOUND;
        } else if (value.score >= beta) {
            item.flag = Flag.LOWER_BOUND;
        } else {
            item.flag = Flag.EXACT;
        }
        tt.store(value.board, item);

        return value;
    }
}
