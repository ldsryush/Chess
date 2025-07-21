package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.memory.MemoryAuthDAO;
import dataaccess.mySQL.MySQLAuthDAO;
import exception.ResponseException;
import handlers.LogoutRequest;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LogoutServiceTest {
    static final AuthDAO authDAO;
    static {
        AuthDAO temp;
        try {
            temp = new MySQLAuthDAO();
        } catch (Exception e) {
            temp = new MemoryAuthDAO();
        }
        authDAO = temp;
    }

    static final LogoutService service = new LogoutService(authDAO);

    @BeforeEach
    void clear() throws DataAccessException {
        authDAO.clear();
    }

    @Test
    void testLogoutUserInvalid() {
        LogoutRequest request = new LogoutRequest("invalid-token");
        Assertions.assertThrows(ResponseException.class, () -> service.logoutUser(request));
    }

    @Test
    void testLogoutUserValid() throws ResponseException, DataAccessException {
        UserData user = new UserData("realUser", "realPassword", "email@email.com");
        AuthData authData = authDAO.createAuth(user);
        LogoutRequest request = new LogoutRequest(authData.authToken());

        Assertions.assertDoesNotThrow(() -> service.logoutUser(request));

        Assertions.assertFalse(authDAO.authExists(authData.authToken()));
    }
}
