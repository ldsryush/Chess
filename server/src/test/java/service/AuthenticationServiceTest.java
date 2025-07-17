package service;

import dataAccess.memory.MemoryAuthDAO;
import exception.ResponseException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import Service.AuthenticationService;

public class AuthenticationServiceTest {
    static final MemoryAuthDAO authDAO = new MemoryAuthDAO();
    static final AuthenticationService service = new AuthenticationService(authDAO);

    @BeforeEach
    void clear() {
        authDAO.clear();
    }


    @Test
    void testAuthenticate() throws ResponseException {
        UserData userData = new UserData("patrick", "12345", "test@email.com");

        AuthData authData = authDAO.createAuth(userData);

        Assertions.assertThrows(ResponseException.class, () -> service.authenticate("fakeAuth"));
        Assertions.assertTrue(service.authenticate(authData.authToken()));
    }

    @Test
    void testGetAuthData() {
        UserData userData = new UserData("patrick", "12345", "test@email.com");

        AuthData authData = authDAO.createAuth(userData);

        Assertions.assertEquals(authData, service.getAuthData(authData.authToken()));
        Assertions.assertNotEquals(authData, service.getAuthData("fake auth"));
    }

}
