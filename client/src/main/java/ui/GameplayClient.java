package ui;

import chess.*;
import websocket.WebSocketFacade;

import java.io.IOException;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class GameplayClient {

    private final WebSocketFacade webSocketFacade;
    private final GameplayUI gameplayUI;
    private final Scanner scanner;
    private final String authToken;
    private final int gameID;
    private String playerColor = "observer";
    private boolean gameActive = true;

    public GameplayClient(String serverUrl, String authToken, int gameID) {
        this.authToken = authToken;
        this.gameID = gameID;
        this.gameplayUI = new GameplayUI();
        this.scanner = new Scanner(System.in);

        try {
            String wsUrl = serverUrl.replace("http://", "ws://").replace("https://", "wss://") + "/ws";
            this.webSocketFacade = new WebSocketFacade(wsUrl, gameplayUI);

            System.out.println("🌐 Connecting to WebSocket...");
            webSocketFacade.connect(authToken, gameID);

        } catch (Exception e) {
            System.err.println("Failed to connect to game: " + e.getMessage());
            throw new RuntimeException("Could not establish WebSocket connection", e);
        }
    }

    public void run() {
        System.out.println(SET_TEXT_COLOR_GREEN + "🎮 Entering gameplay mode..." + SET_TEXT_COLOR_BLACK);
        gameplayUI.displayHelp();

        // Optional: force initial board draw if needed
        System.out.println("🧪 Waiting for game state...");
        // You could also call gameplayUI.drawBoard() here if you want a placeholder

        while (gameActive) {
            System.out.print(SET_TEXT_COLOR_GREEN + "\n[GAMEPLAY] >>> " + SET_TEXT_COLOR_BLACK);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;

            try {
                handleCommand(input);
            } catch (Exception e) {
                System.out.println(SET_TEXT_COLOR_RED + "Error: " + e.getMessage() + SET_TEXT_COLOR_BLACK);
            }
        }
    }

    private void handleCommand(String input) throws IOException {
        String[] tokens = input.toLowerCase().split("\\s+");
        String command = tokens[0];

        switch (command) {
            case "help" -> gameplayUI.displayHelp();
            case "redraw" -> gameplayUI.drawBoard();
            case "leave", "quit" -> leave();
            case "move" -> makeMove(tokens);
            case "resign" -> resign();
            case "highlight" -> highlightMoves(tokens);
            case "status" -> gameplayUI.displayGameInfo();
            default -> System.out.println("Unknown command '" + command + "'. Type 'help' for available commands.");
        }
    }

    private void makeMove(String[] tokens) throws IOException {
        if ("observer".equals(playerColor)) {
            System.out.println("Observers cannot make moves");
            return;
        }

        if (tokens.length < 3) {
            System.out.println("Usage: move <from> <to> [promotion]");
            return;
        }

        ChessPosition start = gameplayUI.parsePosition(tokens[1]);
        ChessPosition end = gameplayUI.parsePosition(tokens[2]);

        if (start == null || end == null) {
            System.out.println("Invalid position format. Use format like 'e2' or 'a8'");
            return;
        }

        ChessPiece.PieceType promotion = null;
        if (tokens.length > 3) {
            promotion = parsePromotionPiece(tokens[3]);
            if (promotion == null) {
                System.out.println("Invalid promotion piece. Use: queen, rook, bishop, or knight");
                return;
            }
        }

        ChessMove move = new ChessMove(start, end, promotion);

        System.out.println("Making move: " + gameplayUI.positionToString(start) +
                " -> " + gameplayUI.positionToString(end) +
                (promotion != null ? " (promote to " + promotion + ")" : ""));

        webSocketFacade.makeMove(authToken, gameID, move);
    }

    private ChessPiece.PieceType parsePromotionPiece(String piece) {
        return switch (piece.toLowerCase()) {
            case "queen", "q" -> ChessPiece.PieceType.QUEEN;
            case "rook", "r" -> ChessPiece.PieceType.ROOK;
            case "bishop", "b" -> ChessPiece.PieceType.BISHOP;
            case "knight", "n", "k" -> ChessPiece.PieceType.KNIGHT;
            default -> null;
        };
    }

    private void highlightMoves(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: highlight <position>");
            return;
        }

        ChessPosition position = gameplayUI.parsePosition(tokens[1]);
        if (position == null) {
            System.out.println("Invalid position format. Use format like 'e2' or 'a8'");
            return;
        }

        gameplayUI.displayLegalMoves(position);
    }

    private void resign() throws IOException {
        if ("observer".equals(playerColor)) {
            System.out.println("Observers cannot resign");
            return;
        }

        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if ("yes".equals(confirmation) || "y".equals(confirmation)) {
            webSocketFacade.resign(authToken, gameID);
            System.out.println("Resignation sent...");
        } else {
            System.out.println("Resignation cancelled");
        }
    }

    private void leave() throws IOException {
        System.out.println("Leaving game...");
        webSocketFacade.leave(authToken, gameID);
        webSocketFacade.close();
        gameActive = false;
        System.out.println(SET_TEXT_COLOR_GREEN + "👋 Left the game. Returning to main menu." + SET_TEXT_COLOR_BLACK);
    }

    public void setPlayerColor(String color) {
        this.playerColor = color;
        gameplayUI.setPlayerColor(color);
        System.out.println("Connected as: " + color);
    }
}
