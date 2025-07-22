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
    void testJoinGameBad() throws DataAccessException, ResponseException {
        AuthData authData = new AuthData("testUser", "12345");
        GameID gameID = gameService.createGame(new CreateGameRequest(authData.authToken(), "testGame"));

        // Invalid game ID
        Assertions.assertThrows(ResponseException.class, () ->
                service.joinGame(new JoinGameRequest("BLACK", 5), authData));

        // Invalid player color
        Assertions.assertThrows(ResponseException.class, () ->
                service.joinGame(new JoinGameRequest("green", gameID.gameID()), authData));
    }

    @Test
    void testJoinGameGood() throws DataAccessException, ResponseException {
        AuthData authData = new AuthData("testUser", "12345");
        GameID gameID = gameService.createGame(new CreateGameRequest(authData.authToken(), "testGame"));

        JoinGameRequest req = new JoinGameRequest("WHITE", gameID.gameID());

        // First join succeeds
        Assertions.assertDoesNotThrow(() -> service.joinGame(req, authData));

        // Second join with same token and color should fail
        Assertions.assertThrows(ResponseException.class, () -> service.joinGame(req, authData));
    }
}
