package Service;

import dataAccess.memory.MemoryAuthDAO;
import exception.ResponseException;
import handlers.LogoutRequest;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LogoutServiceTest {
    static final MemoryAuthDAO authDAO = new MemoryAuthDAO();
    static final LogoutService service = new LogoutService(authDAO);

    @BeforeEach
    void clear() {
        authDAO.clear();
    }

    @Test
    void testLogoutUserInvalid() {
        Assertions.assertThrows(ResponseException.class, () -> service.logoutUser(new LogoutRequest("1234")));
    }

    @Test
    void testLogoutUserValid() {
        UserData userData = new UserData("realUser", "realPassword", "email@email.com");

        AuthData authData = authDAO.createAuth(userData);

        Assertions.assertDoesNotThrow(() -> service.logoutUser(new LogoutRequest(authData.authToken())));
    }
}
