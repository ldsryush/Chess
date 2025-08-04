package model;

public class ServerMessage {
    private String type;
    private String payload;

    public ServerMessage() {}

    public ServerMessage(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "ServerMessage{" +
                "type='" + type + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
