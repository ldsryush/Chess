package dataaccess;

import model.GameData;
import java.util.*;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextID = 1;

    public GameData createGame(String gameName, String username) {
        GameData game = new GameData(nextID++, null, null, gameName, null);
        games.put(game.getGameID(), game);
        return game;
    }

    public List<GameData> listGames() {
        return new ArrayList<>(games.values());
    }

    public GameData getGame(int gameID) throws DataAccessException {
        if (!games.containsKey(gameID)) {
            throw new DataAccessException("Game ID not found.");
        }
        return games.get(gameID);
    }

    public void updateGame(GameData game) throws DataAccessException {
        if (!games.containsKey(game.getGameID())) {
            throw new DataAccessException("Game not found.");
        }
        games.put(game.getGameID(), game);
    }

    public void clear() {
        games.clear();
        nextID = 1;
    }
}
