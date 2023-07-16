package com.ps.realize;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import net.gotev.uploadservice.UploadServiceConfig;
import net.gotev.uploadservice.logger.UploadServiceLogger;
import net.gotev.uploadservice.okhttp.OkHttpStack;

public class MyApp extends Application {
    private static MyApp instance;

    private final String notificationChannelID = "TestChannel";

    public static MyApp getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        createNotificationChannel();

        UploadServiceConfig.initialize(
                this,
                notificationChannelID,
                false
        );
        UploadServiceConfig.setHttpStack(new OkHttpStack());

        UploadServiceLogger.setDevelopmentMode(true);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    notificationChannelID,
                    "TestApp Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
//            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationManager manager = getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }

}