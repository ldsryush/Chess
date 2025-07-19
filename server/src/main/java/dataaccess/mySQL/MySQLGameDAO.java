package dataaccess.mySQL;

import dataaccess.GameDAO;
import model.GameData;

import java.util.Collection;

public class MySQLGameDAO implements GameDAO {
    @Override
    public void clear() {

    }

    @Override
    public void addGame(GameData gameData) {

    }

    @Override
    public GameData getGame(int gameID) {
        return null;
    }

    @Override
    public Collection<GameData> listGames() {
        return null;
    }

    @Override
    public void updateGame(Integer integer, GameData newGame) {

    }
}