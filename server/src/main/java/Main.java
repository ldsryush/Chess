import chess.*;
import server.Server;

/**
 * Entry point for the chess server application.
 */
public class Main {
    public static void main(String[] args) {
        // Create and start the server on port 8080
        Server server = new Server();
        server.run(8080);
    }
}
