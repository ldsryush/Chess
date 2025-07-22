package dataaccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO {

    void clear() throws DataAccessException;

    AuthData createAuth(UserData userData) throws DataAccessException;

    boolean authExists(String authToken) throws DataAccessException;

    AuthData getAuthData(String authToken) throws DataAccessException;

    boolean deleteAuth(String authToken) throws DataAccessException;
}