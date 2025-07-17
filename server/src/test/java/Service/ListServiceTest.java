package Service;

import dataAccess.memory.MemoryGameDAO;
import handlers.CreateGameRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import exception.ResponseException;

public class ListServiceTest {
    static final MemoryGameDAO gameDAO = new MemoryGameDAO();
    static final ListService service = new ListService(gameDAO);
    static final GameService gameService = new GameService(gameDAO);

    @BeforeEach
    void clear() {
        gameDAO.clear();
    }

    @Test
    void testGetGamesEmpty() {
        Assertions.assertTrue(service.getGames().isEmpty());
    }

    @Test
    void testGetGamesNonEmpty() throws ResponseException {
        gameService.createGame(new CreateGameRequest("game1", "WHITE"));
        gameService.createGame(new CreateGameRequest("game2", "BLACK"));
        gameService.createGame(new CreateGameRequest("game3", "WHITE"));

        Assertions.assertEquals(3, service.getGames().size());
    }
}
