package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import exception.ResponseException;
import model.CreateGameRequest;
import model.GameData;
import model.GameID;

import java.util.Random;

/**
 * Handles game-related operations: creation, retrieval, moves, resignations.
 */
public class GameService {
    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public GameID createGame(CreateGameRequest newGame) throws ResponseException, DataAccessException {
        if (newGame == null || newGame.gameName() == null || newGame.gameName().isBlank()) {
            throw new ResponseException(400, "Missing or invalid game name");
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

    public GameData getGameData(int gameID) throws ResponseException, DataAccessException {
        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            throw new ResponseException(400, "Game not found");
        }
        return gameData;
    }

    public void makeMove(int gameID, String username, chess.ChessMove move) throws ResponseException, DataAccessException {
        // Synchronize on gameID to prevent concurrent modifications to the same game
        synchronized (("game_move_" + gameID).intern()) {
            GameData gameData = getGameData(gameID);
            ChessGame game = gameData.game();

            // Additional validation - ensure the game isn't over
            if (game.isGameOver()) {
                throw new ResponseException(400, "Cannot move: game is already over");
            }

            try {
                game.makeMove(move);
            } catch (Exception e) {
                throw new ResponseException(400, "Invalid move: " + e.getMessage());
            }

            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            );

            gameDAO.updateGame(updatedGame);
        }
    }

    public void resignPlayer(int gameID, String username) throws ResponseException, DataAccessException {
        GameData gameData = getGameData(gameID);
        ChessGame game = gameData.game();

        game.setGameOver(true);

        GameData updatedGame = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );

        gameDAO.updateGame(updatedGame);
    }

    /**
     * Clears the player assignment for a given color in the game.
     *
     * @param gameID the ID of the game
     * @param color the color to clear (WHITE or BLACK)
     * @throws DataAccessException if a database error occurs
     */
    public void clearPlayerColor(int gameID, ChessGame.TeamColor color) throws DataAccessException {
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            return;
        }

        String white = game.whiteUsername();
        String black = game.blackUsername();

        if (color == ChessGame.TeamColor.WHITE) {
            white = null;
        } else if (color == ChessGame.TeamColor.BLACK) {
            black = null;
        }

        GameData updatedGame = new GameData(
                game.gameID(),
                white,
                black,
                game.gameName(),
                game.game()
        );

        gameDAO.updateGame(updatedGame);
    }
}
