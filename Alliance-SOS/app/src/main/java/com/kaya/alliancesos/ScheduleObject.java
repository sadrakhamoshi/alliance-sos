package com.kaya.alliancesos;

import android.widget.TextView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

    public String GetDate(String mFrom_TimeZoneId) {
        DateTime tmp = this.getDateTime();
        Instant instant = Instant.now();
        ZonedDateTime source = instant.atZone(ZoneId.of(mFrom_TimeZoneId)).
                withYear(Integer.parseInt(tmp.getYear())).
                withMonth(Integer.parseInt(tmp.getMonth()) + 1).
                withDayOfMonth(Integer.parseInt(tmp.getDay())).
                withHour(Integer.parseInt(tmp.getHour())).
                withMinute(Integer.parseInt(tmp.getMinute()))
                .withSecond(0);
        ZonedDateTime target = source.toInstant().atZone(ZoneId.systemDefault());
        return DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm").format(target);
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.YEAR, Integer.parseInt(this.getDateTime().getYear()));
//        calendar.set(Calendar.MONTH, Integer.parseInt(this.getDateTime().getMonth()));
//        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(this.getDateTime().getDay()));
//        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(this.getDateTime().getHour()));
//        calendar.set(Calendar.MINUTE, Integer.parseInt(this.getDateTime().getMinute()));
//        calendar.set(Calendar.SECOND, 0);
//
//        TimeZone from = TimeZone.getTimeZone(mFrom_TimeZoneId);
//        TimeZone to = TimeZone.getTimeZone(mTo_TimezoneId);
//        int from_offset = from.getOffset(Calendar.ZONE_OFFSET);
//        int to_offset = to.getOffset(Calendar.ZONE_OFFSET);
//
//        int diff = to_offset - from_offset;
//
//        long time = calendar.getTimeInMillis() + diff;
//        Date newDate = new Date(time);
//
//        String pattern = "MM/dd/yyyy HH:mm";
//        SimpleDateFormat df = new SimpleDateFormat(pattern, Locale.ENGLISH);
//        String res = df.format(newDate);
//        return res;
    }

    public ZonedDateTime ConvertedTime(String mFrom_TimeZoneId) {
        DateTime tmp = this.getDateTime();
        Instant instant = Instant.now();
        ZonedDateTime source = instant.atZone(ZoneId.of(mFrom_TimeZoneId)).
                withYear(Integer.parseInt(tmp.getYear())).
                withMonth(Integer.parseInt(tmp.getMonth()) + 1).
                withDayOfMonth(Integer.parseInt(tmp.getDay())).
                withHour(Integer.parseInt(tmp.getHour())).
                withMinute(Integer.parseInt(tmp.getMinute()))
                .withSecond(0);
        ZonedDateTime target = source.toInstant().atZone(ZoneId.systemDefault());
        return target;
    }

    @Override
    public String toString() {
        return "ScheduleObject{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }

    public Date GetDate_DateFormat(String mFrom_TimeZoneId, String mTo_TimezoneId) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(this.getDateTime().getYear()));
        calendar.set(Calendar.MONTH, Integer.parseInt(this.getDateTime().getMonth()));
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(this.getDateTime().getDay()));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(this.getDateTime().getHour()));
        calendar.set(Calendar.MINUTE, Integer.parseInt(this.getDateTime().getMinute()));
        calendar.set(Calendar.SECOND, 0);

        TimeZone from = TimeZone.getTimeZone(mFrom_TimeZoneId);
        TimeZone to = TimeZone.getTimeZone(mTo_TimezoneId);
        int from_offset = from.getOffset(Calendar.ZONE_OFFSET);
        int to_offset = to.getOffset(Calendar.ZONE_OFFSET);

        int diff = to_offset - from_offset;

        long time = calendar.getTimeInMillis() + diff;
        Date newDate = new Date(time);
        return newDate;
    }
}
