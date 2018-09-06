package Graphics.Hexagon;


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
    public ArrayList<double[]> computeCorners(int q, int r, double[] cX, double[] cY) {
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

    public boolean isBoundingHex(ArrayList<double[]> hexagon) {
        return false;
    }

//    private double[] indexByPoint(double x, double y) {
//        Double ci = Math.floor(x / side);
//        Double cx = x - side * ci;
//
//        Double ty = y - (ci % 2) * height / 2;
//        Double cj = Math.floor(ty / height);
//        Double cy = ty - height * cj;
//
//        if (cx > Math.abs(radius / 2 - radius * cy / height)) {
//            return new double[]{ci.intValue(), cj.intValue()};
//        } else {
//            Double r_ci = ci - 1;
//            Double r_cj = cj + (ci % 2) - (cy < height / 2 ? 1 : 0);
//            return new double[]{r_ci.intValue(), r_cj.intValue()};
//        }
//    }
}
