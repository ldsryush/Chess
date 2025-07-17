package dataAccess.memory;

import dataAccess.GameDAO;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;

/**
 * In-memory implementation of GameDAO for managing game data.
 */
public class MemoryGameDAO implements GameDAO {

    // Stores games mapped by their unique game ID
    private final HashMap<Integer, GameData> gameStore = new HashMap<>();

    /**
     * Clears all stored games.
     */
    public void clear() {
        gameStore.clear();
    }

    /**
     * Adds a new game to the store.
     *
     * @param gameData the game to add
     */
    @Override
    public void addGame(GameData gameData) {
        int gameID = gameData.gameID();
        gameStore.put(gameID, gameData);
    }

    /**
     * Retrieves a game by its ID.
     *
     * @param gameID the ID of the game
     * @return the GameData, or null if not found
     */
    @Override
    public GameData getGame(int gameID) {
        return gameStore.get(gameID);
    }

    /**
     * Lists all stored games.
     *
     * @return a collection of all GameData objects
     */
    @Override
    public Collection<GameData> listGames() {
        return gameStore.values();
    }

    /**
     * Updates an existing game.
     *
     * @param gameID   the ID of the game to update
     * @param gameData the new game data
     */
    @Override
    public void updateGame(Integer gameID, GameData gameData) {
        gameStore.put(gameID, gameData);
    }
}
