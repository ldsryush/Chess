package dao;

import dataaccess.DataAccessException;
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
    void testGetUserNotFound() throws DataAccessException {
        Assertions.assertNull(dao.getUser("ghost"));
    }
}
