package ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import static ui.EscapeSequences.*;

public class BoardDisplay {

    private static final int BOARD_SIZE = 8;
    private static final String[] FILE_LABELS = { "a", "b", "c", "d", "e", "f", "g", "h" };

    public static void main(String[] args) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);

        boolean isBlackView = args.length > 0 && args[0].equalsIgnoreCase("BLACK");

        drawHeader(out, isBlackView);
        drawBoard(out, isBlackView);
        drawHeader(out, isBlackView);

        out.print(SET_BG_COLOR_WHITE);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void drawHeader(PrintStream out, boolean reverse) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print("   "); // left gutter

        if (reverse) {
            for (int i = BOARD_SIZE - 1; i >= 0; i--) {
                out.print(String.format(" %s ", FILE_LABELS[i]));
            }
        } else {
            for (int i = 0; i < BOARD_SIZE; i++) {
                out.print(String.format(" %s ", FILE_LABELS[i]));
            }
        }

        out.println(RESET_ALL);
    }

    private static void drawBoard(PrintStream out, boolean reverse) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            int rank = reverse ? row + 1 : BOARD_SIZE - row;

            // Left gutter
            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.print(String.format(" %d ", rank));
            out.print(RESET_ALL);

            // Board squares
            for (int col = 0; col < BOARD_SIZE; col++) {
                int fileIndex = reverse ? BOARD_SIZE - col - 1 : col;
                boolean isLightSquare = (row + col) % 2 == 0;
                String bgColor = isLightSquare ? SET_BG_COLOR_WHITE : SET_BG_COLOR_BLACK;

                out.print(bgColor);
                out.print(getFormattedPiece(row, fileIndex, reverse));
            }

            // Right gutter
            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.print(String.format(" %d ", rank));
            out.println(RESET_ALL);
        }
    }

    private static String getFormattedPiece(int row, int col, boolean reverse) {
        String piece = " ";

        if (!reverse) {
            if (row == 0) piece = BLACK_PIECES_ROW[col];
            else if (row == 1) piece = BLACK_PAWN;
            else if (row == 6) piece = WHITE_PAWN;
            else if (row == 7) piece = WHITE_PIECES_ROW[col];
        } else {
            if (row == 0) piece = WHITE_PIECES_ROW[BOARD_SIZE - col - 1];
            else if (row == 1) piece = WHITE_PAWN;
            else if (row == 6) piece = BLACK_PAWN;
            else if (row == 7) piece = BLACK_PIECES_ROW[BOARD_SIZE - col - 1];
        }

        return String.format(" %s ", piece); // always 3 characters wide
    }
}
