package Service;

import dataAccess.GameDAO;
import model.GameData;
import model.GameResponseData;

import java.util.Collection;
import java.util.HashSet;

public class ListService {
    private final GameDAO gameDAO;

    public ListService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public Collection<GameResponseData> getGames() {
        Collection<GameData> allGames = gameDAO.listGames();
        Collection<GameResponseData> gameResponseData = new HashSet<>();
        for (var game : allGames) {
            gameResponseData.add(new GameResponseData(
                    game.gameID(), game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName()));
        }

        return gameResponseData;
    }
}
