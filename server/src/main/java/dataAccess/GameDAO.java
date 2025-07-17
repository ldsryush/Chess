package dataAccess;

import model.GameData;

import java.util.Collection;

/**
 * Interface for managing game data in the persistence layer.
 */
public interface GameDAO {

    /**
     * Clears all stored game data.
     */
    void clear();

    /**
     * Adds a new game to the data store.
     *
     * @param gameData the game to add
     */
    void addGame(GameData gameData);

    /**
     * Retrieves a game by its unique ID.
     *
     * @param gameID the ID of the game to retrieve
     * @return the GameData object, or null if not found
     */
    GameData getGame(int gameID);

    /**
     * Lists all stored games.
     *
     * @return a collection of all GameData objects
     */
    Collection<GameData> listGames();

    /**
     * Updates an existing game with new data.
     *
     * @param gameID   the ID of the game to update
     * @param newGame  the updated GameData
     */
    void updateGame(Integer gameID, GameData newGame);
}
