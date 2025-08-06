package websocket.messages;

public class NotificationMessage extends ServerMessage {

    private final String message;

    public NotificationMessage(String message) {
        super(ServerMessage.ServerMessageType.NOTIFICATION);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
