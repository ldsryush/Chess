package service;

import dataAccess.memory.MemoryGameDAO;
import exception.ResponseException;
import handlers.CreateGameRequest;
import handlers.JoinGameRequest;
import model.AuthData;
import model.GameID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import Service.JoinService;
import Service.GameService;

public class JoinServiceTest {
    static final MemoryGameDAO gameDAO = new MemoryGameDAO();
    static final JoinService service = new JoinService(gameDAO);
    static final GameService gameService = new GameService(gameDAO);

    @BeforeEach
    void clear() {
        gameDAO.clear();
    }

    @Test
    void testJoinGameBad() throws ResponseException {
        GameID gameID = gameService.createGame(new CreateGameRequest("testGame", "WHITE"));

        AuthData authData = new AuthData("testUser", "12345");

        Assertions.assertThrows(ResponseException.class, () -> service.joinGame(new JoinGameRequest("BLACK", 5), authData));
        Assertions.assertThrows(ResponseException.class, () -> service.joinGame(new JoinGameRequest("green", gameID.gameID()), authData));
    }

    @Test
    void testJoinGameGood() throws ResponseException {
        GameID gameID = gameService.createGame(new CreateGameRequest("testGame", "WHITE"));
        JoinGameRequest req = new JoinGameRequest("WHITE", gameID.gameID());

        AuthData authData = new AuthData("testUser", "12345");

        Assertions.assertDoesNotThrow(() -> service.joinGame(req, authData));
        Assertions.assertThrows(ResponseException.class, () -> service.joinGame(req, authData));
    }
}
