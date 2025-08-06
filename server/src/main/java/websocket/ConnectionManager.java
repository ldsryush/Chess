package websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final Map<Integer, List<ClientConnection>> gameConnections = new ConcurrentHashMap<>();
    private final Map<Session, Integer> sessionToGame = new ConcurrentHashMap<>();
    private final Map<Session, ClientConnection> sessionToConnection = new ConcurrentHashMap<>();

    public void addConnection(int gameID, ClientConnection connection) {
        Session session = connection.getSession();

        gameConnections
                .computeIfAbsent(gameID, id -> Collections.synchronizedList(new ArrayList<>()))
                .add(connection);

        sessionToGame.put(session, gameID);
        sessionToConnection.put(session, connection);
    }

    public void removeConnection(Session session) {
        ClientConnection connection = sessionToConnection.remove(session);
        Integer gameID = sessionToGame.remove(session);

        if (connection != null && gameID != null) {
            List<ClientConnection> connections = gameConnections.get(gameID);
            if (connections != null) {
                connections.remove(connection);
                if (connections.isEmpty()) {
                    gameConnections.remove(gameID);
                }
            }
        }
    }

    public void broadcastToGame(int gameID, ServerMessage message) {
        List<ClientConnection> connections = gameConnections.get(gameID);
        if (connections == null) return;

        synchronized (connections) {
            for (ClientConnection connection : connections) {
                if (connection.isOpen()) {
                    connection.send(message);
                } else {
                    System.err.println("Connection closed: unable to send message to " + connection.getUserName());
                }
            }
        }
    }

    public void broadcastToOthers(int gameID, ClientConnection sender, ServerMessage message) {
        List<ClientConnection> connections = gameConnections.get(gameID);
        if (connections == null) return;

        synchronized (connections) {
            for (ClientConnection connection : connections) {
                if (!connection.equals(sender) && connection.isOpen()) {
                    connection.send(message);
                }
            }
        }
    }

    public Integer getGameID(ClientConnection connection) {
        return sessionToGame.get(connection.getSession());
    }

    public ClientConnection getConnection(Session session) {
        return sessionToConnection.get(session);
    }

    public Set<Integer> getActiveGameIDs() {
        return gameConnections.keySet();
    }

    public List<ClientConnection> getConnectionsInGame(int gameID) {
        return gameConnections.getOrDefault(gameID, Collections.emptyList());
    }
}
