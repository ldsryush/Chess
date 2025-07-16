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
        if (userRequest.username() == null || userRequest.password() == null || userRequest.email() == null){
            throw new ResponseException(400, "error: bad request");
        }

        UserData userData = new UserData(userRequest.username(), userRequest.password(), userRequest.email());
        if (this.userDAO.isUser(userData)) {
            throw new ResponseException(403, "error: already taken");
        } else {
            this.userDAO.createUser(userData);

            return this.authDAO.createAuth(userData);
        }
    }

}
