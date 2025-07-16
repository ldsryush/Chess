package dataAccess;

import dataAccess.memory.MemoryAuthDAO;
import dataAccess.memory.MemoryGameDAO;
import dataAccess.memory.MemoryUserDAO;

public class DataAccess {
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private GameDAO gameDAO;

    public DataAccess(DataLocation dataLocation) {
        switch (dataLocation) {
            case SQL -> throw new UnsupportedOperationException("SQL support not implemented yet");
            case MEMORY -> {
                this.authDAO = new MemoryAuthDAO();
                this.userDAO = new MemoryUserDAO();
                this.gameDAO = new MemoryGameDAO();
            }
        }
    }

    public AuthDAO getAuthDAO() {
        return authDAO;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public GameDAO getGameDAO() {
        return gameDAO;
    }

    public enum DataLocation {
        SQL,
        MEMORY
    }
}