package dataaccess;

import exception.ResponseException;

import java.sql.SQLException;

public class DataAccess {
    private static final String[] CREATE_STATEMENTS = {
            """
        CREATE TABLE IF NOT EXISTS USERS (
            `ID` int NOT NULL AUTO_INCREMENT,
            `NAME` varchar(255) NOT NULL,
            `PASSWORD` varchar(255) NOT NULL,
            `EMAIL` varchar(255) NOT NULL,
            PRIMARY KEY (`ID`),
            INDEX(ID)
        )
        """,
            """
        CREATE TABLE IF NOT EXISTS AUTH (
            `NAME` varchar(255) NOT NULL,
            `TOKEN` varchar(255) NOT NULL
        )
        """,
            """
        CREATE TABLE IF NOT EXISTS GAME (
            `ID` int NOT NULL,
            `WHITENAME` varchar(255),
            `BLACKNAME` varchar(255),
            `GAMENAME` varchar(255) NOT NULL,
            `JSON` TEXT NOT NULL
        )
        """
    };

    public static void configureDatabase() throws ResponseException {
        try {
            DatabaseManager.createDatabase();
            try (var conn = DatabaseManager.getConnection()) {
                for (var statement : CREATE_STATEMENTS) {
                    try (var preparedStatement = conn.prepareStatement(statement)) {
                        preparedStatement.executeUpdate();
                    }
                }
            } catch (SQLException ex) {
                throw new ResponseException(500, String.format("Unable to configure database: %s", ex.getMessage()));
            }
        } catch (DataAccessException ex) {
            throw new ResponseException(500, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}