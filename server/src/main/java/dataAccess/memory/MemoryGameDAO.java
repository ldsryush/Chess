package dataAccess.memory;

import dataAccess.GameDAO;
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
    public void clear() {
        gameList.clear();
    }

    /**
     * Adds a new game into a HashMap with gameIDs as keys
     * @param gameData GameData containing all necessary
     */
    @Override
    public void addGame(GameData gameData) {
        var gameID = gameData.gameID();
        gameList.put(gameID, gameData);
    }

    /**
     * Returns a game associated with a specified ID
     * @param gameID the gameID of the game desired
     * @return GameData object containing all the data on the game desired
     */
    @Override
    public GameData getGame(int gameID) {
        return gameList.get(gameID);
    }

    /**
     * Lists all the games in memory
     * @return Collection of GameData objects - all games in memory
     */
    @Override
    public Collection<GameData> listGames() {
        return gameList.values();
    }

    /**
     * Updates a specified game
     * @param gameID the gameID of the game to update
     * @param gameData GameData object containing the updated game
     */
    @Override
    public void updateGame(Integer gameID, GameData gameData) {
        gameList.put(gameID, gameData);
    }
}
