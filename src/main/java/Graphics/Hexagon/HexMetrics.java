package Graphics.Hexagon;


import java.awt.*;
import java.util.ArrayList;

public class HexMetrics {

    public double width;
    public double height;
    private double radius;
    private double side;
    private double[] cornersX;
    private double[] cornersY;
    public HexMetrics(Double radius) {
        this.radius = radius;
        width       = this.radius * Math.sqrt(3);
        height      = this.radius * 2;
        side        = this.radius * 3. / 2;

        // Initialize the relative corners for a pointy hexagon
        cornersX    = new double[]{width/2, width, width, width/2, 0, 0};
        cornersY    = new double[]{0, .25*height, .75*height, height, .75*height, .25*height};
    }

    /**
     * Compute the corners of the hexagon at hex index [q,r]
     */
    private double x_correction = -1;
    public ArrayList<double[]> computeCorners(int q, int r) {
        double[] cX = new double[6];
        double[] cY = new double[6];
        double mX = radius * (Math.sqrt(3) * q + Math.sqrt(3)/2 * r);
        double mY = r * side;
        if (x_correction == -1) x_correction = mX;
        for (int i=0; i<6; i++) {
            cX[i] = mX + cornersX[i] - x_correction;
            cY[i] = mY + cornersY[i];
        }
        return new ArrayList<double[]>(){{add(cX); add(cY);}};
    }

    /**
     * Finds the hex index [q,r] from a canvas pixel coordinate [x,y]
     */
    public double[] pixel_to_hex(double x, double y) {
        double q = ((Math.sqrt(3)/3) * (x+x_correction) - (1./3) * y) / radius;
        double r = ((2./3) * y) / radius;
        return new double[]{q, r};
    }

    /**
     * Evaluate whether the point [pointX, pointY] is within the convex hull of the polygon [cornersX, cornersY]
     */
    public boolean isBoundingHex(double[] cornersX, double[] cornersY, int pointX, int pointY) {
        int[] cornersX_i = new int[cornersX.length];
        int[] cornersY_i = new int[cornersY.length];
        for (int x = 0; x < cornersX.length; x++) {
            cornersX_i[x] = (int) cornersX[x];
        }
        for (int y = 0; y < cornersY.length; y++) {
            cornersY_i[y] = (int) cornersY[y];
        }
        Polygon poly = new Polygon(cornersX_i, cornersY_i, cornersX.length);
        boolean contains = poly.contains(new Point(pointX, pointY));
        return contains;
    }
}
