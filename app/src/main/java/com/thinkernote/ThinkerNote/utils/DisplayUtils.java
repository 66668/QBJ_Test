package com.thinkernote.ThinkerNote.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by yuguo on 2014/12/26.
 */

public class DisplayUtils {
    public static final double OneCmToInch = 0.39;
    public static final double OneInchToCm = 2.54;

    public static double cm2inch(double d) {
        return d * OneCmToInch;
    }

    public static double inch2cm(double d) {
        return d * OneInchToCm;
    }

    public static int cm2dp(Context context, double d) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int) (d * OneCmToInch * 160 * dm.density);
    }

    public static int px2dp(Context context, int px) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int) (px * dm.density + 0.5f);
    }

    public static int dp2px(Context context, int dp) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int) (dp / dm.density + 0.5f);
    }
}
