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
    static final MemoryUserDAO USER_DAO = new MemoryUserDAO();
    static final MemoryAuthDAO AUTH_DAO = new MemoryAuthDAO();
    static final LoginService SERVICE = new LoginService(USER_DAO, AUTH_DAO);

    @BeforeEach
    void clear() throws DataAccessException {
        USER_DAO.clear();
        AUTH_DAO.clear();
    }

    @Test
    void testLoginUnauthorized() {
        Assertions.assertThrows(ResponseException.class, () ->
                SERVICE.login(new LoginRequest("fakeName", "1234")));
    }

    @Test
    void testLoginValid() throws ResponseException, DataAccessException {
        String rawPassword = "realPassword";
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));

        UserData userData = new UserData("realName", hashedPassword, "realEmail@email.com");
        USER_DAO.createUser(userData);

        LoginRequest request = new LoginRequest(userData.username(), rawPassword);

        Assertions.assertDoesNotThrow(() -> SERVICE.login(request));

        AuthData authData = SERVICE.login(request);
        Assertions.assertNotNull(authData);
        Assertions.assertEquals(userData.username(), authData.username());
    }
}