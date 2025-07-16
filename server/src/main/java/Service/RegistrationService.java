package Service;

import dataAccess.AuthDAO;
import dataAccess.UserDAO;
import exception.ResponseException;
import handlers.RegistrationRequest;
import model.AuthData;
import model.UserData;

public class RegistrationService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public RegistrationService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData registerUser(RegistrationRequest userRequest) throws ResponseException {
        if (isInvalid(userRequest.username()) ||
                isInvalid(userRequest.password()) ||
                isInvalid(userRequest.email())) {
            throw new ResponseException(400, "error: bad request");
        }

        UserData userData = new UserData(userRequest.username(), userRequest.password(), userRequest.email());

        if (this.userDAO.isUser(userData)) {
            throw new ResponseException(403, "error: already taken");
        }

        this.userDAO.createUser(userData);
        return this.authDAO.createAuth(userData);
    }

    private boolean isInvalid(String value) {
        return value == null || value.isBlank();
    }
}