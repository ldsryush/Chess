package model;

public record GameName(String gameName) {
    public String name() {
        return gameName;
    }
}
