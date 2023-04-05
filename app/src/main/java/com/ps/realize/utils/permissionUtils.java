package com.ps.realize.utils;

import android.content.Context;

import com.ps.realize.MainActivity;

public class permissionUtils {
    private static final int REQUEST = 112;
//    private Context mContext = MainActivity.getMainActivity();

    /*public void checkWriteExternalStoragePermission( ActivityCompat activityCompat){
        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (!  activityCompat.hasPermissions(mContext, PERMISSIONS)) {
                ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS, REQUEST );
            } else {
                //do here
            }
        } else {
            //do here
        }

    }
    */
    /*public  boolean isStoragePermissionGranted(ActivityCompat activityCompat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activityCompat.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
//                Log.v(TAG,"Permission is granted");
                return true;
            } else {

//                Log.v(TAG,"Permission is revoked");
//                ActivityCompat.requestPermissions(activityCompat.getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
//            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

}*/
}
