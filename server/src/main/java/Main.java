import chess.*;
import server.Server;

/**
 * Entry point for the chess server application.
 */
public class Main {
    /**
     * Initializes and starts the server on the specified port.
     *
     * @param args command-line arguments (not used)
     */

    public static void main(String[] args) {
        final int defaultPort = 8080;
        Server chessServer = new Server();
        chessServer.run(defaultPort);
    }
}
