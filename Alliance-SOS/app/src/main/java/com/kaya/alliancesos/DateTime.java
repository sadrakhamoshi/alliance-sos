package com.kaya.alliancesos;

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

    public void decrease_month() {
        String curr_month = this.getMonth();
        int integer_month = Integer.parseInt(curr_month);
        integer_month--;
        this.setMonth(integer_month + "");
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

}
