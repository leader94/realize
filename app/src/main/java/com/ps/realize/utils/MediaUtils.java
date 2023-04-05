package com.ps.realize.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MediaUtils {

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


}
