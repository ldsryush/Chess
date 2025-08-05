package websocket;

import websocket.messages.ServerMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final Map<Integer, List<ClientConnection>> gameConnections = new ConcurrentHashMap<>();
    private final Map<ClientConnection, Integer> connectionToGame = new ConcurrentHashMap<>();

    public void addConnection(int gameID, ClientConnection connection) {
        gameConnections
                .computeIfAbsent(gameID, id -> Collections.synchronizedList(new ArrayList<>()))
                .add(connection);
        connectionToGame.put(connection, gameID);
    }

    public void removeConnection(ClientConnection connection) {
        Integer gameID = connectionToGame.remove(connection);
        if (gameID != null) {
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
        return connectionToGame.get(connection);
    }

    public Set<Integer> getActiveGameIDs() {
        return gameConnections.keySet();
    }

    public List<ClientConnection> getConnectionsInGame(int gameID) {
        return gameConnections.getOrDefault(gameID, Collections.emptyList());
    }
}
