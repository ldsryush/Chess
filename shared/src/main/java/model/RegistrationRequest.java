package model;

/**
 * Represents a request to register a new user.
 *
 * @param username the desired username
 * @param password the user's password
 * @param email    the user's email address
 */
public record RegistrationRequest(String username, String password, String email) {
}
