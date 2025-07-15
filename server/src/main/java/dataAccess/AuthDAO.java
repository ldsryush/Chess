package dataAccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO {
    void clear();

    AuthData createAuth(UserData userData);

    boolean authExists(String authToken);

    AuthData getAuth(String authToken);

    boolean deleteAuth(String authToken);
}
