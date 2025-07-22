package dataaccess.mySQL;

import dataaccess.AuthDAO;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import exception.ResponseException;
import model.AuthData;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MySQLAuthDAO implements AuthDAO {

    private Connection getConnection() throws DataAccessException {
        try {
            DataAccess.configureDatabase();
            return DatabaseManager.getConnection();
        } catch (ResponseException e) {
            throw new DataAccessException("Error: failed to configure database", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("TRUNCATE TABLE AUTH")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to clear AUTH table", e);
        }
    }

    @Override
    public AuthData createAuth(UserData userData) throws DataAccessException {
        String token = UUID.randomUUID().toString();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO AUTH (NAME, TOKEN) VALUE (?, ?)")) {
            ps.setString(1, userData.username());
            ps.setString(2, token);
            ps.executeUpdate();
            return new AuthData(userData.username(), token);
        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to create auth token", e);
        }
    }

    @Override
    public boolean authExists(String authToken) throws DataAccessException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT TOKEN FROM AUTH WHERE TOKEN=?")) {
            ps.setString(1, authToken);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to check auth token", e);
        }
    }

    @Override
    public AuthData getAuthData(String authToken) throws DataAccessException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT NAME FROM AUTH WHERE TOKEN=?")) {
            ps.setString(1, authToken);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;    // real “not found” → 401 in AuthenticationService
                }
                String username = rs.getString("NAME");
                return new AuthData(username, authToken);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to fetch auth data", e);
        }
    }

    @Override
    public boolean deleteAuth(String authToken) throws DataAccessException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM AUTH WHERE TOKEN=?")) {
            ps.setString(1, authToken);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error: failed to delete auth token", e);
        }
    }
}
