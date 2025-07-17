package service;

import dataaccess.AuthDAO;
import exception.ResponseException;
import handlers.LogoutRequest;

/**
 * Service responsible for handling user logout requests.
 */
public class LogoutService {
    private final AuthDAO authDAO;

    /**
     * Constructs a LogoutService with the given AuthDAO.
     *
     * @param authDAO the data access object for authentication tokens
     */
    public LogoutService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    /**
     * Logs out a user by deleting their authentication token.
     *
     * @param authToken the logout request containing the token to invalidate
     * @throws ResponseException if the token does not exist or is invalid
     */
    public void logoutUser(LogoutRequest authToken) throws ResponseException {
        // Attempt to delete the token
        boolean removed = authDAO.deleteAuth(authToken.authToken());

        // If deletion failed, token was invalid
        if (!removed) {
            throw new ResponseException(401, "error: unauthorized");
        }
    }
}
