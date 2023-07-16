package com.ps.realize.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import net.gotev.uploadservice.data.UploadNotificationConfig;
import net.gotev.uploadservice.data.UploadNotificationStatusConfig;
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest;

import org.json.JSONObject;

import java.util.Iterator;

import okhttp3.MediaType;

public class UploadServiceUtils {
    private static final String notificationChannelID = "TestChannel";
    private static final String notificationTitle = "Uploading...";

    public static void multiPartUpload(Context context, String url, JSONObject formFields, String filePath, String fileName, String mimeType) {
        MultipartUploadRequest request = new MultipartUploadRequest(context.getApplicationContext(), url);

        Iterator<String> keys = formFields.keys();
        try {
            while (keys.hasNext()) {
                String key = keys.next();
                if (formFields.get(key) instanceof String) {
                    String value = formFields.getString(key);
                    request.addParameter(key, value);
                }
            }
            request.addFileToUpload(filePath, "file", fileName, String.valueOf(MediaType.parse(mimeType)));


//            request.setNotificationConfig((context1, s) -> {
//                return createNotificationChannel(context);
//            });

            request.startUpload();
        } catch (Exception e) {
            e.printStackTrace();
        }


//       request.addFileToUpload(filePath, fileName);
    }


    private static UploadNotificationConfig createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    notificationChannelID,
                    "TestApp Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
//            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationManager manager = context.getApplicationContext().getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
        UploadNotificationStatusConfig uploadNotificationStatusConfig = new UploadNotificationStatusConfig(notificationTitle,
                "uploading...");
        UploadNotificationStatusConfig uploadNotificationStatusConfigSucces = new UploadNotificationStatusConfig(notificationTitle,
                "finished!");
        UploadNotificationStatusConfig uploadNotificationStatusConfigError = new UploadNotificationStatusConfig(notificationTitle,
                "Error!");
        UploadNotificationStatusConfig uploadNotificationStatusConfigCancele = new UploadNotificationStatusConfig(notificationTitle,
                "Canceled");

        UploadNotificationConfig uploadNotificationConfig = new UploadNotificationConfig(notificationChannelID,
                false,
                uploadNotificationStatusConfig, uploadNotificationStatusConfigSucces,
                uploadNotificationStatusConfigError,
                uploadNotificationStatusConfigCancele
        );


        return uploadNotificationConfig;
    }
}
