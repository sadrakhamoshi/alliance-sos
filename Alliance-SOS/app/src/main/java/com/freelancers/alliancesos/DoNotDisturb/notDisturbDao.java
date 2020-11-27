package com.freelancers.alliancesos.DoNotDisturb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface notDisturbDao {
    @Insert
    void insert(notDisturbObject rule);

    @Query("SELECT * FROM notDisturbObject")
    List<notDisturbObject> getAllRules();

    @Query("DELETE FROM notDisturbObject")
    void deleteAll();

    @Delete
    void deleteRule(notDisturbObject target);
}
