package dataAccess.mySQL;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.UserDAO;
import exception.ResponseException;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLUserDAO implements UserDAO {
    private final Connection conn;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public MySQLUserDAO() throws ResponseException {
        DataAccess.configureDatabase();
        try {
            conn = DatabaseManager.getConnection();
        } catch (DataAccessException e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    @Override
    public boolean isUser(UserData userData) throws DataAccessException {
        String sql = "SELECT 1 FROM USERS WHERE NAME=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userData.username());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error checking user existence", ex);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT PASSWORD, EMAIL FROM USERS WHERE NAME=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String passwordHash = rs.getString("PASSWORD");
                    String email = rs.getString("EMAIL");
                    return new UserData(username, passwordHash, email);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user", e);
        }
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        String sql = "INSERT INTO USERS (NAME, PASSWORD, EMAIL) VALUES (?, ?, ?)";
        String hashedPassword = encoder.encode(userData.password());

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userData.username());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, userData.email());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating user", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "TRUNCATE TABLE USERS";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing users", e);
        }
    }

    public boolean verifyPassword(String username, String inputPassword) throws DataAccessException {
        UserData user = getUser(username);
        return user != null && encoder.matches(inputPassword, user.password());
    }
}
