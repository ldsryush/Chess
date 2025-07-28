package ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import static ui.EscapeSequences.*;

/**
 * The BoardDisplay class provides methods for displaying the chess board and pieces on the console.
 */
public class BoardDisplay {

    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int LINE_WIDTH_IN_CHARS = 1;
    private static final String[] HEADERS = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};
    private static final String[] WHITE_PIECES = {WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK};
    private static final String[] BLACK_PIECES = {BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_QUEEN, BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK};


    /**
     * Displays the chess board with pieces on the console.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        out.print(ERASE_SCREEN);

        drawHeadersForward(out);

        drawChessBoardForward(out);

        drawHeadersForward(out);

        out.println(SET_BG_COLOR_WHITE);

        drawHeadersBackward(out);

        drawChessBoardBackward(out);

        drawHeadersBackward(out);

        out.print(SET_BG_COLOR_WHITE);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    /**
     * Draws the column headers for the chess board in backward order.
     *
     * @param out The output stream to print to.
     */
    private static void drawHeadersBackward(PrintStream out) {
        setBlack(out);

        out.print(SET_BG_COLOR_LIGHT_GREY);

        out.print(EMPTY.repeat(LINE_WIDTH_IN_CHARS));
        for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
            printHeaderText(out, HEADERS[BOARD_SIZE_IN_SQUARES - boardCol - 1]);

        }
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(EMPTY.repeat(LINE_WIDTH_IN_CHARS));

        out.println(SET_BG_COLOR_WHITE);
    }

    /**
     * Draws the column headers for the chess board in forward order.
     *
     * @param out The output stream to print to.
     */
    private static void drawHeadersForward(PrintStream out) {

        setBlack(out);

        out.print(SET_BG_COLOR_LIGHT_GREY);

        out.print(EMPTY.repeat(LINE_WIDTH_IN_CHARS));
        for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
            printHeaderText(out, HEADERS[boardCol]);

        }
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(EMPTY.repeat(LINE_WIDTH_IN_CHARS));

        out.println(SET_BG_COLOR_WHITE);

    }

    /**
     * Prints a header text on the console.
     *
     * @param out    The output stream to print to.
     * @param player The header text to print.
     */
    private static void printHeaderText(PrintStream out, String player) {
        out.print(SET_BG_COLOR_LIGHT_GREY);

        out.print(player);

        setBlack(out);
    }

    /**
     * Draws the chess board with pieces in forward order.
     *
     * @param out The output stream to print to.
     */
    private static void drawChessBoardForward(PrintStream out) {
        for (int row = 0; row < 8; row++) {
            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.print(" ");
            out.print(BOARD_SIZE_IN_SQUARES - row);
            out.print(" ");
            if (row < 2) {
                out.print(SET_TEXT_COLOR_RED);
            } else {
                out.print(SET_TEXT_COLOR_BLUE);
            }
            for (int col = 0; col < 8; col++) {
                if ((col + row) % 2 == 0) {
                    out.print(SET_BG_COLOR_WHITE);
                } else {
                    out.print(SET_BG_COLOR_BLACK);
                }
                if (row == 0) {
                    out.print(BLACK_PIECES[col]);
                } else if (row == 1) {
                    out.print(BLACK_PAWN);
                } else if (row == 6) {
                    out.print(WHITE_PAWN);
                } else if (row == 7) {
                    out.print(WHITE_PIECES[col]);
                } else {
                    out.print(EMPTY.repeat(LINE_WIDTH_IN_CHARS));
                }
            }
            out.print(SET_TEXT_COLOR_BLACK);
            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.print(" ");
            out.print(BOARD_SIZE_IN_SQUARES - row);
            out.print(" ");

            out.println(SET_BG_COLOR_WHITE);
        }
    }

    /**
     * Draws the chess board with pieces in backward order.
     *
     * @param out The output stream to print to.
     */
    private static void drawChessBoardBackward(PrintStream out) {
        for (int row = 0; row < 8; row++) {
            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.print(" ");
            out.print(row + 1);
            out.print(" ");
            if (row < 2) {
                out.print(SET_TEXT_COLOR_BLUE);
            } else {
                out.print(SET_TEXT_COLOR_RED);
            }
            for (int col = 0; col < 8; col++) {
                if ((col + row) % 2 == 0) {
                    out.print(SET_BG_COLOR_WHITE);
                } else {
                    out.print(SET_BG_COLOR_BLACK);
                }
                if (row == 0) {
                    out.print(WHITE_PIECES[BOARD_SIZE_IN_SQUARES - col - 1]);
                } else if (row == 1) {
                    out.print(WHITE_PAWN);
                } else if (row == 6) {
                    out.print(BLACK_PAWN);
                } else if (row == 7) {
                    out.print(BLACK_PIECES[BOARD_SIZE_IN_SQUARES - col - 1]);
                } else {
                    out.print(EMPTY.repeat(LINE_WIDTH_IN_CHARS));
                }
            }
            out.print(SET_TEXT_COLOR_BLACK);
            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.print(" ");
            out.print(row + 1);
            out.print(" ");

            out.print(SET_BG_COLOR_WHITE);
            out.println();
        }
    }

    /**
     * Sets the text and background color to black on the console.
     *
     * @param out The output stream to print to.
     */
    private static void setBlack(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_BLACK);
    }
}
