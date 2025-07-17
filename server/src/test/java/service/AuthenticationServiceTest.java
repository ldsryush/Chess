package service;

import dataaccess.memory.MemoryAuthDAO;
import exception.ResponseException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class AuthenticationServiceTest {
    static final MemoryAuthDAO AUTH_DAO = new MemoryAuthDAO();
    static final AuthenticationService SERVICE = new AuthenticationService(AUTH_DAO);

    @BeforeEach
    void clear() {
        AUTH_DAO.clear();
    }


    @Test
    void testAuthenticate() throws ResponseException {
        UserData userData = new UserData("patrick", "12345", "test@email.com");

        AuthData authData = AUTH_DAO.createAuth(userData);

        Assertions.assertThrows(ResponseException.class, () -> SERVICE.authenticate("fakeAuth"));
        Assertions.assertTrue(SERVICE.authenticate(authData.authToken()));
    }

    @Test
    void testGetAuthData() {
        UserData userData = new UserData("patrick", "12345", "test@email.com");

        AuthData authData = AUTH_DAO.createAuth(userData);

        Assertions.assertEquals(authData, SERVICE.getAuthData(authData.authToken()));
        Assertions.assertNotEquals(authData, SERVICE.getAuthData("fake auth"));
    }

}
