package Agent;

import Agent.Agent;

public class MinMaxBasic implements Agent {

    public int[][] GetMove() {
        int[] move1 = new int[]{0,0};
        int[] move2 = new int[]{0,0};
        return new int[][]{move1, move2};
    }
}
