package model;

import com.google.gson.*;

public record User(String username, String password, String email) {

    public String toString() {
        return new Gson().toJson(this);
    }
}