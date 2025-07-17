package Service;

import dataAccess.GameDAO;
import exception.ResponseException;
import handlers.JoinGameRequest;
import model.AuthData;
import model.GameData;

import java.util.Objects;

/**
 * Service responsible for handling requests to join an existing game.
 */
public class JoinService {
    private final GameDAO gameDAO;

    /**
     * Constructs a JoinService with the given GameDAO.
     *
     * @param gameDAO the data access object for retrieving and updating games
     */
    public JoinService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    /**
     * Processes a request to join a game as either WHITE or BLACK.
     *
     * @param request   the join game request containing game ID and desired color
     * @param authData  the authenticated user's data
     * @throws ResponseException if the request is invalid or the color is already taken
     */
    public void joinGame(JoinGameRequest request, AuthData authData) throws ResponseException {
        // Validate request parameters
        if (request.gameID() == null || request.playerColor() == null) {
            throw new ResponseException(400, "error: bad request");
        }

        // Retrieve the game from the DAO
        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new ResponseException(400, "error: bad request");
        }

        // Extract current player assignments
        String whiteUsername = game.whiteUsername();
        String blackUsername = game.blackUsername();

        // Assign user to requested color if available
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
        } else {
            // Invalid color value
            throw new ResponseException(400, "error: bad request");
        }

        // Create updated GameData with new player assignment
        GameData newGame = new GameData(
                request.gameID(),
                whiteUsername,
                blackUsername,
                game.gameName(),
                game.game());

        // Persist the updated game
        gameDAO.updateGame(request.gameID(), newGame);
    }
}
