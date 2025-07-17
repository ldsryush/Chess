package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

/**
 * Service responsible for clearing all data from the application.
 * This includes users, authentication tokens, and games.
 */
public class ClearService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    /**
     * Constructs a ClearService with the given DAOs.
     *
     * @param userDAO the data access object for users
     * @param authDAO the data access object for authentication tokens
     * @param gameDAO the data access object for games
     */
    public ClearService(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    /**
     * Clears all stored data in the system.
     * This method resets the user, auth, and game stores.
     */
    public void clearDatabase() {
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }
}
