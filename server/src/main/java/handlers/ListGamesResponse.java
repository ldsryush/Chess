package handlers;

import model.GameResponseData;

import java.util.Collection;

public record ListGamesResponse(Collection<GameResponseData> games) {
}
