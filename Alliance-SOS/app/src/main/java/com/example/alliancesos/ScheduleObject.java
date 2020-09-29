package com.example.alliancesos;

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

    public String GetDate(String mFrom_TimeZoneId, String mTo_TimezoneId) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(this.getDateTime().getYear()));
        calendar.set(Calendar.MONTH, Integer.parseInt(this.getDateTime().getMonth()));
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(this.getDateTime().getDay()));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(this.getDateTime().getHour()));
        calendar.set(Calendar.MINUTE, Integer.parseInt(this.getDateTime().getMinute()));
        calendar.set(Calendar.SECOND, 0);

        int multi_from = 1;
        int multi_to = 1;
        String from = TimeZone.getTimeZone(mFrom_TimeZoneId).getDisplayName(true, TimeZone.SHORT);
        if (from.contains("-"))
            multi_from = -1;
        String to = TimeZone.getTimeZone(mTo_TimezoneId).getDisplayName(true, TimeZone.SHORT);
        if (to.contains("-1"))
            multi_to = -1;
        from = from.replace("GMT", "").replace("+", "").replace("-", "");
        to = to.replace("GMT", "").replace("+", "").replace("-", "");

        SimpleDateFormat format = new SimpleDateFormat("hh:mm");
        Date date1 = format.parse(from);
        Date date2 = format.parse(to);

        long diff = date2.getTime() * multi_to - date1.getTime() * multi_from;

        int hours = (int) (diff / (1000 * 60 * 60));
        int minutes = (int) ((diff / (1000 * 60)) % 60);
        Date newDate = new Date(calendar.getTimeInMillis() + 60 * 60 * 1000 * hours + 60 * 1000 * minutes);

        String pattern = "MM/dd/yyyy HH:mm";
        SimpleDateFormat df = new SimpleDateFormat(pattern, Locale.ENGLISH);
        String res = df.format(newDate);
        return res;
    }
}
