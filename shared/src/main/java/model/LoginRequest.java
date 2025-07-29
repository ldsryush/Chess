package model;

/**
 * Represents a request to log in with a username and password.
 *
 * @param username the user's username
 * @param password the user's password
 */
public record LoginRequest(String username, String password) {
}
