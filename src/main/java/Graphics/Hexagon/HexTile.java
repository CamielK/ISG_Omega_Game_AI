package Graphics.Hexagon;

import Library.Enum.Color;

/**
 * Inspired by: https://gist.github.com/Akjir/5721503
 */
public class HexTile {

    private int q;
    private int r;
    private double[] cornersX = new double[6];
    private double[] cornersY = new double[6];
    private Color color = Color.EMPTY;
    private int group = 0;
    private int placedId = -1; // Turn (or search depth) at which this tile was placed, used for debugging
    private String placedBy = ""; // Player id by which this tile was placed, used for debugging

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

    public int getPlacedId() {
        return placedId;
    }

    public void setPlacedId(int placedId) {
        this.placedId = placedId;
    }

    public String getPlacedBy() {
        return placedBy;
    }

    public void setPlacedBy(String placedBy) {
        this.placedBy = placedBy;
    }
}