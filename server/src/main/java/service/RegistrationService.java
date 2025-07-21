package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import exception.ResponseException;
import handlers.RegistrationRequest;
import model.AuthData;
import model.UserData;
import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Handles requests to register new users
 */
public class RegistrationService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    /**
     * @param userDAO UserDAO object providing access to the user data
     * @param authDAO AuthDAO object providing access to the authorization data
     */
    public RegistrationService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    /**
     * Registers the user by checking for duplicates, saving the user into the database,
     * creating an authentication token, saving that to the database, and returning it.
     *
     * @param userRequest The user object to be registered
     * @return the AuthToken object that has been created
     * @throws ResponseException indicating either invalid fields (bad request) or that the username is already taken
     */
    public AuthData registerUser(RegistrationRequest userRequest) throws ResponseException {
        if (userRequest.username() == null || userRequest.password() == null || userRequest.email() == null) {
            throw new ResponseException(400, "error: bad request");
        }

        String hashedPassword = BCrypt.withDefaults().hashToString(12, userRequest.password().toCharArray());
        UserData userData = new UserData(userRequest.username(), hashedPassword, userRequest.email());

        try {
            if (this.userDAO.isUser(userData)) {
                throw new ResponseException(403, "error: already taken");
            } else {
                this.userDAO.createUser(userData);
                return this.authDAO.createAuth(userData);
            }
        } catch (DataAccessException e) {
            throw new ResponseException(500, e.getMessage());
        }
    }
}
