package dao;
//tests
import dataaccess.DataAccessException;
import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryUserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthDAOTest {
    static final MemoryAuthDAO authDAO = new MemoryAuthDAO();
    static final MemoryUserDAO userDAO = new MemoryUserDAO();

    @BeforeEach
    void clear() throws DataAccessException {
        authDAO.clear();
        userDAO.clear();
    }

    @Test
    void testCreateAuthSuccess() throws DataAccessException {
        UserData user = new UserData("alice", "pass123", "alice@example.com");
        userDAO.createUser(user);

        AuthData auth = authDAO.createAuth(user);
        Assertions.assertNotNull(auth);
        Assertions.assertEquals("alice", auth.username());

        AuthData fetched = authDAO.getAuthData(auth.authToken());
        Assertions.assertEquals(auth, fetched);
    }

    @Test
    void testAuthExistsTrue() throws DataAccessException {
        UserData user = new UserData("bob", "securePass", "bob@example.com");
        userDAO.createUser(user);
        AuthData auth = authDAO.createAuth(user);

        Assertions.assertTrue(authDAO.authExists(auth.authToken()));
    }

    @Test
    void testAuthExistsFalse() throws DataAccessException {
        Assertions.assertFalse(authDAO.authExists("nonexistent-token"));
    }

    @Test
    void testGetAuthDataNotFound() throws DataAccessException {
        Assertions.assertNull(authDAO.getAuthData("ghost-token"));
    }

    @Test
    void testDeleteAuthSuccess() throws DataAccessException {
        UserData user = new UserData("carol", "pass456", "carol@example.com");
        userDAO.createUser(user);
        AuthData auth = authDAO.createAuth(user);

        Assertions.assertTrue(authDAO.deleteAuth(auth.authToken()));
        Assertions.assertNull(authDAO.getAuthData(auth.authToken()));
    }

    @Test
    void testDeleteAuthInvalidToken() throws DataAccessException {
        Assertions.assertFalse(authDAO.deleteAuth("invalid-token"));
    }

    @Test
    void testClearAuths() throws DataAccessException {
        UserData user = new UserData("dave", "pass789", "dave@example.com");
        userDAO.createUser(user);
        AuthData auth = authDAO.createAuth(user);

        authDAO.clear();
        Assertions.assertFalse(authDAO.authExists(auth.authToken()));
    }
}
