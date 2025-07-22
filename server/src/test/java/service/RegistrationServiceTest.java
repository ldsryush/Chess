package service;

import service.RegistrationService;
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
    static final UserDAO userDAO = new MemoryUserDAO();
    static final AuthDAO authDAO = new MemoryAuthDAO();
    static final RegistrationService service = new RegistrationService(userDAO, authDAO);

    @BeforeEach
    void clear() throws DataAccessException {
        userDAO.clear();
        authDAO.clear();
    }

    @Test
    void testRegisterUserGood() {
        RegistrationRequest realReq = new RegistrationRequest(
                "realName",
                "realPassword",
                "email@email.com");

//        Test that a real user can be registered
        Assertions.assertDoesNotThrow(() -> service.registerUser(realReq));

//        Test against registering again
        Assertions.assertThrows(ResponseException.class, () -> service.registerUser(realReq));
    }

    @Test
    void testRegisterUserBad() {
        RegistrationRequest badReq = new RegistrationRequest(
                null,
                "badPassword",
                "badEmail"
        );

//        Ensure that good requests must be handled
        Assertions.assertThrows(ResponseException.class, () -> service.registerUser(badReq));
    }
}