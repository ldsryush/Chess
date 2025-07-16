package Service;

import dataAccess.AuthDAO;
import exception.ResponseException;
import handlers.LogoutRequest;

public class LogoutService {
    private final AuthDAO authDAO;

    public LogoutService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public void logoutUser(LogoutRequest authToken) throws ResponseException {
        boolean removed = authDAO.deleteAuth(authToken.authToken());
        if (!removed) {
            throw new ResponseException(401, "error: unauthorized");
        }
    }
}
