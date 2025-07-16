package Service;

import chess.ChessGame;
import dataAccess.GameDAO;
import exception.ResponseException;
import handlers.CreateGameRequest;
import model.GameData;
import model.GameID;

import java.util.Random;

public class GameService {
    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public GameID createGame(CreateGameRequest newGame) throws ResponseException {
        if (isInvalid(newGame.gameName())) {
            throw new ResponseException(400, "error: bad request");
        }

        Random random = new Random();
        int gameID = random.nextInt(1000000);

        ChessGame game = new ChessGame();
        GameData gameData = new GameData(
                gameID,
                null,
                null,
                newGame.gameName(),
                game
        );
        this.gameDAO.addGame(gameData);

        return new GameID(gameID);
    }

    private boolean isInvalid(String value) {
        return value == null || value.isBlank();
    }
}