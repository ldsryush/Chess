package ui;

public class EscapeSequences {

    private static final String ESC = "\u001b";

    // Reset codes
    public static final String RESET_ALL = ESC + "[0m";
    public static final String RESET_BG_COLOR = ESC + "[49m";
    public static final String RESET_TEXT_COLOR = ESC + "[39m";
    public static final String LINE = "_".repeat(60) + "\n";

    // Text colors (256-color mode)
    private static final String SET_TEXT_COLOR = ESC + "[38;5;";
    public static final String SET_TEXT_COLOR_BLACK = SET_TEXT_COLOR + "0m";
    public static final String SET_TEXT_COLOR_RED = SET_TEXT_COLOR + "160m";
    public static final String SET_TEXT_COLOR_GREEN = SET_TEXT_COLOR + "46m";
    public static final String SET_TEXT_COLOR_BLUE = SET_TEXT_COLOR + "12m";
    public static final String SET_TEXT_COLOR_MAGENTA = SET_TEXT_COLOR + "5m";
    public static final String SET_TEXT_COLOR_WHITE = SET_TEXT_COLOR + "15m";
    public static final String SET_TEXT_COLOR_YELLOW = SET_TEXT_COLOR + "226m";

    // Background colors (256-color mode)
    private static final String SET_BG_COLOR = ESC + "[48;5;";
    public static final String SET_BG_COLOR_BLACK = SET_BG_COLOR + "0m";
    public static final String SET_BG_COLOR_LIGHT_GREY = SET_BG_COLOR + "242m";
    public static final String SET_BG_COLOR_DARK_GREY = SET_BG_COLOR + "236m";
    public static final String SET_BG_COLOR_WHITE = SET_BG_COLOR + "15m";
    public static final String SET_BG_COLOR_YELLOW = SET_BG_COLOR + "226m";
    public static final String SET_BG_COLOR_GREEN = SET_BG_COLOR + "46m";
    public static final String SET_BG_COLOR_DARK_GREEN = SET_BG_COLOR + "22m";

    // Screen control
    public static final String ERASE_SCREEN = ESC + "[H" + ESC + "[2J";

    // Square formatting
    public static final int SQUARE_WIDTH = 3;
    public static final String EMPTY_SQUARE = " ".repeat(SQUARE_WIDTH);

    // ASCII chess pieces (for alignment-safe CLI rendering)
    public static final String WHITE_KING   = "K";
    public static final String WHITE_QUEEN  = "Q";
    public static final String WHITE_ROOK   = "R";
    public static final String WHITE_BISHOP = "B";
    public static final String WHITE_KNIGHT = "N";
    public static final String WHITE_PAWN   = "P";

    public static final String BLACK_KING   = "k";
    public static final String BLACK_QUEEN  = "q";
    public static final String BLACK_ROOK   = "r";
    public static final String BLACK_BISHOP = "b";
    public static final String BLACK_KNIGHT = "n";
    public static final String BLACK_PAWN   = "p";

    // Piece rows
    public static final String[] WHITE_PIECES_ROW = {
            WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN,
            WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK
    };

    public static final String[] BLACK_PIECES_ROW = {
            BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_QUEEN,
            BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK
    };
}
