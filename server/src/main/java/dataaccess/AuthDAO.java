package dataaccess;

import model.AuthData;
import model.UserData;

/**
 * Interface for managing authentication tokens and user sessions.
 */
public interface AuthDAO {

    /**
     * Clears all stored authentication tokens.
     */
    void clear();

    /**
     * Creates a new authentication token for the given user.
     *
     * @param userData the user to authenticate
     * @return the generated AuthData containing the token and username
     */
    AuthData createAuth(UserData userData);

    /**
     * Checks if an authentication token exists.
     *
     * @param authToken the token to check
     * @return true if the token exists, false otherwise
     */
    boolean authExists(String authToken);

    /**
     * Retrieves the AuthData associated with a token.
     *
     * @param authToken the token to retrieve
     * @return the AuthData, or null if not found
     */
    AuthData getAuth(String authToken);

    /**
     * Deletes an authentication token.
     *
     * @param authToken the token to delete
     * @return true if the token was deleted, false if it didn't exist
     */
    boolean deleteAuth(String authToken);
}
