package com.example.alliancesos.DbForRingtone;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.alliancesos.AlarmRequest.RequestCode;
import com.example.alliancesos.AlarmRequest.RequestDao;
import com.example.alliancesos.DoNotDisturb.*;

@Database(entities = {ringtone.class, notDisturbObject.class, RequestCode.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ringtoneDao dao();

    public abstract notDisturbDao disturbDao();

    public abstract RequestDao requestDao();
}
