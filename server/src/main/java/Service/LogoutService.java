package Service;

import dataaccess.memory.MemoryAuthDAO;
import exception.ResponseException;
import handlers.LogoutRequest;

public class LogoutService {
    private final MemoryAuthDAO memoryAuthDAO;

    public LogoutService(MemoryAuthDAO memoryAuthDAO) {
        this.memoryAuthDAO = memoryAuthDAO;
    }


    public void logoutUser(LogoutRequest authToken) throws ResponseException {
        boolean removed = memoryAuthDAO.deleteAuth(authToken.authToken());
        if (!removed) {
            throw new ResponseException(401, "error: unauthorized");
        }
    }
}