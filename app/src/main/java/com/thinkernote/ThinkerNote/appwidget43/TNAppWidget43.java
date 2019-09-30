package com.thinkernote.ThinkerNote.appwidget43;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.thinkernote.ThinkerNote.Activity.TNNoteEditAct;
import com.thinkernote.ThinkerNote.Activity.TNSplashAct;

/**
 * APP小部件 4*3布局广播
 */
public class TNAppWidget43 extends AppWidgetProvider {
    private static final String TAG = "AppWidget";

    public final static String APP_START = "com.thinkernote.ThinkerNote.appwidget.action.START";//
    public final static String APP_AUTO = "com.thinkernote.ThinkerNote.appwidget.action.AUTO";//原本用于list自动刷新
    public final static String APP_ADD = "com.thinkernote.ThinkerNote.appwidget.action.ADD";
    public final static String APP_REFRESH = "com.thinkernote.ThinkerNote.appwidget.action.REFRESH";//手动刷新
    public final static String APP_ITEM_DETAIL = "com.thinkernote.ThinkerNote.appwidget.action.LISTITEM";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "TNAppWidget43--onReceive");
        //各种跳转的设置
        switch (intent.getAction()) {
            case APP_START:
                //判断登陆在跳转
                Intent startIntent = new Intent(context, TNSplashAct.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(startIntent);
                break;
            case APP_ADD:
                //判断登陆在跳转
                Intent addIntent = new Intent(context, TNNoteEditAct.class);
                addIntent.setAction(TNAppWidget43.APP_ADD);
                addIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(addIntent);
                break;
            case APP_REFRESH:
                TNRemoteViews remoteViews = new TNRemoteViews(context);
                remoteViews.syncStart();
                remoteViews.notifyAppWidgetViewDataChanged();
                break;
            case APP_AUTO:
                TNRemoteViews autoViews = new TNRemoteViews(context);
                autoViews.notifyAppWidgetViewDataChanged();
                break;
            case APP_ITEM_DETAIL:
                Bundle extras = intent.getExtras();
                if (extras == null) {
                    return;
                }
                Intent newIntent = new Intent();
                //自定义跳转参数，使用的地方获取参数，就可以跳转到指定act
                String uri = TNAppWidegtConst.SCHEME_HOST + "?className=com.thinkernote.ThinkerNote.Activity.TNNoteViewAct";
                Uri data = Uri.parse(uri);
                newIntent.setData(data);
                newIntent.putExtras(extras);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(newIntent);
                break;
            default:
                break;
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "TNAppWidget43--onUpdate");
        //封装自定义更新
        TNRemoteViews mRemoteViews = new TNRemoteViews(context);//初始化
        mRemoteViews.setOnLogoClickPendingIntent();
        mRemoteViews.setOnAddClickPendingIntent();
        mRemoteViews.setOnRefreshClickPendingIntent();
        mRemoteViews.bindListViewAdapter();

        // 更新所有的widget
        appWidgetManager.updateAppWidget(appWidgetIds, mRemoteViews);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

}

