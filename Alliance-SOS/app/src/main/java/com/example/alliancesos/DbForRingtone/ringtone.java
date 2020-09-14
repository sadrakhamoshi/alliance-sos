package com.example.alliancesos.DbForRingtone;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ringtone {
    public ringtone() {
    }

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "path")
    public String path;

    public ringtone(String id, String path) {
        this.id = id;
        this.path = path;
    }
}
