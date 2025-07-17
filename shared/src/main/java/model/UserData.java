package model;

/**
 * Represents user account information.
 *
 * @param username the unique username of the user
 * @param password the user's password (stored as plain text in this model)
 * @param email    the user's email address
 */
public record UserData(String username, String password, String email) {
}
