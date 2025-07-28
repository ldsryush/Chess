package ui;

import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.UserData;
import server.ServerFacade;

import java.util.Arrays;

public class Client {
    private final Repl repl;
    private State state = State.LOGGED_OUT;
    private UserData userData;
    private final ServerFacade server;
    private final String serverUrl;

    public Client(String serverUrl, Repl repl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.repl = repl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "login" -> login(params);
                case "register" -> register(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    public String help() {
        if (state == State.LOGGED_OUT) {
            return """
                    - login <username> <password>
                    - register <username> <password> <email>
                    - help
                    - quit""";
        }
        return """
                - create <NAME> - create a game with specified name
                - list - list all games
                - join <ID> [WHITE|BLACK|<empty>] - joins a game to play or watch
                - observe <ID> - watch a game
                - logout - logs you out
                - quit - finished playing
                - help - see possible commands (if you're seeing this message, you don't need to use this command)
                """;
    }

    private String joinGame(String[] params) {
        return null;
    }

    private String observeGame(String[] params) {
        return null;
    }

    private String listGames() {
        return null;
    }

    private String createGame(String[] params) {
        return null;
    }

    private String logout() {
        return null;
    }

    private String register(String[] params) throws ResponseException {
        if (params.length == 3) {
            UserData userData = new UserData(params[0], params[1], params[2]);
            AuthData authData = server.registerUser(userData);
            state = State.LOGGED_IN;
            return new Gson().toJson(authData);
        }
        throw new ResponseException(400, "error: bad request");
    }

    private String login(String[] params) throws ResponseException {
        if (params.length == 2) {
            userData = new UserData(params[0], params[1], null);
            AuthData authData = server.loginUser(userData);
            state = State.LOGGED_IN;
            return new Gson().toJson(authData);
        }
        throw new ResponseException(400, "error: unauthorized");
    }
}