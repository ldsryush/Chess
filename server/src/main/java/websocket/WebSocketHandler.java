package websocket;

import chess.ChessGame;
import dataaccess.DataAccessException;
import exception.ResponseException;
import model.AuthData;
import model.JoinGameRequest;
import service.AuthenticationService;
import service.GameService;
import service.JoinService;
import websocket.commands.UserGameCommand;
import websocket.commands.UserGameCommand.CommandType;
import websocket.messages.*;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

import static websocket.GsonFactory.gson;

@ServerEndpoint(value = "/connect")
public class WebSocketHandler {

    private static GameService gameService;
    private static AuthenticationService authService;
    private static JoinService joinService;

    public static void configure(GameService gs, AuthenticationService as, JoinService js) {
        gameService = gs;
        authService = as;
        joinService = js;
    }

    private final ConnectionManager connectionManager = new ConnectionManager();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Client connected: " + session.getId());
    }

    @OnMessage
    public void onMessage(String json, Session session) {
        System.out.println("Received: " + json);
        try {
            UserGameCommand cmd = gson.fromJson(json, UserGameCommand.class);
            CommandType type = cmd.getCommandType();

            switch (type) {
                case CONNECT -> handleConnect(session, cmd);
                case MAKE_MOVE -> handleMove(connectionManager.getConnection(session), cmd);
                case RESIGN -> handleResign(connectionManager.getConnection(session));
                case LEAVE -> handleLeave(connectionManager.getConnection(cmd.getAuthToken()));
                default -> sendRaw(session, new ErrorMessage("Unknown command"));
            }

        } catch (Exception e) {
            sendRaw(session, new ErrorMessage("Invalid command format"));
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("Client disconnected: " + session.getId() + " Reason: " + reason);
        ClientConnection conn = connectionManager.getConnection(session);
        if (conn != null) handleLeave(conn);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
        ClientConnection conn = connectionManager.getConnection(session);
        if (conn != null) handleLeave(conn);
    }

    private void handleConnect(Session session, UserGameCommand cmd) {
        try {
            AuthData auth = authService.getAuthData(cmd.getAuthToken());
            if (auth == null) {
                sendRaw(session, new ErrorMessage("Invalid auth token"));
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
            conn.send(new LoadGameMessage(game, color));

            String roleMsg = switch (color) {
                case "white", "black" -> user + " connected to the game as " + color;
                default -> user + " connected as observer";
            };
            connectionManager.broadcastToOthers(gameID, conn, new NotificationMessage(roleMsg));

        } catch (ResponseException | DataAccessException e) {
            sendRaw(session, new ErrorMessage("Connect failed: " + e.getMessage()));
        } catch (Exception e) {
            sendRaw(session, new ErrorMessage("Unexpected error: " + e.getMessage()));
        }
    }

    private void handleMove(ClientConnection conn, UserGameCommand cmd) {
        if (conn == null) {
            sendRaw(cmd.getAuthToken(), new ErrorMessage("No active connection"));
            return;
        }

        int gameID = conn.getGameID();
        String user = conn.getUserName();

        try {
            var data = gameService.getGameData(gameID);
            ChessGame game = data.game();

            boolean validTurn = (game.getTeamTurn() == ChessGame.TeamColor.WHITE && user.equals(data.whiteUsername()))
                    || (game.getTeamTurn() == ChessGame.TeamColor.BLACK && user.equals(data.blackUsername()));

            if (!validTurn) {
                conn.send(new ErrorMessage("Invalid move: not your turn"));
                return;
            }

            gameService.makeMove(gameID, user, cmd.getMove());
            ChessGame updated = gameService.getGameData(gameID).game();

            conn.send(new LoadGameMessage(updated, conn.getPlayerColor()));
            connectionManager.broadcastToOthers(gameID, conn, new LoadGameMessage(updated, conn.getPlayerColor()));

            var mv = cmd.getMove();
            var piece = updated.getBoard().getPiece(mv.getEndPosition());
            String text = String.format("%s moved %s from %s to %s",
                    user,
                    piece != null ? piece.getPieceType() : "a piece",
                    mv.getStartPosition(),
                    mv.getEndPosition());
            connectionManager.broadcastToOthers(gameID, conn, new NotificationMessage(text));

            if (updated.isGameOver()) {
                String loser = updated.getTeamTurn() == ChessGame.TeamColor.WHITE
                        ? data.whiteUsername()
                        : data.blackUsername();
                connectionManager.broadcastToGame(gameID, new NotificationMessage(loser + " is in checkmate"));
            } else if (updated.isInCheck(updated.getTeamTurn())) {
                String checkedPlayer = updated.getTeamTurn() == ChessGame.TeamColor.WHITE
                        ? data.whiteUsername()
                        : data.blackUsername();
                connectionManager.broadcastToGame(gameID, new NotificationMessage(checkedPlayer + " is in check"));
            }

        } catch (ResponseException | DataAccessException e) {
            conn.send(new ErrorMessage("Move failed: " + e.getMessage()));
        } catch (Exception e) {
            conn.send(new ErrorMessage("Unexpected error: " + e.getMessage()));
        }
    }

    private void handleResign(ClientConnection conn) {
        if (conn == null) return;

        int gameID = conn.getGameID();
        String user = conn.getUserName();

        try {
            var data = gameService.getGameData(gameID);

            if (!user.equals(data.whiteUsername()) && !user.equals(data.blackUsername())) {
                conn.send(new ErrorMessage("Only players can resign"));
                return;
            }

            if (data.game().isGameOver()) {
                conn.send(new ErrorMessage("Game already over"));
                return;
            }

            gameService.resignPlayer(gameID, user);
            connectionManager.broadcastToGame(gameID, new NotificationMessage(user + " resigned the game"));

        } catch (ResponseException | DataAccessException e) {
            conn.send(new ErrorMessage("Resign failed: " + e.getMessage()));
        } catch (Exception e) {
            conn.send(new ErrorMessage("Unexpected error: " + e.getMessage()));
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

            connectionManager.broadcastToGame(gameID, new NotificationMessage(user + " left the game"));

        } catch (ResponseException | DataAccessException e) {
            System.err.println("Leave cleanup failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during leave: " + e.getMessage());
        }
    }

    private void sendRaw(Session session, ServerMessage message) {
        try {
            session.getBasicRemote().sendText(gson.toJson(message));
        } catch (IOException e) {
            System.err.println("Failed to send message: " + e.getMessage());
        }
    }

    private void sendRaw(String authToken, ServerMessage message) {
        ClientConnection conn = connectionManager.getConnection(authToken);
        if (conn != null) sendRaw(conn.getSession(), message);
    }
}
