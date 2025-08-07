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

    private static final Gson gson = new Gson(); // ✅ Direct Gson instance

    private static GameService gameService;
    private static AuthenticationService authService;
    private static JoinService joinService;
    private static NotificationHandler notificationHandler;

    public static void configure(GameService gs, AuthenticationService as, JoinService js, NotificationHandler nh) {
        gameService = gs;
        authService = as;
        joinService = js;
        notificationHandler = nh;
    }

    private final ConnectionManager connectionManager = new ConnectionManager();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Client connected: " + session.getRemoteAddress());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("Received: " + message);
        try {
            UserGameCommand cmd = gson.fromJson(message, UserGameCommand.class);
            CommandType type = cmd.getCommandType();

            switch (type) {
                case CONNECT -> handleConnect(session, cmd);
                case MAKE_MOVE, RESIGN -> {
                    ClientConnection conn = connectionManager.getConnection(session);
                    if (conn == null) {
                        sendRaw(session, gson.toJson(new ErrorMessage("No active connection")));
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
                        sendRaw(session, gson.toJson(new ErrorMessage("No active connection")));
                        return;
                    }
                    handleLeave(conn);
                }
                default -> sendRaw(session, gson.toJson(new ErrorMessage("Unknown command")));
            }

        } catch (Exception e) {
            sendRaw(session, gson.toJson(new ErrorMessage("Invalid command format")));
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int status, String reason) {
        System.out.println("Client disconnected: " + session.getRemoteAddress() + " Reason: " + reason);
        ClientConnection conn = connectionManager.getConnection(session);
        if (conn != null) handleLeave(conn);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
        ClientConnection conn = connectionManager.getConnection(session);
        if (conn != null) handleLeave(conn);
    }

    private void handleConnect(Session session, UserGameCommand cmd) {
        try {
            AuthData auth = authService.getAuthData(cmd.getAuthToken());
            if (auth == null) {
                sendRaw(session, gson.toJson(new ErrorMessage("Invalid auth token")));
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
            notificationHandler.notifyOthers(conn, new NotificationMessage(roleMsg)); // ✅ Broadcast to others

        } catch (ResponseException | DataAccessException e) {
            sendRaw(session, gson.toJson(new ErrorMessage("Connect failed: " + e.getMessage())));
        } catch (Exception e) {
            sendRaw(session, gson.toJson(new ErrorMessage("Unexpected error: " + e.getMessage())));
        }
    }

    private void handleMove(ClientConnection conn, UserGameCommand cmd) {
        int gameID = conn.getGameID();
        String user = conn.getUserName();

        try {
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
            notificationHandler.error(conn, new ErrorMessage("Move failed: " + e.getMessage()));
        } catch (Exception e) {
            notificationHandler.error(conn, new ErrorMessage("Unexpected error: " + e.getMessage()));
        }
    }

    private void handleResign(ClientConnection conn) {
        if (conn == null) return;

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
            notificationHandler.error(conn, new ErrorMessage("Resign failed: " + e.getMessage()));
        } catch (Exception e) {
            notificationHandler.error(conn, new ErrorMessage("Unexpected error: " + e.getMessage()));
        }
    }

    private void handleLeave(ClientConnection conn) {
        if (conn == null) return;

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
        }
    }

    private void sendRaw(Session session, String json) {
        try {
            session.getRemote().sendString(json);
        } catch (IOException e) {
            System.err.println("Failed to send message: " + e.getMessage());
        }
    }
}
