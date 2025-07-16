package dataAccess.memory;

import dataAccess.UserDAO;
import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {

    private  final HashMap<String, UserData> allUsers = new HashMap<>();

    public boolean isUser(UserData userData) {
        return allUsers.containsKey(userData.username());
    }

    public UserData getUser(String username) {
        return allUsers.get(username);
    }

    public void createUser(UserData userData) {
        allUsers.put(userData.username(), userData);
    }

    public void clear() {
        allUsers.clear();
    }
}
