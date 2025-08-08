package websocket.messages;

import com.google.gson.*;
import java.lang.reflect.Type;

public class ServerMessageDeserializer implements JsonDeserializer<ServerMessage> {
    @Override
    public ServerMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();

        if (!obj.has("serverMessageType")) {
            throw new JsonParseException("Missing serverMessageType field");
        }

        String typeStr = obj.get("serverMessageType").getAsString();
        ServerMessage.ServerMessageType type;
        try {
            type = ServerMessage.ServerMessageType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Unknown serverMessageType: " + typeStr);
        }

        switch (type) {
            case LOAD_GAME:
                return context.deserialize(json, LoadGameMessage.class);
            case NOTIFICATION:
                return context.deserialize(json, NotificationMessage.class);
            case ERROR:
                return context.deserialize(json, ErrorMessage.class);
            default:
                throw new JsonParseException("Unhandled serverMessageType: " + type);
        }
    }
}
