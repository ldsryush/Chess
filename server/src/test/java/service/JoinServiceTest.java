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
    void clear() {
        gameDAO.clear();
    }

    @Test
    void testJoinGameBad() {
        try {
            AuthData authData = new AuthData("testUser", "12345");
            GameID gameID = gameService.createGame(new CreateGameRequest(authData.authToken(), "testGame"));

            Assertions.assertThrows(ResponseException.class, () ->
                    service.joinGame(new JoinGameRequest("BLACK", 5), authData));

            Assertions.assertThrows(ResponseException.class, () ->
                    service.joinGame(new JoinGameRequest("green", gameID.gameID()), authData));
        } catch (DataAccessException | ResponseException e) {
            Assertions.fail();
        }
    }

    @Test
    void testJoinGameGood() {
        try {
            AuthData authData = new AuthData("testUser", "12345");
            GameID gameID = gameService.createGame(new CreateGameRequest(authData.authToken(), "testGame"));

            JoinGameRequest req = new JoinGameRequest("WHITE", gameID.gameID());

            Assertions.assertDoesNotThrow(() -> service.joinGame(req, authData));

            Assertions.assertThrows(ResponseException.class, () -> service.joinGame(req, authData));
        } catch (DataAccessException | ResponseException e) {
            Assertions.fail();
        }
    }
}
