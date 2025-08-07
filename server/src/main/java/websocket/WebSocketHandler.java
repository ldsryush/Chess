package websocket;

import chess.ChessGame;
import dataaccess.DataAccessException;
import exception.ResponseException;
import model.AuthData;
import model.JoinGameRequest;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.AuthenticationService;
import service.GameService;
import service.JoinService;
import websocket.commands.UserGameCommand;
import websocket.commands.UserGameCommand.CommandType;
import websocket.messages.*;

import com.google.gson.Gson;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private static final Gson GSON = new Gson();

    private static GameService gameService;
    private static AuthenticationService authService;
    private static JoinService joinService;
    private static NotificationHandler notificationHandler;
    private static ConnectionManager connectionManager;

    public static void configure(
            GameService gs,
            AuthenticationService as,
            JoinService js,
            NotificationHandler nh,
            ConnectionManager cm
    ) {
        gameService = gs;
        authService = as;
        joinService = js;
        notificationHandler = nh;
        connectionManager = cm;

        System.out.println("WebSocketHandler configured with all dependencies");
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Client connected: " + session.getRemoteAddress());
        if (gameService == null) {
            System.err.println("ERROR: WebSocketHandler not configured! Call configure() first.");
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("Received: " + message);

        if (gameService == null || authService == null || connectionManager == null) {
            System.err.println("ERROR: WebSocketHandler not properly configured");
            sendRaw(session, GSON.toJson(new ErrorMessage("Server configuration error")));
            return;
        }

        try {
            UserGameCommand cmd = GSON.fromJson(message, UserGameCommand.class);
            if (cmd == null || cmd.getCommandType() == null) {
                sendRaw(session, GSON.toJson(new ErrorMessage("Invalid command format")));
                return;
            }

            CommandType type = cmd.getCommandType();

            switch (type) {
                case CONNECT -> handleConnect(session, cmd);
                case MAKE_MOVE, RESIGN -> {
                    ClientConnection conn = connectionManager.getConnection(session);
                    if (conn == null) {
                        sendRaw(session, GSON.toJson(new ErrorMessage("No active connection")));
                        return;
                    }
                    if (type == CommandType.MAKE_MOVE) {
                        handleMove(conn, cmd);
                    } else {
                        handleResign(conn);
                    }
                }
                case LEAVE -> {
                    ClientConnection conn = connectionManager.getConnection(cmd.getAuthToken());
                    if (conn == null) {
                        sendRaw(session, GSON.toJson(new ErrorMessage("No active connection")));
                        return;
                    }
                    handleLeave(conn);
                }
                default -> sendRaw(session, GSON.toJson(new ErrorMessage("Unknown command: " + type)));
            }

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
            sendRaw(session, GSON.toJson(new ErrorMessage("Invalid command format: " + e.getMessage())));
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int status, String reason) {
        System.out.println("Client disconnected: " + session.getRemoteAddress() + " Reason: " + reason);
        if (connectionManager != null) {
            ClientConnection conn = connectionManager.getConnection(session);
            if (conn != null) {
                handleLeave(conn);
            }
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
        error.printStackTrace();
        if (connectionManager != null) {
            ClientConnection conn = connectionManager.getConnection(session);
            if (conn != null) {
                handleLeave(conn);
            }
        }
    }

    private void handleConnect(Session session, UserGameCommand cmd) {
        try {
            if (cmd.getAuthToken() == null || cmd.getGameID() == null) {
                sendRaw(session, GSON.toJson(new ErrorMessage("Missing auth token or game ID")));
                return;
            }

            AuthData auth = authService.getAuthData(cmd.getAuthToken());
            if (auth == null) {
                sendRaw(session, GSON.toJson(new ErrorMessage("Invalid auth token")));
                return;
            }

            String user = auth.username();
            int gameID = cmd.getGameID();
            var data = gameService.getGameData(gameID);

            boolean isWhite = user.equals(data.whiteUsername());
            boolean isBlack = user.equals(data.blackUsername());
            boolean joinNew = false;
            String color;

            if (isWhite) {
                color = "white";
            } else if (isBlack) {
                color = "black";
            } else if (data.whiteUsername() == null) {
                color = "white";
                joinNew = true;
            } else if (data.blackUsername() == null) {
                color = "black";
                joinNew = true;
            } else {
                color = "observer";
            }

            if (joinNew && !"observer".equals(color)) {
                joinService.joinGame(new JoinGameRequest(color.toUpperCase(), gameID), auth);
            }

            ClientConnection conn = new ClientConnection(user, session, cmd.getAuthToken(), gameID, color);
            connectionManager.addConnection(gameID, conn);

            ChessGame game = gameService.getGameData(gameID).game();
            notificationHandler.loadGame(conn, new LoadGameMessage(game, color));

            String roleMsg = switch (color) {
                case "white", "black" -> user + " connected to the game as " + color;
                default -> user + " connected as observer";
            };
            notificationHandler.notifyOthers(conn, new NotificationMessage(roleMsg));

        } catch (ResponseException | DataAccessException e) {
            System.err.println("Connect failed: " + e.getMessage());
            sendRaw(session, GSON.toJson(new ErrorMessage("Connect failed: " + e.getMessage())));
        } catch (Exception e) {
            System.err.println("Unexpected connect error: " + e.getMessage());
            e.printStackTrace();
            sendRaw(session, GSON.toJson(new ErrorMessage("Unexpected error: " + e.getMessage())));
        }
    }

    private void handleMove(ClientConnection conn, UserGameCommand cmd) {
        int gameID = conn.getGameID();
        String user = conn.getUserName();

        try {
            if (cmd.getMove() == null) {
                notificationHandler.error(conn, new ErrorMessage("Move cannot be null"));
                return;
            }

            AuthData auth = authService.getAuthData(cmd.getAuthToken());
            if (auth == null || !auth.username().equals(user)) {
                notificationHandler.error(conn, new ErrorMessage("Invalid auth token"));
                return;
            }

            var data = gameService.getGameData(gameID);
            ChessGame game = data.game();

            boolean validTurn = (game.getTeamTurn() == ChessGame.TeamColor.WHITE && user.equals(data.whiteUsername()))
                    || (game.getTeamTurn() == ChessGame.TeamColor.BLACK && user.equals(data.blackUsername()));

            if (!validTurn) {
                notificationHandler.error(conn, new ErrorMessage("Invalid move: not your turn"));
                return;
            }

            gameService.makeMove(gameID, user, cmd.getMove());
            ChessGame updated = gameService.getGameData(gameID).game();

            notificationHandler.loadGame(conn, new LoadGameMessage(updated, conn.getPlayerColor()));
            notificationHandler.notifyOthers(conn, new LoadGameMessage(updated, conn.getPlayerColor()));

            var mv = cmd.getMove();
            var piece = updated.getBoard().getPiece(mv.getEndPosition());
            String text = String.format("%s moved %s from %s to %s",
                    user,
                    piece != null ? piece.getPieceType() : "a piece",
                    mv.getStartPosition(),
                    mv.getEndPosition());
            notificationHandler.notifyOthers(conn, new NotificationMessage(text));

            if (updated.isGameOver()) {
                String loser = updated.getTeamTurn() == ChessGame.TeamColor.WHITE
                        ? data.whiteUsername()
                        : data.blackUsername();
                notificationHandler.notifyGame(gameID, new NotificationMessage(loser + " is in checkmate"));
            } else if (updated.isInCheck(updated.getTeamTurn())) {
                String checkedPlayer = updated.getTeamTurn() == ChessGame.TeamColor.WHITE
                        ? data.whiteUsername()
                        : data.blackUsername();
                notificationHandler.notifyGame(gameID, new NotificationMessage(checkedPlayer + " is in check"));
            }

        } catch (ResponseException | DataAccessException e) {
            System.err.println("Move failed: " + e.getMessage());
            notificationHandler.error(conn, new ErrorMessage("Move failed: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected move error: " + e.getMessage());
            e.printStackTrace();
            notificationHandler.error(conn, new ErrorMessage("Unexpected error: " + e.getMessage()));
        }
    }

    private void handleResign(ClientConnection conn) {
        if (conn == null) {
            return;
        }

        int gameID = conn.getGameID();
        String user = conn.getUserName();

        try {
            var data = gameService.getGameData(gameID);

            if (!user.equals(data.whiteUsername()) && !user.equals(data.blackUsername())) {
                notificationHandler.error(conn, new ErrorMessage("Only players can resign"));
                return;
            }

            if (data.game().isGameOver()) {
                notificationHandler.error(conn, new ErrorMessage("Game already over"));
                return;
            }

            gameService.resignPlayer(gameID, user);
            notificationHandler.notifyGame(gameID, new NotificationMessage(user + " resigned the game"));

        } catch (ResponseException | DataAccessException e) {
            System.err.println("Resign failed: " + e.getMessage());
            notificationHandler.error(conn, new ErrorMessage("Resign failed: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected resign error: " + e.getMessage());
            e.printStackTrace();
            notificationHandler.error(conn, new ErrorMessage("Unexpected error: " + e.getMessage()));
        }
    }

    private void handleLeave(ClientConnection conn) {
        if (conn == null) {
            return;
        }

        int gameID = conn.getGameID();
        String user = conn.getUserName();
        connectionManager.removeConnection(conn.getSession());

        try {
            var data = gameService.getGameData(gameID);
            if (user.equals(data.whiteUsername())) {
                gameService.clearPlayerColor(gameID, ChessGame.TeamColor.WHITE);
            } else if (user.equals(data.blackUsername())) {
                gameService.clearPlayerColor(gameID, ChessGame.TeamColor.BLACK);
            }

            notificationHandler.notifyGame(gameID, new NotificationMessage(user + " left the game"));

        } catch (ResponseException | DataAccessException e) {
            System.err.println("Leave cleanup failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during leave: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendRaw(Session session, String json) {
        if (session == null || !session.isOpen()) {
            System.err.println("Cannot send message: session is null or closed");
            return;
        }

        try {
            session.getRemote().sendString(json);
        } catch (IOException e) {
            System.err.println("Failed to send message: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}