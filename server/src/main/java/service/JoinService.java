package service;

import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import exception.ResponseException;
import model.JoinGameRequest;
import model.AuthData;
import model.GameData;

import java.util.Objects;

/**
 * Handles requests to join or observe a game
 */
public class JoinService {
    private final GameDAO gameDAO;

    public JoinService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    /**
     * Joins a game using the data from the request to join the game
     * as well as the username found from authenticating the user.
     *
     * @param request  JoinGameRequest object, containing playerColor and gameID
     * @param authData AuthData object used to get the player's username
     * @throws ResponseException    if game doesn't exist, color is already taken or invalid
     * @throws DataAccessException if a database error occurs
     */
    public void joinGame(JoinGameRequest request, AuthData authData) throws ResponseException, DataAccessException {
        if (request == null || request.gameID() == null || request.playerColor() == null || request.playerColor().isBlank()) {
            throw new ResponseException(400, "error: missing required fields");
        }

        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new ResponseException(400, "error: game not found");
        }

        String whiteUsername = game.whiteUsername();
        String blackUsername = game.blackUsername();

        String requester = authData.username();

        switch (request.playerColor().toUpperCase()) {
            case "WHITE" -> {
                if (whiteUsername != null) {
                    throw new ResponseException(403, "error: color already taken");
                }
                whiteUsername = requester;
            }
            case "BLACK" -> {
                if (blackUsername != null) {
                    throw new ResponseException(403, "error: color already taken");
                }
                blackUsername = requester;
            }
            case "OBSERVER" -> {
                // No updates to game state — observer is allowed silently
                return;
            }
            default -> throw new ResponseException(400, "error: invalid color");
        }

        GameData updatedGame = new GameData(
                request.gameID(),
                whiteUsername,
                blackUsername,
                game.gameName(),
                game.game()
        );

        gameDAO.updateGame(updatedGame);
    }

    public void observeGame(int gameID, AuthData authData) throws ResponseException, DataAccessException {
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new ResponseException(400, "error: game not found");
        }
    }
}