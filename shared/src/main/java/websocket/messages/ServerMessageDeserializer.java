package websocket.messages;

import com.google.gson.*;
import java.lang.reflect.Type;

public class ServerMessageDeserializer implements JsonDeserializer<ServerMessage> {

    @Override
    public ServerMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();
        String typeStr = obj.get("serverMessageType").getAsString();
        ServerMessage.ServerMessageType type = ServerMessage.ServerMessageType.valueOf(typeStr);

        return switch (type) {
            case LOAD_GAME -> context.deserialize(json, LoadGameMessage.class);
            case NOTIFICATION -> context.deserialize(json, NotificationMessage.class);
            case ERROR -> context.deserialize(json, ErrorMessage.class);
        };
    }
}
