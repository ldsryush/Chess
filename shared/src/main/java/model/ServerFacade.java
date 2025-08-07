package model;

import com.google.gson.Gson;
import exception.ResponseException;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ServerFacade {
    private final String serverUrl;
    private String authToken;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthData registerUser(UserData userData) throws ResponseException {
        var path = "/user";
        AuthData authData = makeRequest("POST", path, userData, AuthData.class, false);
        if (authData != null) {
            authToken = authData.authToken();
        }
        return authData;
    }

    public AuthData loginUser(UserData userData) throws ResponseException {
        var path = "/session";
        AuthData authData = makeRequest("POST", path, userData, AuthData.class, false);
        if (authData != null) {
            authToken = authData.authToken();
        }
        return authData;
    }

    public void logoutUser() throws ResponseException {
        var path = "/session";
        makeRequest("DELETE", path, null, null, true);
        authToken = null;
    }

    public void joinGame(JoinGameRequest request) throws ResponseException {
        var path = "/game";
        makeRequest("PUT", path, request, null, true);
    }

    /** Observe game as spectator */
    public void observeGame(int gameID) throws ResponseException {
        var path = "/game/observe/" + gameID;
        makeRequest("PUT", path, null, null, true);
    }

    public GameID createGame(GameName name) throws ResponseException {
        var path = "/game";
        var request = new CreateGameRequest(name.name(), "RED");
        return makeRequest("POST", path, request, GameID.class, true);
    }

    public ArrayList<GameResponseData> listGames() throws ResponseException {
        var path = "/game";
        GameResponseList resp = makeRequest("GET", path, null, GameResponseList.class, true);
        return resp.games();
    }

    public void clear() throws ResponseException {
        var path = "/db";
        makeRequest("DELETE", path, null, null, true);
    }

    private <T> T makeRequest(String method, String path, Object request,
                              Class<T> responseClass, boolean requiresAuth) throws ResponseException {
        try {
            var fullUrl = serverUrl + path;
            URL url = new URI(fullUrl).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (requiresAuth && authToken != null) {
                http.setRequestProperty("authorization", authToken);
            }

            if (request != null) {
                http.setRequestProperty("Content-Type", "application/json");
                var json = new Gson().toJson(request);
                try (var os = http.getOutputStream()) {
                    os.write(json.getBytes());
                }
            }

            http.connect();
            int status = http.getResponseCode();
            if (status / 100 != 2) {
                throw new ResponseException(status, "failure: " + status);
            }

            if (responseClass != null && http.getContentLength() != 0) {
                try (InputStream is = http.getInputStream();
                     InputStreamReader reader = new InputStreamReader(is)) {
                    return new Gson().fromJson(reader, responseClass);
                }
            }

            return null;
        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }
}