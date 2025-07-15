package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {
    private final Map<String, UserData> users = new HashMap<>();

    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.getUsername())) {
            throw new DataAccessException("Username already exists.");
        }
        users.put(user.getUsername(), user);
    }

    public UserData getUser(String username) {
        return users.get(username);
    }

    public void clear() {
        users.clear();
    }
}
