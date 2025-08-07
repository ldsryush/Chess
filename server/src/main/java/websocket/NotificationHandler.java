package websocket;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public interface NotificationHandler {

    // Send to one client
    void loadGame(ClientConnection recipient, LoadGameMessage message);
    void error(ClientConnection recipient, ErrorMessage message);

    // Broadcast to others in the same game
    void notifyOthers(ClientConnection sender, NotificationMessage message);
    void notifyOthers(ClientConnection sender, LoadGameMessage message);

    // Broadcast to all in the game
    void notifyGame(int gameID, NotificationMessage message);
}