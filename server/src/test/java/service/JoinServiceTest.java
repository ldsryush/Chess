package service;

import dataaccess.DataAccessException;
import dataaccess.memory.MemoryGameDAO;
import exception.ResponseException;
import model.CreateGameRequest;
import model.JoinGameRequest;
import model.AuthData;
import model.GameID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JoinServiceTest {
    static final MemoryGameDAO GAME_DAO = new MemoryGameDAO();
    static final JoinService SERVICE = new JoinService(GAME_DAO);
    static final GameService GAME_SERVICE = new GameService(GAME_DAO);

    @BeforeEach
    void clear() throws DataAccessException {
        GAME_DAO.clear();
    }

    @Test
    void testJoinGameBad() throws DataAccessException, ResponseException {
        AuthData authData = new AuthData("testUser", "12345");
        GameID gameID = GAME_SERVICE.createGame(new CreateGameRequest(authData.authToken(), "testGame"));

        // Invalid game ID
        Assertions.assertThrows(ResponseException.class, () ->
                SERVICE.joinGame(new JoinGameRequest("BLACK", 5), authData));

        // Invalid player color
        Assertions.assertThrows(ResponseException.class, () ->
                SERVICE.joinGame(new JoinGameRequest("green", gameID.gameID()), authData));
    }

    @Test
    void testJoinGameGood() throws DataAccessException, ResponseException {
        AuthData authData = new AuthData("testUser", "12345");
        GameID gameID = GAME_SERVICE.createGame(new CreateGameRequest(authData.authToken(), "testGame"));

        JoinGameRequest req = new JoinGameRequest("WHITE", gameID.gameID());

        // First join succeeds
        Assertions.assertDoesNotThrow(() -> SERVICE.joinGame(req, authData));

        // Second join with same token and color should fail
        Assertions.assertThrows(ResponseException.class, () -> SERVICE.joinGame(req, authData));
    }
}
