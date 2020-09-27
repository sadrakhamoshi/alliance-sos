package com.example.alliancesos;

import android.text.TextUtils;

import java.io.Serializable;
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

    public String GetDate(String fromTZ, String toTZ) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(this.getDateTime().getYear()));
        calendar.set(Calendar.MONTH, Integer.parseInt(this.getDateTime().getMonth()));
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(this.getDateTime().getDay()));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(this.getDateTime().getHour()));
        calendar.set(Calendar.MINUTE, Integer.parseInt(this.getDateTime().getMinute()));
        calendar.set(Calendar.SECOND, 0);
        String from = TimeZone.getTimeZone(fromTZ).getDisplayName(true, TimeZone.SHORT);
        from = from.replace("GMT", "");
        String[] h_m_seperated = from.split(":");

        Integer h_from = 0, m_from = 0;
        try {
            h_from = Integer.parseInt(h_m_seperated[0]);
        } catch (Exception e) {
        }
        try {
            m_from = Integer.parseInt(h_m_seperated[1]);
        } catch (Exception e) {
        }
        if (from.contains("-")) {
            m_from *= -1;
        }
        Date targetTime_in_GMT = new Date(calendar.getTimeInMillis() - (h_from * 60 * 60 * 1000 + m_from * 60 * 1000));


        String target = TimeZone.getTimeZone(toTZ).getDisplayName(true, TimeZone.SHORT);
        target = target.replace("GMT", "");
        String[] h_m_spereated2 = target.split(":");
        Integer h_target = 0, m_target = 0;
        try {
            h_target = Integer.parseInt(h_m_spereated2[0]);
        } catch (Exception e) {
        }
        try {
            m_target = Integer.parseInt(h_m_spereated2[1]);
        } catch (Exception e) {
        }
        if (target.contains("-")) {
            m_target *= -1;
        }
        Date newDate = new Date(targetTime_in_GMT.getTime() + (h_target * 60 * 60 * 1000 + m_target * 60 * 1000));
        calendar.setTime(newDate);
        String pattern = "MM/dd/yyyy HH:mm";
        SimpleDateFormat df = new SimpleDateFormat(pattern, Locale.ENGLISH);
        String res = df.format(calendar.getTime());
        return res;
    }
}
