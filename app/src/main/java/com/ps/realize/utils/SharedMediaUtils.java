package com.ps.realize.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SharedMediaUtils {
    public static Uri createImageFile(Context context) {
        // Add a specific media item.
        ContentResolver resolver = context.getContentResolver();

        // Find all image files on the primary external storage device.
        Uri imageCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            imageCollection = MediaStore.Images.Media
                    .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        // Publish a new image.
        ContentValues newImageDetails = new ContentValues();
        newImageDetails.put(MediaStore.Images.Media.DISPLAY_NAME,
                "IMG_" + System.currentTimeMillis() + ".jpg");
        String folderPath = Environment.DIRECTORY_PICTURES + File.separator + CommonService.APP_NAME;
        newImageDetails.put(MediaStore.MediaColumns.RELATIVE_PATH, folderPath);

        // Keep a handle to the new image's URI in case you need to modify it
        // later.
        Uri newImageUri = resolver
                .insert(imageCollection, newImageDetails);
        return newImageUri;
    }

    public static Uri createVideoFile(Context context) {
        // Add a specific media item.
        ContentResolver resolver = context.getContentResolver();

        // Find all image files on the primary external storage device.
        Uri videoCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            videoCollection = MediaStore.Video.Media
                    .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            videoCollection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        // Publish a new image.
        ContentValues newVideoDetails = new ContentValues();
        newVideoDetails.put(MediaStore.Video.Media.DISPLAY_NAME,
                "VID_" + System.currentTimeMillis() + ".mp4");
        String folderPath = Environment.DIRECTORY_MOVIES + File.separator + CommonService.APP_NAME;
        newVideoDetails.put(MediaStore.MediaColumns.RELATIVE_PATH, folderPath);

        // Keep a handle to the new image's URI in case you need to modify it
        // later.
        Uri newVideoUri = resolver
                .insert(videoCollection, newVideoDetails);
        return newVideoUri;
    }

    public static Uri writeImageFile(Context context, Bitmap bitmap) {
        // Open a specific media item using InputStream.
        ContentResolver resolver = context.getContentResolver();
        Uri uri = createImageFile(context);

        // "rw" for read-and-write.
        // "rwt" for truncating or overwriting existing file contents.
        String readWriteOverwriteMode = "rwt";
        try (ParcelFileDescriptor pfd =
                     resolver.openFileDescriptor(uri, readWriteOverwriteMode)) {

            OutputStream fOut = new FileOutputStream(pfd.getFileDescriptor());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return uri;
    }

    public static Uri writeVideoFile(Context context, InputStream inputStream) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = createVideoFile(context);

        String readWriteOverwriteMode = "rwt";
        try (ParcelFileDescriptor pfd =
                     resolver.openFileDescriptor(uri, readWriteOverwriteMode)) {
            OutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor());
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            outputStream.flush();
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return uri;
    }
}
