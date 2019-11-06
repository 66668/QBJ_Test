package com.thinkernote.ThinkerNote.utils.actfun;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;


import com.thinkernote.ThinkerNote.BuildConfig;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.bean.localdata.TNUser;
import com.thinkernote.ThinkerNote.utils.MLog;

public class TNUtilsView {
    private static final String TAG = "TNUtilsView";

    public static String saveViewToImage(View view) {
        view.setDrawingCacheEnabled(true);
        Bitmap bmp = view.getDrawingCache();
        if (bmp == null) {
            MLog.e(TAG, "tuya bitmap is null");
            return null;
        }
        String path = TNUtilsAtt.SaveBitmapToImage(bmp);
        MLog.d(TAG, "view to image: " + path);
        view.destroyDrawingCache();
        return path;
    }

    public static String saveViewToImage(View view, String path, int width, int height) {
        MLog.d(TAG, "layout to image: " + path);
        view.setDrawingCacheEnabled(true);
        view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        view.layout(0, 0, view.getMeasuredWidth(),
                view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();

        if (bmp == null) {
            return null;
        }

        File file = new File(path);
        FileOutputStream out;
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            out = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);

        view.destroyDrawingCache();
        return path;
    }

    public static void saveAccountInfoToImage(Activity act, TNUser user) {
        LayoutInflater layoutInflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FrameLayout fl = (FrameLayout) layoutInflater.inflate(
                R.layout.account_info, null);

        ((TextView) fl.findViewById(R.id.account_name)).setText(
                String.format(act.getString(R.string.account_info_name), user.username));
//		((TextView)fl.findViewById(R.id.account_password)).setText(
//				String.format(act.getString(R.string.account_info_password), user.preCode));
        String name = "/qingbiji/" + user.username + ".jpg";
        View accountLayout = fl.findViewById(R.id.account_layout);
        String path = saveViewToImage(accountLayout, Environment.getExternalStorageDirectory().getPath() + name
                , 320, 480);
        if (path == null) {
            return;
        }
        //添加到相册
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//7.0+版本安全设置
            contentUri = FileProvider.getUriForFile(act, BuildConfig.APPLICATION_ID + ".FileProvider", new File(path));
        } else {//7.0-正常调用
            contentUri = Uri.fromFile(new File(path));
        }
        intent.setData(contentUri);
        act.sendBroadcast(intent);

        if (path != null)
            TNUtilsUi.showToast(String.format(act.getString(R.string.account_info_saved), path));
    }
}
