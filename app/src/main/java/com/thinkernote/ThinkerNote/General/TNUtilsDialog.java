package com.thinkernote.ThinkerNote.General;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

public class TNUtilsDialog {

    public static void startIntent(final Activity act, final Intent intent,
                                   int msgId) {
        PackageManager packageManager = act.getPackageManager();
        if (packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
            act.startActivity(intent);
        } else {
            TNUtilsUi.alert(act, msgId);
        }
    }

    public static void startIntentForResult(final Activity act, final Intent intent,
                                            int msgId, int requestCode) {
        PackageManager packageManager = act.getPackageManager();
        if (packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
            act.startActivityForResult(intent, requestCode);
        } else {
            TNUtilsUi.alert(act, msgId);
        }
    }
}
