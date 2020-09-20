package com.example.alliancesos.DbForRingtone;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.alliancesos.DoNotDisturb.*;

@Database(entities = {ringtone.class, notDisturbObject.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ringtoneDao dao();

    public abstract notDisturbDao disturbDao();
}
