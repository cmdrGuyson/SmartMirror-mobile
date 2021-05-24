package com.guyson.smartmirror.model;

public class CalendarEvent {

    private String date, time, title;
    private long id;

    public CalendarEvent(long id, String date, String time, String title) {
        this.date = date;
        this.time = time;
        this.title = title;
    }

    public CalendarEvent() {}

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
