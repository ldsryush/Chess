package dataaccess.memory;

import dataaccess.AuthDAO;
import model.AuthData;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private HashMap<String, String> authTokens = new HashMap<>();

    public void clear() {
        authTokens.clear();
    }

    @Override
    public AuthData createAuth(UserData userData) {
        AuthData authData = new AuthData(userData.username(), UUID.randomUUID().toString());
        authTokens.put(authData.authToken(), authData.username());
        return authData;
    }

    @Override
    public boolean authExists(AuthData authData) {
        return authTokens.containsKey(authData.authToken());
    }

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