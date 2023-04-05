package com.ps.realize.utils;


import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class CommonService {
    private static final String TAG = CommonService.class.getSimpleName();

    public static boolean bARSupported = false;
    public static boolean bARInstalled = false;

    public static void replaceFragment(AppCompatActivity activity, int layoutId, Fragment fragment, String tag) {
        LinearLayout frmLayout = (LinearLayout) activity.findViewById(layoutId);
        frmLayout.setVisibility(View.VISIBLE);
        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        transaction.replace(layoutId, fragment, tag);
        transaction.addToBackStack(null);  // persists the fragment
        transaction.commit();
    }

    public static void addFragment(AppCompatActivity activity, int layoutId, Fragment fragment, String tag) {
        LinearLayout frmLayout = (LinearLayout) activity.findViewById(layoutId);

        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        transaction.add(layoutId, fragment, tag);
//        transaction.addToBackStack(null);   // DO NOT UNCOMMENT
        transaction.commit();
    }

}
