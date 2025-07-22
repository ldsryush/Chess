package dataaccess;

import dataaccess.memory.MemoryUserDAO;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserDAOTest {
    MemoryUserDAO dao = new MemoryUserDAO();

    @BeforeEach
    void reset() throws DataAccessException {
        dao.clear();
    }

    @Test
    void testCreateUserSuccess() throws DataAccessException {
        UserData user = new UserData("alice", "pass", "a@b.com");
        dao.createUser(user);
        Assertions.assertEquals(user, dao.getUser("alice"));
    }

    @Test
    void testCreateUserDuplicateFails() throws DataAccessException {
        UserData user = new UserData("bob", "pass", "bob@email.com");
        dao.createUser(user);
        Assertions.assertThrows(DataAccessException.class, () -> dao.createUser(user));
    }

    @Test
    void testGetUserNotFound() throws DataAccessException {
        Assertions.assertNull(dao.getUser("ghost"));
    }

    @Test
    void testGetUserExists() throws DataAccessException {
        UserData user = new UserData("carol", "pass", "c@e.com");
        dao.createUser(user);
        Assertions.assertEquals(user, dao.getUser("carol"));
    }

    @Test
    void testIsUserTrue() throws DataAccessException {
        UserData user = new UserData("dave", "pass", "d@e.com");
        dao.createUser(user);
        Assertions.assertTrue(dao.isUser(user));
    }

    @Test
    void testIsUserFalse() {
        UserData fake = new UserData("eve", "pass", "e@e.com");
        Assertions.assertFalse(dao.isUser(fake));
    }

    @Test
    void testClearRemovesAllUsers() throws DataAccessException {
        UserData user = new UserData("frank", "pass", "f@e.com");
        dao.createUser(user);
        dao.clear();
        Assertions.assertNull(dao.getUser("frank"));
    }
}