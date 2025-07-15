package dataAccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    void clear();

    void addGame(GameData gameData);

    GameData getGame(int gameID);

    Collection<GameData> listGames();

    void updateGame(Integer integer, GameData newGame);
}
