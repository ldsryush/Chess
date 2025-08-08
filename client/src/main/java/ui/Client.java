package ui;

import exception.ResponseException;
import model.*;
import model.ServerFacade;
import model.JoinGameRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import com.google.gson.JsonObject;

import static ui.EscapeSequences.*;

public class Client {
    public static State state = State.LOGGED_OUT;
    private final ServerFacade server;
    private final Repl repl;
    private ArrayList<GameResponseData> allGames;
    private String playerColor;
    private Integer currentGameID;

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
                case "redraw"   -> redrawBoard();
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
                "observe <ID>", "redraw", "logout", "quit", "help"
        };
        String[] description = {
                "create a game with specified name",
                "list all games",
                "joins a game to play",
                "watch a game",
                "redraw the current board",
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

    private String redrawBoard() {
        if (playerColor == null || currentGameID == null) {
            return "No active game. Join or observe a game first.";
        }

        try {
            System.out.println("\n" + SET_TEXT_COLOR_GREEN + "Redrawing board..." + SET_TEXT_COLOR_WHITE);
            BoardDisplay.main(new String[]{playerColor});
            System.out.println(SET_TEXT_COLOR_BLUE + "\nBoard redrawn. Use 'redraw' to refresh again." + SET_TEXT_COLOR_WHITE);
            return "";
        } catch (Exception e) {
            return "Error displaying board: " + e.getMessage();
        }
    }

    private String createGame(String[] params) {
        if (state == State.LOGGED_OUT) {
            return "Must login first";
        }
        String name = String.join(" ", params);
        try {
            GameID id = server.createGame(new GameName(name));
            updateGames();
            return "Game " + name + " created with ID " + id.gameID() + ". Use 'join " + id.gameID() + " WHITE' to join.";
        } catch (ResponseException e) {
            return "Couldn't create game: " + e.getMessage();
        }
    }

    private String listGames() {
        if (state == State.LOGGED_OUT) {
            return "Must login first";
        }
        updateGames();
        return buildGameList();
    }

    private String joinGame(String[] params) {
        if (state == State.LOGGED_OUT) {
            return "Must login first";
        }
        if (params.length != 2) {
            return "Usage: join <ID> [WHITE|BLACK]";
        }

        int gameID;
        try {
            gameID = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return "Invalid game ID. Use: join <gameID> WHITE";
        }

        try {
            updateGames();
            // Find game by actual game ID, not list index
            GameResponseData game = null;
            for (GameResponseData g : allGames) {
                if (g.gameID() == gameID) {
                    game = g;
                    break;
                }
            }

            if (game == null) {
                return "Game with ID " + gameID + " not found";
            }

            return joinGameWithColor(params[1], game);
        } catch (Exception e) {
            return "Error joining game: " + e.getMessage();
        }
    }

    private String joinGameWithColor(String colorParam, GameResponseData game) {
        String color = colorParam.toUpperCase();

        if ("WHITE".equals(color) && game.whiteUsername() != null) {
            return "Can't join as white";
        } else if ("BLACK".equals(color) && game.blackUsername() != null) {
            return "Can't join as black";
        } else if (!"WHITE".equals(color) && !"BLACK".equals(color)) {
            return "Invalid color.";
        }

        try {
            server.joinGame(new JoinGameRequest(color, game.gameID()));
            playerColor = color;
            currentGameID = game.gameID();

            System.out.println("\n" + SET_TEXT_COLOR_GREEN + "Successfully joined game as " + color + "!" + SET_TEXT_COLOR_WHITE);
            System.out.println(SET_TEXT_COLOR_YELLOW + "Displaying initial board..." + SET_TEXT_COLOR_WHITE);
            BoardDisplay.main(new String[]{playerColor});
            System.out.println("\n" + SET_TEXT_COLOR_BLUE + "Use 'redraw' to refresh the board." + SET_TEXT_COLOR_WHITE);

            String authToken = server.getAuthToken();
            String serverUrl = server.getServerUrl();
            //GamePlayUI
            GameplayUI gameplayUI = new GameplayUI();
            gameplayUI.setPlayerColor(color.toLowerCase());
            gameplayUI.start(serverUrl, authToken, game.gameID());

            return "";

        } catch (ResponseException e) {
            return "Can't join as " + color.toLowerCase();
        } catch (Exception e) {
            return "Failed to start game: " + e.getMessage();
        }
    }

    private String observeGame(String[] params) {
        if (state == State.LOGGED_OUT) {
            return "Must login first";
        }
        if (params.length != 1) {
            return "Usage: observe <ID>";
        }

        int gameID;
        try {
            gameID = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return "Invalid game ID. Use: observe <gameID>";
        }

        try {
            updateGames();
            // Find game by actual game ID, not list index
            GameResponseData game = null;
            for (GameResponseData g : allGames) {
                if (g.gameID() == gameID) {
                    game = g;
                    break;
                }
            }

            if (game == null) {
                return "Game with ID " + gameID + " not found";
            }

            server.observeGame(game.gameID());

            playerColor = "WHITE"; // observers default to white view
            currentGameID = game.gameID();

            System.out.println("\n" + SET_TEXT_COLOR_GREEN + "Successfully joined as observer!" + SET_TEXT_COLOR_WHITE);
            System.out.println(SET_TEXT_COLOR_YELLOW + "Displaying initial board..." + SET_TEXT_COLOR_WHITE);
            BoardDisplay.main(new String[]{playerColor});
            System.out.println("\n" + SET_TEXT_COLOR_BLUE + "Use 'redraw' to refresh the board." + SET_TEXT_COLOR_WHITE);

            String authToken = server.getAuthToken();
            String serverUrl = server.getServerUrl();

            GameplayUI gameplayUI = new GameplayUI();
            gameplayUI.setPlayerColor("observer");
            gameplayUI.start(serverUrl, authToken, game.gameID());

            return "";

        } catch (ResponseException e) {
            return "Observe failed: " + e.getMessage();
        } catch (Exception e) {
            return "Failed to start observation: " + e.getMessage();
        }
    }

    private String clearDataBase() throws ResponseException {
        server.clear();
        return "Database cleared.";
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

    private String logout() {
        if (state == State.LOGGED_OUT) {
            return "Must login first";
        }
        state = State.LOGGED_OUT;
        playerColor = null;
        currentGameID = null;
        try {
            server.logoutUser();
        } catch (ResponseException e) {
            return "Failed to log out";
        }
        return "Logged out user";
    }

    private String buildGameList() {
        StringBuilder sb = new StringBuilder();
        sb.append(LINE)
                .append(String.format("| %-8s| %-14s| %-14s| %-12s|\n",
                        "Game ID", "White Player", "Black Player", "Game Name"))
                .append(LINE);
        for (GameResponseData g : allGames) {
            sb.append(String.format("| %-8d| %-14s| %-14s| %-12s|\n",
                            g.gameID(),
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

    public void handleServerMessage(JsonObject message) {
        String type = message.get("serverMessageType").getAsString();

        switch (type) {
            case "NOTIFICATION" -> {
                String note = message.get("message").getAsString();
                repl.showNotification(note);
            }
            case "ERROR" -> {
                String error = message.get("message").getAsString();
                repl.showNotification("❌ Error: " + error);
            }
            default -> {
                repl.showNotification("⚠️ Unknown message type: " + type);
            }
        }
    }
}
