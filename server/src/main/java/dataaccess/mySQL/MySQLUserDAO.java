package dataaccess.mySQL;

import dataaccess.UserDAO;
import model.UserData;

public class MySQLUserDAO implements UserDAO {
    @Override
    public boolean isUser(UserData userData) {
        return false;
    }

    @Override
    public UserData getUser(String username) {
        return null;
    }

    @Override
    public void createUser(UserData userData) {

    }

    @Override
    public void clear() {

    }
}