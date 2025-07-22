
package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    void clear() throws dataaccess.DataAccessException;

    void addGame(GameData gameData) throws dataaccess.DataAccessException;

    GameData getGame(int gameID) throws dataaccess.DataAccessException;

    Collection<GameData> listGames() throws dataaccess.DataAccessException;

    void updateGame(GameData newGame) throws dataaccess.DataAccessException;
}
