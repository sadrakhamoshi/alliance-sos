package com.example.alliancesos;

public class Message {
    private String createdBy;
    private String createdTime;
    private ScheduleObject scheduleObject;

    public Message(String writer, String created, ScheduleObject sch) {
        scheduleObject = sch;
        createdBy = writer;
        createdTime = created;
    }

    public Message() {
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
