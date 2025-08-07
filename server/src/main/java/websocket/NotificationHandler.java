package websocket;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public interface NotificationHandler {

    // Send to one client
    void notify(ClientConnection recipient, NotificationMessage message);
    void loadGame(ClientConnection recipient, LoadGameMessage message);
    void error(ClientConnection recipient, ErrorMessage message);

    // Broadcast to others in the same game
    void notifyOthers(ClientConnection sender, NotificationMessage message); // ✅ Added this
    void notifyOthers(ClientConnection sender, LoadGameMessage message);

    // Broadcast to all in the game
    void notifyGame(int gameID, NotificationMessage message);
}
