package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import exception.ResponseException;
import handlers.RegistrationRequest;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Handles requests to register new users
 */
public class RegistrationService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

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
     * @throws ResponseException for bad input or duplicate username
     * @throws DataAccessException for database failures
     */
    public AuthData registerUser(RegistrationRequest userRequest) throws ResponseException, DataAccessException {
        if (userRequest.username() == null || userRequest.password() == null || userRequest.email() == null) {
            throw new ResponseException(400, "error: bad request");
        }

        String hashedPassword = BCrypt.hashpw(userRequest.password(), BCrypt.gensalt(12));
        UserData userData = new UserData(userRequest.username(), hashedPassword, userRequest.email());

        if (this.userDAO.isUser(userData)) {
            throw new ResponseException(403, "error: already taken");
        }

        this.userDAO.createUser(userData);
        return this.authDAO.createAuth(userData);
    }
}