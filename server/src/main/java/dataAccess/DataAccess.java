package dataAccess;

import dataAccess.memory.MemoryAuthDAO;
import dataAccess.memory.MemoryGameDAO;
import dataAccess.memory.MemoryUserDAO;

/**
 * DataAccess class to provide access to the correct data access objects
 */
public class DataAccess {
    private dataAccess.AuthDAO authDAO;
    private UserDAO userDAO;
    private GameDAO gameDAO;

    /**
     * Upon initialization, determines whether to use SQL server or memory
     * @param dataLocation DataLocation object indicating where to look for data
     */
    public DataAccess(DataLocation dataLocation) {
        if (dataLocation == DataLocation.SQL) {
            try {
                throw new DataAccessException("Can't yet use SQL");
            } catch (DataAccessException e) {
                throw new RuntimeException(e);
            }
        } else if (dataLocation == DataLocation.MEMORY) {
            this.authDAO = new MemoryAuthDAO();
            this.userDAO = new MemoryUserDAO();
            this.gameDAO = new MemoryGameDAO();
        }
    }

    /**
     * Getter function for the AuthDAO object
     * @return AuthDAO object providing access to authorization data
     */
    public dataAccess.AuthDAO getAuthDAO() {
        return authDAO;
    }

    /**
     * Getter function for the UserDAO object
     * @return AuthDAO object providing access to authorization data
     */
    public UserDAO getUserDAO() {
        return userDAO;
    }

    /**
     * Getter function for the GameDAO object
     * @return AuthDAO object providing access to authorization data
     */
    public GameDAO getGameDAO() {
        return gameDAO;
    }

    /**
     * Objects to indicate the data location to use for the server
     */
    public enum DataLocation {
        SQL,
        MEMORY
    }
}
