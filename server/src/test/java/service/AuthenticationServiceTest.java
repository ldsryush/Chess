package service;

import dataaccess.DataAccessException;
import dataaccess.memory.MemoryAuthDAO;
import exception.ResponseException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class AuthenticationServiceTest {
    static final MemoryAuthDAO authDAO = new MemoryAuthDAO();
    static final AuthenticationService service = new AuthenticationService(authDAO);

    @BeforeEach
    void clear() throws DataAccessException {
        authDAO.clear();
    }

    @Test
    void testAuthenticate() throws ResponseException, DataAccessException {
        UserData userData = new UserData("patrick", "12345", "test@email.com");
        AuthData authData = authDAO.createAuth(userData);

        // ✅ Negative test case: invalid token
        Assertions.assertThrows(ResponseException.class, () -> service.authenticate("fakeAuth"));

        // ✅ Positive test case: valid token should not throw
        Assertions.assertDoesNotThrow(() -> service.authenticate(authData.authToken()));
    }

    @Test
    void testGetAuthData() throws ResponseException, DataAccessException {
        UserData userData = new UserData("patrick", "12345", "test@email.com");
        AuthData authData = authDAO.createAuth(userData);

        // ✅ Positive test case
        Assertions.assertEquals(authData, service.getAuthData(authData.authToken()));

        // ✅ Negative test case: invalid token should throw
        Assertions.assertThrows(ResponseException.class, () -> service.getAuthData("fake auth"));
    }
}