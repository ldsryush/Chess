package Service;

import dataAccess.AuthDAO;
import dataAccess.GameDAO;

/**
 * Handles requests to clear the game
 */
public class ClearService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    /**
     * Accepts objects providing data access
     * @param userDAO UserDAO object providing access to the user database
     * @param authDAO AuthDAO object providing access to the authorization database
     * @param gameDAO GameDAO object providing access to the game database
     */
    public ClearService(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    /**
     * Clears the databases by calling each DAO's clear function
     */
    public void clearDatabase() {
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }
}
