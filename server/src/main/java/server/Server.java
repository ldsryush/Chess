package server;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import dataaccess.mysql.MySQLAuthDAO;
import dataaccess.mysql.MySQLGameDAO;
import dataaccess.mysql.MySQLUserDAO;
import exception.ErrorMessage;
import exception.ResponseException;
import model.*;
import service.RegistrationService;
import service.LoginService;
import service.LogoutService;
import service.ListService;
import service.JoinService;
import service.GameService;
import service.ClearService;
import service.AuthenticationService;
import spark.Spark;
import spark.Request;
import spark.Response;
import websocket.ConnectionManager;
import websocket.NotificationHandler;
import websocket.ClientConnection;
import websocket.WebSocketHandler;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public class Server {

    private final RegistrationService registrationService;
    private final LoginService loginService;
    private final LogoutService logoutService;
    private final ListService listService;
    private final JoinService joinService;
    private final GameService gameService;
    private final ClearService clearService;
    private final AuthenticationService authService;

    public Server() {
        try {
            AuthDAO authDAO    = new MySQLAuthDAO();
            GameDAO gameDAO    = new MySQLGameDAO();
            UserDAO userDAO    = new MySQLUserDAO();

            registrationService = new RegistrationService(userDAO, authDAO);
            loginService        = new LoginService(userDAO, authDAO);
            logoutService       = new LogoutService(authDAO);
            listService         = new ListService(gameDAO);
            joinService         = new JoinService(gameDAO);
            gameService         = new GameService(gameDAO);
            clearService        = new ClearService(userDAO, authDAO, gameDAO);
            authService         = new AuthenticationService(authDAO);
        } catch (ResponseException ex) {
            System.out.printf("Unable to connect to database: %s%n", ex.getMessage());
            throw new RuntimeException("Server initialization failed by database error", ex);
        }
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        ConnectionManager connectionManager = new ConnectionManager();

        NotificationHandler handler = new NotificationHandler() {
            @Override
            public void loadGame(ClientConnection recipient, LoadGameMessage message) {
                recipient.send(message);
            }

            @Override
            public void updateGame(ClientConnection recipient, LoadGameMessage message) {
                recipient.send(message);
            }

            @Override
            public void error(ClientConnection recipient, websocket.messages.ErrorMessage message) {
                recipient.send(message);
            }

            @Override
            public void notifyOthers(ClientConnection sender, LoadGameMessage message) {
                connectionManager.broadcastToOthers(sender.getGameID(), sender, message);
            }

            @Override
            public void notifyOthers(ClientConnection sender, NotificationMessage message) {
                connectionManager.broadcastToOthers(sender.getGameID(), sender, message);
            }

            @Override
            public void notifyGame(int gameID, NotificationMessage message) {
                connectionManager.broadcastToGame(gameID, message);
            }

            @Override
            public void notifyGame(int gameID, LoadGameMessage message) {
                connectionManager.broadcastToGame(gameID, message);
            }
        };


        // Configure WebSocketHandler with dependencies
        WebSocketHandler.configure(
                gameService,
                authService,
                joinService,
                handler,
                connectionManager
        );

        Spark.webSocket("/ws", WebSocketHandler.class);

        Spark.post("/user",                 this::registrationHandler);
        Spark.post("/session",              this::loginUser);
        Spark.delete("/session",            this::logoutUser);
        Spark.get("/game",                  this::getGames);
        Spark.post("/game",                 this::createGame);
        Spark.put("/game",                  this::joinGame);
        Spark.put("/game/observe/:gameID",  this::observeGame);
        Spark.delete("/db",                 this::clearApp);

        Spark.exception(ResponseException.class,   this::responseExceptionHandler);
        Spark.exception(DataAccessException.class, this::dataExceptionHandler);

        Spark.init();
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
        String msg = "Error: " + e.getMessage();
        response.body(new Gson().toJson(new ErrorMessage(msg)));
    }

    private void dataExceptionHandler(DataAccessException e, Request request, Response response) {
        response.status(500);
        String msg = "Error: " + e.getMessage();
        response.body(new Gson().toJson(new ErrorMessage(msg)));
    }

    private Object registrationHandler(Request request, Response response)
            throws ResponseException, DataAccessException {
        response.type("application/json");
        RegistrationRequest user = new Gson().fromJson(request.body(), RegistrationRequest.class);

        if (user.username() == null || user.username().isBlank()
                || user.password() == null || user.password().isBlank()
                || user.email() == null || user.email().isBlank()) {
            throw new ResponseException(400, "Missing required fields");
        }

        AuthData authData = registrationService.registerUser(user);
        response.status(200);
        return new Gson().toJson(authData);
    }

    private Object loginUser(Request request, Response response)
            throws ResponseException, DataAccessException {
        response.type("application/json");
        LoginRequest user = new Gson().fromJson(request.body(), LoginRequest.class);
        AuthData authData = loginService.login(user);
        response.status(200);
        return new Gson().toJson(authData);
    }

    private Object logoutUser(Request request, Response response)
            throws ResponseException, DataAccessException {
        response.type("application/json");
        LogoutRequest authToken = new LogoutRequest(request.headers("authorization"));
        authService.authenticate(authToken.authToken());
        logoutService.logoutUser(authToken);
        response.status(200);
        return "{}";
    }

    private Object getGames(Request request, Response response)
            throws ResponseException, DataAccessException {
        response.type("application/json");
        String authToken = request.headers("authorization");
        authService.authenticate(authToken);
        var games = listService.getGames();
        response.status(200);
        return new Gson().toJson(new ListGamesResponse(games));
    }

    private Object createGame(Request request, Response response)
            throws ResponseException, DataAccessException {
        response.type("application/json");
        String authToken = request.headers("authorization");
        authService.authenticate(authToken);
        CreateGameRequest newGame = new Gson().fromJson(request.body(), CreateGameRequest.class);
        GameID gameID = gameService.createGame(newGame);
        response.status(200);
        return new Gson().toJson(gameID);
    }

    private Object joinGame(Request request, Response response)
            throws ResponseException, DataAccessException {
        response.type("application/json");
        String authToken = request.headers("authorization");
        authService.authenticate(authToken);
        AuthData authData = authService.getAuthData(authToken);
        JoinGameRequest joinInfo = new Gson().fromJson(request.body(), JoinGameRequest.class);
        joinService.joinGame(joinInfo, authData);
        response.status(200);
        return "{}";
    }

    private Object observeGame(Request request, Response response)
            throws ResponseException, DataAccessException {
        response.type("application/json");
        String authToken = request.headers("authorization");
        authService.authenticate(authToken);
        AuthData authData = authService.getAuthData(authToken);
        int gameID = Integer.parseInt(request.params(":gameID"));
        joinService.observeGame(gameID, authData);
        response.status(200);
        return "{}";
    }

    private Object clearApp(Request request, Response response)
            throws ResponseException, DataAccessException {
        response.type("application/json");
        clearService.clearDatabase();
        response.status(200);
        return "{}";
    }
}
