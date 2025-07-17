package dataAccess;

import dataAccess.memory.MemoryAuthDAO;
import dataAccess.memory.MemoryGameDAO;
import dataAccess.memory.MemoryUserDAO;

/**
 * Centralized access point for data access objects like DAOs.
 * Supports multiple storage backends.
 */
public class DataAccess {

    private final AuthDAO authDAO;
    private final UserDAO userDAO;
    private final GameDAO gameDAO;

    /**
     * Initializes the data access layer based on the specified location.
     *
     * @param dataLocation the storage backend to use
     */
    public DataAccess(DataLocation dataLocation) {
        switch (dataLocation) {
            case SQL -> throw new UnsupportedOperationException("SQL support not implemented yet");
            case MEMORY -> {
                this.authDAO = new MemoryAuthDAO();
                this.userDAO = new MemoryUserDAO();
                this.gameDAO = new MemoryGameDAO();
            }
            default -> throw new IllegalArgumentException("Unknown data location: " + dataLocation);
        }
    }

    /**
     * @return the AuthDAO instance
     */
    public AuthDAO getAuthDAO() {
        return authDAO;
    }

    /**
     * @return the UserDAO instance
     */
    public UserDAO getUserDAO() {
        return userDAO;
    }

    /**
     * @return the GameDAO instance
     */
    public GameDAO getGameDAO() {
        return gameDAO;
    }

    /**
     * Enum representing supported data storage backend
     */
    public enum DataLocation {
        SQL,
        MEMORY
    }
}
