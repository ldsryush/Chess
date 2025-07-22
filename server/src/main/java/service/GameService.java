package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import exception.ResponseException;
import handlers.CreateGameRequest;
import model.GameData;
import model.GameID;

import java.util.Random;

/**
 * Handles requests to create new games
 */
public class GameService {
    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    /**
     * Creates a new game using the data stored in a CreateGameRequest object.
     * Validates input, generates a random gameID, and stores the game.
     *
     * @param newGame an object containing the name of the new game
     * @return GameID object containing the gameID for the new game
     * @throws ResponseException    if the game name is missing or invalid (400)
     * @throws DataAccessException if a database error occurs (500)
     */
    public GameID createGame(CreateGameRequest newGame) throws ResponseException, DataAccessException {
        // ✅ Validate input
        if (newGame == null || newGame.gameName() == null || newGame.gameName().isBlank()) {
            throw new ResponseException(400, "error: missing or invalid game name");
        }

        Random random = new Random();
        int gameID = random.nextInt(1_000_000);

        ChessGame game = new ChessGame();
        GameData gameData = new GameData(
                gameID,
                null,
                null,
                newGame.gameName(),
                game
        );

        gameDAO.addGame(gameData);

        return new GameID(gameID);
    }
}