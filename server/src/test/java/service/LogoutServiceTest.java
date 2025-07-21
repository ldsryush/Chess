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
        AuthDAO authDAO1;
        try {
            authDAO1 = new MySQLAuthDAO();
        } catch (ResponseException ignore) {
            authDAO1 = new MemoryAuthDAO();
        }
        authDAO = authDAO1;
    }
    static final LogoutService service = new LogoutService(authDAO);

    @BeforeEach
    void clear() throws DataAccessException {
        authDAO.clear();
    }

    @Test
    void testLogoutUserInvalid() {
//        Test that a user that is not logged in cannot log out
        Assertions.assertThrows(ResponseException.class, () -> service.logoutUser(new LogoutRequest("1234")));
    }

    @Test
    void testLogoutUserValid() {
        UserData userData = new UserData("realUser", "realPassword", "email@email.com");

        AuthData authData = null;
        try {
            authData = authDAO.createAuth(userData);
        } catch (DataAccessException e) {
            Assertions.fail();
        }

//        Test that a user currently logged in actually can log in
        AuthData finalAuthData = authData;
        Assertions.assertDoesNotThrow(() -> service.logoutUser(new LogoutRequest(finalAuthData.authToken())));
    }
}
