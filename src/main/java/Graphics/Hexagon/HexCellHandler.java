package Graphics.Hexagon;

import javafx.scene.paint.Color;

public class HexCellHandler {

    private Color p1_color = Color.rgb(255, 255, 255);
    private Color p2_color = Color.rgb(35, 32, 29);
    private Color p3_color = Color.rgb(218, 97, 28);
    private Color p4_color = Color.rgb(0, 142, 219);
    private Color empty_color = Color.rgb(118, 254, 118);

    public void refresh(int q, int r, HexTile[][] pieces, HexGraphic hg) {
        switch (pieces[q][r].getValue()) {
            case 0:
                hg.fill = empty_color;
                break;
            case 1:
                hg.fill = p1_color;
                break;
            case 2:
                hg.fill = p2_color;
                break;
            case 3:
                hg.fill = p3_color;
                break;
            case 4:
                hg.fill = p4_color;
                break;
            default:
                hg.fill = empty_color;
        }
    }

}