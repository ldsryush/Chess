package ui;

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
    private String playerColor;

    public Client(String serverUrl, Repl repl) {
        server = new ServerFacade(serverUrl);
        this.repl = repl;
    }

    public String eval(String input) {
        try {
            var tokens = input.split(" ");
            var cmd    = tokens.length > 0 ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            return switch (cmd) {
                case "login"    -> login(params);
                case "register" -> register(params);
                case "logout"   -> logout();
                case "create"   -> createGame(params);
                case "list"     -> listGames();
                case "join"     -> joinGame(params);
                case "observe"  -> observeGame(params);
                case "cleardb"  -> clearDataBase();
                case "quit"     -> "quit";
                default         -> help();
            };
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    public String help() {
        String[] commands = {
                "create <NAME>", "list", "join <ID> [WHITE|BLACK]",
                "observe <ID>", "logout", "quit", "help"
        };
        String[] description = {
                "create a game with specified name",
                "list all games",
                "joins a game to play",
                "watch a game",
                "logs you out",
                "finished playing",
                "list possible commands"
        };

        if (state == State.LOGGED_OUT) {
            commands = new String[] {
                    "register <USERNAME> <PASSWORD> <EMAIL>",
                    "login <USERNAME> <PASSWORD>", "quit", "help"
            };
            description = new String[] {
                    "create an account", "login and play", "stop playing", "list possible commands"
            };
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < commands.length; i++) {
            sb.append(SET_TEXT_COLOR_BLUE)
                    .append(" - ").append(commands[i])
                    .append(SET_TEXT_COLOR_MAGENTA)
                    .append(" - ").append(description[i])
                    .append("\n");
        }
        return sb.toString();
    }

    private String joinGame(String[] params) {
        if (state == State.LOGGED_OUT) {
            return "Must login first";
        }
        if (params.length != 2) {
            return "Usage: join <ID> [WHITE|BLACK]";
        }

        int idx;
        try {
            idx = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return "Invalid game index. Use: join 1 WHITE";
        }

        try {
            updateGames();
            GameResponseData game = allGames.get(idx - 1);
            return joinGameWithColor(params[1], game);
        } catch (IndexOutOfBoundsException e) {
            return "Requested game doesn't exist";
        }
    }

    private String observeGame(String[] params) {
        if (state == State.LOGGED_OUT) {
            return "Must login first";
        }
        if (params.length != 1) {
            return "Usage: observe <ID>";
        }

        int idx;
        try {
            idx = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return "Invalid game index. Use: observe 1";
        }

        try {
            updateGames();
            GameResponseData game = allGames.get(idx - 1);
            server.observeGame(game.gameID());
            BoardDisplay.main(new String[]{"WHITE"});
            return "";
        } catch (IndexOutOfBoundsException e) {
            return "Requested game doesn't exist";
        } catch (ResponseException e) {
            return "Observe failed: " + e.getMessage();
        }
    }

    private String joinGameWithColor(String colorParam, GameResponseData game) {
        String color = colorParam.toUpperCase();

        if ("WHITE".equals(color)) {
            if (game.whiteUsername() != null) {
                return "Can't join as white";
            }
        } else if ("BLACK".equals(color)) {
            if (game.blackUsername() != null) {
                return "Can't join as black";
            }
        } else {
            return "Invalid color.";
        }

        try {
            server.joinGame(new JoinGameRequest(color, game.gameID()));
            playerColor = color;
        } catch (ResponseException e) {
            return "Can't join as " + color.toLowerCase();
        }

        BoardDisplay.main(new String[]{playerColor});
        return "";
    }

    private String listGames() {
        if (state == State.LOGGED_OUT) {
            return "Must login first";
        }
        updateGames();
        return buildGameList();
    }

    private String createGame(String[] params) {
        if (state == State.LOGGED_OUT) {
            return "Must login first";
        }
        String name = String.join(" ", params);
        try {
            GameID id = server.createGame(new GameName(name));
            updateGames();
            for (int i = 0; i < allGames.size(); i++) {
                if (allGames.get(i).gameID() == id.gameID()) {
                    return "Game " + name + " created with ID " + (i + 1);
                }
            }
            return "Error creating game, please try again";
        } catch (ResponseException e) {
            return "Couldn't create game: " + e.getMessage();
        }
    }

    private String logout() {
        if (state == State.LOGGED_OUT) {
            return "Must login first";
        }
        state = State.LOGGED_OUT;
        try {
            server.logoutUser();
        } catch (ResponseException e) {
            return "Failed to log out";
        }
        return "Logged out user";
    }

    private String register(String[] params) {
        if (state == State.LOGGED_IN) {
            return "Must logout first";
        }
        if (params.length == 3) {
            UserData ud = new UserData(params[0], params[1], params[2]);
            try {
                AuthData ad = server.registerUser(ud);
                state = State.LOGGED_IN;
                return "Logged in as " + ad.username();
            } catch (ResponseException e) {
                return "Invalid credentials";
            }
        }
        return "Invalid credentials";
    }

    private String login(String[] params) {
        if (state == State.LOGGED_IN) {
            return "Must logout first";
        }
        if (params.length == 2) {
            UserData ud = new UserData(params[0], params[1], null);
            try {
                AuthData ad = server.loginUser(ud);
                state = State.LOGGED_IN;
                return "Logged in as " + ad.username();
            } catch (ResponseException e) {
                return "Invalid login";
            }
        }
        return "Invalid credentials";
    }

    private String buildGameList() {
        StringBuilder sb = new StringBuilder();
        sb.append(LINE)
                .append(String.format("| ID  | %-14s| %-14s| %-12s|\n",
                        "White Player", "Black Player", "Game Name"))
                .append(LINE);
        for (int i = 0; i < allGames.size(); i++) {
            GameResponseData g = allGames.get(i);
            sb.append(String.format("| %-4d| %-14s| %-14s| %-12s|\n",
                            i + 1,
                            g.whiteUsername() != null ? g.whiteUsername() : "-",
                            g.blackUsername() != null ? g.blackUsername() : "-",
                            g.gameName()))
                    .append(LINE);
        }
        return sb.toString();
    }

    private void updateGames() {
        try {
            ArrayList<GameResponseData> fresh = server.listGames();
            ArrayList<GameResponseData> merged = new ArrayList<>();
            if (allGames != null) {
                merged.addAll(findExistingGames(fresh));
                merged.addAll(findNewGames(fresh));
            } else {
                merged = fresh;
            }
            allGames = merged;
        } catch (ResponseException e) {
            System.out.println(e.getMessage());
        }
    }

    private ArrayList<GameResponseData> findExistingGames(ArrayList<GameResponseData> fresh) {
        ArrayList<GameResponseData> existing = new ArrayList<>();
        for (GameResponseData curr : allGames) {
            for (GameResponseData n : fresh) {
                if (Objects.equals(n.gameID(), curr.gameID())) {
                    existing.add(n);
                }
            }
        }
        return existing;
    }

    private ArrayList<GameResponseData> findNewGames(ArrayList<GameResponseData> fresh) {
        ArrayList<GameResponseData> added = new ArrayList<>();
        for (GameResponseData n : fresh) {
            boolean seen = false;
            for (GameResponseData curr : allGames) {
                if (Objects.equals(n.gameID(), curr.gameID())) {
                    seen = true;
                    break;
                }
            }
            if (!seen) {
                added.add(n);
            }
        }
        return added;
    }

    private String clearDataBase() throws ResponseException {
        server.clear();
        return "Database cleared.";
    }
}