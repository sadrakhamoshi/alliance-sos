package com.kaya.alliancesos;

import android.text.TextUtils;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class Event implements Serializable, Comparable<Event> {
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

    @Override
    public int compareTo(Event o) {
        DateTime tmp = this.getScheduleObject().getDateTime();
        Instant instant = Instant.now();
        ZonedDateTime source = instant.atZone(ZoneId.of(this.getCreatedTimezoneId())).
                withYear(Integer.parseInt(tmp.getYear())).
                withMonth(Integer.parseInt(tmp.getMonth()) + 1).
                withDayOfMonth(Integer.parseInt(tmp.getDay())).
                withHour(Integer.parseInt(tmp.getHour())).
                withMinute(Integer.parseInt(tmp.getMinute()))
                .withSecond(0);
        ZonedDateTime target = source.toInstant().atZone(ZoneId.of("GMT"));

        Instant instant2 = Instant.now();
        DateTime tmp2 = o.getScheduleObject().getDateTime();
        ZonedDateTime source2 = instant2.atZone(ZoneId.of(o.getCreatedTimezoneId())).
                withYear(Integer.parseInt(tmp2.getYear())).
                withMonth(Integer.parseInt(tmp2.getMonth()) + 1).
                withDayOfMonth(Integer.parseInt(tmp2.getDay())).
                withHour(Integer.parseInt(tmp2.getHour())).
                withMinute(Integer.parseInt(tmp2.getMinute()))
                .withSecond(0);
        ZonedDateTime target2 = source2.toInstant().atZone(ZoneId.of("GMT"));

        if (target.isAfter(target2))
            return 1;
        if (target2.isAfter(target))
            return -1;
        return 0;
    }
}
