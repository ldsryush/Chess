package websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.Objects;

public class ClientConnection {
    private final String userName;
    private final Session session;

    public ClientConnection(String userName, Session session) {
        this.userName = userName;
        this.session = session;
    }

    public String getUserName() {
        return userName;
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
            System.err.println("Session closed for " + userName + ", message not sent.");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ClientConnection)) return false;
        ClientConnection other = (ClientConnection) obj;
        return Objects.equals(session, other.session);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session);
    }
}
