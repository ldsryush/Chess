package websocket;

import model.ServerMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connectionManager = new ConnectionManager();
    private final Map<Session, ClientConnection> sessionMap = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Client connected: " + session.getRemoteAddress());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("Received message: " + message);

        ServerMessage msg = parseMessage(message);

        switch (msg.getType()) {
            case "CONNECT":
                handleConnect(session, msg);
                break;
            case "MOVE":
            case "CHAT":
                handleBroadcast(session, msg);
                break;
            case "RESIGN":
            case "LEAVE":
                handleDisconnect(session);
                break;
            default:
                System.err.println("Unknown message type: " + msg.getType());
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Client disconnected: " + reason);
        handleDisconnect(session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
        handleDisconnect(session);
    }

    private ServerMessage parseMessage(String json) {
        try {
            return new com.google.gson.Gson().fromJson(json, ServerMessage.class);
        } catch (Exception e) {
            System.err.println("Failed to parse message: " + e.getMessage());
            return new ServerMessage("ERROR", "Invalid message format");
        }
    }

    private void handleConnect(Session session, ServerMessage msg) {
        String userName = msg.getPayload(); // assuming payload contains username
        int gameID = extractGameIDFromSession(session); // implement this as needed

        ClientConnection connection = new ClientConnection(userName, session);
        sessionMap.put(session, connection);
        connectionManager.addConnection(gameID, connection);

        ServerMessage joinMsg = new ServerMessage("JOIN", userName + " joined game " + gameID);
        connectionManager.broadcastToOthers(gameID, connection, joinMsg);
    }

    private void handleBroadcast(Session session, ServerMessage msg) {
        ClientConnection sender = sessionMap.get(session);
        if (sender == null) return;

        Integer gameID = connectionManager.getGameID(sender);
        if (gameID != null) {
            connectionManager.broadcastToOthers(gameID, sender, msg);
        }
    }

    private void handleDisconnect(Session session) {
        ClientConnection connection = sessionMap.remove(session);
        if (connection != null) {
            connectionManager.removeConnection(connection);
            Integer gameID = connectionManager.getGameID(connection);
            if (gameID != null) {
                ServerMessage leaveMsg = new ServerMessage("LEAVE", connection.getUserName() + " left game " + gameID);
                connectionManager.broadcastToGame(gameID, leaveMsg);
            }
        }
    }

    private int extractGameIDFromSession(Session session) {
        return 1;
    }
}
