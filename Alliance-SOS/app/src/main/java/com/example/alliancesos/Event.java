package com.example.alliancesos;

import com.example.alliancesos.Utils.MessageType;

public class Event {
    private String eventId;
    private String createdBy;
    private String createdTime;
    private ScheduleObject scheduleObject;

    public Event(String eventId, String writer, String created, ScheduleObject sch) {
        scheduleObject = sch;
        this.eventId = eventId;
        createdBy = writer;
        createdTime = created;
    }

    public Event() {
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
