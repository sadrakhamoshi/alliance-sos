package com.freelancers.alliancesos.AlarmRequest;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface RequestDao {
    @Insert
    void insert(RequestCode requestCode);

    @Delete
    void deleteRule(RequestCode requestCode);

    @Query("SELECT * FROM requestCode WHERE id LIKE :id")
    RequestCode getById(String id);
}
