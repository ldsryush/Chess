package dataaccess;

import model.GameData;
import java.util.List;

public interface GameDAO {
    GameData createGame(String gameName, String username) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    void clear();
}
