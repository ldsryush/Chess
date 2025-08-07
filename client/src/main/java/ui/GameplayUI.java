package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece;
import websocket.ClientNotificationHandler;

import java.util.Collection;

import static ui.EscapeSequences.*;

public class GameplayUI implements ClientNotificationHandler {

    private ChessGame currentGame;
    private String playerColor = "observer";

    public GameplayUI() {
        this.currentGame = new ChessGame();
    }

    @Override
    public void notify(String message) {
        System.out.println(SET_TEXT_COLOR_YELLOW + "📢 " + message + SET_TEXT_COLOR_BLACK);
    }

    @Override
    public void updateGame(ChessGame game) {
        System.out.println("updateGame triggered");
        this.currentGame = game;

        ChessPiece pieceAtE2 = game.getBoard().getPiece(new ChessPosition(2, 5));
        ChessPiece pieceAtE4 = game.getBoard().getPiece(new ChessPosition(4, 5));
        System.out.println("🔍 Piece at e2: " + (pieceAtE2 != null ? pieceAtE2.getPieceType() : "none"));
        System.out.println("🔍 Piece at e4: " + (pieceAtE4 != null ? pieceAtE4.getPieceType() : "none"));
        System.out.println("🔍 Current turn: " + game.getTeamTurn());

        System.out.println(SET_TEXT_COLOR_BLUE + "📋 Game updated!" + SET_TEXT_COLOR_BLACK);
        drawBoard();
    }


    @Override
    public void displayError(String error) {
        System.out.println(SET_TEXT_COLOR_RED + "Error: " + error + SET_TEXT_COLOR_BLACK);
    }

    public void displayHelp() {
        System.out.println(SET_TEXT_COLOR_BLUE + "\n" + "=".repeat(50));
        System.out.println("🎮 CHESS GAME HELP");
        System.out.println("=".repeat(50));
        System.out.println("Available Commands:");
        System.out.println("  help                     - Show this help menu");
        System.out.println("  redraw                   - Redraw the chess board");
        System.out.println("  move <from> <to>         - Make a move (e.g., 'move e2 e4')");
        System.out.println("  move <from> <to> <piece> - Make a move with pawn promotion");
        System.out.println("                            (e.g., 'move e7 e8 queen')");
        System.out.println("  highlight <pos>          - Show legal moves for piece at position");
        System.out.println("                            (e.g., 'highlight e2')");
        System.out.println("  resign                   - Resign from the game");
        System.out.println("  leave                    - Leave the game");
        System.out.println("  status                   - Show current game information");
        System.out.println();
        System.out.println("Position Format: Use algebraic notation (a1-h8)");
        System.out.println("Examples: a1, e4, h8");
        System.out.println("=".repeat(50) + SET_TEXT_COLOR_BLACK);
    }

    public void drawBoard() {
        if (currentGame == null) {
            System.out.println("No game loaded");
            return;
        }

        ChessBoard board = currentGame.getBoard();
        if (board == null) {
            System.out.println("Game board is null");
            return;
        }

        boolean whiteOnBottom = !"BLACK".equalsIgnoreCase(playerColor);
        System.out.println("Drawing board with whiteOnBottom = " + whiteOnBottom);
        drawChessBoard(board, whiteOnBottom, null);
    }

    public void displayLegalMoves(ChessPosition position) {
        if (currentGame == null) {
            System.out.println("No game loaded");
            return;
        }

        Collection<ChessMove> legalMoves = currentGame.validMoves(position);
        if (legalMoves.isEmpty()) {
            System.out.println("No legal moves available for piece at " +
                    positionToString(position));
            return;
        }

        System.out.println("Legal moves for piece at " +
                positionToString(position) + ":");
        boolean whiteOnBottom = !"BLACK".equalsIgnoreCase(playerColor);
        drawChessBoard(currentGame.getBoard(), whiteOnBottom, legalMoves);
    }

    private void drawChessBoard(ChessBoard board,
                                boolean whiteOnBottom,
                                Collection<ChessMove> highlightMoves) {
        // Build highlight set and track selected start position
        var highlightPositions = new java.util.HashSet<ChessPosition>();
        ChessPosition selectedPosition = null;

        if (highlightMoves != null) {
            for (ChessMove move : highlightMoves) {
                highlightPositions.add(move.getEndPosition());
                selectedPosition = move.getStartPosition();
            }
        }

        // Column headers
        System.out.print("   ");
        if (whiteOnBottom) {
            for (char c = 'a'; c <= 'h'; c++) {
                System.out.print(" " + c + " ");
            }
        } else {
            for (char c = 'h'; c >= 'a'; c--) {
                System.out.print(" " + c + " ");
            }
        }
        System.out.println();

        // Rows
        for (int row = 1; row <= 8; row++) {
            int displayRow = whiteOnBottom ? 9 - row : row;
            System.out.print(" " + displayRow + " ");

            for (int col = 1; col <= 8; col++) {
                int displayCol = whiteOnBottom ? col : 9 - col;
                ChessPosition pos = new ChessPosition(displayRow, displayCol);
                ChessPiece piece = board.getPiece(pos);

                boolean isLight = (displayRow + displayCol) % 2 == 0;
                String bgColor;
                if (pos.equals(selectedPosition)) {
                    bgColor = SET_BG_COLOR_YELLOW;
                } else if (highlightPositions.contains(pos)) {
                    bgColor = isLight ? SET_BG_COLOR_GREEN : SET_BG_COLOR_DARK_GREEN;
                } else {
                    bgColor = isLight ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
                }

                System.out.print(bgColor);

                if (piece == null) {
                    System.out.print("   ");
                } else {
                    String symbol = getPieceSymbol(piece);
                    String textColor = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? SET_TEXT_COLOR_WHITE
                            : SET_TEXT_COLOR_BLACK;
                    System.out.print(textColor + " " + symbol + " ");
                }

                System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
            }

            System.out.println(" " + displayRow);
        }

        // Footer headers
        System.out.print("   ");
        if (whiteOnBottom) {
            for (char c = 'a'; c <= 'h'; c++) {
                System.out.print(" " + c + " ");
            }
        } else {
            for (char c = 'h'; c >= 'a'; c--) {
                System.out.print(" " + c + " ");
            }
        }
        System.out.println();
    }

    private String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING   -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? "K" : "k";
            case QUEEN  -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? "Q" : "q";
            case ROOK   -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? "R" : "r";
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? "B" : "b";
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? "N" : "n";
            case PAWN   -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? "P" : "p";
        };
    }

    public ChessPosition parsePosition(String input) {
        if (input == null || input.length() != 2) return null;
        char file = Character.toLowerCase(input.charAt(0));
        char rank = input.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') return null;
        int col = file - 'a' + 1;
        int row = rank - '1' + 1;
        return new ChessPosition(row, col);
    }

    public String positionToString(ChessPosition pos) {
        if (pos == null) return "";
        char file = (char) ('a' + pos.getColumn() - 1);
        char rank = (char) ('1' + pos.getRow() - 1);
        return "" + file + rank;
    }

    public void displayGameInfo() {
        if (currentGame == null) {
            System.out.println("No game loaded");
            return;
        }

        System.out.println(SET_TEXT_COLOR_BLUE + "\nGAME STATUS");
        System.out.println("Current turn: " + currentGame.getTeamTurn());
        System.out.println("Your role: " + playerColor);

        if (currentGame.isInCheck(ChessGame.TeamColor.WHITE)) {
            System.out.println(SET_TEXT_COLOR_RED + "WHITE is in check!" + SET_TEXT_COLOR_BLUE);
        }
        if (currentGame.isInCheck(ChessGame.TeamColor.BLACK)) {
            System.out.println(SET_TEXT_COLOR_RED + "BLACK is in check!" + SET_TEXT_COLOR_BLUE);
        }

        if (currentGame.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            System.out.println(SET_TEXT_COLOR_RED + "WHITE is in checkmate! BLACK wins!" + SET_TEXT_COLOR_BLUE);
        } else if (currentGame.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            System.out.println(SET_TEXT_COLOR_RED + "BLACK is in checkmate! WHITE wins!" + SET_TEXT_COLOR_BLUE);
        } else if (currentGame.isInStalemate(ChessGame.TeamColor.WHITE) ||
                currentGame.isInStalemate(ChessGame.TeamColor.BLACK)) {
            System.out.println(SET_TEXT_COLOR_YELLOW + "Game is in stalemate!" + SET_TEXT_COLOR_BLUE);
        }

        System.out.println(SET_TEXT_COLOR_BLACK);
    }

    public void setPlayerColor(String color) {
        this.playerColor = color;
    }
}
