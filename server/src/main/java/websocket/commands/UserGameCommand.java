package websocket.commands;

import chess.ChessMove;

/**
 * Represents a command sent by a client over WebSocket.
 * Supports CONNECT, MAKE_MOVE, RESIGN, and LEAVE.
 */
public class UserGameCommand {

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        RESIGN,
        LEAVE
    }

    private CommandType commandType;
    private String authToken;
    private int gameID;
    private ChessMove move;

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public int getGameID() {
        return gameID;
    }

    public ChessMove getMove() {
        return move;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public void setMove(ChessMove move) {
        this.move = move;
    }
}
