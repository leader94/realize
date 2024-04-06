package com.ps.realize.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.FileProvider;

import com.ps.realize.core.interfaces.CallbackListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MediaUtils {

    private static final String TAG = MediaUtils.class.getSimpleName();

    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }

    public static long getFileSize(Context context, Uri uri) {
        try {
            AssetFileDescriptor fileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            long fileSize = fileDescriptor.getLength();
            return fileSize;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void pickImageFromGalleryIntent(ActivityResultLauncher<Intent> activityResultLauncher) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        activityResultLauncher.launch(chooserIntent);
    }

    public static void pickVideoFromGalleryIntent(ActivityResultLauncher<Intent> activityResultLauncher) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("video/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("video/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Video");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        activityResultLauncher.launch(chooserIntent);
    }

    public static void openCameraForImageIntent(ActivityResultLauncher<Intent> activityResultLauncher, Context context) {
        Intent m_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg");
        Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        m_intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
//        startActivityForResult(m_intent, REQUEST_CAMERA_IMAGE);

        activityResultLauncher.launch(m_intent);

//        Intent m_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        Uri imageUri = getImageUri();
//        m_intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//        activityResultLauncher.launch(m_intent);
    }

    private static Uri getImageUri() {
        Uri m_imgUri = null;
        File m_file;
        try {
            SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String m_curentDateandTime = m_sdf.format(new Date());
            String m_imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + m_curentDateandTime + ".jpg";
            m_file = new File(m_imagePath);
            m_imgUri = Uri.fromFile(m_file);
        } catch (Exception p_e) {
        }
        return m_imgUri;
    }

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static Bitmap getBitmap(Activity activity, Context context, Uri imageUri) {
        PermissionUtils.checkReadExtPermissions(activity);
        Bitmap bitmap = null;
        try {
            if (Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            } else {
                ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), imageUri);
                bitmap = ImageDecoder.decodeBitmap(source);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static void copyImage(Context context, Uri sourceUri, Uri destinationUri, CallbackListener callbackListener) {
        AsyncTask.execute(() -> {
            try {
                ContentResolver resolver = context.getContentResolver();

                // Open an input stream from the source URI
                InputStream inputStream = resolver.openInputStream(sourceUri);
                if (inputStream == null) {
                    return;
                }

                // Open an output stream to the destination URI
                OutputStream outputStream = resolver.openOutputStream(destinationUri);
                if (outputStream == null) {
                    inputStream.close();
                    return;
                }

                // Copy the data from the input stream to the output stream
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                }

                // Close streams
                bufferedInputStream.close();
                bufferedOutputStream.close();

                // Optionally, you may want to update the MediaStore to reflect the new image
                updateMediaStore(context, destinationUri);

            } catch (IOException e) {
                Log.e(TAG, "Error copying image", e);
                callbackListener.onFailure();
            }
            callbackListener.onSuccess();
        });
    }


    private static void updateMediaStore(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.IS_PENDING, 0);
        resolver.update(uri, values, null, null);
    }

}
