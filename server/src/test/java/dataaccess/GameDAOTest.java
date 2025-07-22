package dataaccess;

import chess.ChessGame;
import dataaccess.memory.MemoryGameDAO;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameDAOTest {
    MemoryGameDAO dao = new MemoryGameDAO();

    @BeforeEach
    void reset() throws DataAccessException {
        dao.clear();
    }

    @Test
    void testAddAndGetGameSuccess() throws DataAccessException {
        GameData game = new GameData(1, null, null, "CoolGame", new ChessGame());
        dao.addGame(game);
        Assertions.assertEquals(game, dao.getGame(1));
    }

    @Test
    void testGetGameNotFound() throws DataAccessException {
        Assertions.assertNull(dao.getGame(999));
    }
}
