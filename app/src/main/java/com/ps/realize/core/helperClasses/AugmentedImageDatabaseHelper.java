package com.ps.realize.core.helperClasses;

import android.content.Context;
import android.util.Log;

import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class AugmentedImageDatabaseHelper {

    private static final String TAG = "AugmentedImageDatabaseHelper";
    static String aiDBfilename = "augmented_image_database.dat";

    public static void saveDatabase(Context context, AugmentedImageDatabase database) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Filename for the saved database
                try {


                    // Serialize the AugmentedImageDatabase object
                    File file = new File(context.getFilesDir(), aiDBfilename);
                    OutputStream outputStream = new FileOutputStream(file);
                    database.serialize(outputStream);

                    outputStream.close();
                    Log.d(TAG, "Augmented image database saved successfully");

                } catch (Exception e) {
                    Log.e(TAG, "Error saving augmented image database: ", e);
                    e.printStackTrace();

                }
            }
        }).start();

    }

    public static AugmentedImageDatabase loadDatabase(Context context, Session session) throws Exception {
        AugmentedImageDatabase imageDatabase;
        try {
            File file = new File(context.getFilesDir(), aiDBfilename);
            FileInputStream fis = new FileInputStream(file);
            imageDatabase = AugmentedImageDatabase.deserialize(session, fis);
            Log.d(TAG, "Augmented image database loaded successfully");
            return imageDatabase;
        } catch (IOException e) {
            // The Augmented Image database could not be deserialized; handle this error appropriately.
            throw new Exception("Failed to load AIDB from file");
        }
    }
}
