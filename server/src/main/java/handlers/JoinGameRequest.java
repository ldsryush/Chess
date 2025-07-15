package handlers;

public record JoinGameRequest(String authToken, String playerColor, String gameID) {
}