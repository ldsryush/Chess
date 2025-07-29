package server;

import com.google.gson.Gson;
import dataaccess.*;
import dataaccess.mysql.MySQLAuthDAO;
import dataaccess.mysql.MySQLGameDAO;
import dataaccess.mysql.MySQLUserDAO;
import exception.ErrorMessage;
import exception.ResponseException;
import model.*;
import service.*;
import spark.*;

import java.util.Collection;

public class Server {

    private RegistrationService registrationService;
    private LoginService loginService;
    private LogoutService logoutService;
    private ListService listService;
    private JoinService joinService;
    private GameService gameService;
    private ClearService clearService;
    private AuthenticationService authService;

    public Server() {
        try {
            AuthDAO authDAO = new MySQLAuthDAO();
            GameDAO gameDAO = new MySQLGameDAO();
            UserDAO userDAO = new MySQLUserDAO();

            registrationService = new RegistrationService(userDAO, authDAO);
            loginService = new LoginService(userDAO, authDAO);
            logoutService = new LogoutService(authDAO);
            listService = new ListService(gameDAO);
            joinService = new JoinService(gameDAO);
            gameService = new GameService(gameDAO);
            clearService = new ClearService(userDAO, authDAO, gameDAO);
            authService = new AuthenticationService(authDAO);
        } catch (ResponseException ex) {
            System.out.printf("Unable to connect to database: %s%n", ex.getMessage());
            throw new RuntimeException("Server initialization failed by database error", ex);
        }
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");
        Spark.init();

        Spark.post("/user", this::registrationHandler);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);
        Spark.get("/game", this::getGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearApp);

        Spark.exception(ResponseException.class, this::responseExceptionHandler);
        Spark.exception(DataAccessException.class, this::dataExceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public int port() {
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
    }

    private void responseExceptionHandler(ResponseException e, Request request, Response response) {
        response.status(e.getStatusCode());
        response.body(new Gson().toJson(new ErrorMessage(e.getMessage())));
    }

    private void dataExceptionHandler(DataAccessException e, Request request, Response response) {
        response.status(500);
        // Prefix with "Error: " so the test sees the word "Error"
        String msg = "Error: " + e.getMessage();
        response.body(new Gson().toJson(new ErrorMessage(msg)));
    }

    private Object registrationHandler(Request request, Response response)
            throws ResponseException, DataAccessException {
        response.type("application/json");
        var user = new Gson().fromJson(request.body(), RegistrationRequest.class);
        AuthData authData = registrationService.registerUser(user);
        response.status(200);
        return new Gson().toJson(authData);
    }

    private Object loginUser(Request request, Response response)
            throws ResponseException, DataAccessException {
        response.type("application/json");
        var user = new Gson().fromJson(request.body(), LoginRequest.class);
        AuthData authData = loginService.login(user);
        response.status(200);
        return new Gson().toJson(authData);
    }

    private Object logoutUser(Request request, Response response)
            throws ResponseException, DataAccessException {
        response.type("application/json");
        var authToken = new LogoutRequest(request.headers("authorization"));
        authService.authenticate(authToken.authToken());
        logoutService.logoutUser(authToken);
        response.status(200);
        return "{}";
    }

    private Object getGames(Request request, Response response)
            throws ResponseException, DataAccessException {
        var authToken = request.headers("authorization");
        authService.authenticate(authToken);
        Collection<GameResponseData> allGames = listService.getGames();
        response.status(200);
        return new Gson().toJson(new ListGamesResponse(allGames));
    }

    private Object joinGame(Request request, Response response)
            throws ResponseException, DataAccessException {
        var authToken = request.headers("authorization");
        authService.authenticate(authToken);
        AuthData authData = authService.getAuthData(authToken);
        var joinInfo = new Gson().fromJson(request.body(), JoinGameRequest.class);
        joinService.joinGame(joinInfo, authData);
        response.status(200);
        return "{}";
    }

    private Object createGame(Request request, Response response)
            throws ResponseException, DataAccessException {
        var authToken = request.headers("authorization");
        authService.authenticate(authToken);
        var newGame = new Gson().fromJson(request.body(), CreateGameRequest.class);
        GameID gameID = gameService.createGame(newGame);
        response.status(200);
        return new Gson().toJson(gameID);
    }

    private Object clearApp(Request request, Response response)
            throws ResponseException, DataAccessException {
        clearService.clearDatabase();
        response.status(200);
        return "{}";
    }
}
