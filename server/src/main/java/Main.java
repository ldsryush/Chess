import chess.*;
import chess.server.Server;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        server.run(8080);
    }
}