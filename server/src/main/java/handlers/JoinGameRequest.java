package handlers;

/**
 * Represents request to join existing games
 *
 * @param playerColor the color the player wishes to play as (e.g., "WHITE" or "BLACK")
 * @param gameID      the ID of the game to join
 */
public record JoinGameRequest(String playerColor, Integer gameID) {
}
