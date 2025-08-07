package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import websocket.messages.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketFacade {

    private final Map<Integer, ChessGame> games = new ConcurrentHashMap<>();
    private final Map<Integer, Boolean> resigned = new ConcurrentHashMap<>();

    public LoadGameMessage handleConnect(String playerColor, int gameID) {
        games.putIfAbsent(gameID, new ChessGame());
        ChessGame game = games.get(gameID);
        return new LoadGameMessage(game, playerColor);
    }

    public ServerMessage handleMove(int gameID, ChessMove move, String playerColor) {
        ChessGame game = games.get(gameID);
        if (game == null) {
            return new ErrorMessage("Game not found");
        }

        if (resigned.getOrDefault(gameID, false)) {
            return new ErrorMessage("Game is over. Cannot make moves.");
        }

        try {
            game.makeMove(move);
            boolean isCheckmate = game.isGameOver(); // or use your own checkmate logic

            if (isCheckmate) {
                resigned.put(gameID, true);
                return new NotificationMessage("Checkmate!");
            }

            return new LoadGameMessage(game, playerColor);
        } catch (Exception e) {
            return new ErrorMessage("Invalid move: " + e.getMessage());
        }
    }

    public ServerMessage handleResign(int gameID, String playerName) {
        if (resigned.getOrDefault(gameID, false)) {
            return new ErrorMessage("Game already ended.");
        }

        resigned.put(gameID, true);
        return new NotificationMessage(playerName + " resigned");
    }

    public NotificationMessage handleLeave(int gameID, String playerName) {
        return new NotificationMessage(playerName + " left game " + gameID);
    }

    public ChessGame getGame(int gameID) {
        return games.get(gameID);
    }

    public boolean isGameOver(int gameID) {
        return resigned.getOrDefault(gameID, false);
    }
}