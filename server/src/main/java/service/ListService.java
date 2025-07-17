package service;

import dataaccess.GameDAO;
import model.GameData;
import model.GameResponseData;

import java.util.Collection;
import java.util.HashSet;

/**
 * Service responsible for retrieving a list of available games.
 */
public class ListService {
    private final GameDAO gameDAO;

    /**
     * Constructs a ListService with the given GameDAO.
     *
     * @param gameDAO the data access object for retrieving game data
     */
    public ListService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    /**
     * Retrieves a collection of GameResponseData objects representing all available games.
     *
     * @return a collection of simplified game data for client responses
     */
    public Collection<GameResponseData> getGames() {
        // Retrieve all stored games
        Collection<GameData> allGames = gameDAO.listGames();

        // Convert each GameData into a GameResponseData
        Collection<GameResponseData> gameResponseData = new HashSet<>();
        for (var game : allGames) {
            gameResponseData.add(new GameResponseData(
                    game.gameID(),
                    game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName()));
        }

        return gameResponseData;
    }
}
