package com.guyson.smartmirror.model;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String uid, email, firstName, lastName, location;

    private List<String> news = new ArrayList<>();
    private List<String> happy = new ArrayList<>();
    private List<String> sad = new ArrayList<>();
    private List<String> neutral = new ArrayList<>();
    private boolean configuredFaceRecognition;

    public User() {}


    public User(String uid, String email, String firstName, String lastName) {
        this.uid = uid;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.configuredFaceRecognition = false;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getNews() {
        return news;
    }

    public void setNews(List<String> news) {
        this.news = news;
    }

    public List<String> getHappy() {
        return happy;
    }

    public void setHappy(List<String> happy) {
        this.happy = happy;
    }

    public List<String> getSad() {
        return sad;
    }

    public void setSad(List<String> sad) {
        this.sad = sad;
    }

    public List<String> getNeutral() {
        return neutral;
    }

    public void setNeutral(List<String> neutral) {
        this.neutral = neutral;
    }

    public boolean isConfiguredFaceRecognition() {
        return configuredFaceRecognition;
    }

    public void setConfiguredFaceRecognition(boolean configuredFaceRecognition) {
        this.configuredFaceRecognition = configuredFaceRecognition;
    }
}
