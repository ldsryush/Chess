package websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessageDeserializer;

public class GsonFactory {
    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ServerMessage.class, new ServerMessageDeserializer())
            .create();
}
