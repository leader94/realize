package com.ps.realize.core.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.ps.realize.R;

public class CustomCardView  extends LinearLayout {
    public CustomCardView(Context context) {
        super(context);
        initBackground();
    }

    public CustomCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initBackground();
    }

    public CustomCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBackground();
    }

    private void initBackground() {
        setBackground(ViewUtils.generateBackgroundWithShadow(this, R.color.white ,
              R.dimen.card_radius_corner,R.color.card_shadowColor,R.dimen.card_elevation, Gravity.BOTTOM));
    }
}
