package client;

import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;
import model.UserData;
import model.AuthData;
import exception.ResponseException;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void sampleTest() throws ResponseException {
        UserData user = new UserData("testUser", "testPass", "test@example.com");
        AuthData auth = facade.registerUser(user);
        Assertions.assertNotNull(auth);
        Assertions.assertEquals("testUser", auth.username());
    }
}
