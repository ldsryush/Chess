package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import exception.ResponseException;
import handlers.LoginRequest;
import model.AuthData;
import model.UserData;
import at.favre.lib.crypto.bcrypt.BCrypt; // ✅ added bcrypt import

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
     * Logs in a user using the data from the LoginRequest,
     * @param loginRequest LoginRequest object containing username and password
     * @return AuthData object containing the username and authToken created upon login
     * @throws ResponseException if password is incorrect or username is unknown
     */
    public AuthData login(LoginRequest loginRequest) throws ResponseException, DataAccessException {
        UserData userData = this.userDAO.getUser(loginRequest.username());

        if (userData == null) {
            throw new ResponseException(401, "error: unauthorized");
        }

        // ✅ Remove any previous auth token
        authDAO.deleteAuth(userData.username());

        // ✅ Verify bcrypt hashed password
        BCrypt.Result result = BCrypt.verifyer().verify(
                loginRequest.password().toCharArray(),
                userData.password()
        );

        if (result.verified) {
            return authDAO.createAuth(userData);
        } else {
            throw new ResponseException(401, "error: unauthorized");
        }
    }
}
