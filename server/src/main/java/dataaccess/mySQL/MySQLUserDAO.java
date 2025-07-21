package dataaccess.mySQL;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.UserDAO;
import exception.ResponseException;
import model.UserData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Class to provide access to the database for UserData
 */
public class MySQLUserDAO implements UserDAO {
    private final Connection conn;

    /**
     * Connects to the database
     * @throws ResponseException if connection fails
     */
    public MySQLUserDAO() throws ResponseException {
        DataAccess.configureDatabase();
        try {
            conn = DatabaseManager.getConnection();
        } catch (DataAccessException e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    /**
     * Checks whether a given user exists in the database
     * @param userData object containing information on the user
     * @return true if the user exists/false if they don't
     * @throws DataAccessException if anything fails
     */
    @Override
    public boolean isUser(UserData userData) throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement("SELECT NAME FROM USERS WHERE NAME=?")) {
            preparedStatement.setString(1, userData.username());
            try (var rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    /**
     * Gets the UserData object associated with a given username
     * @param username username to search for
     * @return UserData object containing all the user's data
     * @throws DataAccessException if anything fails
     */
    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement("SELECT PASSWORD, EMAIL from USERS where NAME=?")) {
            preparedStatement.setString(1, username);
            try (var rs = preparedStatement.executeQuery()) {
                String password = "";
                String email = "";
                while (rs.next()) {
                    password = rs.getString("PASSWORD");
                    email = rs.getString("EMAIL");
                }
                if (Objects.equals(password, "") && Objects.equals(email, "")) {
                    return null;
                }
                return new UserData(username, password, email);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Creates a user using the data given
     * @param userData the data to add to the database
     * @throws DataAccessException if anything fails
     */
    @Override
    public void createUser(UserData userData) throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement("INSERT INTO USERS (NAME, PASSWORD, EMAIL) VALUE(?, ?, ?)")) {
            preparedStatement.setString(1, userData.username());
            preparedStatement.setString(2, userData.password());
            preparedStatement.setString(3, userData.email());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Clears the entire database
     * @throws DataAccessException if anything fails
     */
    @Override
    public void clear() throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement("TRUNCATE TABLE USERS")) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

}
