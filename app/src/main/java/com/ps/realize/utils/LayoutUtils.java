package com.ps.realize.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.ps.realize.MyApp;
import com.ps.realize.core.interfaces.IKeyboardListener;

public class LayoutUtils {
    public static int dpToPx(int dp) {
        float density = MyApp.getContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
