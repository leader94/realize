package com.ps.realize.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;

import com.ps.realize.core.interfaces.IKeyboardListener;

public class KeyboardUtils {
    // Threshold for minimal keyboard height.
    final int MIN_KEYBOARD_HEIGHT_PX = 150;
    private View decorView;
    private Activity _activity;
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

    public void initKeyBoardListener(Activity activity, IKeyboardListener listener) {
        _activity = activity;
        // Top-level window decor view.
        decorView = _activity.getWindow().getDecorView();

        onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            // Retrieve visible rectangle inside window.
            private final Rect windowVisibleDisplayFrame = new Rect();
            private int lastVisibleDecorViewHeight;

            @Override
            public void onGlobalLayout() {
                decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
                final int visibleDecorViewHeight = windowVisibleDisplayFrame.height();

                if (lastVisibleDecorViewHeight != 0) {
                    if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX) {
                        listener.onKeyboardShow();
                    } else if (lastVisibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX < visibleDecorViewHeight) {
                        listener.onKeyboardHide();
                    }
                }
                // Save current decor view height for the next call.
                lastVisibleDecorViewHeight = visibleDecorViewHeight;
            }
        };

        // Register global layout listener.
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public void removeKeyboardListener() {
        decorView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public  void hideKeyboard(View view){
        InputMethodManager inputMethodManager = (InputMethodManager) _activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static void backPress(Fragment fragment){
        fragment.getActivity().onBackPressed();
//        fragment.getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));

    }
}
