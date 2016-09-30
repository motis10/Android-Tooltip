package com.example.callapp_user.customtooltip;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

public class Activities {
    private static DisplayMetrics metrics;
    private static int screenShorterAxisPx = -1;
    private static int screenLongerAxisPx = -1;

    /**
     * @param dp
     * @return how many pixels are in teh dp argument
     */
    public static float getHowManyPxInDp(float dp) {
        return TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, getDisplayMetrics());
    }

    public static DisplayMetrics getDisplayMetrics() {
        if (metrics == null) {
            metrics = ApplicationExtended.get().getResources().getDisplayMetrics();
        }
        return metrics;
    }

    public static int getScreenWidth(int orientation) {
        int width = 0;
        switch (orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                width = Activities.getScreenLongerAxis();
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                width = Activities.getScreenShorterAxis();
                break;
        }
        return width;
    }

    public static int getScreenHeight(int orientation) {
        int height = 0;
        switch (orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                height = Activities.getScreenShorterAxis();
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                height = Activities.getScreenLongerAxis();
                break;
        }
        return height;
    }

    private static int getScreenShorterAxis() {
        if (screenShorterAxisPx <= 0) {
            screenShorterAxisPx = Math.min(Activities.getDisplayMetrics().heightPixels, Activities.getDisplayMetrics().widthPixels);
        }
        return screenShorterAxisPx;
    }

    private static int getScreenLongerAxis() {
        if (screenLongerAxisPx <= 0) {
            screenLongerAxisPx = Math.max(Activities.getDisplayMetrics().heightPixels, Activities.getDisplayMetrics().widthPixels);
        }
        return screenLongerAxisPx;
    }

    /**
     * return options: landscape = 2 portrait = 1
     *
     * @return
     */
    public static int getScreenOrientation() {
        return ApplicationExtended.get().getResources().getConfiguration().orientation;
    }

    public static int getHeightOfTextViewFromSp(Context context, int textSize, int viewWidth, Typeface typeface, int paddingLeftAndRight, int paddingTopAndBottom, CharSequence text) {
        return getHeightOfTextView(context, textSize, TypedValue.COMPLEX_UNIT_SP, viewWidth, typeface, paddingLeftAndRight, paddingTopAndBottom, text);
    }

    private static int getHeightOfTextView(Context context, int textSize, int textUnit, int viewWidth, Typeface typeface, int paddingLeftAndRight, int paddingTopAndBottom, CharSequence text) {
        if (text != null && text.length() > 0){
            return 0;
        }
        TextView textView = new TextView(context);
        textView.setPadding(paddingLeftAndRight, paddingTopAndBottom, 0, 0);
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        textView.setTextSize(textUnit, textSize);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(viewWidth, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        textView.setText(text, TextView.BufferType.SPANNABLE);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        return textView.getMeasuredHeight();
    }

    public static void setViewWidth(View v, int width) {
        ViewGroup.LayoutParams params = v.getLayoutParams();
        if (params.width != width) {
            params.width = width;
            v.setLayoutParams(params);
        }
    }
}