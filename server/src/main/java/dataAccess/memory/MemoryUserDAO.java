package dataAccess.memory;

import dataAccess.UserDAO;
import model.UserData;

import java.util.HashMap;

/**
 * In-memory implementation of UserDAO for managing user accounts.
 */
public class MemoryUserDAO implements UserDAO {

    // Stores users mapped by their username
    private final HashMap<String, UserData> userStore = new HashMap<>();

    /**
     * Checks if a user already exists.
     *
     * @param userData the user to check
     * @return true if the user exists, false otherwise
     */
    @Override
    public boolean isUser(UserData userData) {
        return userStore.containsKey(userData.username());
    }

    /**
     * Retrieves a user by username.
     *
     * @param username the username to look up
     * @return the UserData, or null if not found
     */
    @Override
    public UserData getUser(String username) {
        return userStore.get(username);
    }

    /**
     * Creates a new user.
     *
     * @param userData the user to add
     */
    @Override
    public void createUser(UserData userData) {
        userStore.put(userData.username(), userData);
    }

    /**
     * Clears all stored users.
     */
    public void clear() {
        userStore.clear();
    }
}
