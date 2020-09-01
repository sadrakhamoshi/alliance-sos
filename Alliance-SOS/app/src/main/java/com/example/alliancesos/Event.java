package com.example.alliancesos;

import com.example.alliancesos.Utils.MessageType;

public class Event {
    private String createdBy;
    private String createdTime;
    private ScheduleObject scheduleObject;
    private Integer type;

    public Event(String writer, String created, ScheduleObject sch) {
        scheduleObject = sch;
        type = MessageType.NOTIFICATION_TYPE;
        createdBy = writer;
        createdTime = created;
    }

    public Event() {
    }

    public Integer getType() {
        return type;
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
