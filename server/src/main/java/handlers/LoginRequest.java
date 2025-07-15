package handlers;

import com.google.gson.Gson;

public record LoginRequest(String username, String password) {

    public String toString() {
        return new Gson().toJson(this);
    }
}