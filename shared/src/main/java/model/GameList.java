package model;

import java.util.Collection;

public record GameList(Collection<GameResponseData> games) {
}