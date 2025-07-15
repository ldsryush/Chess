package server;

import com.google.gson.Gson;
import dataAccess.GameDAO;
import dataAccess.memory.MemoryAuthDAO;
import dataAccess.memory.MemoryGameDAO;
import dataAccess.memory.MemoryUserDAO;
import exception.ErrorMessage;
import exception.ResponseException;
import handlers.ListGamesRequest;
import handlers.LoginRequest;
import handlers.LogoutRequest;
import handlers.RegistrationRequest;
import model.AuthData;
import model.GameData;
import model.UserData;
import Service.*;
import spark.*;

import java.nio.file.Paths;

public class Server {

    private final MemoryAuthDAO memoryAuthDAO = new MemoryAuthDAO();
    private final MemoryUserDAO memoryUserDAO = new MemoryUserDAO();
    private final MemoryGameDAO memoryGameDAO = new MemoryGameDAO();

    private final RegistrationService registrationService = new RegistrationService(memoryUserDAO, memoryAuthDAO);
    private final LoginService loginService = new LoginService(memoryUserDAO, memoryAuthDAO);
    private final LogoutService logoutService = new LogoutService(memoryAuthDAO);
    private final ListService listService = new ListService();
    private final JoinService joinService = new JoinService();
    private final GameService gameService = new GameService();
    private final ClearService clearService = new ClearService(memoryUserDAO, memoryAuthDAO, memoryGameDAO);



    public Server() {
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");
        Spark.init();


        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registrationHandler);

        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);

        Spark.get("/game", this::getGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);

        Spark.delete("/db", this::clearApp);

        Spark.exception(ResponseException.class, this::exceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void exceptionHandler(ResponseException e, Request request, Response response) {
        response.status(e.StatusCode());
    }


    public int port() {
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
    }

    /** Registers new users
     *
     * @param request - the HTTP request
     * @param response - the HTTP response
     * @return JSON object containing the response body
     */
    private Object registrationHandler(Request request, Response response) {
//        Complete

        response.type("application/json");
        var user = new Gson().fromJson(request.body(), RegistrationRequest.class);
        try {
            AuthData authData = registrationService.registerUser(user);
            response.status(200);
            response.body(new Gson().toJson(authData));
            return new Gson().toJson(authData);
        } catch (ResponseException e) {
            response.status(e.StatusCode());
            response.body(new Gson().toJson(new ErrorMessage(e.getMessage())));
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        }
    }

    private Object loginUser(Request request, Response response) {
        response.type("application/json");
        var user = new Gson().fromJson(request.body(), LoginRequest.class);

        try {
            AuthData authData = loginService.login(user);
            response.status(200);
            response.body(new Gson().toJson(authData));
            return new Gson().toJson(authData);
        } catch (ResponseException e) {
            response.status(e.StatusCode());
            response.body(new Gson().toJson(new ErrorMessage(e.getMessage())));
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        }
    }

    private Object logoutUser(Request request, Response response) {
        response.type("application/json");
        var authToken = new LogoutRequest(request.headers("authorization"));

        try {
            logoutService.logoutUser(authToken);
            response.status(200);
            return "";
        } catch (ResponseException e) {
            response.status(e.StatusCode());
            response.body(new Gson().toJson(new ErrorMessage(e.getMessage())));
            return new Gson().toJson(new ErrorMessage(e.getMessage()));
        }
    }

    private Object getGames(Request request, Response response) {
//        TODO
        var authToken = new ListGamesRequest(request.headers("authorization"));

        return null;
    }

    private Object joinGame(Request request, Response response) {
//        TODO
        var playerColor = request.queryParams("playerColor");
        var gameID = request.queryParams("gameID");

        return "";
    }

    private Object createGame(Request request, Response response) {
//        TODO
        var newGame = request.queryParams("gameName");



        response.status(200);
        return "{ 'gameID':1234}";
    }

    private Object clearApp(Request request, Response response) {
//        TODO
        clearService.clearDatabase();
        response.status(200);
        return "";
    }
}