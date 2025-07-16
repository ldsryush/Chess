package Service;

import dataAccess.AuthDAO;
import dataAccess.UserDAO;
import exception.ResponseException;
import handlers.LoginRequest;
import model.AuthData;
import model.UserData;

import java.util.Objects;

public class LoginService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public LoginService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }


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
