package Service;

import dataAccess.AuthDAO;
import exception.ResponseException;
import model.AuthData;

public class AuthenticationService {
    private final AuthDAO authDAO;

    public AuthenticationService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public boolean authenticate(String authToken) throws ResponseException {
        if (!this.authDAO.authExists(authToken)) {
            throw new ResponseException(401, "error: unauthorized");
        } else {
            return true;
        }
    }

    public AuthData getAuthData(String authToken) {
        return authDAO.getAuth(authToken);
    }
}
