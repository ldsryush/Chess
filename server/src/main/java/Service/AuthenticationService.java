package Service;

import dataAccess.AuthDAO;
import exception.ResponseException;
import model.AuthData;

/**
 * Service responsible for validating authentication tokens and retrieving associated user data.
 */
public class AuthenticationService {
    private final AuthDAO authDAO;

    /**
     * Constructs an AuthenticationService with the given AuthDAO.
     *
     * @param authDAO the data access object for authentication tokens
     */
    public AuthenticationService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    /**
     * Validates whether the given authentication token exists.
     *
     * @param authToken the token to validate
     * @return true if the token is valid
     * @throws ResponseException if the token is missing or invalid
     */
    public boolean authenticate(String authToken) throws ResponseException {
        if (!this.authDAO.authExists(authToken)) {
            throw new ResponseException(401, "error: unauthorized");
        } else {
            return true;
        }
    }

    /**
     * Retrieves the AuthData associated with a given token.
     *
     * @param authToken the token to look up
     * @return the AuthData object, or null if not found
     */
    public AuthData getAuthData(String authToken) {
        return authDAO.getAuth(authToken);
    }
}
