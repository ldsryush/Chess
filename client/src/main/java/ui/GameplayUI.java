package ui;

import chess.*;
import websocket.ClientNotificationHandler;
import websocket.WebSocketFacade;

import java.util.*;

import static ui.EscapeSequences.*;

public class GameplayUI implements ClientNotificationHandler {

    private ChessGame currentGame;
    private String playerColor = "observer";
    private WebSocketFacade webSocketFacade;
    private String authToken;
    private int gameID;
    private final Scanner scanner = new Scanner(System.in);

    public GameplayUI() {
        this.currentGame = new ChessGame();
    }

    public void start(String serverUrl, String authToken, int gameID) {
        this.authToken = authToken;
        this.gameID = gameID;

        try {
            String wsUrl = serverUrl
                    .replace("http://", "ws://")
                    .replace("https://", "wss://")
                    + "/ws";
            //pass gameplay.ui into websocketfacade
            this.webSocketFacade = new WebSocketFacade(wsUrl, this);

            // Connect with the player's color if they're a player, otherwise as observer
            if (!"observer".equals(playerColor)) {
                webSocketFacade.connect(authToken, gameID, playerColor);
            } else {
                webSocketFacade.connect(authToken, gameID);
            }

            // Command loop
            while (true) {
                System.out.print(SET_TEXT_COLOR_GREEN + "[GAMEPLAY] >>> " + SET_TEXT_COLOR_BLACK);
                String input = scanner.nextLine().trim();
                handleCommand(input);
            }
        } catch (Exception e) {
            System.err.println("❌ Could not establish WebSocket connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void notify(String message) {
        System.out.println("\n" + SET_TEXT_COLOR_YELLOW + "📢 " + message + SET_TEXT_COLOR_BLACK);
        System.out.print(SET_TEXT_COLOR_GREEN + "[GAMEPLAY] >>> " + SET_TEXT_COLOR_BLACK);
    }

    @Override
    public void updateGame(ChessGame game) {
        if (game == null) {
            System.err.println("❌ Received null game!");
            return;
        }

        this.currentGame = game;

        try {
            System.out.print("\033[2J\033[H");
            System.out.print(ERASE_SCREEN);
            System.out.flush();

            drawBoard();
            displayGameStatus();

            System.out.print(SET_TEXT_COLOR_GREEN + "\n[GAMEPLAY] >>> " + SET_TEXT_COLOR_BLACK);
            System.out.flush();
        } catch (Exception e) {
            System.err.println("Error in updateGame: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void displayError(String error) {
        System.out.println("\n" + SET_TEXT_COLOR_RED + "❌ Error: " + error + SET_TEXT_COLOR_BLACK);
        System.out.print(SET_TEXT_COLOR_GREEN + "[GAMEPLAY] >>> " + SET_TEXT_COLOR_BLACK);
    }

    /**
     * Parses and routes user commands.
     */
    public void handleCommand(String input) {
        String[] tokens = input.split("\\s+");
        if (tokens.length == 0 || tokens[0].isBlank()) {
            return;
        }

        switch (tokens[0].toLowerCase()) {
            case "help" -> displayHelp();
            case "redraw" -> drawBoard();
            case "test" -> testBoard();
            case "status" -> displayGameInfo();
            case "highlight" -> {
                if (tokens.length < 2) {
                    System.out.println("Usage: highlight <position>");
                    return;
                }
                ChessPosition pos = parsePosition(tokens[1]);
                if (pos == null) {
                    System.out.println("Invalid position format.");
                    return;
                }
                displayLegalMoves(pos);
            }
            case "resign" -> {
                if (!"observer".equals(playerColor)) {
                    System.out.print("Are you sure you want to resign? (yes/no): ");
                    String confirmation = scanner.nextLine().trim().toLowerCase();
                    if ("yes".equals(confirmation) || "y".equals(confirmation)) {
                        System.out.println("You resigned.");
                        try {
                            webSocketFacade.resign(authToken, gameID);
                        } catch (Exception e) {
                            System.err.println("❌ Failed to resign: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Resign cancelled.");
                    }
                } else {
                    System.out.println("Observers cannot resign.");
                }
            }
            case "leave" -> {
                System.out.println("Leaving the game...");
                try {
                    webSocketFacade.leave(authToken, gameID);
                    webSocketFacade.close();
                } catch (Exception e) {
                    System.err.println("❌ Failed to leave: " + e.getMessage());
                }
                // Exit the gameplay loop to return to Post-Login UI
                return;
            }
            case "move" -> {
                if (tokens.length < 3) {
                    System.out.println("Usage: move <from> <to> [promotion]");
                    return;
                }
                ChessPosition from = parsePosition(tokens[1]);
                ChessPosition to = parsePosition(tokens[2]);
                if (from == null || to == null) {
                    System.out.println("Invalid move format.");
                    return;
                }
                ChessPiece.PieceType promotion = null;
                if (tokens.length == 4) {
                    promotion = switch (tokens[3].toLowerCase()) {
                        case "queen" -> ChessPiece.PieceType.QUEEN;
                        case "rook" -> ChessPiece.PieceType.ROOK;
                        case "bishop" -> ChessPiece.PieceType.BISHOP;
                        case "knight" -> ChessPiece.PieceType.KNIGHT;
                        default -> null;
                    };
                }
                ChessMove move = new ChessMove(from, to, promotion);
                System.out.println("Submitting move: " +
                        positionToString(from) + " → " + positionToString(to));
                try {
                    webSocketFacade.makeMove(authToken, gameID, move);
                } catch (Exception e) {
                    System.err.println("❌ Failed to send move: " + e.getMessage());
                }
            }
            default -> System.out.println("Unknown command. Type 'help' for options.");
        }
    }

    public void displayHelp() {
        System.out.println(SET_TEXT_COLOR_BLUE + "\n" + "=".repeat(50));
        System.out.println("🎮 CHESS GAME HELP");
        System.out.println("=".repeat(50));
        System.out.println("Available Commands:");
        System.out.println("  help                     - Show this help menu");
        System.out.println("  redraw                   - Redraw the chess board");
        System.out.println("  test                     - Test board display with fresh game");
        System.out.println("  move <from> <to>         - Make a move (e.g., 'move e2 e4')");
        System.out.println("  move <from> <to> <piece> - Make a move with pawn promotion");
        System.out.println("  highlight <pos>          - Show legal moves for piece at position");
        System.out.println("  resign                   - Resign from the game");
        System.out.println("  leave                    - Leave the game");
        System.out.println("  status                   - Show current game information");
        System.out.println("=".repeat(50) + SET_TEXT_COLOR_BLACK);
    }

    public void testBoard() {
        System.out.println("🧪 TESTING BOARD DISPLAY");
        ChessGame testGame = new ChessGame();
        this.currentGame = testGame;
        System.out.print(ERASE_SCREEN);
        drawBoard();
    }

    public void drawBoard() {
        if (currentGame == null || currentGame.getBoard() == null) {
            System.out.println("❌ No game or board loaded");
            return;
        }
        ChessBoard board = currentGame.getBoard();
        boolean whiteOnBottom = !"black".equalsIgnoreCase(playerColor);
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
        System.out.println("\nLegal moves for piece at " +
                positionToString(position) + ":");
        boolean whiteOnBottom = !"black".equalsIgnoreCase(playerColor);
        drawChessBoard(currentGame.getBoard(), whiteOnBottom, legalMoves);
    }

    private void drawChessBoard(ChessBoard board,
                                boolean whiteOnBottom,
                                Collection<ChessMove> highlightMoves) {
        Set<ChessPosition> highlightPositions = new HashSet<>();
        ChessPosition selectedPosition = null;
        if (highlightMoves != null) {
            for (ChessMove move : highlightMoves) {
                highlightPositions.add(move.getEndPosition());
                selectedPosition = move.getStartPosition();
            }
        }

        System.out.println();
        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + "   ");
        if (whiteOnBottom) {
            for (char c = 'a'; c <= 'h'; c++) {
                System.out.print(" " + c + " ");
            }
        } else {
            for (char c = 'h'; c >= 'a'; c--) {
                System.out.print(" " + c + " ");
            }
        }
        System.out.println(RESET_ALL);

        for (int row = 1; row <= 8; row++) {
            int displayRow = whiteOnBottom ? 9 - row : row;
            System.out.print(SET_BG_COLOR_LIGHT_GREY +
                    SET_TEXT_COLOR_BLACK + " " + displayRow + " " + RESET_ALL);

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
                    bgColor = isLight ? SET_BG_COLOR_WHITE : SET_BG_COLOR_BLACK;
                }

                System.out.print(bgColor);
                if (piece == null) {
                    System.out.print("   ");
                } else {
                    String symbol = getPieceSymbol(piece);
                    String textColor = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? SET_TEXT_COLOR_BLUE
                            : SET_TEXT_COLOR_RED;
                    System.out.print(textColor + " " + symbol + " ");
                }
                System.out.print(RESET_ALL);
            }

            System.out.println(SET_BG_COLOR_LIGHT_GREY +
                    SET_TEXT_COLOR_BLACK + " " + displayRow + " " + RESET_ALL);
        }

        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + "   ");
        if (whiteOnBottom) {
            for (char c = 'a'; c <= 'h'; c++) {
                System.out.print(" " + c + " ");
            }
        } else {
            for (char c = 'h'; c >= 'a'; c--) {
                System.out.print(" " + c + " ");
            }
        }
        System.out.println(RESET_ALL);
        System.out.println();

        System.out.println(SET_TEXT_COLOR_BLUE +
                "Current turn: " + currentGame.getTeamTurn() + RESET_ALL);
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
        if (input == null || input.length() != 2) {
            return null;
        }
        char file = Character.toLowerCase(input.charAt(0));
        char rank = input.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            return null;
        }
        int col = file - 'a' + 1;
        int row = rank - '1' + 1;
        return new ChessPosition(row, col);
    }

    public String positionToString(ChessPosition pos) {
        if (pos == null) {
            return "";
        }
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

        displayGameStatus();
        System.out.println(SET_TEXT_COLOR_BLACK);
    }

    private void displayGameStatus() {
        if (currentGame == null) {
            return;
        }

        if (currentGame.isInCheck(ChessGame.TeamColor.WHITE)) {
            System.out.println(SET_TEXT_COLOR_RED + "⚠️  WHITE is in check!" + RESET_ALL);
        }
        if (currentGame.isInCheck(ChessGame.TeamColor.BLACK)) {
            System.out.println(SET_TEXT_COLOR_RED + "⚠️  BLACK is in check!" + RESET_ALL);
        }

        if (currentGame.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            System.out.println(SET_TEXT_COLOR_RED + "🏁 WHITE is in checkmate! BLACK wins!" + RESET_ALL);
        } else if (currentGame.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            System.out.println(SET_TEXT_COLOR_RED + "🏁 BLACK is in checkmate! WHITE wins!" + RESET_ALL);
        } else if (currentGame.isInStalemate(ChessGame.TeamColor.WHITE) ||
                currentGame.isInStalemate(ChessGame.TeamColor.BLACK)) {
            System.out.println(SET_TEXT_COLOR_YELLOW + "🤝 Game is in stalemate!" + RESET_ALL);
        }
    }

    public void setPlayerColor(String color) {
        this.playerColor = color;
    }
}
