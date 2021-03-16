package com.guyson.smartmirror.models;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String uid, email, firstName, lastName;

    private boolean configuredFaceRecognition, configuredLocation, configuredHappy, configuredSad, configuredNeutral, configuredNews;
    private List<String> topics = new ArrayList<>();

    public User() {}

    public User(String uid, String email, String firstName, String lastName) {
        this.uid = uid;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.configuredFaceRecognition = false;
        this.configuredHappy = false;
        this.configuredSad = false;
        this.configuredNeutral = false;
        this.configuredLocation =false;
        this.configuredNews = false;
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

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public boolean isConfiguredFaceRecognition() {
        return configuredFaceRecognition;
    }

    public void setConfiguredFaceRecognition(boolean configuredFaceRecognition) {
        this.configuredFaceRecognition = configuredFaceRecognition;
    }

    public boolean isConfiguredLocation() {
        return configuredLocation;
    }

    public void setConfiguredLocation(boolean configuredLocation) {
        this.configuredLocation = configuredLocation;
    }

    public boolean isConfiguredHappy() {
        return configuredHappy;
    }

    public void setConfiguredHappy(boolean configuredHappy) {
        this.configuredHappy = configuredHappy;
    }

    public boolean isConfiguredSad() {
        return configuredSad;
    }

    public void setConfiguredSad(boolean configuredSad) {
        this.configuredSad = configuredSad;
    }

    public boolean isConfiguredNeutral() {
        return configuredNeutral;
    }

    public void setConfiguredNeutral(boolean configuredNeutral) {
        this.configuredNeutral = configuredNeutral;
    }

    public boolean isConfiguredNews() {
        return configuredNews;
    }

    public void setConfiguredNews(boolean configuredNews) {
        this.configuredNews = configuredNews;
    }
}
