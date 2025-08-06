package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import chess.ChessGame;
import model.AuthData;
import service.AuthenticationService;
import service.GameService;
import service.JoinService;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connectionManager = new ConnectionManager();
    private final Map<Session, ClientConnection> sessionMap = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    private final GameService gameService;
    private final AuthenticationService authService;
    private final JoinService joinService;

    public WebSocketHandler(GameService gameService, AuthenticationService authService, JoinService joinService) {
        this.gameService = gameService;
        this.authService = authService;
        this.joinService = joinService;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Client connected: " + session.getRemoteAddress());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String json) {
        System.out.println("Received message: " + json);

        try {
            UserGameCommand command = gson.fromJson(json, UserGameCommand.class);
            ClientConnection connection = sessionMap.get(session);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, command);
                case MAKE_MOVE -> handleMove(connection, command);
                case RESIGN -> handleResign(connection);
                case LEAVE -> handleLeave(connection);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse command: " + e.getMessage());
            sendRaw(session, gson.toJson(new ErrorMessage("Invalid command format")));
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Client disconnected: " + reason);
        handleLeave(sessionMap.get(session));
        sessionMap.remove(session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
        handleLeave(sessionMap.get(session));
        sessionMap.remove(session);
    }

    private void handleConnect(Session session, UserGameCommand command) {
        try {
            AuthData authData = authService.getAuthData(command.getAuthToken());
            if (authData == null) {
                sendRaw(session, gson.toJson(new ErrorMessage("Invalid auth token")));
                return;
            }

            String username = authData.username();
            ClientConnection connection = new ClientConnection(username, session);
            sessionMap.put(session, connection);
            connectionManager.addConnection(command.getGameID(), connection);

            var gameData = gameService.getGameData(command.getGameID());
            ChessGame game = gameData.game();

            String playerColor;
            if (username.equals(gameData.whiteUsername())) {
                playerColor = "WHITE";
            } else if (username.equals(gameData.blackUsername())) {
                playerColor = "BLACK";
            } else {
                playerColor = "OBSERVER";
            }

            LoadGameMessage loadGame = new LoadGameMessage(game, playerColor);
            connection.send(loadGame);

            NotificationMessage joinMsg = new NotificationMessage(username + " joined game " + command.getGameID());
            connectionManager.broadcastToOthers(command.getGameID(), connection, joinMsg);

        } catch (Exception e) {
            sendRaw(session, gson.toJson(new ErrorMessage("Connect failed: " + e.getMessage())));
        }
    }

    private void handleMove(ClientConnection connection, UserGameCommand command) {
        if (connection == null) return;

        int gameID = connectionManager.getGameID(connection);
        String username = connection.getUserName();

        try {
            AuthData authData = authService.getAuthData(command.getAuthToken());
            if (authData == null || !authData.username().equals(username)) {
                connection.send(new ErrorMessage("Invalid auth token"));
                return;
            }

            ChessGame game = gameService.getGameData(gameID).game();
            var currentTurn = game.getTeamTurn();
            var whitePlayer = gameService.getGameData(gameID).whiteUsername();
            var blackPlayer = gameService.getGameData(gameID).blackUsername();

            boolean isTurnValid = (currentTurn == ChessGame.TeamColor.WHITE && username.equals(whitePlayer)) ||
                    (currentTurn == ChessGame.TeamColor.BLACK && username.equals(blackPlayer));

            if (!isTurnValid) {
                connection.send(new ErrorMessage("Invalid move: not your turn"));
                return;
            }

            gameService.makeMove(gameID, username, command.getMove());

            ChessGame updatedGame = gameService.getGameData(gameID).game();
            var start = command.getMove().getStartPosition();
            var end = command.getMove().getEndPosition();
            var movedPiece = updatedGame.getBoard().getPiece(end);

            LoadGameMessage update = new LoadGameMessage(updatedGame, username);
            connectionManager.broadcastToGame(gameID, update);

            String moveText = String.format("%s moved %s from %s to %s",
                    username,
                    movedPiece != null ? movedPiece.getPieceType() : "a piece",
                    start.toString(),
                    end.toString());

            NotificationMessage notify = new NotificationMessage(moveText);
            connectionManager.broadcastToOthers(gameID, connection, notify);

        } catch (Exception e) {
            connection.send(new ErrorMessage("Invalid move: " + e.getMessage()));
        }
    }

    private void handleResign(ClientConnection connection) {
        if (connection == null) return;

        int gameID = connectionManager.getGameID(connection);
        String username = connection.getUserName();

        try {
            var gameData = gameService.getGameData(gameID);
            var whitePlayer = gameData.whiteUsername();
            var blackPlayer = gameData.blackUsername();
            var game = gameData.game();

            if (!username.equals(whitePlayer) && !username.equals(blackPlayer)) {
                connection.send(new ErrorMessage("Only players can resign"));
                return;
            }

            if (game.isGameOver()) {
                connection.send(new ErrorMessage("Game is already over"));
                return;
            }

            gameService.resignPlayer(gameID, username);
            NotificationMessage notify = new NotificationMessage(username + " resigned");
            connectionManager.broadcastToGame(gameID, notify);

        } catch (Exception e) {
            connection.send(new ErrorMessage("Resign failed: " + e.getMessage()));
        }
    }

    private void handleLeave(ClientConnection connection) {
        if (connection == null) return;

        int gameID = connectionManager.getGameID(connection);
        String username = connection.getUserName();

        connectionManager.removeConnection(connection);

        try {
            var gameData = gameService.getGameData(gameID);
            var whitePlayer = gameData.whiteUsername();
            var blackPlayer = gameData.blackUsername();

            if (username.equals(whitePlayer)) {
                gameService.clearPlayerColor(gameID, ChessGame.TeamColor.WHITE);
            } else if (username.equals(blackPlayer)) {
                gameService.clearPlayerColor(gameID, ChessGame.TeamColor.BLACK);
            }

        } catch (Exception e) {
            System.err.println("Failed to clear player color: " + e.getMessage());
        }

        NotificationMessage leaveMsg = new NotificationMessage(username + " left game " + gameID);
        connectionManager.broadcastToGame(gameID, leaveMsg);
    }


    private void sendRaw(Session session, String json) {
        try {
            session.getRemote().sendString(json);
        } catch (IOException e) {
            System.err.println("Failed to send raw message: " + e.getMessage());
        }
    }
}
