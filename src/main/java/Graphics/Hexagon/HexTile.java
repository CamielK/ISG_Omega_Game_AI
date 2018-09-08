package Graphics.Hexagon;

import javafx.scene.canvas.Canvas;
import Enum.Color;

/**
 * Inspired by: https://gist.github.com/Akjir/5721503
 */
public class HexTile extends Canvas {

    private int q;
    private int r;
    private double[] cornersX = new double[6];
    private double[] cornersY = new double[6];
    private Color color = Color.EMPTY;
    private int group = 0;

    public HexTile(int q, int r) {
        setQ(q);
        setR(r);
    }

    public int getQ() {
        return q;
    }

    public void setQ(int q) {
        this.q = q;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public double[] getCornersX() {
        return cornersX;
    }

    public void setCornersX(double[] cornersX) {
        this.cornersX = cornersX;
    }

    public double[] getCornersY() {
        return cornersY;
    }

    public void setCornersY(double[] cornersY) {
        this.cornersY = cornersY;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }
}