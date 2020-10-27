package com.example.alliancesos;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTime implements Serializable {
    private String year, month, day, hour, minute;

    public DateTime(String y, String mo, String d, String h, String mi) {
        year = y;
        month = mo;
        day = d;
        hour = h;
        minute = mi;
    }

    public DateTime() {
    }

    public String getDay() {
        return day;
    }

    public String getHour() {
        return hour;
    }

    public String getMinute() {
        return minute;
    }

    public String getMonth() {
        return month;
    }

    public String getYear() {
        return year;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String DisplayTime() {
        String result = this.getYear() + "-" + this.getMonth() + "-" + this.getDay() + " " + this.getHour() + ":" + this.getMinute();
        return result;
    }
    public String ConvertTime(String mFrom_TimeZoneId, String mTo_TimezoneId) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(this.getYear()));
        calendar.set(Calendar.MONTH, Integer.parseInt(this.getMonth()));
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(this.getDay()));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(this.getHour()));
        calendar.set(Calendar.MINUTE, Integer.parseInt(this.getMinute()));
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
