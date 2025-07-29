package client;

import org.junit.jupiter.api.*;
import server.Server;
import model.ServerFacade;
import model.UserData;
import model.AuthData;
import model.GameName;
import model.JoinGameRequest;
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
    @Test
    void registerUser_positive() throws ResponseException {
        var user = new UserData("regUser", "pass", "email@example.com");
        var auth = facade.registerUser(user);
        Assertions.assertEquals("regUser", auth.username());
    }

    @Test
    void registerUser_negative() {
        var user = new UserData("", "", "bad email");
        Assertions.assertThrows(ResponseException.class, () -> facade.registerUser(user));
    }

    @Test
    void loginUser_positive() throws ResponseException {
        var user = new UserData("loginUser", "pass", "email@example.com");
        facade.registerUser(user);
        var auth = facade.loginUser(user);
        Assertions.assertEquals("loginUser", auth.username());
    }

    @Test
    void loginUser_negative() {
        var user = new UserData("unknown", "wrong", "x@example.com");
        Assertions.assertThrows(ResponseException.class, () -> facade.loginUser(user));
    }

    @Test
    void logoutUser_positive() throws ResponseException {
        var user = new UserData("logoutUser", "pass", "e@example.com");
        facade.registerUser(user);
        facade.loginUser(user);
        facade.logoutUser();  // Should succeed without exception
    }

    @Test
    void logoutUser_negative() {
        Assertions.assertThrows(ResponseException.class, () -> facade.logoutUser());
    }

    @Test
    void createGame_positive() throws ResponseException {
        var gameID = facade.createGame(new GameName("Battle of Wits"));
        Assertions.assertNotNull(gameID);
    }

    @Test
    void createGame_negative() {
        Assertions.assertThrows(ResponseException.class, () -> facade.createGame(new GameName("")));
    }

    @Test
    void listGames_positive() throws ResponseException {
        var games = facade.listGames();
        Assertions.assertNotNull(games);
    }

    @Test
    void listGames_negative() {
        Assertions.assertThrows(ResponseException.class, () -> facade.listGames());
    }

    @Test
    void joinGame_positive() throws ResponseException {
        var auth = facade.registerUser(new UserData("joinUser", "pass", "e@x.com"));
        var gameID = facade.createGame(new GameName("Tactical Showdown"));
        var request = new JoinGameRequest("WHITE", gameID.gameID());
        facade.joinGame(request);
    }

    @Test
    void joinGame_negative() throws ResponseException {
        var request = new JoinGameRequest("WHITE", 9999); // Invalid game ID
        Assertions.assertThrows(ResponseException.class, () -> facade.joinGame(request));
    }

    @Test
    void clear_positive() throws ResponseException {
        var user = new UserData("clearUser", "pass", "clear@example.com");
        facade.registerUser(user);
        facade.loginUser(user);
        facade.createGame(new GameName("ToBeDeleted"));
        facade.clear();
        facade.registerUser(user);
        facade.loginUser(user);

        var games = facade.listGames();
        Assertions.assertTrue(games.isEmpty());
    }


    @Test
    void clear_negative() throws ResponseException {
        facade.clear();
        facade.clear();
    }

}
