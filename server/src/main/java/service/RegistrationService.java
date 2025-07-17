package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import exception.ResponseException;
import handlers.RegistrationRequest;
import model.AuthData;
import model.UserData;

/**
 * Service responsible for handling user registration requests.
 */
public class RegistrationService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    /**
     * Constructs a RegistrationService with the given UserDAO and AuthDAO.
     *
     * @param userDAO the data access object for user data
     * @param authDAO the data access object for authentication tokens
     */
    public RegistrationService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    /**
     * Registers a new user and returns an authentication token.
     *
     * @param userRequest the registration request containing username, password, and email
     * @return an AuthData object representing the authenticated session
     * @throws ResponseException if the request is invalid or the username is already taken
     */
    public AuthData registerUser(RegistrationRequest userRequest) throws ResponseException {
        // Validate input fields
        if (isInvalid(userRequest.username()) ||
                isInvalid(userRequest.password()) ||
                isInvalid(userRequest.email())) {
            throw new ResponseException(400, "error: bad request");
        }

        // Create a new UserData object
        UserData userData = new UserData(
                userRequest.username(),
                userRequest.password(),
                userRequest.email()
        );

        // Check if the username is already taken
        if (this.userDAO.isUser(userData)) {
            throw new ResponseException(403, "error: already taken");
        }

        // Store the new user and generate an auth token
        this.userDAO.createUser(userData);
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
