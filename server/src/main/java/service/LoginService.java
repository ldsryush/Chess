package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import exception.ResponseException;
import model.LoginRequest;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Handles requests for a user to log in
 */
public class LoginService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public LoginService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    /**
     * Logs in a user using the data from the LoginRequest.
     *
     * @param loginRequest LoginRequest object containing username and password
     * @return AuthData object containing the username and authToken created upon login
     * @throws ResponseException    if input is invalid or credentials are incorrect
     * @throws DataAccessException if a database error occurs
     */
    public AuthData login(LoginRequest loginRequest) throws ResponseException, DataAccessException {
        if (loginRequest == null ||
                loginRequest.username() == null || loginRequest.username().isBlank() ||
                loginRequest.password() == null || loginRequest.password().isBlank()) {
            throw new ResponseException(400, "error: missing username or password");
        }

        UserData userData = userDAO.getUser(loginRequest.username());

        if (userData == null || !BCrypt.checkpw(loginRequest.password(), userData.password())) {
            throw new ResponseException(401, "error: unauthorized");
        }

        authDAO.deleteAuth(userData.username());

        return authDAO.createAuth(userData);
    }
}