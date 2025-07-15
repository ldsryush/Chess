package Service;

import dataAccess.AuthDAO;
import exception.ResponseException;
import model.AuthData;

/**
 * Class AuthenticationService
 * Handles requests to authenticate a user before allowing the user to modify the game
 */
public class AuthenticationService {
    private final AuthDAO authDAO;

    /**
     * Constructor, accepts an AuthDAO to use to access the authorization database
     * @param authDAO AuthDAO object providing access to the authorization database
     */
    public AuthenticationService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    /**
     * Authenticates a token, throwing an exception if the authorization token is invalid
     * @param authToken a String of the authToken provided in the HTTP header
     * @throws ResponseException Indicating that the authToken is not valid
     */
    public boolean authenticate(String authToken) throws ResponseException {
        if (!this.authDAO.authExists(authToken)) {
            throw new ResponseException(401, "error: unauthorized");
        } else {
            return true;
        }
    }

    /**
     * Returns the AuthData object associated with a given authToken
     * @param authToken the String of an authToken
     * @return AuthData object associated
     */
    public AuthData getAuthData(String authToken) {
        return authDAO.getAuth(authToken);
    }
}
