package dataaccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO {
    public void clear();

    public AuthData createAuth(UserData userData);

    public boolean authExists(AuthData authData);

    public boolean deleteAuth(String authToken);
}