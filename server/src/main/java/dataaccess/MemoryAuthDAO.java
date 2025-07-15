package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO {
    private final Map<String, AuthData> authTokens = new HashMap<>();

    public void createAuth(AuthData auth) {
        authTokens.put(auth.getAuthToken(), auth);
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        if (!authTokens.containsKey(authToken)) {
            throw new DataAccessException("Invalid auth token.");
        }
        return authTokens.get(authToken);
    }

    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }

    public void clear() {
        authTokens.clear();
    }
}
