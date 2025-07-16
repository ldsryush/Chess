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
        if (isInvalid(loginRequest.username()) || isInvalid(loginRequest.password())) {
            throw new ResponseException(400, "error: bad request");
        }

        UserData userData = this.userDAO.getUser(loginRequest.username());
        if (userData == null || !Objects.equals(userData.password(), loginRequest.password())) {
            throw new ResponseException(401, "error: unauthorized");
        }

        return this.authDAO.createAuth(userData);
    }

    private boolean isInvalid(String value) {
        return value == null || value.isBlank();
    }
}

