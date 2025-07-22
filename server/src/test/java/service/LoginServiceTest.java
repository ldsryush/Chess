package service;

import dataaccess.DataAccessException;
import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryUserDAO;
import exception.ResponseException;
import handlers.LoginRequest;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

public class LoginServiceTest {
    static final MemoryUserDAO userDAO = new MemoryUserDAO();
    static final MemoryAuthDAO authDAO = new MemoryAuthDAO();
    static final LoginService service = new LoginService(userDAO, authDAO);

    @BeforeEach
    void clear() throws DataAccessException {
        userDAO.clear();
        authDAO.clear();
    }

    @Test
    void testLoginUnauthorized() {
        Assertions.assertThrows(ResponseException.class, () ->
                service.login(new LoginRequest("fakeName", "1234")));
    }

    @Test
    void testLoginValid() throws ResponseException, DataAccessException {
        String rawPassword = "realPassword";
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));

        UserData userData = new UserData("realName", hashedPassword, "realEmail@email.com");
        userDAO.createUser(userData);

        LoginRequest request = new LoginRequest(userData.username(), rawPassword);

        Assertions.assertDoesNotThrow(() -> service.login(request));

        AuthData authData = service.login(request);
        Assertions.assertNotNull(authData);
        Assertions.assertEquals(userData.username(), authData.username());
    }
}
