package service;

import dataaccess.DataAccessException;
import dataaccess.memory.MemoryGameDAO;
import handlers.CreateGameRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ListServiceTest {
    static final MemoryGameDAO gameDAO = new MemoryGameDAO();
    static final ListService service = new ListService(gameDAO);
    static final GameService gameService = new GameService(gameDAO);

    @BeforeEach
    void clear() throws DataAccessException {
        gameDAO.clear();
    }

    @Test
    void testGetGamesEmpty() {
        try {
            Assertions.assertTrue(service.getGames().isEmpty());
        } catch (DataAccessException e) {
            Assertions.fail("Data access error during empty list check");
        }
    }

    @Test
    void testGetGamesNonEmpty() {
        try {
            gameService.createGame(new CreateGameRequest("game1", "auth1"));
            gameService.createGame(new CreateGameRequest("game2", "auth2"));
            gameService.createGame(new CreateGameRequest("game3", "auth3"));

            Assertions.assertEquals(3, service.getGames().size());
        } catch (DataAccessException e) {
            Assertions.fail("Data access error during non-empty list check");
        }
    }
}
