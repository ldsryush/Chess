package service;

import dataaccess.DataAccessException;
import dataaccess.memory.MemoryGameDAO;
import exception.ResponseException;
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
        CreateGameRequest newGame = new CreateGameRequest("testToken", "testGame");

        GameID gameID = null;
        try {
            gameID = service.createGame(newGame);
        } catch (DataAccessException | ResponseException e) {
            Assertions.fail();
        }

        Assertions.assertNotNull(gameDAO.getGame(gameID.gameID()));
    }

    @Test
    void testCreateGameNull() {
        Assertions.assertNull(gameDAO.getGame(1234));
    }
}
