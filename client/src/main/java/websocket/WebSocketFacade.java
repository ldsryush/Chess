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
public class WebSocketFacade extends Endpoint {

    private Session session;
    private final ClientNotificationHandler notificationHandler;
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ServerMessage.class, new ServerMessageDeserializer())
            .create();

    public WebSocketFacade(String url, ClientNotificationHandler notificationHandler) throws Exception {
        this.notificationHandler = notificationHandler;

        URI socketURI = new URI(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, socketURI);

        this.session.addMessageHandler((MessageHandler.Whole<String>) this::handleServerMessage);
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        System.out.println("WebSocket connection opened");
    }

    // Existing commands
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

    private void handleServerMessage(String message) {
        try {
            ServerMessage serverMessage = GSON.fromJson(message, ServerMessage.class);

            switch (serverMessage.getServerMessageType()) {
                case LOAD_GAME -> {
                    LoadGameMessage loadGame = GSON.fromJson(message, LoadGameMessage.class);
                    System.out.println("Received LOAD_GAME message"); // Add this
                    notificationHandler.updateGame(loadGame.getGame());
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

    public void close() throws IOException {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}
