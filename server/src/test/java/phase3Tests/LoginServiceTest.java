package phase3Tests;

import Service.LoginService;
import dataAccess.memory.MemoryAuthDAO;
import dataAccess.memory.MemoryUserDAO;
import exception.ResponseException;
import handlers.LoginRequest;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoginServiceTest {
    static final MemoryUserDAO userDAO = new MemoryUserDAO();
    static final MemoryAuthDAO authDAO = new MemoryAuthDAO();
    static final LoginService service = new LoginService(userDAO, authDAO);

    @BeforeEach
    void clear() {
        userDAO.clear();
        authDAO.clear();
    }

    @Test
    void testLoginUnauthorized() {
        Assertions.assertThrows(ResponseException.class, () -> service.login(new LoginRequest("fakeName", "1234")));
    }

    @Test
    void testLoginValid() throws ResponseException {


        UserData userData = new UserData("realName", "realPassword", "realEmail@email.com");

        userDAO.createUser(userData);

        Assertions.assertDoesNotThrow(() -> service.login(new LoginRequest(userData.username(), userData.password())));
        Assertions.assertInstanceOf(AuthData.class, service.login(new LoginRequest(userData.username(), userData.password())));
    }
}
