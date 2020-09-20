package com.example.alliancesos;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.TimeZone;

public class Event implements Serializable {
    private String eventId;
    private String createdBy;
    private long timeInMillisecond;
    private String createdTimezoneId;
    private ScheduleObject scheduleObject;

    public Event(String eventId, String writer, long timeInMillisecond, ScheduleObject sch, String timezone) {
        scheduleObject = sch;
        this.eventId = eventId;
        createdBy = writer;
        this.timeInMillisecond = timeInMillisecond;
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

    public long getTimeInMillisecond() {
        return timeInMillisecond;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setTimeInMillisecond(long timeInMillisecond) {
        this.timeInMillisecond = timeInMillisecond;
    }

    public void setScheduleObject(ScheduleObject scheduleObject) {
        this.scheduleObject = scheduleObject;
    }
}