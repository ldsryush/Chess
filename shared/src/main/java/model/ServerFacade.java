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
        AuthData authData = this.makeRequest("POST", path, userData, AuthData.class, false);
        if (authData != null) {
            authToken = authData.authToken();
        }
        return authData;
    }

    public AuthData loginUser(UserData userData) throws ResponseException {
        var path = "/session";
        AuthData authData = this.makeRequest("POST", path, userData, AuthData.class, false);
        if (authData != null) {
            authToken = authData.authToken();
        }
        return authData;
    }

    public void logoutUser() throws ResponseException {
        var path = "/session";
        this.makeRequest("DELETE", path, null, null, true);
        authToken = null;
    }

    public void joinGame(JoinGameRequest request) throws ResponseException {
        var path = "/game";
        this.makeRequest("PUT", path, request, null, true);
    }

    public GameID createGame(GameName name) throws ResponseException {
        var path = "/game";
        var request = new CreateGameRequest(name.name(), "RED");
        return this.makeRequest("POST", path, request, GameID.class, true);
    }

    public ArrayList<GameResponseData> listGames() throws ResponseException {
        var path = "/game";
        GameResponseList response = this.makeRequest("GET", path, null, GameResponseList.class, true);
        return response.games();
    }

    public void clear() throws ResponseException {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, true);
    }

    // Primary request method with auth toggle
    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, boolean requiresAuth) throws ResponseException {
        try {
            URL url = new URI(serverUrl + path).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (requiresAuth && authToken != null) {
                http.setRequestProperty("authorization", authToken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        return makeRequest(method, path, request, responseClass, true);
    }

    private void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.setRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        int status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
