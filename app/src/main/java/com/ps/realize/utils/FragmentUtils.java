package com.ps.realize.utils;


import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentUtils {
    private static final String TAG = FragmentUtils.class.getSimpleName();

    public static String APP_NAME = "Realise";
    public static boolean bARSupported = false;
    public static boolean bARInstalled = false;

    @Deprecated  // NOT needed, keeping for future use maybe
    public static void replaceFragmentV2(AppCompatActivity activity, int layoutId, Fragment oldFragment, Fragment fragment, String tag) {
        LinearLayout frmLayout = activity.findViewById(layoutId);
        frmLayout.setVisibility(View.VISIBLE);
        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(layoutId, fragment, tag);
        }
        if (oldFragment != null && oldFragment.isAdded()) {
            transaction.hide(oldFragment);
        }
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    public static void replaceFragment(AppCompatActivity activity, int layoutId, Fragment fragment, String tag) {
        LinearLayout frmLayout = activity.findViewById(layoutId);
        frmLayout.setVisibility(View.VISIBLE);
        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(layoutId, fragment, tag);
        transaction.addToBackStack(tag);  // persists the fragment
        transaction.commit();
    }

    public static void addFragment(AppCompatActivity activity, int layoutId, Fragment fragment, String tag) {
        LinearLayout frmLayout = activity.findViewById(layoutId);

        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        transaction.add(layoutId, fragment, tag);
//        transaction.addToBackStack(null);   // DO NOT UNCOMMENT
        transaction.commit();
    }

    public static void popFragmentsTillName(AppCompatActivity activity, String name) {
        FragmentManager manager = activity.getSupportFragmentManager();
        manager.popBackStack(name, FragmentManager.POP_BACK_STACK_INCLUSIVE);

//        int count = manager.getBackStackEntryCount();
//        int id = manager.getBackStackEntryAt(count).getId();
//        manager.popBackStack(id, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public static void popAllFragments(AppCompatActivity activity) {
        FragmentManager manager = activity.getSupportFragmentManager();
        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
