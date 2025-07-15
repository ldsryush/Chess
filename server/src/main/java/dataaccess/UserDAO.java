package dataaccess;

import exception.ResponseException;
import model.UserData;

public interface UserDAO {

    public boolean isUser(UserData userData) throws ResponseException;

    public UserData getUser(String username);

    public void createUser(UserData userData);

    public void clear();

}