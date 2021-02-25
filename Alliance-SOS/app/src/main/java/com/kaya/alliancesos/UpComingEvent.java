package com.kaya.alliancesos;

import java.util.TimeZone;

public class UpComingEvent {
    private String upcomingName;
    private DateTime upcomingTime;
    private String sourceTimeZoneId;

    public UpComingEvent() {

    }

    public UpComingEvent(String name, DateTime time) {
        upcomingName = name;
        upcomingTime = time;
        sourceTimeZoneId = TimeZone.getDefault().getID();
    }

    public String getSourceTimeZoneId() {
        return sourceTimeZoneId;
    }

    public String getUpcomingName() {
        return upcomingName;
    }

    public DateTime getUpcomingTime() {
        return upcomingTime;
    }

    public void setUpcomingName(String upcomingName) {
        this.upcomingName = upcomingName;
    }

    public void setUpcomingTime(DateTime upcomingTime) {
        this.upcomingTime = upcomingTime;
    }
}
