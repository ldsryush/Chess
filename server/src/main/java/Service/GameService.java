package Service;

import chess.ChessGame;
import dataAccess.GameDAO;
import handlers.CreateGameRequest;
import model.GameData;
import model.GameID;

import java.util.Random;

/**
 * Handles requests to create new games
 */
public class GameService {
    private final GameDAO gameDAO;

    /**
     * Receives a GameDAO object to provide access to the game data
     * @param gameDAO GameDAO object providing access to the game data
     */
    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    /**
     * Creates a new game using the data stored in a CreateGameRequest object.
     * Generates a random number to use as the gameID
     * Initializes a new ChessGame and new GameData object to represent the game
     * @param newGame an object containing the name of the new game
     * @return GameID object containing the gameID for the new game
     */
    public GameID createGame(CreateGameRequest newGame) {
//        Generate the game ID
        Random random = new Random();
        int gameID = random.nextInt(1000000);

//        Initialize a new game
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(
                gameID,
                null,
                null,
                newGame.gameName(),
                game
        );
//        Add the game to the database
        this.gameDAO.addGame(gameData);

        return new GameID(gameID);
    }
}
