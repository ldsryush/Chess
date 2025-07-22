package dataaccess.mysql;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.UserDAO;
import exception.ResponseException;
import model.UserData;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLUserDAO implements UserDAO {

    private Connection getConnection() throws DataAccessException {
        try {
            DataAccess.configureDatabase();
            return DatabaseManager.getConnection();
        } catch (ResponseException e) {
            throw new DataAccessException("Failed to configure database", e);
        }
    }

    @Override
    public boolean isUser(UserData userData) throws DataAccessException {
        try (var conn = getConnection();
             var preparedStatement = conn.prepareStatement("SELECT NAME FROM USERS WHERE NAME=?")) {
            preparedStatement.setString(1, userData.username());
            try (var rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to check if user exists", ex);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = getConnection();
             var preparedStatement = conn.prepareStatement("SELECT PASSWORD, EMAIL FROM USERS WHERE NAME=?")) {
            preparedStatement.setString(1, username);
            try (var rs = preparedStatement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String password = rs.getString("PASSWORD");
                String email = rs.getString("EMAIL");
                return new UserData(username, password, email);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch user", e);
        }
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        try (var conn = getConnection();
             var preparedStatement = conn.prepareStatement(
                     "INSERT INTO USERS (NAME, PASSWORD, EMAIL) VALUE (?, ?, ?)")) {
            preparedStatement.setString(1, userData.username());
            preparedStatement.setString(2, userData.password());
            preparedStatement.setString(3, userData.email());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create user", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = getConnection();
             var preparedStatement = conn.prepareStatement("TRUNCATE TABLE USERS")) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear USERS table", e);
        }
    }
}