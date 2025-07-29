package ui;

import chess.ChessBoard;
import exception.ResponseException;
import model.*;
import model.ServerFacade;
import model.JoinGameRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static ui.EscapeSequences.*;

public class Client {
    public static State state = State.LOGGED_OUT;
    private final ServerFacade server;
    private final Repl repl;
    private ArrayList<GameResponseData> allGames;

    public Client(String serverUrl, Repl repl) {
        server = new ServerFacade(serverUrl);
        this.repl = repl;
    }

    public String eval(String input) {
        try {
            var tokens = input.split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "login" -> login(params);
                case "register" -> register(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join", "observe" -> joinGame(params);
                case "cleardb" -> clearDataBase();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    public String help() {
        String[] commands = {
                "create <NAME>", "list", "join <ID> [WHITE|BLACK|<empty>]",
                "observe <ID>", "logout", "quit", "help"
        };
        String[] description = {
                "create a game with specified name",
                "list all games",
                "joins a game to play or watch",
                "watch a game",
                "logs you out",
                "finished playing",
                "list possible commands (if you're seeing this message, you don't need to use this command)"
        };

        if (state == State.LOGGED_OUT) {
            commands = new String[] {
                    "register <USERNAME> <PASSWORD> <EMAIL>",
                    "login <USERNAME> <PASSWORD>",
                    "quit", "help"
            };
            description = new String[] {
                    "create an account", "login and play", "stop playing", "list possible commands"
            };
        }

        StringBuilder response = new StringBuilder();
        for (int i = 0; i < commands.length; i++) {
            response.append(SET_TEXT_COLOR_BLUE)
                    .append(" - ")
                    .append(commands[i])
                    .append(SET_TEXT_COLOR_MAGENTA)
                    .append(" - ")
                    .append(description[i])
                    .append("\n");
        }
        return response.toString();
    }

    private String joinGame(String[] params) {
        if (state == State.LOGGED_OUT) return "Must login first";
        int idx = Integer.parseInt(params[0]);

        try {
            updateGames();
            var game = allGames.get(idx - 1);
            if (params.length == 1) return observeGame(game);
            if (params.length == 2) return joinGameWithColor(params[1], game);
        } catch (IndexOutOfBoundsException e) {
            return "Requested game doesn't exist";
        }

        return "Invalid input";
    }

    private String observeGame(GameResponseData game) {
        try {
            server.joinGame(new JoinGameRequest(null, game.gameID()));
        } catch (ResponseException e) {
            return "Failed to observe game, try later.";
        }
        BoardDisplay.main(new String[]{new ChessBoard().toString()});
        return "";
    }

    private String joinGameWithColor(String colorParam, GameResponseData game) {
        String color = colorParam.toUpperCase();

        if (color.equals("WHITE") && game.whiteUsername() != null) return "Can't join as white";
        if (color.equals("BLACK") && game.blackUsername() != null) return "Can't join as black";
        if (!color.equals("WHITE") && !color.equals("BLACK")) return "Invalid color.";

        try {
            server.joinGame(new JoinGameRequest(color, game.gameID()));
        } catch (ResponseException e) {
            return "Can't join as " + color.toLowerCase();
        }

        BoardDisplay.main(new String[]{""});
        return "";
    }

    private String listGames() {
        if (state == State.LOGGED_OUT) return "Must login first";
        updateGames();
        return buildGameList();
    }

    private String createGame(String[] params) {
        if (state == State.LOGGED_OUT) return "Must login first";
        String name = String.join(" ", params);
        GameID gameID;
        try {
            gameID = server.createGame(new GameName(name));
            updateGames();
            for (int idx = 0; idx < allGames.size(); idx++) {
                if (allGames.get(idx).gameID() == gameID.gameID()) {
                    return "Game " + name + " created with ID " + (idx + 1);
                }
            }
            return "Error creating game, please try again";
        } catch (ResponseException e) {
            return "Couldn't create game with that name, try again.";
        }
    }

    private String logout() {
        if (state == State.LOGGED_OUT) return "Must login first";
        state = State.LOGGED_OUT;
        try {
            server.logoutUser();
        } catch (ResponseException e) {
            return "Failed to log out";
        }
        return "Logged out user";
    }

    private String register(String[] params) {
        if (state == State.LOGGED_IN) return "Must logout first";
        if (params.length == 3) {
            UserData userData = new UserData(params[0], params[1], params[2]);
            AuthData authData;
            try {
                authData = server.registerUser(userData);
            } catch (ResponseException e) {
                return "Invalid credentials";
            }
            state = State.LOGGED_IN;
            return "Logged in as " + authData.username();
        }
        return "Invalid credentials";
    }

    private String login(String[] params) {
        if (state == State.LOGGED_IN) return "Must logout first";
        if (params.length == 2) {
            UserData userData = new UserData(params[0], params[1], null);
            AuthData authData;
            try {
                authData = server.loginUser(userData);
            } catch (ResponseException e) {
                return "Invalid login";
            }
            state = State.LOGGED_IN;
            return "Logged in as " + authData.username();
        }
        return "Invalid credentials";
    }

    private String buildGameList() {
        StringBuilder response = new StringBuilder();
        response.append(LINE);
        response.append(String.format("| ID  | %-14s| %-14s| %-12s|\n",
                "White Player", "Black Player", "Game Name"));
        response.append(LINE);
        for (int idx = 0; idx < allGames.size(); idx++) {
            var game = allGames.get(idx);
            response.append(String.format("| %-4d| %-14s| %-14s| %-12s|\n",
                    idx + 1, game.whiteUsername(), game.blackUsername(), game.gameName()));
            response.append(LINE);
        }
        return response.toString();
    }

    private void updateGames() {
        try {
            var newGames = server.listGames();
            ArrayList<GameResponseData> tempGames = new ArrayList<>();

            if (allGames != null) {
                for (var currGame : allGames) {
                    for (var newGame : newGames) {
                        if (Objects.equals(newGame.gameID(), currGame.gameID())) {
                            tempGames.add(newGame);
                        }
                    }
                }
                for (var newGame : newGames) {
                    boolean found = false;
                    for (var currGame : allGames) {
                        if (Objects.equals(newGame.gameID(), currGame.gameID())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        tempGames.add(newGame);
                    }
                }
            } else {
                tempGames = newGames;
            }
            allGames = tempGames;
        } catch (ResponseException e) {
            System.out.println(e.getMessage());
        }
    }

    private String clearDataBase() throws ResponseException {
        server.clear();
        return "Database cleared.";
    }
}
