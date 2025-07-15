package Service;

import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryUserDAO;
import exception.ResponseException;
import handlers.RegistrationRequest;
import model.AuthData;
import model.UserData;

import java.util.*;

public class RegistrationService {

    private final MemoryUserDAO memoryUserDAO;
    private final MemoryAuthDAO memoryAuthDAO;

    public RegistrationService(MemoryUserDAO memoryUserDAO, MemoryAuthDAO memoryAuthDAO) {
        this.memoryUserDAO = memoryUserDAO;
        this.memoryAuthDAO = memoryAuthDAO;
    }

    public AuthData registerUser(RegistrationRequest userRequest) throws ResponseException {

        if (userRequest.username() == null || userRequest.password() == null || userRequest.email() == null){
            throw new ResponseException(400, "error: bad request");
        }

        UserData userData = new UserData(userRequest.username(), userRequest.password(), userRequest.email());
        if (memoryUserDAO.isUser(userData)) {
            throw new ResponseException(403, "error: already taken");
        } else {

            memoryUserDAO.createUser(userData);

            return memoryAuthDAO.createAuth(userData);
        }
    }

    private static AuthData generateAuth(UserData userData) {
        return new AuthData(userData.username(), UUID.randomUUID().toString());
    }

}