package handlers;

/**
 * Represents request to create new game.
 *
 * @param authToken the authentication token of the requesting user
 * @param gameName  the name of the game to be created
 */
public record CreateGameRequest(String authToken, String gameName) {
}
