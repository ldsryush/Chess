package Service;

import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryUserDAO;
import exception.ResponseException;
import handlers.LoginRequest;
import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.Objects;

public class LoginService {

    private final MemoryUserDAO memoryUserDAO;
    private final MemoryAuthDAO memoryAuthDAO;

    public LoginService(MemoryUserDAO memoryUserDAO, MemoryAuthDAO memoryAuthDAO) {
        this.memoryUserDAO = memoryUserDAO;
        this.memoryAuthDAO = memoryAuthDAO;
    }

    public AuthData login(LoginRequest loginRequest) throws ResponseException {
//        TODO
        UserData userData = memoryUserDAO.getUser(loginRequest.username());
        if (userData == null) {
            throw new ResponseException(401, "error: unauthorized");
        }
        if (Objects.equals(userData.password(), loginRequest.password())) {
            return memoryAuthDAO.createAuth(userData);
        } else {
            throw new ResponseException(401, "error: unauthorized");
        }
    }
}