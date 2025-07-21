package dataaccess.mySQL;

import dataaccess.AuthDAO;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import exception.ResponseException;
import model.AuthData;
import model.UserData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

/**
 * Provides access to the authorization data
 */
public class MySQLAuthDAO implements AuthDAO {
    private final Connection conn;

    /**
     * Sets up the connection to the database
     * @throws ResponseException If anything fails
     */
    public MySQLAuthDAO() throws ResponseException {
        DataAccess.configureDatabase();
        try {
            conn = DatabaseManager.getConnection();
        } catch (DataAccessException e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    /**
     * Clears the database
     * @throws DataAccessException if anything fails
     */
    @Override
    public void clear() throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement("TRUNCATE TABLE AUTH")) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

    }

    /**
     * Creates an authToken and adds the data to the database
     * @param userData the user data to add
     * @return authData with the username and authToken
     * @throws DataAccessException if anything fails
     */
    @Override
    public AuthData createAuth(UserData userData) throws DataAccessException {
        var authToken = UUID.randomUUID().toString();
        try (var preparedStatement = conn.prepareStatement("INSERT INTO AUTH (NAME, TOKEN) VALUE (?, ?)")) {
            preparedStatement.setString(1, userData.username());
            preparedStatement.setString(2, authToken);

            preparedStatement.executeUpdate();

            return new AuthData(userData.username(), authToken);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Determines whether a given user exists given a particular authToken
     * @param authToken the authToken to search the database for
     * @return true if it exists, false if not
     * @throws DataAccessException if anything fails
     */
    @Override
    public boolean authExists(String authToken) throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement("SELECT TOKEN FROM AUTH WHERE TOKEN=?")) {
            preparedStatement.setString(1, authToken);
            try (var rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    /**
     * Finds the user associated with a given authToken
     * @param authToken the token to search the database for
     * @return AuthData object containing the user found
     * @throws DataAccessException if anything fails in the query
     */
    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement("SELECT NAME from AUTH where TOKEN=?")) {
            preparedStatement.setString(1, authToken);
            try (var rs = preparedStatement.executeQuery()) {
                String username = "";
                while (rs.next()) {
                    username = rs.getString("NAME");
                }
                if (Objects.equals(username, "")) {
                    throw new DataAccessException("invalid authorization token");
                }
                return new AuthData(username, authToken);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Deletes an authtoken from the database and indicates whether it was successful
     * @param authToken the authtoken to delete
     * @return true if it was deleted, false if not
     * @throws DataAccessException if anything fails
     */
    @Override
    public boolean deleteAuth(String authToken) throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement("DELETE FROM AUTH WHERE TOKEN=?")) {
            preparedStatement.setString(1, authToken);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
