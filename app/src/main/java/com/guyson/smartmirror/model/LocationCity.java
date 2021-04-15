package com.guyson.smartmirror.model;

public class LocationCity {
    private String name, country;
    private int id;

    public LocationCity(String name, String country, int id) {
        this.name = name;
        this.country = country;
        this.id = id;
    }

    public LocationCity() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
