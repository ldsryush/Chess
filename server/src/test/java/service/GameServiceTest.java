package service;

import dataaccess.DataAccessException;
import dataaccess.memory.MemoryGameDAO;
import exception.ResponseException;
import model.CreateGameRequest;
import model.GameID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameServiceTest {
    static final MemoryGameDAO GAME_DAO = new MemoryGameDAO();
    static final GameService SERVICE = new GameService(GAME_DAO);

    @BeforeEach
    void clear() throws DataAccessException {
        GAME_DAO.clear();
    }

    @Test
    void testCreateGameNotNull() throws DataAccessException, ResponseException {
        CreateGameRequest newGame = new CreateGameRequest("testToken", "testGame");

        GameID gameID = SERVICE.createGame(newGame);
        Assertions.assertNotNull(GAME_DAO.getGame(gameID.gameID()));
    }

    @Test
    void testCreateGameNull() throws DataAccessException {
        Assertions.assertNull(GAME_DAO.getGame(1234));
    }
}
