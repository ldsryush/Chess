package service;

import dataaccess.DataAccessException;
import dataaccess.memory.MemoryGameDAO;
import exception.ResponseException;
import handlers.CreateGameRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ListServiceTest {
    static final MemoryGameDAO GAME_DAO = new MemoryGameDAO();
    static final ListService SERVICE = new ListService(GAME_DAO);
    static final GameService GAME_SERVICE = new GameService(GAME_DAO);

    @BeforeEach
    void clear() throws DataAccessException {
        GAME_DAO.clear();
    }

    @Test
    void testGetGamesEmpty() {
        try {
            Assertions.assertTrue(SERVICE.getGames().isEmpty());
        } catch (DataAccessException e) {
            Assertions.fail("Data access error during empty list check");
        }
    }

    @Test
    void testGetGamesNonEmpty() {
        try {
            GAME_SERVICE.createGame(new CreateGameRequest("auth1", "game1"));
            GAME_SERVICE.createGame(new CreateGameRequest("auth2", "game2"));
            GAME_SERVICE.createGame(new CreateGameRequest("auth3", "game3"));

            Assertions.assertEquals(3, SERVICE.getGames().size());
        } catch (DataAccessException | ResponseException e) {
            Assertions.fail("Error during non-empty list check: " + e.getMessage());
        }
    }
}