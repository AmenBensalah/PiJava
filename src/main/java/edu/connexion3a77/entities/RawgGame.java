package edu.connexion3a77.entities;

public class RawgGame {
    private final int id;
    private final String name;
    private final double rating;
    private final String released;
    private final String imageUrl;
    private final String rawgUrl;

    public RawgGame(int id, String name, double rating, String released, String imageUrl, String rawgUrl) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.released = released;
        this.imageUrl = imageUrl;
        this.rawgUrl = rawgUrl;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }

    public String getReleased() {
        return released;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getRawgUrl() {
        return rawgUrl;
    }
}
