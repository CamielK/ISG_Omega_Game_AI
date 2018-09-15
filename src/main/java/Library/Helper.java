package Library;

import Graphics.Hexagon.HexTile;

import java.util.Arrays;

public class Helper {
    /**
     * Returns a deep copy of the supplied game state
     */
    public static HexTile[][] getGameStateDeepCopy(HexTile[][] hexTiles) {
        int axialSize = hexTiles.length;
        HexTile[][] copy = new HexTile[axialSize][axialSize];
        for (int q=0; q<axialSize; q++) {
            for (int r=0; r<axialSize; r++) {
                HexTile tile = hexTiles[q][r];
                if (tile != null) {
                    HexTile copy_tile = new HexTile(q, r);
                    copy_tile.setGroup(tile.getGroup());
                    copy_tile.setColor(tile.getColor());
                    copy_tile.setPlacedId(tile.getPlacedId());
                    copy_tile.setPlacedBy(tile.getPlacedBy());
                    copy_tile.setCornersX(Arrays.copyOf(tile.getCornersX(), tile.getCornersX().length));
                    copy_tile.setCornersY(Arrays.copyOf(tile.getCornersY(), tile.getCornersY().length));
                    copy[q][r] = copy_tile;
                }
            }
        }
        return copy;
    }
}
