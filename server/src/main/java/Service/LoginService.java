package Service;

import dataAccess.AuthDAO;
import exception.ResponseException;
import handlers.LoginRequest;
import model.AuthData;
import model.UserData;

import java.util.Objects;

/**
 * Handles requests for a user to log in
 */
public class LoginService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    /**
     *
     * @param userDAO UserDAO object providing access to the user data
     * @param authDAO AuthDAO object providing access to the authorization data
     */
    public LoginService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    /**
     * Logs in a user using the data from the LoginRequest, containing
     * Strings for the username and password
     * @param loginRequest LoginRequest object containing username and password
     * @return AuthData object containing the username and authToken created upon login
     * @throws ResponseException indicating that the user is unauthorized (if password is incorrect)
     */
    public AuthData login(LoginRequest loginRequest) throws ResponseException {
        UserData userData = this.userDAO.getUser(loginRequest.username());
        if (userData == null) {
            throw new ResponseException(401, "error: unauthorized");
        }
        if (Objects.equals(userData.password(), loginRequest.password())) {
            return this.authDAO.createAuth(userData);
        } else {
            throw new ResponseException(401, "error: unauthorized");
        }
    }
}
