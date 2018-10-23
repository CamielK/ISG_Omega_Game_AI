package Omega.Library;

import Omega.Library.Enum.Color;

public class Config {
    public static boolean GFX_GROUP_ENABLED = false;
    public static boolean GFX_AXES_ENABLED = false;
    public static boolean GFX_PLACED_ENABLED = false;

    public static final int NUM_PLAYERS = 2;
    public static final Color[] COLORS_IN_PLAY = new Color[]{Color.WHITE, Color.BLACK, Color.EMPTY};

    public static final int MAX_GAME_TIME = 60 * 15; // Max search time per game in seconds
}
