package model;

public class GameData {
    private int gameID;
    private String whiteUsername;
    private String blackUsername;
    private String gameName;
    private String game; // Serialized ChessGame object

    public GameData(int gameID, String whiteUsername, String blackUsername, String gameName, String game) {
        this.gameID = gameID;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
        this.gameName = gameName;
        this.game = game;
    }

}
