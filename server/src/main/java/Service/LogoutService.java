package Service;

import dataAccess.AuthDAO;
import exception.ResponseException;
import handlers.LogoutRequest;

/**
 * Handles requests to log a user out
 */
public class LogoutService {
    private final AuthDAO authDAO;

    /**
     *
     * @param authDAO AuthDAO object providing access to the authorization data
     */
    public LogoutService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    /**
     * Logs a user out, returning nothing
     * @param authToken LogoutRequest object containing a string with the authToken of the user to be logged out
     * @throws ResponseException Indicates that the user is not authorized (provided invalid authToken)
     */
    public void logoutUser(LogoutRequest authToken) throws ResponseException {
        boolean removed = authDAO.deleteAuth(authToken.authToken());
        if (!removed) {
            throw new ResponseException(401, "error: unauthorized");
        }
    }
}
