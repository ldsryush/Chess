package service;

import dataaccess.DataAccessException;
import dataaccess.memory.MemoryGameDAO;
import exception.ResponseException;
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
            gameService.createGame(new CreateGameRequest("auth1", "game1"));
            gameService.createGame(new CreateGameRequest("auth2", "game2"));
            gameService.createGame(new CreateGameRequest("auth3", "game3"));

            Assertions.assertEquals(3, service.getGames().size());
        } catch (DataAccessException | ResponseException e) {
            Assertions.fail("Error during non-empty list check: " + e.getMessage());
        }
    }
}