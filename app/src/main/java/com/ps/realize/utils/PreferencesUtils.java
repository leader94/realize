package com.ps.realize.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.ps.realize.R;

public class PreferencesUtils {
    public static void setUserId(Activity activity, String userId) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(activity.getString(R.string.user_id), userId);
        editor.apply();   // async writing to db
    }

    public static String getUserId(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        String userId = sharedPref.getString(activity.getString(R.string.user_id), null);
        return userId;
    }
}
