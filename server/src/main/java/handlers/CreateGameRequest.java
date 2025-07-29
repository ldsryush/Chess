package handlers;

/**
 * Represents a request to create a new game.
 *
 * @param gameName    the name of the game to be created
 * @param playerColor the color chosen by the player
 */
public record CreateGameRequest(String gameName, String playerColor) {
}
