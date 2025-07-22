package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import exception.ResponseException;
import model.AuthData;

/**
 * Handles authentication by validating auth tokens against the AuthDAO.
 * Propagates DataAccessException so Spark can return 500 on DB failures.
 */
public class AuthenticationService {
    private final AuthDAO authDAO;

    public AuthenticationService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    /**
     * Verifies the token exists and is valid.
     *
     * @param token the auth token from the Authorization header
     * @throws ResponseException    if the token is missing or invalid (401)
     * @throws DataAccessException if a database error occurs (500)
     */
    public void authenticate(String token) throws ResponseException, DataAccessException {
        if (token == null || token.isBlank()) {
            throw new ResponseException(401, "error: missing authorization header");
        }

        AuthData authData;
        try {
            authData = authDAO.getAuthData(token);
        } catch (DataAccessException e) {
            throw e; // ✅ Let Spark handle this as 500
        }

        if (authData == null) {
            throw new ResponseException(401, "error: unauthorized");
        }
    }

    /**
     * Retrieves the AuthData for a valid token.
     *
     * @param token the auth token
     * @return the AuthData if token is valid
     * @throws ResponseException    if the token is missing or invalid (401)
     * @throws DataAccessException if a database error occurs (500)
     */
    public AuthData getAuthData(String token) throws ResponseException, DataAccessException {
        if (token == null || token.isBlank()) {
            throw new ResponseException(401, "error: missing authorization header");
        }

        AuthData authData;
        try {
            authData = authDAO.getAuthData(token);
        } catch (DataAccessException e) {
            throw e;
        }

        if (authData == null) {
            throw new ResponseException(401, "error: unauthorized");
        }

        return authData;
    }
}
