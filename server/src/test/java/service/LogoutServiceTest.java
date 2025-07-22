package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.memory.MemoryAuthDAO;
import dataaccess.mysql.MySQLAuthDAO;
import exception.ResponseException;
import handlers.LogoutRequest;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LogoutServiceTest {
    static final AuthDAO AUTH_DAO;
    static {
        AuthDAO temp;
        try {
            temp = new MySQLAuthDAO();
        } catch (Exception e) {
            temp = new MemoryAuthDAO();
        }
        AUTH_DAO = temp;
    }

    static final LogoutService SERVICE = new LogoutService(AUTH_DAO);

    @BeforeEach
    void clear() throws DataAccessException {
        AUTH_DAO.clear();
    }

    @Test
    void testLogoutUserInvalid() {
        LogoutRequest request = new LogoutRequest("invalid-token");
        Assertions.assertThrows(ResponseException.class, () -> SERVICE.logoutUser(request));
    }

    @Test
    void testLogoutUserValid() throws ResponseException, DataAccessException {
        UserData user = new UserData("realUser", "realPassword", "email@email.com");
        AuthData authData = AUTH_DAO.createAuth(user);
        LogoutRequest request = new LogoutRequest(authData.authToken());

        Assertions.assertDoesNotThrow(() -> SERVICE.logoutUser(request));

        Assertions.assertFalse(AUTH_DAO.authExists(authData.authToken()));
    }
}