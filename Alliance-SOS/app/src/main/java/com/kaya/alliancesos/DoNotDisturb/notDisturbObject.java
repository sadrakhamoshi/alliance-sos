package com.kaya.alliancesos.DoNotDisturb;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Entity
public class notDisturbObject {


    public notDisturbObject() {

    }

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "from")
    public String from;

    @ColumnInfo(name = "until")
    public String until;

    @ColumnInfo(name = "daily")
    public boolean daily;

    @ColumnInfo(name = "repeated")
    public boolean repeated;

    @ColumnInfo(name = "day")
    public String day;

    public notDisturbObject(String id, String day, String from, String until) {

        this.day = day;
        this.id = id;
        this.from = from;
        this.until = until;
        repeated = false;
        this.daily = false;
    }

    public static String[] splitTime(String input) {
        String[] result = input.split(":");
        return result;
    }

    public static Date DisplayDate(String date, String time) throws ParseException {
        String res = date + 'T' + time + 'Z';
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm'Z'");
        return dateFormat.parse(res);
    }

    public static String[] splitDate(String input) {
        String[] result = input.split("/");
        return result;
    }

    public static Integer getDayOfWeek(String[] dates) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(dates[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(dates[1]));
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dates[2]));
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static String DisplayDayOfWeek(String dateFormat) throws ParseException {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy/MM/dd");
        Date dt1 = format1.parse(dateFormat);
        DateFormat format2 = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        String finalDay = format2.format(dt1);
        return finalDay;
    }
}
