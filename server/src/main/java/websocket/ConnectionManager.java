package websocket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import model.ServerMessage;

public class ConnectionManager {

    private final Map<Integer, List<ClientConnection>> gameConnections = new ConcurrentHashMap<>();
    private final Map<ClientConnection, Integer> connectionToGame = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

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

        String json = gson.toJson(message);
        synchronized (connections) {
            for (ClientConnection connection : connections) {
                sendMessage(connection, json);
            }
        }
    }

    public void broadcastToOthers(int gameID, ClientConnection sender, ServerMessage message) {
        List<ClientConnection> connections = gameConnections.get(gameID);
        if (connections == null) return;

        String json = gson.toJson(message);
        synchronized (connections) {
            for (ClientConnection connection : connections) {
                if (!connection.equals(sender)) {
                    sendMessage(connection, json);
                }
            }
        }
    }

    private void sendMessage(ClientConnection connection, String json) {
        if (connection.isOpen()) {
            connection.send(json);
        } else {
            System.err.println("Connection closed: unable to send message.");
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
