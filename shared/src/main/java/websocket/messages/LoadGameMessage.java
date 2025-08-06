package websocket.messages;

import chess.ChessGame;
import com.google.gson.annotations.SerializedName;

public class LoadGameMessage extends ServerMessage {

    @SerializedName("game")
    private final ChessGame game;

    @SerializedName("playerColor")
    private final String playerColor;

    public LoadGameMessage(ChessGame game, String playerColor) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
        this.playerColor = playerColor;
    }

    public ChessGame getGame() {
        return game;
    }

    public String getPlayerColor() {
        return playerColor;
    }
}
