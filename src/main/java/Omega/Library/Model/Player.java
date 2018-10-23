package Omega.Library.Model;

import Omega.Agent.Agent;
import Omega.Agent.Human;
import Omega.Library.Config;
import Omega.Library.Enum.Color;
import Omega.Graphics.Hexagon.HexBoard;

public class Player {

    private int id;
    private int number;
    private Agent agent = new Human();
    private int score = 0;
    private Color color;

    private int totalTimeLeft;
    private int totalTurnsLeft = 0;

    public Player(int id) {
        this.id = id;
        number = id + 1;
    }

    public void GetMove(HexBoard board, Color[] tilesToPlace) {
        // If this is the first move of the game, reset resource restrictions
        if (totalTurnsLeft == 0) {
            totalTurnsLeft = board.getMaxTurns();
            totalTimeLeft = Config.MAX_GAME_TIME;
        }

        agent.GetMove(this, board, tilesToPlace);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalTurnsLeft() {
        return totalTurnsLeft;
    }

    public void setTotalTurnsLeft(int totalTurnsLeft) {
        this.totalTurnsLeft = totalTurnsLeft;
    }

    public int getTotalTimeLeft() {
        return totalTimeLeft;
    }

    public void setTotalTimeLeft(int totalTimeLeft) {
        this.totalTimeLeft = totalTimeLeft;
    }

}
