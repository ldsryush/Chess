package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import chess.ChessGame;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connectionManager = new ConnectionManager();
    private final Map<Session, ClientConnection> sessionMap = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("🔌 Client connected: " + session.getRemoteAddress());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String json) {
        System.out.println("📨 Received message: " + json);

        try {
            UserGameCommand command = gson.fromJson(json, UserGameCommand.class);
            ClientConnection connection = sessionMap.get(session);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, command);
                case MAKE_MOVE -> handleMove(connection, command);
                case RESIGN -> handleResign(connection, command);
                case LEAVE -> handleLeave(connection);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse command: " + e.getMessage());
            try {
                session.getRemote().sendString("Invalid command format");
            } catch (IOException ioException) {
                System.err.println("Failed to send error message: " + ioException.getMessage());
            }
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("🔌 Client disconnected: " + reason);
        handleLeave(sessionMap.get(session));
        sessionMap.remove(session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("⚠️ WebSocket error: " + error.getMessage());
        handleLeave(sessionMap.get(session));
        sessionMap.remove(session);
    }

    private void handleConnect(Session session, UserGameCommand command) {
        ClientConnection connection = new ClientConnection(command.getAuthToken(), session);
        sessionMap.put(session, connection);
        connectionManager.addConnection(command.getGameID(), connection);

        // Stubbed game object — replace with actual game retrieval
        ChessGame game = new ChessGame();
        LoadGameMessage loadGame = new LoadGameMessage(game, command.getAuthToken());
        connection.send(loadGame);

        NotificationMessage joinMsg = new NotificationMessage(command.getAuthToken() + " joined game " + command.getGameID());
        connectionManager.broadcastToOthers(command.getGameID(), connection, joinMsg);
    }

    private void handleMove(ClientConnection connection, UserGameCommand command) {
        if (connection == null) return;

        int gameID = connectionManager.getGameID(connection);

        // Stubbed move validation — replace with actual logic
        boolean valid = true;
        boolean checkmate = false;

        if (valid) {
            ChessGame updatedGame = new ChessGame(); // Replace with updated game state
            LoadGameMessage update = new LoadGameMessage(updatedGame, connection.getUserName());
            connectionManager.broadcastToGame(gameID, update);

            if (checkmate) {
                NotificationMessage notify = new NotificationMessage("Checkmate!");
                connectionManager.broadcastToGame(gameID, notify);
            }
        } else {
            ErrorMessage error = new ErrorMessage("Invalid move");
            connection.send(error);
        }
    }

    private void handleResign(ClientConnection connection, UserGameCommand command) {
        if (connection == null) return;

        int gameID = connectionManager.getGameID(connection);

        // Stubbed resign logic — replace with actual game state check
        boolean alreadyResigned = false;

        if (alreadyResigned) {
            connection.send(new ErrorMessage("You already resigned"));
        } else {
            NotificationMessage notify = new NotificationMessage(connection.getUserName() + " resigned");
            connectionManager.broadcastToGame(gameID, notify);
        }
    }

    private void handleLeave(ClientConnection connection) {
        if (connection == null) return;

        int gameID = connectionManager.getGameID(connection);
        connectionManager.removeConnection(connection);

        NotificationMessage leaveMsg = new NotificationMessage(connection.getUserName() + " left game " + gameID);
        connectionManager.broadcastToGame(gameID, leaveMsg);
    }
}
