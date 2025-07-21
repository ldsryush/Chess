package service;

import service.AuthenticationService;
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
    void clear() {
        authDAO.clear();
    }


    @Test
    void testAuthenticate() throws ResponseException, DataAccessException {
        UserData userData = new UserData("patrick", "12345", "test@email.com");

        AuthData authData = authDAO.createAuth(userData);

//        Negative test case
        Assertions.assertThrows(ResponseException.class, () -> service.authenticate("fakeAuth"));
//        Positive test case
        Assertions.assertTrue(service.authenticate(authData.authToken()));
    }

    @Test
    void testGetAuthData() throws DataAccessException {
        UserData userData = new UserData("patrick", "12345", "test@email.com");

        AuthData authData = authDAO.createAuth(userData);

//        Positive test case
        Assertions.assertEquals(authData, service.getAuthData(authData.authToken()));
//        Negative test case
        Assertions.assertNotEquals(authData, service.getAuthData("fake auth"));
    }

}
