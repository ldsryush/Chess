package dataaccess.memory;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private final HashMap<String, AuthData> authTokens = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        authTokens.clear();
    }

    @Override
    public AuthData createAuth(UserData userData) throws DataAccessException {
        AuthData authData = new AuthData(userData.username(), UUID.randomUUID().toString());
        authTokens.put(authData.authToken(), authData);
        return authData;
    }

    @Override
    public boolean authExists(String authToken) throws DataAccessException {
        return authTokens.containsKey(authToken);
    }

    @Override
    public AuthData getAuthData(String authToken) throws DataAccessException {
        return authTokens.get(authToken);
    }

    @Override
    public boolean deleteAuth(String authToken) throws DataAccessException {
        if (authTokens.containsKey(authToken)) {
            authTokens.remove(authToken);
            return true;
        } else {
            return false;
        }
    }
}
