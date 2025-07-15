package handlers;

public record CreateGameRequest(String authToken, String gameName) {
}