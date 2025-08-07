package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Objects;

public class ClientConnection {
    private final String userName;
    private final Session session;
    private final String authToken;
    private static final Gson gson = new Gson();

    public ClientConnection(String userName, Session session, String authToken) {
        this.userName = userName;
        this.session = session;
        this.authToken = authToken;
    }

    public Session getSession() {
        return session;
    }

    public String getUserName() {
        return userName;
    }

    public String getAuthToken() {
        return authToken;
    }

    public boolean isOpen() {
        return session != null && session.isOpen();
    }

    public void send(String message) {
        if (isOpen()) {
            try {
                session.getRemote().sendString(message);
            } catch (IOException e) {
                System.err.println("Failed to send message to " + userName + ": " + e.getMessage());
            }
        } else {
            System.out.println("ℹ️ Skipping send — session closed for " + userName);
        }
    }

    public void send(ServerMessage message) {
        if (!isOpen()) {
            System.out.println("Skipping send — session closed for " + userName);
            return;
        }
        String json = gson.toJson(message);
        System.out.println("Sent to " + userName + ": " + json);
        send(json);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ClientConnection)) return false;
        ClientConnection other = (ClientConnection) obj;
        return Objects.equals(userName, other.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName);
    }
}
