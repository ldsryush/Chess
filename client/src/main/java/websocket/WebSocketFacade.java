package websocket;

import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessageDeserializer;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebSocketFacade {

    private Session session;
    private final ClientNotificationHandler notificationHandler;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ServerMessage.class, new ServerMessageDeserializer())
            .setPrettyPrinting()
            .create();

    public WebSocketFacade(String url, ClientNotificationHandler notificationHandler) throws Exception {
        this.notificationHandler = notificationHandler;

        URI socketURI = new URI(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, socketURI);
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("WebSocket connection opened");
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            ServerMessage serverMessage = GSON.fromJson(message, ServerMessage.class);

            switch (serverMessage.getServerMessageType()) {
                case LOAD_GAME -> {
                    LoadGameMessage loadGame = GSON.fromJson(message, LoadGameMessage.class);
                    if (loadGame.getPlayerColor() != null) {
                        notificationHandler.setPlayerColor(loadGame.getPlayerColor());
                    }
                    if (loadGame.getGame() != null) {
                        notificationHandler.updateGame(loadGame.getGame());
                    } else {
                        notificationHandler.displayError("Received empty game data");
                    }
                }
                case NOTIFICATION -> {
                    NotificationMessage notification = GSON.fromJson(message, NotificationMessage.class);
                    notificationHandler.notify(notification.getMessage());
                }
                case ERROR -> {
                    ErrorMessage error = GSON.fromJson(message, ErrorMessage.class);
                    notificationHandler.displayError(error.getErrorMessage());
                }
            }
        } catch (Exception e) {
            notificationHandler.displayError("Error processing server message: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error: " + throwable.getMessage());
    }

    public void connect(String authToken, int gameID) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
    }

    public void connect(String authToken, int gameID, String desiredColor) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, desiredColor));
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move));
    }

    public void resign(String authToken, int gameID) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID));
    }

    public void leave(String authToken, int gameID) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
    }

    public void getValidMoves(String authToken, int gameID, ChessPosition position) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.GET_VALID_MOVES, authToken, gameID, position));
    }

    public void redrawBoard(String authToken, int gameID) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.REDRAW, authToken, gameID));
    }

    public void requestHelp(String authToken, int gameID) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.HELP, authToken, gameID));
    }

    private void sendCommand(UserGameCommand command) throws IOException {
        if (session != null && session.isOpen()) {
            String json = GSON.toJson(command);
            session.getBasicRemote().sendText(json);
        }
    }

    public void close() throws IOException {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}
