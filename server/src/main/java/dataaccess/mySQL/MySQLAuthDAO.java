package dataaccess.mySQL;

import dataaccess.AuthDAO;
import model.AuthData;
import model.UserData;

public class MySQLAuthDAO implements AuthDAO {
    @Override
    public void clear() {

    }

    @Override
    public AuthData createAuth(UserData userData) {
        return null;
    }

    @Override
    public boolean authExists(String authToken) {
        return false;
    }

    @Override
    public AuthData getAuth(String authToken) {
        return null;
    }

    @Override
    public boolean deleteAuth(String authToken) {
        return false;
    }
}