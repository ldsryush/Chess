package websocket;

import websocket.messages.ServerMessage;
import org.eclipse.jetty.websocket.api.Session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final Map<Session, ClientConnection> sessionToConnection = new ConcurrentHashMap<>();
    private final Map<String, ClientConnection> tokenToConnection = new ConcurrentHashMap<>();
    private final Map<Integer, List<ClientConnection>> gameToConnections = new ConcurrentHashMap<>();
    private final Set<Session> removedSessions = ConcurrentHashMap.newKeySet(); // Prevent double cleanup

    public void addConnection(int gameID, ClientConnection connection) {
        Session session = connection.getSession();
        sessionToConnection.put(session, connection);
        tokenToConnection.put(connection.getAuthToken(), connection);
        gameToConnections.computeIfAbsent(gameID, k -> new ArrayList<>()).add(connection);
        System.out.println("Added connection for " + connection.getUserName() + " to game " + gameID);
    }

    public ClientConnection getConnection(Session session) {
        System.out.println("🔍 Looking up session: " + session);
        return sessionToConnection.get(session);
    }

    public ClientConnection getConnection(String authToken) {
        ClientConnection conn = tokenToConnection.get(authToken);
        System.out.println(conn != null ? "Found connection for token: " + authToken
                : "No connection found for token: " + authToken);
        return conn;
    }

    public int getGameID(ClientConnection connection) {
        for (var entry : gameToConnections.entrySet()) {
            if (entry.getValue().contains(connection)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public void removeConnection(Session session) {
        if (removedSessions.contains(session)) {
            System.out.println("Skipping removal — session already cleaned up.");
            return;
        }
        removedSessions.add(session);

        ClientConnection connection = sessionToConnection.remove(session);
        if (connection == null) {
            System.out.println("Skipping removal — no connection found for session.");
            return;
        }

        tokenToConnection.remove(connection.getAuthToken());

        for (var connections : gameToConnections.values()) {
            connections.remove(connection);
        }
    }

    public void broadcastToGame(int gameID, ServerMessage message) {
        List<ClientConnection> connections = gameToConnections.get(gameID);
        if (connections != null) {
            System.out.println("Broadcasting to all in game " + gameID + ": " + message);
            for (ClientConnection conn : connections) {
                conn.send(message);
            }
        } else {
            System.out.println("No connections found for game " + gameID);
        }
    }

    public void broadcastToOthers(int gameID, ClientConnection sender, ServerMessage message) {
        List<ClientConnection> connections = gameToConnections.get(gameID);
        if (connections != null) {
            System.out.println("Broadcasting to others in game " + gameID + ": " + message);
            for (ClientConnection conn : connections) {
                if (!conn.equals(sender)) {
                    System.out.println("➡️ Sending to: " + conn.getUserName());
                    conn.send(message);
                }
            }
        } else {
            System.out.println("No connections found for game " + gameID);
        }
    }
}
