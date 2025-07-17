package phase3Tests;

import Service.RegistrationService;
import dataAccess.AuthDAO;
import dataAccess.UserDAO;
import dataAccess.memory.MemoryAuthDAO;
import dataAccess.memory.MemoryUserDAO;
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
    void clear() {
        userDAO.clear();
        authDAO.clear();
    }

    @Test
    void testRegisterUserGood() {
        RegistrationRequest realReq = new RegistrationRequest(
                "realName",
                "realPassword",
                "email@email.com");

        Assertions.assertDoesNotThrow(() -> service.registerUser(realReq));

        Assertions.assertThrows(ResponseException.class, () -> service.registerUser(realReq));
    }

    @Test
    void testRegisterUserBad() {
        RegistrationRequest badReq = new RegistrationRequest(
                null,
                "badPassword",
                "badEmail"
        );

        Assertions.assertThrows(ResponseException.class, () -> service.registerUser(badReq));
    }
}
