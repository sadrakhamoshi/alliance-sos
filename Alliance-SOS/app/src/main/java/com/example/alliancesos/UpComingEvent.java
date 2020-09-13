package com.example.alliancesos;

public class UpComingEvent {
    private String upcomingName;
    private String upcomingTime;

    public UpComingEvent() {
    }

    public UpComingEvent(String name, String time) {
        upcomingName = name;
        upcomingTime = time;
    }

    public String getUpcomingName() {
        return upcomingName;
    }

    public String getUpcomingTime() {
        return upcomingTime;
    }

    public void setUpcomingName(String upcomingName) {
        this.upcomingName = upcomingName;
    }

    public void setUpcomingTime(String upcomingTime) {
        this.upcomingTime = upcomingTime;
    }
}
