package dataAccess.memory;

import dataAccess.AuthDAO;
import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.UUID;

/**
 * Implementation of AuthDAO, providing access to authorization data in memory
 */
public class MemoryAuthDAO implements AuthDAO {
    private final HashMap<String, AuthData> authTokens = new HashMap<>();

    /**
     * Clears all authTokens in memory
     */
    public void clear() {
        authTokens.clear();
    }

    /**
     * Creates new authorization data
     * @param userData UserData object to associate with the AuthData
     * @return AuthData object containing username and authToken
     */
    @Override
    public AuthData createAuth(UserData userData) {
        AuthData authData = new AuthData(userData.username(), UUID.randomUUID().toString());
        authTokens.put(authData.authToken(), authData);
        return authData;
    }

    /**
     * Indicates whether a given authToken is associated with a particular AuthData object in memory
     * @param authToken the authToken to compare against the list of AuthData objects
     * @return boolean - true if valid, false if not
     */
    @Override
    public boolean authExists(String authToken) {
        return authTokens.containsKey(authToken);
    }

    /**
     * Gets the AuthData object associated with an authToken
     * @param authToken the authToken string to compare against the database
     * @return the AuthData object associated
     */
    @Override
    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    /**
     * Deletes an AuthData object from memory
     * @param authToken the authToken to be removed
     * @return true if successfully removed, false if not
     */
    @Override
    public boolean deleteAuth(String authToken) {
        if (authTokens.containsKey(authToken)) {
            authTokens.remove(authToken);
            return true;
        } else {
            return false;
        }
    }
}
