package dataAccess.memory;

import dataAccess.AuthDAO;
import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.UUID;

/**
 * In-memory implementation of AuthDAO for managing authentication tokens.
 */
public class MemoryAuthDAO implements AuthDAO {

    // Stores auth tokens mapped to their corresponding AuthData
    private final HashMap<String, AuthData> tokenStore = new HashMap<>();

    /**
     * Clears all stored authentication tokens.
     */
    public void clear() {
        tokenStore.clear();
    }

    /**
     * Creates a new authentication token for the given user.
     *
     * @param userData the user to authenticate
     * @return the generated AuthData
     */
    @Override
    public AuthData createAuth(UserData userData) {
        String token = UUID.randomUUID().toString();
        AuthData authData = new AuthData(userData.username(), token);
        tokenStore.put(token, authData);
        return authData;
    }

    /**
     * Checks if an authentication token exists.
     *
     * @param authToken the token to check
     * @return true if the token exists, false otherwise
     */
    @Override
    public boolean authExists(String authToken) {
        return tokenStore.containsKey(authToken);
    }

    /**
     * Retrieves the AuthData associated with a token.
     *
     * @param authToken the token to retrieve
     * @return the AuthData, or null if not found
     */
    @Override
    public AuthData getAuth(String authToken) {
        return tokenStore.get(authToken);
    }

    /**
     * Deletes an authentication token.
     *
     * @param authToken the token to delete
     * @return true if the token was deleted, false if it didn't exist
     */
    @Override
    public boolean deleteAuth(String authToken) {
        return tokenStore.remove(authToken) != null;
    }
}
