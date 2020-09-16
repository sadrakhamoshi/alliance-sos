package com.example.alliancesos.DoNotDisturb;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.HashMap;

@Entity
public class notDisturbObject {

    public notDisturbObject(String id) {
        repeat = false;
        this.id = id;
    }

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

    @ColumnInfo(name = "repeat")
    public boolean repeat;

    @ColumnInfo(name = "day")
    public String day;

    public notDisturbObject(String id, String day, String from, String until) {

        this.day = day;
        this.id = id;
        this.from = from;
        this.until = until;
        this.repeat = false;
    }

    public HashMap<String, String> splitTime(String input) {
        String[] result = input.split(":");
        HashMap<String, String> res = new HashMap<>();
        res.put("hour", result[0]);
        res.put("minute", result[1]);
        return res;
    }
}
