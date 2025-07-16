package dataAccess.memory;

import dataAccess.GameDAO;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private final HashMap<Integer, GameData> gameList = new HashMap<>();

    public void clear() {
        gameList.clear();
    }

    @Override
    public void addGame(GameData gameData) {
        var gameID = gameData.gameID();
        gameList.put(gameID, gameData);
    }

    @Override
    public GameData getGame(int gameID) {
        return gameList.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() {
        return gameList.values();
    }

    @Override
    public void updateGame(Integer gameID, GameData gameData) {
        gameList.put(gameID, gameData);
    }
}
