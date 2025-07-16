package Service;

import chess.ChessGame;
import dataAccess.GameDAO;
import handlers.CreateGameRequest;
import model.GameData;
import model.GameID;

import java.util.Random;

public class GameService {
    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public GameID createGame(CreateGameRequest newGame) {

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
}
