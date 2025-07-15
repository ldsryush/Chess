package dataaccess.memory;

import dataaccess.UserDAO;
import exception.ResponseException;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class MemoryUserDAO implements UserDAO {

    private  final HashMap<String, UserData> allUsers = new HashMap<>();

    public boolean isUser(UserData userData) throws ResponseException {
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