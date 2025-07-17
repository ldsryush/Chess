package service;

import dataaccess.memory.MemoryAuthDAO;
import exception.ResponseException;
import handlers.LogoutRequest;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LogoutServiceTest {
    static final MemoryAuthDAO AUTH_DAO = new MemoryAuthDAO();
    static final LogoutService SERVICE = new LogoutService(AUTH_DAO);

    @BeforeEach
    void clear() {
        AUTH_DAO.clear();
    }

    @Test
    void testLogoutUserInvalid() {
        Assertions.assertThrows(ResponseException.class, () -> SERVICE.logoutUser(new LogoutRequest("1234")));
    }

    @Test
    void testLogoutUserValid() {
        UserData userData = new UserData("realUser", "realPassword", "email@email.com");

        AuthData authData = AUTH_DAO.createAuth(userData);

        Assertions.assertDoesNotThrow(() -> SERVICE.logoutUser(new LogoutRequest(authData.authToken())));
    }
}
