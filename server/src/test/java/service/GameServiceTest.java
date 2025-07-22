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
    void testCreateGameNotNull() throws DataAccessException, ResponseException {
        CreateGameRequest newGame = new CreateGameRequest("testToken", "testGame");

        GameID gameID = service.createGame(newGame);
        Assertions.assertNotNull(gameDAO.getGame(gameID.gameID()));
    }

    @Test
    void testCreateGameNull() throws DataAccessException {
        Assertions.assertNull(gameDAO.getGame(1234));
    }
}
