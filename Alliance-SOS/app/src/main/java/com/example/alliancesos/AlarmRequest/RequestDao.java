package com.example.alliancesos.AlarmRequest;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;

import com.example.alliancesos.DoNotDisturb.notDisturbObject;

@Dao
public interface RequestDao {
    @Insert
    void insert(RequestCode requestCode);

    @Delete
    void deleteRule(RequestCode requestCode);
}
