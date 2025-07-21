package service;

import dataaccess.DataAccessException;
import dataaccess.memory.MemoryGameDAO;
import handlers.CreateGameRequest;
import model.GameID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameServiceTest {
    static final MemoryGameDAO gameDAO = new MemoryGameDAO();
    static final GameService service = new GameService(gameDAO);

    @BeforeEach
    void clear() throws DataAccessException {
        gameDAO.clear();
    }

    @Test
    void testCreateGameNotNull() {
        CreateGameRequest newGame = new CreateGameRequest("testGame", "testToken");

        GameID gameID = null;
        try {
            gameID = service.createGame(newGame);
        } catch (DataAccessException e) {
            Assertions.fail();
        }

        // Positive test case: game should be retrievable
        Assertions.assertNotNull(gameDAO.getGame(gameID.gameID()));
    }

    @Test
    void testCreateGameNull() {
        // Negative test case: game ID 1234 should not exist
        Assertions.assertNull(gameDAO.getGame(1234));
    }
}
