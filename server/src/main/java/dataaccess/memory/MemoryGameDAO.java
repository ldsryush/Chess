package dataaccess.memory;

import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;

/**
 * An implementation of GameDAO to store GameData objects in memory
 */
public class MemoryGameDAO implements GameDAO {
    private final HashMap<Integer, GameData> gameList = new HashMap<>();

    /**
     * Clears the entire GameDAO in memory
     */
    @Override
    public void clear() throws DataAccessException {
        gameList.clear();
    }

    /**
     * Adds a new game into a HashMap with gameIDs as keys.
     * Throws DataAccessException if the gameID already exists.
     */
    @Override
    public void addGame(GameData gameData) throws DataAccessException {
        var gameID = gameData.gameID();
        if (gameList.containsKey(gameID)) {
            throw new DataAccessException("Game already exists: ID " + gameID);
        }
        gameList.put(gameID, gameData);
    }

    /**
     * Returns a game associated with a specified ID
     */
    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return gameList.get(gameID);
    }

    /**
     * Lists all the games in memory
     */
    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return gameList.values();
    }

    /**
     * Updates a specified game.
     * Throws DataAccessException if the game does not exist.
     */
    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        var gameID = gameData.gameID();
        if (!gameList.containsKey(gameID)) {
            throw new DataAccessException("Game not found: ID " + gameID);
        }
        gameList.put(gameID, gameData);
    }
}
