package websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final Map<Session, ClientConnection> sessionToConnection = new ConcurrentHashMap<>();
    private final Map<String, ClientConnection> tokenToConnection = new ConcurrentHashMap<>();
    private final Map<Integer, List<ClientConnection>> gameToConnections = new ConcurrentHashMap<>();
    private final Set<Session> removedSessions = ConcurrentHashMap.newKeySet();

    public void addConnection(int gameID, ClientConnection connection) {
        Session session = connection.getSession();
        sessionToConnection.put(session, connection);
        tokenToConnection.put(connection.getAuthToken(), connection);
        gameToConnections
                .computeIfAbsent(gameID, k -> new ArrayList<>())
                .add(connection);

        System.out.println("📌 Added connection for " + connection.getUserName()
                + " (" + connection.getPlayerColor() + ") to game " + gameID);
    }

    public ClientConnection getConnection(Session session) {
        return sessionToConnection.get(session);
    }

    public ClientConnection getConnection(String authToken) {
        return tokenToConnection.get(authToken);
    }

    public List<ClientConnection> getConnectionsForGame(int gameID) {
        return gameToConnections.getOrDefault(gameID, Collections.emptyList());
    }

    public void removeConnection(Session session) {
        if (!removedSessions.add(session)) return;

        ClientConnection conn = sessionToConnection.remove(session);
        if (conn == null) return;

        tokenToConnection.remove(conn.getAuthToken());
        gameToConnections.values().forEach(list -> list.remove(conn));
    }

    public void broadcastToGame(int gameID, ServerMessage message) {
        List<ClientConnection> conns = gameToConnections.get(gameID);
        if (conns != null) {
            for (ClientConnection c : conns) {
                c.send(message);
            }
        }
    }

    public void broadcastToOthers(int gameID,
                                  ClientConnection sender,
                                  ServerMessage message) {
        List<ClientConnection> conns = gameToConnections.get(gameID);
        if (conns != null) {
            for (ClientConnection c : conns) {
                if (!c.equals(sender)) {
                    c.send(message);
                }
            }
        }
    }
}
