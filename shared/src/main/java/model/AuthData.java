package model;

/**
 * Represents authentication data for a user session.
 *
 * @param username  the username associated with the session
 * @param authToken the unique token identifying the session
 */
public record AuthData(String username, String authToken) {
}
