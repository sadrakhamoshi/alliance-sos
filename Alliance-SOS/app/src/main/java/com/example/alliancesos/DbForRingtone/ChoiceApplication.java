package com.example.alliancesos.DbForRingtone;


import android.content.Context;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class ChoiceApplication {

    public AppDatabase appDatabase;

    public ChoiceApplication(Context context) {
        appDatabase = Room.databaseBuilder(context, AppDatabase.class, "choice_db")
                .addMigrations(createMigration()).build();
    }

    private static Migration createMigration() {
        return new Migration(2, 3) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `notDisturb` (`id` STRING, "
                        + "`from` STRING, " + "`until` STRING, " + "`daily` BOOLEAN, " + "`repeated` BOOLEAN, " + "`day` STRING , PRIMARY KEY(`id`))");
            }
        };
    }
}

