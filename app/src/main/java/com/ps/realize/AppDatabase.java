package com.ps.realize;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ps.realize.core.daos.user.UserDao;
import com.ps.realize.core.datamodels.User;

@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase _instance = null;

    public static AppDatabase getInstance(Context context) {
        if (_instance == null) {
            _instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "tag_it_db").build();
        }

        return _instance;
    }

    public abstract UserDao userDao();
}