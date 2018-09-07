package Library;

import Agent.Agent;
import Agent.Human;
import Enum.Color;

public class Player {

    private int id;
    private int number;
    private Agent agent = new Human();
    private int score = 0;
    private Color color;

    public int[][] getAgentMove() {
        if (agent instanceof Human) {
            return null;
        } else {
            return agent.GetMove();
        }
    }

    public Player(int id) {
        this.id = id;
        number = id + 1;
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
}
