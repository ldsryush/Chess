package Service;

import dataAccess.GameDAO;
import exception.ResponseException;
import handlers.JoinGameRequest;
import model.AuthData;
import model.GameData;

import java.util.Objects;

public class JoinService {
    private final GameDAO gameDAO;

    public JoinService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public void joinGame(JoinGameRequest request, AuthData authData) throws ResponseException {
        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new ResponseException(400, "error: bad request");
        }

        String whiteUsername = game.whiteUsername();
        String blackUsername = game.blackUsername();

        if (Objects.equals(request.playerColor(), "WHITE")) {
            if (whiteUsername != null) {
                throw new ResponseException(403, "error: already taken");
            }
            whiteUsername = authData.username();
        } else if (Objects.equals(request.playerColor(), "BLACK")) {
            if (blackUsername != null) {
                throw new ResponseException(403, "error: already taken");
            }
            blackUsername = authData.username();
        } else if (request.playerColor() != null) {
            throw new ResponseException(400, "error: bad request");
        }

        GameData newGame = new GameData(
                request.gameID(),
                whiteUsername,
                blackUsername,
                game.gameName(),
                game.game());

        gameDAO.updateGame(request.gameID(), newGame);
    }

}
