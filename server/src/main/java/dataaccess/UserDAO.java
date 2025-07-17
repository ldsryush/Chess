package dataaccess;

import model.UserData;

/**
 * Interface for managing user data in the persistence layer.
 */
public interface UserDAO {

    /**
     * Checks if a user already exists in the data store.
     *
     * @param userData the user to check
     * @return true if the user exists, false otherwise
     */
    boolean isUser(UserData userData);

    /**
     * Retrieves a user by their username.
     *
     * @param username the username to look up
     * @return the UserData object, or null if not found
     */
    UserData getUser(String username);

    /**
     * Creates a new user in the data store.
     *
     * @param userData the user to add
     */
    void createUser(UserData userData);

    /**
     * Clears all stored user data.
     */
    void clear();
}
