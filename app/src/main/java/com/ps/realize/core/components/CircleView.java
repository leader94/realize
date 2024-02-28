package com.ps.realize.core.components;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;


public class CircleView extends View {
    private GradientDrawable gradientDrawable;

    public CircleView(Context context) {
        super(context);
        init();
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        // Create a gradient drawable with round shape
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setColor(getResources().getColor(android.R.color.holo_green_dark)); // Set color to green

        // Set background drawable to the view
        setBackground(gradientDrawable);

        // Set layout parameters to make the view a circle
//        setLayoutParams(new ViewGroup.LayoutParams(ViewUtils.getSizeInDp(getContext(), 10), ViewUtils.getSizeInDp(getContext(), 10)));
    }

    public void setColor(int argb) {
        gradientDrawable.setColor(argb);
    }
}
