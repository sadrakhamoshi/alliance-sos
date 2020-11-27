package com.freelancers.alliancesos.DbForRingtone;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.freelancers.alliancesos.AlarmRequest.RequestCode;
import com.freelancers.alliancesos.AlarmRequest.RequestDao;
import com.freelancers.alliancesos.DoNotDisturb.*;

@Database(entities = {ringtone.class, notDisturbObject.class, RequestCode.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ringtoneDao dao();

    public abstract notDisturbDao disturbDao();

    public abstract RequestDao requestDao();
}
