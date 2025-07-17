package server;

import com.google.gson.Gson;
import dataAccess.*;
import exception.*;
import handlers.*;
import model.*;
import Service.*;
import spark.*;

import java.util.Collection;

/**
 * Main server class that configures HTTP routes and handles incoming requests.
 */
public class Server {
    private static final Gson gson = new Gson();

    // Data Access Layer
    private final DataAccess dataAccess = new DataAccess(DataAccess.DataLocation.MEMORY);
    private final AuthDAO authDAO = dataAccess.getAuthDAO();
    private final UserDAO userDAO = dataAccess.getUserDAO();
    private final GameDAO gameDAO = dataAccess.getGameDAO();

    // Service Layer
    private final RegistrationService registrationService = new RegistrationService(userDAO, authDAO);
    private final LoginService loginService = new LoginService(userDAO, authDAO);
    private final LogoutService logoutService = new LogoutService(authDAO);
    private final ListService listService = new ListService(gameDAO);
    private final JoinService joinService = new JoinService(gameDAO);
    private final GameService gameService = new GameService(gameDAO);
    private final ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
    private final AuthenticationService authService = new AuthenticationService(authDAO);

    public Server() {}

    /**
     * Starts the Spark server on the specified port and configures routes.
     *
     * @param desiredPort the port to run the server on
     * @return the actual port the server is running on
     */
    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web"); // Serves static files from /web
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

    /**
     * Configures all HTTP routes and exception handlers.
     */
    private void configureRoutes() {
        configureUserRoutes();
        configureGameRoutes();
        configureAdminRoutes();
        Spark.exception(ResponseException.class, this::handleException);
    }

    private void configureUserRoutes() {
        Spark.post("/user", this::handleUserRegistration);
        Spark.post("/session", this::handleUserLogin);
        Spark.delete("/session", this::handleUserLogout);
    }

    private void configureGameRoutes() {
        Spark.get("/game", this::handleListGames);
        Spark.post("/game", this::handleCreateGame);
        Spark.put("/game", this::handleJoinGame);
    }

    private void configureAdminRoutes() {
        Spark.delete("/db", this::handleClearDatabase);
    }

    private String extractAuthToken(Request request) {
        return request.headers("authorization");
    }

    private void handleException(ResponseException e, Request request, Response response) {
        response.status(e.getStatusCode());
        response.body(gson.toJson(new ErrorMessage(e.getMessage())));
    }

    // Route Handlers

    private Object handleUserRegistration(Request request, Response response) throws ResponseException {
        response.type("application/json");
        var registrationRequest = gson.fromJson(request.body(), RegistrationRequest.class);
        AuthData authData = registrationService.registerUser(registrationRequest);
        response.status(200);
        return gson.toJson(authData);
    }

    private Object handleUserLogin(Request request, Response response) throws ResponseException {
        response.type("application/json");
        var loginRequest = gson.fromJson(request.body(), LoginRequest.class);
        AuthData authData = loginService.login(loginRequest);
        response.status(200);
        return gson.toJson(authData);
    }

    private Object handleUserLogout(Request request, Response response) throws ResponseException {
        response.type("application/json");
        var logoutRequest = new LogoutRequest(extractAuthToken(request));
        logoutService.logoutUser(logoutRequest);
        response.status(200);
        return "";
    }

    private Object handleListGames(Request request, Response response) throws ResponseException {
        authService.authenticate(extractAuthToken(request));
        Collection<GameResponseData> games = listService.getGames();
        response.status(200);
        return gson.toJson(new ListGamesResponse(games));
    }

    private Object handleJoinGame(Request request, Response response) throws ResponseException {
        String authToken = extractAuthToken(request);
        authService.authenticate(authToken);
        AuthData authData = authService.getAuthData(authToken);
        var joinRequest = gson.fromJson(request.body(), JoinGameRequest.class);
        joinService.joinGame(joinRequest, authData);
        response.status(200);
        return "";
    }

    private Object handleCreateGame(Request request, Response response) throws ResponseException {
        authService.authenticate(extractAuthToken(request));
        var createRequest = gson.fromJson(request.body(), CreateGameRequest.class);
        GameID gameID = gameService.createGame(createRequest);
        response.status(200);
        return gson.toJson(gameID);
    }

    private Object handleClearDatabase(Request request, Response response) {
        clearService.clearDatabase();
        response.status(200);
        return "";
    }
}