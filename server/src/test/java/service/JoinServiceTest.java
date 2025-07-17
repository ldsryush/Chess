package service;

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
    static final MemoryGameDAO GAME_DAO = new MemoryGameDAO();
    static final JoinService SERVICE = new JoinService(GAME_DAO);
    static final GameService GAME_Service = new GameService(GAME_DAO);

    @BeforeEach
    void clear() {
        GAME_DAO.clear();
    }

    @Test
    void testJoinGameBad() throws ResponseException {
        GameID gameID = GAME_Service.createGame(new CreateGameRequest("testGame", "WHITE"));

        AuthData authData = new AuthData("testUser", "12345");

        Assertions.assertThrows(ResponseException.class, () -> SERVICE.joinGame(new JoinGameRequest("BLACK", 5), authData));
        Assertions.assertThrows(ResponseException.class, () -> SERVICE.joinGame(new JoinGameRequest("green", gameID.gameID()), authData));
    }

    @Test
    void testJoinGameGood() throws ResponseException {
        GameID gameID = GAME_Service.createGame(new CreateGameRequest("testGame", "WHITE"));
        JoinGameRequest req = new JoinGameRequest("WHITE", gameID.gameID());

        AuthData authData = new AuthData("testUser", "12345");

        Assertions.assertDoesNotThrow(() -> SERVICE.joinGame(req, authData));
        Assertions.assertThrows(ResponseException.class, () -> SERVICE.joinGame(req, authData));
    }
}
