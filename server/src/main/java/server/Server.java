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

/**
 * Main server class that configures HTTP routes and handles incoming requests.
 */
public class Server {
    private static final Gson gson = new Gson();

    // 🔧 Data Access Layer (In-Memory Implementation)
    private final DataAccess dataAccess = new DataAccess(DataAccess.DataLocation.MEMORY);
    private final AuthDAO authDAO = dataAccess.getAuthDAO();
    private final UserDAO userDAO = dataAccess.getUserDAO();
    private final GameDAO gameDAO = dataAccess.getGameDAO();

    // 🧠 Service Layer
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
        configureRoutes();                 // Sets up HTTP endpoints
        Spark.awaitInitialization();       // Waits for server to be ready
        System.out.println("Server running on port " + Spark.port());
        return Spark.port();
    }

    /**
     * @return the current port the server is running on
     */
    public int port() {
        return Spark.port();
    }

    /**
     * Stops the Spark server.
     */
    public void stop() {
        Spark.stop();
    }

    /**
     * Configures all HTTP routes and exception handlers.
     */
    private void configureRoutes() {
        // User account routes
        Spark.post("/user", this::registrationHandler);   // Register new user
        Spark.post("/session", this::loginUser);          // Log in
        Spark.delete("/session", this::logoutUser);       // Log out

        // Game-related routes
        Spark.get("/game", this::getGames);               // List games
        Spark.post("/game", this::createGame);            // Create game
        Spark.put("/game", this::joinGame);               // Join game

        // Admin route
        Spark.delete("/db", this::clearApp);              // Clear all data

        // Global exception handler
        Spark.exception(ResponseException.class, this::exceptionHandler);
    }

    /**
     * Retrieves the authorization token from the request header.
     *
     * @param request the incoming HTTP request
     * @return the value of the "authorization" header
     */
    private String getAuthToken(Request request) {
        return request.headers("authorization");
    }

    /**
     * Handles exceptions thrown during request processing.
     *
     * @param e        the exception
     * @param request  the HTTP request
     * @param response the HTTP response
     */
    private void exceptionHandler(ResponseException e, Request request, Response response) {
        response.status(e.getStatusCode());
        response.body(gson.toJson(new ErrorMessage(e.getMessage())));
    }

    // 🧾 Route Handlers

    /**
     * Handles user registration.
     */
    private Object registrationHandler(Request request, Response response) throws ResponseException {
        response.type("application/json");
        var user = gson.fromJson(request.body(), RegistrationRequest.class);
        AuthData authData = registrationService.registerUser(user);
        response.status(200);
        return gson.toJson(authData);
    }

    /**
     * Handles user login.
     */
    private Object loginUser(Request request, Response response) throws ResponseException {
        response.type("application/json");
        var user = gson.fromJson(request.body(), LoginRequest.class);
        AuthData authData = loginService.login(user);
        response.status(200);
        return gson.toJson(authData);
    }

    /**
     * Handles user logout.
     */
    private Object logoutUser(Request request, Response response) throws ResponseException {
        response.type("application/json");
        var authToken = new LogoutRequest(getAuthToken(request));
        logoutService.logoutUser(authToken);
        response.status(200);
        return "";
    }

    /**
     * Retrieves the list of available games.
     */
    private Object getGames(Request request, Response response) throws ResponseException {
        authService.authenticate(getAuthToken(request));
        Collection<GameResponseData> allGames = listService.getGames();
        response.status(200);
        return gson.toJson(new ListGamesResponse(allGames));
    }

    /**
     * Handles joining a game.
     */
    private Object joinGame(Request request, Response response) throws ResponseException {
        String authToken = getAuthToken(request);
        authService.authenticate(authToken);
        AuthData authData = authService.getAuthData(authToken);
        var joinInfo = gson.fromJson(request.body(), JoinGameRequest.class);
        joinService.joinGame(joinInfo, authData);
        response.status(200);
        return "";
    }

    /**
     * Handles game creation.
     */
    private Object createGame(Request request, Response response) throws ResponseException {
        authService.authenticate(getAuthToken(request));
        var newGame = gson.fromJson(request.body(), CreateGameRequest.class);
        GameID gameID = gameService.createGame(newGame);
        response.status(200);
        return gson.toJson(gameID);
    }

    /**
     * Clears all data in the application (users, games, auth tokens).
     */
    private Object clearApp(Request request, Response response) {
        clearService.clearDatabase();
        response.status(200);
        return "";
    }
}
