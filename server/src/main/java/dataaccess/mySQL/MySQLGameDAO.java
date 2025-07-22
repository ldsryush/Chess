package dataaccess.mySQL;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.GameDAO;
import exception.ResponseException;
import model.GameData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

public class MySQLGameDAO implements GameDAO {
    private final Connection conn;

    /**
     * Constructor for the SQL GameDAO
     * Connects to the database
     * @throws ResponseException if can't connect
     */
    public MySQLGameDAO() throws ResponseException {
        DataAccess.configureDatabase();
        try {
            conn = DatabaseManager.getConnection();
        } catch (DataAccessException e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    /**
     * Clears the entire database
     * @throws DataAccessException if fails to clear
     */
    @Override
    public void clear() throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement("TRUNCATE TABLE GAME")) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Adds a game to the database
     * @param gameData object with the data of the game to be stored
     * @throws DataAccessException If anything fails while storing
     */
    @Override
    public void addGame(GameData gameData) throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement("INSERT INTO GAME (ID, WHITENAME, BLACKNAME, GAMENAME, JSON) VALUES(?, ?, ?, ?, ?)")) {
            preparedStatement.setString(1, String.valueOf(gameData.gameID()));
            preparedStatement.setString(2, gameData.whiteUsername());
            preparedStatement.setString(3, gameData.blackUsername());
            preparedStatement.setString(4, gameData.gameName());
            preparedStatement.setString(5, new Gson().toJson(gameData.game()));

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Gets a game given a specified gameID
     * @param gameID the ID of the desired games
     * @return GameData object of the game with the given ID
     * @throws DataAccessException if anything fails
     */
    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement("SELECT * from GAME where ID=?")) {
            preparedStatement.setString(1, String.valueOf(gameID));
            try (var rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    var whiteUsername = rs.getString("WHITENAME");
                    var blackUsername = rs.getString("BLACKNAME");
                    var gameName = rs.getString("GAMENAME");
                    var game = new Gson().fromJson(
                            rs.getString("JSON"),
                            ChessGame.class
                    );

                    return new GameData(
                            gameID,
                            whiteUsername,
                            blackUsername,
                            gameName,
                            game
                    );
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Lists all the games in the database
     * @return Collection of all the games
     * @throws DataAccessException if anything fails
     */
    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        Collection<GameData> gameList = new HashSet<>();

        try (var preparedStatement = conn.prepareStatement("SELECT * from GAME")) {
            try (var rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    int gameID = rs.getInt("ID");
                    var whiteUsername = rs.getString("WHITENAME");
                    var blackUsername = rs.getString("BLACKNAME");
                    var gameName = rs.getString("GAMENAME");
                    var game = new Gson().fromJson(
                            rs.getString("JSON"),
                            ChessGame.class
                    );

                    gameList.add(new GameData(
                            gameID,
                            whiteUsername,
                            blackUsername,
                            gameName,
                            game
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

        return gameList;
    }

    /**
     * Updates a game using new information for the game
     * @param newGame GameData object containing data for the new game
     * @throws DataAccessException if anything fails
     */
    @Override
    public void updateGame(GameData newGame) throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement(
                "UPDATE GAME SET WHITENAME=?, BLACKNAME=?, GAMENAME=?, JSON=? WHERE ID=?")) {
            preparedStatement.setString(1, newGame.whiteUsername());
            preparedStatement.setString(2, newGame.blackUsername());
            preparedStatement.setString(3, newGame.gameName());
            preparedStatement.setString(4, new Gson().toJson(newGame.game()));
            preparedStatement.setString(5, String.valueOf(newGame.gameID()));

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}