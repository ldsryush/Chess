package websocket;

import chess.ChessGame;

public interface ClientNotificationHandler {
    void notify(String message);
    void updateGame(ChessGame game);
    void displayError(String error);
    void setPlayerColor(String color);
}