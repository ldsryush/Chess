package websocket.commands;

import chess.ChessMove;
import chess.ChessPosition;

import java.util.Objects;

public class UserGameCommand {

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN,

        // New commands
        GET_VALID_MOVES,
        REDRAW,
        HELP
    }

    private final CommandType commandType;
    private final String authToken;
    private final Integer gameID;

    // Optional payloads (only one will be non-null per command)
    private final String desiredColor;
    private final ChessMove move;
    private final ChessPosition position;

    // Base ctor for no‐payload commands: CONNECT, LEAVE, RESIGN, REDRAW, HELP
    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this(commandType, authToken, gameID, null, null, null);
    }

    // CONNECT with desiredColor
    public UserGameCommand(CommandType commandType, String authToken, Integer gameID, String desiredColor) {
        this(commandType, authToken, gameID, desiredColor, null, null);
    }

    // MAKE_MOVE with ChessMove
    public UserGameCommand(CommandType commandType, String authToken, Integer gameID, ChessMove move) {
        this(commandType, authToken, gameID, null, move, null);
    }

    // GET_VALID_MOVES with ChessPosition
    public UserGameCommand(CommandType commandType, String authToken, Integer gameID, ChessPosition position) {
        this(commandType, authToken, gameID, null, null, position);
    }

    // Private unify-all constructor
    private UserGameCommand(CommandType commandType,
                            String authToken,
                            Integer gameID,
                            String desiredColor,
                            ChessMove move,
                            ChessPosition position) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
        this.desiredColor = desiredColor;
        this.move = move;
        this.position = position;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    /** Only for CONNECT(desiredColor) */
    public String getDesiredColor() {
        return desiredColor;
    }

    /** Only for MAKE_MOVE */
    public ChessMove getMove() {
        return move;
    }

    /** Only for GET_VALID_MOVES */
    public ChessPosition getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserGameCommand)) return false;
        UserGameCommand that = (UserGameCommand) o;
        return commandType == that.commandType &&
                Objects.equals(authToken, that.authToken) &&
                Objects.equals(gameID, that.gameID) &&
                Objects.equals(desiredColor, that.desiredColor) &&
                Objects.equals(move, that.move) &&
                Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandType, authToken, gameID, desiredColor, move, position);
    }
}
