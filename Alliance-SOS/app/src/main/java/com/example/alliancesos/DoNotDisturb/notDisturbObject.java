package com.example.alliancesos.DoNotDisturb;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.HashMap;

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

    public static String[] splitDate(String input) {
        String[] result = input.split("/");
        return result;
    }

}
