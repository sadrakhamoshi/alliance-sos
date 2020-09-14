package com.example.alliancesos.DbForRingtone;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = ringtone.class, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ringtoneDao dao();
}
