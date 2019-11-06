package com.thinkernote.ThinkerNote.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * app所有文件路径统一管理
 */

public class FilePathUtils {

    public static int px2dp(Context context, int px) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int) (px * dm.density + 0.5f);
    }

    public static int dp2px(Context context, int dp) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int) (dp / dm.density + 0.5f);
    }
}
