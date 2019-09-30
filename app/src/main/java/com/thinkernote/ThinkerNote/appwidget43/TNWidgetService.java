package com.thinkernote.ThinkerNote.appwidget43;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViewsService;

import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.Utils.SPUtil;

/**
 * 小部件维护的service
 */
public class TNWidgetService extends RemoteViewsService implements Runnable {
    private static final String TAG = "AppWidget";


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TNListRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //执行线程
        new Thread(this).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    //死掉重启
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * 保持service不死
     */
    @Override
    public void run() {
        // 实时发送一个更新的广播
        final String pref_key = "appwidget_news_refresh_time";
        final long updatePeriod = 10 * 60 * 1000;//10min更新
        final long lastRefreshTime = SPUtil.getLong(pref_key, 0);
        final long now = System.currentTimeMillis();
        if (now - lastRefreshTime >= updatePeriod) {
            MLog.d(TAG, "TNWidgetService定时线程--发送APP_AUTO广播");
            // 10分钟内不执行重复的后台更新请求
            SPUtil.putLong(pref_key, now);
            //发送广播
            Intent refreshNowIntent = new Intent(this, TNAppWidget43.class);
            refreshNowIntent.setAction(TNAppWidget43.APP_AUTO);
            sendBroadcast(refreshNowIntent);
        }
        //
        Intent autoRefreshIntent = new Intent(this, TNAppWidget43.class);
        autoRefreshIntent.setAction(TNAppWidget43.APP_AUTO);
        PendingIntent pending = PendingIntent.getBroadcast(TNWidgetService.this, 0, autoRefreshIntent, 0);

        // 60秒更新一次
        long updateTimes = System.currentTimeMillis() + 1 * 60 * 1000;
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, updateTimes, pending);
        stopSelf();
    }

}
