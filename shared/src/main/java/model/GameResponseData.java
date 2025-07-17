package model;

/**
 * Represents a simplified view of a game for client-side responses.
 *
 * @param gameID        the unique identifier of the game
 * @param whiteUsername the username of the white player
 * @param blackUsername the username of the black player
 * @param gameName      the name of the game
 */
public record GameResponseData(Integer gameID, String whiteUsername, String blackUsername, String gameName) {
}
