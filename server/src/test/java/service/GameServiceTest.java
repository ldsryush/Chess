package service;

import dataaccess.memory.MemoryGameDAO;
import exception.ResponseException;
import handlers.CreateGameRequest;
import model.GameID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameServiceTest {
    static final MemoryGameDAO gameDAO = new MemoryGameDAO();
    static final GameService gameService = new GameService(gameDAO);

    @BeforeEach
    void clear() {
        gameDAO.clear();
    }

    @Test
    void testCreateGameValid() throws ResponseException {
        GameID gameID = gameService.createGame(new CreateGameRequest("authToken123", "MyGame"));
        Assertions.assertNotNull(gameID);
        Assertions.assertTrue(gameID.gameID() >= 0);
    }

    @Test
    void testCreateGameInvalidNameEmpty() {
        Assertions.assertThrows(ResponseException.class, () -> {
            gameService.createGame(new CreateGameRequest("authToken123", ""));
        });
    }

    @Test
    void testCreateGameInvalidNameWhitespace() {
        Assertions.assertThrows(ResponseException.class, () -> {
            gameService.createGame(new CreateGameRequest("authToken123", "   "));
        });
    }

    @Test
    void testCreateGameInvalidNameNull() {
        Assertions.assertThrows(ResponseException.class, () -> {
            gameService.createGame(new CreateGameRequest("authToken123", null));
        });
    }

    @Test
    void testCreateMultipleGames() throws ResponseException {
        GameID id1 = gameService.createGame(new CreateGameRequest("authToken123", "Alpha"));
        GameID id2 = gameService.createGame(new CreateGameRequest("authToken123", "Beta"));
        GameID id3 = gameService.createGame(new CreateGameRequest("authToken123", "Gamma"));

        Assertions.assertNotEquals(id1.gameID(), id2.gameID());
        Assertions.assertNotEquals(id2.gameID(), id3.gameID());
        Assertions.assertNotEquals(id1.gameID(), id3.gameID());
    }
}
