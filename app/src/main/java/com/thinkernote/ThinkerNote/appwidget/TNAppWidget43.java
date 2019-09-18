package com.thinkernote.ThinkerNote.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.thinkernote.ThinkerNote.Activity.TNNoteEditAct;
import com.thinkernote.ThinkerNote.Activity.TNSplashAct;
import com.thinkernote.ThinkerNote.R;

/**
 * APP小部件 4*3布局广播
 */
public class TNAppWidget43 extends AppWidgetProvider {

    private final static String MYAPP_START = "com.thinkernote.ThinkerNote.appwidget.action.START";
    private final static String MYAPP_ADD = "com.thinkernote.ThinkerNote.appwidget.action.ADD";
    private final static String MYAPP_PLAY = "android.appwidget.action.MYAPP_PLAY";

    /**
     * 创建布局
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.app_name);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main43);
        //标题
        views.setTextViewText(R.id.tv_title, widgetText);
        //跳转事件
        //（1）打开app
        Intent startIntent = new Intent(context, TNSplashAct.class);//目标act
        startIntent.setAction(MYAPP_START);
        PendingIntent startPendingIntent = PendingIntent.getActivity(context, 0, startIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.logo, startPendingIntent);

        //（2）新笔记
        Intent newIntent = new Intent(context, TNNoteEditAct.class);//目标act
        newIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startIntent.setAction(MYAPP_ADD);
        PendingIntent titlePendingIntent = PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.tv_title, titlePendingIntent);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

