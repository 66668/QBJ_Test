package com.thinkernote.ThinkerNote.base;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDb2;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Service.LocationService;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.http.HttpUtils;
import com.thinkernote.ThinkerNote.http.third.WeichatHttpUtils;

/**
 * sjy 0607
 */
public class TNApplication extends Application {
    private static final String TAG = "TNApplication";
    private static TNApplication application;

    public static TNApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        initialize();

        //新网络框架 log初始化
        MLog.init(true, "SJY");//release版 false

        //新网络框架 初始化
        HttpUtils.getInstance().init(this, MLog.DEBUG);

        //微信初始化网络
        WeichatHttpUtils.getInstance().init(this, MLog.DEBUG);

        //leakcanary初始化（打包时清除）
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return;
//        }
//        LeakCanary.install(this);
    }

    // private methods
    //-------------------------------------------------------------------------------
    private void initialize() {
        TNSettings settings = TNSettings.getInstance();
        settings.appContext = this;
        settings.readPref();

        // db initialize
        TNDb.getInstance();
        TNDb2.getInstance();

        //地图定位
//        TNLBSService.getInstance();
        //地图定位 新版
        LocationService.getInstance();
        watchAppSwitch();
        // 设置此接口后，音频文件和识别结果文件保存在/sdcard/msc/record/目录下
        //com.iflytek.resource.MscSetting.setLogSaved(true);

        //讯飞语音初始化
        StringBuffer param = new StringBuffer();
        param.append("appid="+getString(R.string.app_id));
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE+"="+SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(this, param.toString());

    }

    public void DbReportError(TNAction aAction) {
        MLog.i(TAG, "DbReportError s" + TNSettings.getInstance().topAct);
        //TNUtilsUi.showToast("DB ERROR!!");
        if (TNSettings.getInstance().topAct != null) {
            TNUtilsUi.showNotification(TNSettings.getInstance().topAct,
                    R.string.alert_DBError, true);
        }
        TNSettings.getInstance().hasDbError = true;
        TNSettings.getInstance().savePref(false);

        aAction.finished();
        MLog.i(TAG, "DbReportError e");
    }

    // 检测db错误 /TNDb.java使用
    public void DbReportError(String error) {
        MLog.i("DbReportError s", TNSettings.getInstance().topAct);
        //TNUtilsUi.showToast("DB ERROR!!");
        if (TNSettings.getInstance().topAct != null) {
            TNUtilsUi.showNotification(TNSettings.getInstance().topAct,
                    R.string.alert_DBError, true);
        }
        TNSettings.getInstance().hasDbError = true;
        TNSettings.getInstance().savePref(false);

        MLog.i("DbReportError e");
    }
    // mainAct 2-11-2数据处理时的异常,
    public void htmlError(String error) {
        MLog.i("DbReportError s", TNSettings.getInstance().topAct);
        //TNUtilsUi.showToast("DB ERROR!!");
        if (TNSettings.getInstance().topAct != null) {
            TNUtilsUi.showNotification(TNSettings.getInstance().topAct,
                    error, true);
        }
        TNSettings.getInstance().hasDbError = true;
        TNSettings.getInstance().savePref(false);

        MLog.i("DbReportError e");
    }


    AsyncTask<Object, Object, Object> taskWatcher;
    private void watchAppSwitch() {
        mIsTaskWatchEnable =true;
        //一个线程，让我一直检测
        taskWatcher = new AsyncTask<Object, Object, Object>() {

            @Override
            protected Object doInBackground(Object... params) {

                //把这个while当成看门狗吧。
                while (mIsTaskWatchEnable) {
                    TNUtilsUi.checkLockScreen(TNApplication.this);

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        taskWatcher.execute(null, null);
    }

    private boolean mIsTaskWatchEnable;

    public void watchAppEnable(boolean isTaskWatchEnable) {
        //一个线程，让我一直检测

        if (isTaskWatchEnable && null == taskWatcher) {
            watchAppSwitch();
        } else {
            if (null != taskWatcher) {
                boolean flag = taskWatcher.cancel(true);
                Log.e("test", "taskWatcher.cancel1" + flag);
                if (flag) taskWatcher = null;
            }
        }
        mIsTaskWatchEnable = isTaskWatchEnable;
    }
}
