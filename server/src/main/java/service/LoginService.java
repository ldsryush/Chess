package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import exception.ResponseException;
import handlers.LoginRequest;
import model.AuthData;
import model.UserData;

import java.util.Objects;

/**
 * Service responsible for handling user login requests.
 */
public class LoginService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    /**
     * Constructs a LoginService with the given UserDAO and AuthDAO.
     *
     * @param userDAO the data access object for user data
     * @param authDAO the data access object for authentication tokens
     */
    public LoginService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    /**
     * Authenticates a user based on the provided login request.
     *
     * @param loginRequest the login request containing username and password
     * @return an AuthData object representing the authenticated session
     * @throws ResponseException if the request is invalid or credentials are incorrect
     */
    public AuthData login(LoginRequest loginRequest) throws ResponseException {
        // Validate input
        if (isInvalid(loginRequest.username()) || isInvalid(loginRequest.password())) {
            throw new ResponseException(400, "error: bad request");
        }

        // Retrieve user data
        UserData userData = this.userDAO.getUser(loginRequest.username());

        // Verify credentials
        if (userData == null || !Objects.equals(userData.password(), loginRequest.password())) {
            throw new ResponseException(401, "error: unauthorized");
        }

        // Create and return authentication token
        return this.authDAO.createAuth(userData);
    }

    /**
     * Checks if a string value is null or blank.
     *
     * @param value the string to validate
     * @return true if the value is invalid, false otherwise
     */
    private boolean isInvalid(String value) {
        return value == null || value.isBlank();
    }
}
