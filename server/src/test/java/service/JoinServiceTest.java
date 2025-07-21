package service;

import dataaccess.DataAccessException;
import dataaccess.memory.MemoryGameDAO;
import exception.ResponseException;
import handlers.CreateGameRequest;
import handlers.JoinGameRequest;
import model.AuthData;
import model.GameID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JoinServiceTest {
    static final MemoryGameDAO gameDAO = new MemoryGameDAO();
    static final JoinService service = new JoinService(gameDAO);
    static final GameService gameService = new GameService(gameDAO);

    @BeforeEach
    void clear() throws DataAccessException {
        gameDAO.clear();
    }

    @Test
    void testJoinGameBad() {
        try {
            CreateGameRequest createReq = new CreateGameRequest("testGame", "12345");
            GameID gameID = gameService.createGame(createReq);

            AuthData authData = new AuthData("testUser", "12345");

            // Invalid game ID
            Assertions.assertThrows(ResponseException.class,
                    () -> service.joinGame(new JoinGameRequest("BLACK", 99999), authData));

            // Invalid color
            Assertions.assertThrows(ResponseException.class,
                    () -> service.joinGame(new JoinGameRequest("green", gameID.gameID()), authData));

        } catch (DataAccessException e) {
            Assertions.fail();
        }
    }

    @Test
    void testJoinGameGood() {
        try {
            CreateGameRequest createReq = new CreateGameRequest("testGame", "12345");
            GameID gameID = gameService.createGame(createReq);
            JoinGameRequest req = new JoinGameRequest("WHITE", gameID.gameID());
            AuthData authData = new AuthData("testUser", "12345");

            // Valid join
            Assertions.assertDoesNotThrow(() -> service.joinGame(req, authData));

            // Attempt to join again with same user (should be rejected)
            Assertions.assertThrows(ResponseException.class, () -> service.joinGame(req, authData));

        } catch (DataAccessException e) {
            Assertions.fail();
        }
    }
}
