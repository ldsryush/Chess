package dataaccess.memory;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;

import java.util.HashMap;

/**
 * An implementation of the UserDAO to store UserData in memory
 */
public class MemoryUserDAO implements UserDAO {

    private final HashMap<String, UserData> allUsers = new HashMap<>();

    /**
     * Indicates whether the specified username exists
     * @param userData UserData to check existence of
     * @return true if the user exists, false if not
     */
    public boolean isUser(UserData userData) {
        return allUsers.containsKey(userData.username());
    }

    /**
     * Returns the user associated with a given username
     * @param username String of the user requested
     * @return the UserData object with that username
     */
    public UserData getUser(String username) {
        return allUsers.get(username);
    }

    /**
     * Creates a new user by adding the UserData to the database
     * Throws a DataAccessException if the username already exists
     *
     * @param userData UserData object of the data to be added
     * @throws DataAccessException if username already exists
     */
    public void createUser(UserData userData) throws DataAccessException {
        if (allUsers.containsKey(userData.username())) {
            throw new DataAccessException("User already exists: " + userData.username());
        }
        allUsers.put(userData.username(), userData);
    }

    /**
     * Clears the entire user database
     */
    public void clear() {
        allUsers.clear();
    }
}
