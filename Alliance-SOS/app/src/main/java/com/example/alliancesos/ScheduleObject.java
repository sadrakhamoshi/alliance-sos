package com.example.alliancesos;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public String GetDate(String mFrom_TimeZoneId, String mTo_TimezoneId) {
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

        String pattern = "MM/dd/yyyy HH:mm";
        SimpleDateFormat df = new SimpleDateFormat(pattern, Locale.ENGLISH);
        String res = df.format(newDate);
        return res;

    }
}
