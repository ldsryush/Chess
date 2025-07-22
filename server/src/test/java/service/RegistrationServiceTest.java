package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryUserDAO;
import exception.ResponseException;
import handlers.RegistrationRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RegistrationServiceTest {
    static final UserDAO USER_DAO = new MemoryUserDAO();
    static final AuthDAO AUTH_DAO = new MemoryAuthDAO();
    static final RegistrationService SERVICE = new RegistrationService(USER_DAO, AUTH_DAO);

    @BeforeEach
    void clear() throws DataAccessException {
        USER_DAO.clear();
        AUTH_DAO.clear();
    }

    @Test
    void testRegisterUserGood() {
        RegistrationRequest realReq = new RegistrationRequest(
                "realName",
                "realPassword",
                "email@email.com");

//        Test that a real user can be registered
        Assertions.assertDoesNotThrow(() -> SERVICE.registerUser(realReq));

//        Test against registering again
        Assertions.assertThrows(ResponseException.class, () -> SERVICE.registerUser(realReq));
    }

    @Test
    void testRegisterUserBad() {
        RegistrationRequest badReq = new RegistrationRequest(
                null,
                "badPassword",
                "badEmail"
        );

//        Ensure that good requests must be handled
        Assertions.assertThrows(ResponseException.class, () -> SERVICE.registerUser(badReq));
    }
}