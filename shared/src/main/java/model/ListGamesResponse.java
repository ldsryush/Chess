package model;

import java.util.Collection;

/**
 * Represents a response containing the list of available games.
 *
 * @param games a collection of GameResponseData objects representing each game
 */
public record ListGamesResponse(Collection<GameResponseData> games) {
}
