package com.myapps.ron.family_recipes.recycler;

/**
 * Created by ravi on 16/11/17.
 */

public class Contact {
    String title;
    String image = "https://api.androidhive.info/json/images/keanu.jpg";
    String author;
    String genre;
    String read;



    public Contact() {
    }

    public String getName() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getPhone() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public String getRead() {
        return read;
    }
}

/*
public class Contact {
    String name;
    String image;
    String description;

    public Contact() {
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getPhone() {
        return description;
    }
}
*/