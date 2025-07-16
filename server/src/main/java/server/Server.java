package server;

import com.google.gson.Gson;
import dataAccess.*;
import dataAccess.memory.*;
import exception.*;
import handlers.*;
import model.*;
import Service.*;
import spark.*;

import java.util.Collection;

public class Server {
    private static final Gson gson = new Gson();

    // DAOs
    private final DataAccess dataAccess = new DataAccess(DataAccess.DataLocation.MEMORY);
    private final AuthDAO authDAO = dataAccess.getAuthDAO();
    private final UserDAO userDAO = dataAccess.getUserDAO();
    private final GameDAO gameDAO = dataAccess.getGameDAO();

    // Services
    private final RegistrationService registrationService = new RegistrationService(userDAO, authDAO);
    private final LoginService loginService = new LoginService(userDAO, authDAO);
    private final LogoutService logoutService = new LogoutService(authDAO);
    private final ListService listService = new ListService(gameDAO);
    private final JoinService joinService = new JoinService(gameDAO);
    private final GameService gameService = new GameService(gameDAO);
    private final ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
    private final AuthenticationService authService = new AuthenticationService(authDAO);

    public Server() {}

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");
        configureRoutes();
        Spark.awaitInitialization();
        System.out.println("Server running on port " + Spark.port());
        return Spark.port();
    }

    public int port() {
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
    }

    // 🔧 Route Configuration
    private void configureRoutes() {
        Spark.post("/user", this::registrationHandler);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);
        Spark.get("/game", this::getGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearApp);
        Spark.exception(ResponseException.class, this::exceptionHandler);
    }

    // 🔐 Helper for Authorization Header
    private String getAuthToken(Request request) {
        return request.headers("authorization");
    }

    // ⚠️ Exception Handler
    private void exceptionHandler(ResponseException e, Request request, Response response) {
        response.status(e.getStatusCode());
        response.body(gson.toJson(new ErrorMessage(e.getMessage())));
    }

    // 🧾 Handlers
    private Object registrationHandler(Request request, Response response) throws ResponseException {
        response.type("application/json");
        var user = gson.fromJson(request.body(), RegistrationRequest.class);
        AuthData authData = registrationService.registerUser(user);
        response.status(200);
        return gson.toJson(authData);
    }

    private Object loginUser(Request request, Response response) throws ResponseException {
        response.type("application/json");
        var user = gson.fromJson(request.body(), LoginRequest.class);
        AuthData authData = loginService.login(user);
        response.status(200);
        return gson.toJson(authData);
    }

    private Object logoutUser(Request request, Response response) throws ResponseException {
        response.type("application/json");
        var authToken = new LogoutRequest(getAuthToken(request));
        logoutService.logoutUser(authToken);
        response.status(200);
        return "";
    }

    private Object getGames(Request request, Response response) throws ResponseException {
        authService.authenticate(getAuthToken(request));
        Collection<GameResponseData> allGames = listService.getGames();
        response.status(200);
        return gson.toJson(new ListGamesResponse(allGames));
    }

    private Object joinGame(Request request, Response response) throws ResponseException {
        String authToken = getAuthToken(request);
        authService.authenticate(authToken);
        AuthData authData = authService.getAuthData(authToken);
        var joinInfo = gson.fromJson(request.body(), JoinGameRequest.class);
        joinService.joinGame(joinInfo, authData);
        response.status(200);
        return "";
    }

    private Object createGame(Request request, Response response) throws ResponseException {
        authService.authenticate(getAuthToken(request));
        var newGame = gson.fromJson(request.body(), CreateGameRequest.class);
        GameID gameID = gameService.createGame(newGame);
        response.status(200);
        return gson.toJson(gameID);
    }

    private Object clearApp(Request request, Response response) {
        clearService.clearDatabase();
        response.status(200);
        return "";
    }
}