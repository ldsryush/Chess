package service;

import dataaccess.memory.MemoryGameDAO;
import handlers.CreateGameRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import exception.ResponseException;

public class ListServiceTest {
    static final MemoryGameDAO GAME_DAO = new MemoryGameDAO();
    static final ListService SERVICE = new ListService(GAME_DAO);
    static final GameService GAME_SERVICE = new GameService(GAME_DAO);

    @BeforeEach
    void clear() {
        GAME_DAO.clear();
    }

    @Test
    void testGetGamesEmpty() {
        Assertions.assertTrue(SERVICE.getGames().isEmpty());
    }

    @Test
    void testGetGamesNonEmpty() throws ResponseException {
        GAME_SERVICE.createGame(new CreateGameRequest("game1", "WHITE"));
        GAME_SERVICE.createGame(new CreateGameRequest("game2", "BLACK"));
        GAME_SERVICE.createGame(new CreateGameRequest("game3", "WHITE"));

        Assertions.assertEquals(3, SERVICE.getGames().size());
    }
}
