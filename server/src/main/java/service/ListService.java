package service;

import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import model.GameResponseData;

import java.util.Collection;
import java.util.HashSet;

/**
 * Handles requests to list all the games in the database
 */
public class ListService {
    private final GameDAO gameDAO;

    /**
     * Receives a GameDAO object to provide access to the game data
     * @param gameDAO GameDAO object providing access to the game data
     */
    public ListService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    /**
     * Returns a HashSet of all the GameResponseData
     * Contains only the gameID, player usernames, and gameName (not the ChessGame object)
     * @return Collection of GameResponseData containing data for all the games
     */
    public Collection<GameResponseData> getGames() throws DataAccessException {
        Collection<GameData> allGames = gameDAO.listGames();
        Collection<GameResponseData> gameResponseData = new HashSet<>();
        for (var game : allGames) {
            gameResponseData.add(new GameResponseData(
                    game.gameID(), game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName()));
        }

        return gameResponseData;
    }
}
