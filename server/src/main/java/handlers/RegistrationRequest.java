package handlers;

import com.google.gson.Gson;

public record RegistrationRequest(String username, String password, String email) {

    public String toString() {
        return new Gson().toJson(this);
    }
}