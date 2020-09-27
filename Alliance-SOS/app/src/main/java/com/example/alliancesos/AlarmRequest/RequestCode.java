package com.example.alliancesos.AlarmRequest;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class RequestCode {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "reqCode")
    public String reqCode;


    public RequestCode() {
    }

    public RequestCode(String id, String reqCode) {
        this.id = id;
        this.reqCode = reqCode;
    }
}
