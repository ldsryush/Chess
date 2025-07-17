package handlers;

/**
 * Represents a request to log out a user session.
 *
 * @param authToken the authentication token identifying the session to terminate
 */
public record LogoutRequest(String authToken) {
}
