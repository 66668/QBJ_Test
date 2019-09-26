package com.thinkernote.ThinkerNote.base;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDb2;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Service.LocationService;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.mvp.http.url_main.HttpUtils;

/**
 * sjy 0607
 */
public class TNApplication extends Application {
    private static final String TAG = "TNApplication";
    private boolean isEnryMain = false;//用于小部件判断欢迎页是否再使用。
    private static TNApplication application;

    public static TNApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        isEnryMain = false;
        //log初始化
        MLog.init(true, "SJY");
        initialize();
        //新网络框架 初始化
        HttpUtils.getInstance().init(this, MLog.DEBUG);


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

        // 数据库初始化
        TNDb.getInstance();
        TNDb2.getInstance();

        //地图定位
//        TNLBSService.getInstance();
        //地图定位 新版
        LocationService.getInstance();
        // 设置此接口后，音频文件和识别结果文件保存在/sdcard/msc/record/目录下
        //com.iflytek.resource.MscSetting.setLogSaved(true);

        //讯飞语音初始化
        StringBuffer param = new StringBuffer();
        param.append("appid=" + getString(R.string.app_id));
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(this, param.toString());
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

    //========================用于小部件==============================
    public boolean isEnryMain() {
        return isEnryMain;
    }

    public void setEnryMain(boolean enryMain) {
        isEnryMain = enryMain;
    }
    //======================================================
}
