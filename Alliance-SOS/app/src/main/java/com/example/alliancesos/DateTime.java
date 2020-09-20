package com.example.alliancesos;

import java.io.Serializable;

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
}
