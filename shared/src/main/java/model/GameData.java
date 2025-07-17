package model;

import chess.ChessGame;

/**
 * Represents the full state and metadata of a chess game.
 *
 * @param gameID        the unique identifier for the game
 * @param whiteUsername the username of the player assigned to white (nullable)
 * @param blackUsername the username of the player assigned to black (nullable)
 * @param gameName      the name of the game, as provided by the creator
 * @param game          the actual ChessGame instance representing board state and rules
 */
public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
}
