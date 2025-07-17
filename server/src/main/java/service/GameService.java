package service;

import chess.ChessGame;
import dataaccess.GameDAO;
import exception.ResponseException;
import handlers.CreateGameRequest;
import model.GameData;
import model.GameID;

import java.util.Random;

/**
 * Service responsible for creating new chess games.
 */
public class GameService {
    private final GameDAO gameDAO;

    /**
     * Constructs a GameService with the given GameDAO.
     *
     * @param gameDAO the data access object for storing game data
     */
    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    /**
     * Creates a new game using the provided request data.
     *
     * @param newGame the request containing the desired game name
     * @return a GameID representing the newly created game
     * @throws ResponseException if the game name is invalid
     */
    public GameID createGame(CreateGameRequest newGame) throws ResponseException {
        // Validate game name
        if (isInvalid(newGame.gameName())) {
            throw new ResponseException(400, "error: bad request");
        }

        // Generate a random game ID
        Random random = new Random();
        int gameID = random.nextInt(1000000);

        // Create a new ChessGame instance
        ChessGame game = new ChessGame();

        // Wrap game data into a GameData object
        GameData gameData = new GameData(
                gameID,
                null,               // white player
                null,               // black player
                newGame.gameName(), // game name
                game                // actual game instance
        );

        // Store the game in the DAO
        this.gameDAO.addGame(gameData);

        // Return the game ID
        return new GameID(gameID);
    }

    /**
     * Checks if a string value is null or blank.
     *
     * @param value the string to validate
     * @return true if the value is invalid, false otherwise
     */
    private boolean isInvalid(String value) {
        return value == null || value.isBlank();
    }
}
