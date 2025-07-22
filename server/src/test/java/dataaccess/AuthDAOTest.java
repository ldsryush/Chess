package dataaccess;
//tests
import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryUserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthDAOTest {
    static final MemoryAuthDAO AUTH_DAO = new MemoryAuthDAO();
    static final MemoryUserDAO USER_DAO = new MemoryUserDAO();

    @BeforeEach
    void clear() throws DataAccessException {
        AUTH_DAO.clear();
        USER_DAO.clear();
    }

    @Test
    void testCreateAuthSuccess() throws DataAccessException {
        UserData user = new UserData("alice", "pass123", "alice@example.com");
        USER_DAO.createUser(user);

        AuthData auth = AUTH_DAO.createAuth(user);
        Assertions.assertNotNull(auth);
        Assertions.assertEquals("alice", auth.username());

        AuthData fetched = AUTH_DAO.getAuthData(auth.authToken());
        Assertions.assertEquals(auth, fetched);
    }

    @Test
    void testAuthExistsTrue() throws DataAccessException {
        UserData user = new UserData("bob", "securePass", "bob@example.com");
        USER_DAO.createUser(user);
        AuthData auth = AUTH_DAO.createAuth(user);

        Assertions.assertTrue(AUTH_DAO.authExists(auth.authToken()));
    }

    @Test
    void testAuthExistsFalse() throws DataAccessException {
        Assertions.assertFalse(AUTH_DAO.authExists("nonexistent-token"));
    }

    @Test
    void testGetAuthDataNotFound() throws DataAccessException {
        Assertions.assertNull(AUTH_DAO.getAuthData("ghost-token"));
    }

    @Test
    void testDeleteAuthSuccess() throws DataAccessException {
        UserData user = new UserData("carol", "pass456", "carol@example.com");
        USER_DAO.createUser(user);
        AuthData auth = AUTH_DAO.createAuth(user);

        Assertions.assertTrue(AUTH_DAO.deleteAuth(auth.authToken()));
        Assertions.assertNull(AUTH_DAO.getAuthData(auth.authToken()));
    }

    @Test
    void testDeleteAuthInvalidToken() throws DataAccessException {
        Assertions.assertFalse(AUTH_DAO.deleteAuth("invalid-token"));
    }

    @Test
    void testClearAuths() throws DataAccessException {
        UserData user = new UserData("dave", "pass789", "dave@example.com");
        USER_DAO.createUser(user);
        AuthData auth = AUTH_DAO.createAuth(user);

        AUTH_DAO.clear();
        Assertions.assertFalse(AUTH_DAO.authExists(auth.authToken()));
    }
}