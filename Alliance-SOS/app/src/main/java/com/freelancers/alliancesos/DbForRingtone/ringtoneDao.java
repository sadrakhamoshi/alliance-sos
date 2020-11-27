package com.freelancers.alliancesos.DbForRingtone;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ringtoneDao {
    @Insert
    void insert(ringtone ring);

    @Query("UPDATE ringtone SET path=:newPath WHERE id = :id")
    void updateRingtonePath(String id, String newPath);

    @Query("SELECT * FROM ringtone WHERE id LIKE :id")
    ringtone currentPath(String id);
}
