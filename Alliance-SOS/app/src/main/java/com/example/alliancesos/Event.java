package com.example.alliancesos;

import android.text.TextUtils;

import java.util.TimeZone;

public class Event {
    private String eventId;
    private String createdBy;
    private String createdTime;
    private String createdTimezoneId;
    private ScheduleObject scheduleObject;

    public Event(String eventId, String writer, String created, ScheduleObject sch, String timezone) {
        scheduleObject = sch;
        this.eventId = eventId;
        createdBy = writer;
        createdTime = created;
        createdTimezoneId = timezone;
        if (TextUtils.isEmpty(createdTimezoneId)) {
            createdTimezoneId = TimeZone.getDefault().getID();
        }
    }

    public Event() {
    }

    public String getCreatedTimezoneId() {
        return createdTimezoneId;
    }

    public void setCreatedTimezoneId(String createdTimezoneId) {
        this.createdTimezoneId = createdTimezoneId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public ScheduleObject getScheduleObject() {
        return scheduleObject;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public void setScheduleObject(ScheduleObject scheduleObject) {
        this.scheduleObject = scheduleObject;
    }
}
