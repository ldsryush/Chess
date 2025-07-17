package service;

import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryUserDAO;
import exception.ResponseException;
import handlers.LoginRequest;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoginServiceTest {
    static final MemoryUserDAO USER_DAO = new MemoryUserDAO();
    static final MemoryAuthDAO AUTH_DAO = new MemoryAuthDAO();
    static final LoginService SERVICE = new LoginService(USER_DAO, AUTH_DAO);

    @BeforeEach
    void clear() {
        USER_DAO.clear();
        AUTH_DAO.clear();
    }

    @Test
    void testLoginUnauthorized() {
        Assertions.assertThrows(ResponseException.class, () -> SERVICE.login(new LoginRequest("fakeName", "1234")));
    }

    @Test
    void testLoginValid() throws ResponseException {


        UserData userData = new UserData("realName", "realPassword", "realEmail@email.com");

        USER_DAO.createUser(userData);

        Assertions.assertDoesNotThrow(() -> SERVICE.login(new LoginRequest(userData.username(), userData.password())));
        Assertions.assertInstanceOf(AuthData.class, SERVICE.login(new LoginRequest(userData.username(), userData.password())));
    }
}
