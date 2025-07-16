package Service;

import dataAccess.GameDAO;
import exception.ResponseException;
import handlers.JoinGameRequest;
import model.AuthData;
import model.GameData;

import java.util.Objects;

/**
 * Handles requests to join a game
 */
public class JoinService {
    private final GameDAO gameDAO;

    /**
     * Receives a GameDAO object to provide access to the game data
     * @param gameDAO GameDAO object providing access to the game data
     */
    public JoinService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    /**
     * Joins a game using the data from the request to join the game
     * as well as the username found from authenticating the user.
     * @param request JoinGameRequest object, containing playerColor and gameID
     * @param authData AuthData object used to get the player's username
     * @throws ResponseException if (a) game doesn't exist, (b) color is already taken, or (c) color is invalid
     */
    public void joinGame(JoinGameRequest request, AuthData authData) throws ResponseException {
//        Ensures that the game with the requested ID exists
        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new ResponseException(400, "error: bad request");
        }

//        Assigns the new usernames, making sure they haven't been taken already (also ensures valid color)
        String whiteUsername = game.whiteUsername();
        String blackUsername = game.blackUsername();

        if (Objects.equals(request.playerColor(), "WHITE")) {
            if (whiteUsername != null) {
                throw new ResponseException(403, "error: already taken");
            }
            whiteUsername = authData.username();
        } else if (Objects.equals(request.playerColor(), "BLACK")) {
            if (blackUsername != null) {
                throw new ResponseException(403, "error: already taken");
            }
            blackUsername = authData.username();
        } else if (request.playerColor() != null) {
            throw new ResponseException(400, "error: bad request");
        }

        GameData newGame = new GameData(
                request.gameID(),
                whiteUsername,
                blackUsername,
                game.gameName(),
                game.game());

        gameDAO.updateGame(request.gameID(), newGame);
    }

}
