package com.example.alliancesos;

import java.io.Serializable;

public class ScheduleObject implements Serializable {
    private String title, description;

    private DateTime dateTime;

    public ScheduleObject(String title, String des) {
        this.title = title;
        description = des;
    }

    public ScheduleObject() {
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String GetDate() {
        DateTime dateTime = this.getDateTime();
        String res = dateTime.getYear() + "/" + (dateTime.getMonth() + 1) + "/" + dateTime.getDay() + " " + dateTime.getHour() + ":" + dateTime.getMinute();
        return res;
    }
}
