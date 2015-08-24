package com.example.gael.popularmovies;

import java.io.Serializable;

/**
 * Created by gael on 24/08/15.
 */
public class Poster implements Serializable {
    private final String key;
    private final String value;
    private final String title;
    private final String overview;
    private final String rating;
    private final String date;

    public String getDate() {
        return date;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getRating() {
        return rating;
    }



    public Poster(String key, String value, String title, String overview, String rating, String date) {
        this.key = key;
        this.value = value;
        this.title = title;
        this.overview = overview;
        this.rating = rating;
        this.date = date;
    }






}
