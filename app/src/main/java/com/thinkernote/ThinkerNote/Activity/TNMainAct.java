package com.thinkernote.ThinkerNote.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.Utils.TNActivityManager;
import com.thinkernote.ThinkerNote.dialog.CustomDialog;
import com.thinkernote.ThinkerNote.dialog.UpdateDialog;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnUpgradeListener;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnSyncListener;
import com.thinkernote.ThinkerNote.mvp.p.UpgradePresenter;
import com.thinkernote.ThinkerNote.mvp.p.SyncPresenter;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.main.MainUpgradeBean;
import com.thinkernote.ThinkerNote.mvp.MyRxManager;
import com.thinkernote.ThinkerNote.mvp.http.fileprogress.FileProgressListener;

import java.io.File;

/**
 * 主界面
 * 说明：进入主界面：会同时执行2个异步：onCreate的更新 和 onResume下的configView的同步
 * 同步功能说明：由10多个接口串行调用，比较复杂，所以要注意调用顺序
 * sjy 0702
 */
public class TNMainAct extends TNActBase implements OnClickListener, OnUpgradeListener, OnSyncListener {

    //==================================变量=======================================
    private long mLastClickBackTime = 0;
    private String mDownLoadAPKPath = "";
    private boolean isDestory;
    private TextView mTimeView;
    private TNSettings mSettings = TNSettings.getInstance();
    //
    private UpgradePresenter mainPresenter;//新版本
    private SyncPresenter syncPresenter;//新版本
    File installFile;//安装包file

    //更新弹窗
    private UpdateDialog upgradeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //关闭其他界面
        TNActivityManager.getInstance().finishOtherActivity(this);
        mainPresenter = new UpgradePresenter(this, this);
        syncPresenter = new SyncPresenter(this, this);
        setViews();
        MyRxManager.getInstance().setSyncing(false);//修改状态值
        //第一次进入，打开帮助界面
        if (mSettings.firstLaunch) {
            startActivity(TNHelpAct.class);
        }

        //检查更新
        if (savedInstanceState == null) {
            if (TNUtils.isNetWork()) {
                findUpgrade();
            }
            mSettings.appStartCount += 1;
            mSettings.savePref(false);
        }
        //
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int flag = intent.getIntExtra("FLAG", -1);
        if (flag == 1) {
            CustomDialog.Builder builder = new CustomDialog.Builder(this);
            builder.setMessage("恭喜您！绑定成功");
            builder.setTitle(R.drawable.phone_enable);
            builder.setShowNext(false);
            builder.setPositiveButton("开始使用", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
    }


    protected void setViews() {
        TNUtilsSkin.setImageViewDrawable(this, null, R.id.main_divide, R.drawable.main_divide);

        mTimeView = (TextView) findViewById(R.id.main_lastsync_time);

        /* set listeners */
        findViewById(R.id.main_allnote).setOnClickListener(this);
        findViewById(R.id.main_cameranote).setOnClickListener(this);
        findViewById(R.id.main_newnote).setOnClickListener(this);
        findViewById(R.id.main_project).setOnClickListener(this);
        findViewById(R.id.main_doodlenote).setOnClickListener(this);
        findViewById(R.id.main_serch).setOnClickListener(this);
        findViewById(R.id.main_sync_btn).setOnClickListener(this);
        findViewById(R.id.main_recordnote).setOnClickListener(this);
        findViewById(R.id.main_exchange).setOnClickListener(this);

        findViewById(R.id.main_projectlog_count_layout).setVisibility(View.INVISIBLE);
        findViewById(R.id.main_bootview).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        mLastClickBackTime = 0;
        isDestory = false;
        super.onResume();

    }

    @Override
    public void onDestroy() {
        syncPresenter.finishSync();
        isDestory = true;
        super.onDestroy();
        MLog.d("TNMainAct--onDestroy");
    }

    @Override
    protected void configView() {

        if (TextUtils.isEmpty(mSettings.phone) && mSettings.phoneDialogShowCount < 3 && createStatus == 0) {

            mSettings.phoneDialogShowCount += 1;
            mSettings.savePref(false);
            CustomDialog.Builder builder = new CustomDialog.Builder(this);
            builder.setMessage("检测到您的轻笔记还未绑定手机号，为了安全，请您绑定手机号");
            builder.setTitle(R.drawable.phone_disable);
            builder.setShowNext(true);
            builder.setPositiveButton("绑定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(TNBindPhoneAct.class);
                }
            });
            builder.create().show();
        }

        //第一次进来有网或者在wifi情况下自动同步
        if ((createStatus == 0 && TNUtils.isAutoSync()) || mSettings.firstLaunch) {
            if (MyRxManager.getInstance().isSyncing()) {
                Toast.makeText(this, "正在同步", Toast.LENGTH_SHORT).show();
                return;
            }
            startSyncAnimation();
            TNUtilsUi.showNotification(this, R.string.alert_NoteView_Synchronizing, false);
            //p
            synchronizeData();
        }

        Intent i = null;
        Bundle b = getIntent().getExtras();
        if (b != null) {
            i = (Intent) b.get(Intent.EXTRA_INTENT);
        }
        if (i != null && i.hasExtra("Type") && createStatus == 0) {
            runExtraIntent();
        }

        if (TNSettings.getInstance().originalSyncTime > 0) {
            mTimeView.setText("上次同步时间：" + TNUtilsUi.formatDate(TNMainAct.this,
                    TNSettings.getInstance().originalSyncTime / 1000L));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_newnote: {//综合笔记
                startActivity(TNNoteEditAct.class);
                break;
            }
            case R.id.main_allnote: {//我的笔记
                startActivity(TNMainFragAct.class);
                break;
            }
            case R.id.main_cameranote: {//拍照笔记
                Bundle b = new Bundle();
                b.putString("Target", "camera");
                startActivity(TNNoteEditAct.class, b);
                break;
            }

            case R.id.main_doodlenote: {//涂鸦笔记
                Bundle b = new Bundle();
                b.putString("Target", "doodle");
                startActivity(TNNoteEditAct.class, b);

                break;
            }
            case R.id.main_recordnote: {//录音笔记
                Bundle b = new Bundle();
                b.putString("Target", "record");
                startActivity(TNNoteEditAct.class, b);
                break;
            }
            case R.id.main_project:

                break;

            case R.id.main_exchange: {//设置
                startActivity(TNUserInfoAct.class);
                //debug:
                break;
            }
            case R.id.main_sync_btn: {//同步按钮
                if (TNUtils.isNetWork()) {
                    if (MyRxManager.getInstance().isSyncing()) {
                        Toast.makeText(TNMainAct.this, "正在结束同步，请稍后", Toast.LENGTH_SHORT).show();
                        syncPresenter.cancelSync();
                        endSynchronize(2);
//                        TNUtilsUi.showNotification(this, R.string.alert_Synchronize_TooMuch, false);
                        return;
                    }
                    startSyncAnimation();
                    TNUtilsUi.showNotification(this, R.string.alert_NoteView_Synchronizing, false);

                    synchronizeData();
                } else {
                    TNUtilsUi.showToast(R.string.alert_Net_NotWork);
                    //结束同步按钮动作
                    syncPresenter.cancelSync();
                    endSynchronize(1);
                }
                break;
            }
            case R.id.main_serch: {//搜索
                Bundle b = new Bundle();
                b.putInt("SearchType", 1);
                startActivity(TNSearchAct.class, b);
                break;
            }

            case R.id.main_bootview: {//引导 说明
                findViewById(R.id.main_bootview).setVisibility(View.GONE);
                break;
            }
        }
    }

    public void cancelDialog() {
        findViewById(R.id.main_sync_btn).clearAnimation();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            View v = findViewById(R.id.main_bootview);
            if (v.getVisibility() == View.VISIBLE) {
                v.setVisibility(View.GONE);
                return true;
            }
            long currTime = System.currentTimeMillis();
            if (currTime - mLastClickBackTime > 5000) {
                TNUtilsUi.showShortToast(R.string.click_back_again_exit);
                mLastClickBackTime = currTime;
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 10001:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    TNUtilsUi.openFile(this, installFile);
                } else {
                    //打开未知安装许可
                    Uri packageURI = Uri.parse("package:" + getPackageName());
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                    startActivityForResult(intent, 10002);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 10002:
                checkIsAndroidO();
                break;
            default:
                break;
        }
    }

    /**
     * 同步按钮的动画
     */
    private void startSyncAnimation() {
        RotateAnimation rAnimation = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rAnimation.setDuration(3000);
        rAnimation.setRepeatCount(99999);
        rAnimation.setInterpolator(new LinearInterpolator());
        findViewById(R.id.main_sync_btn).startAnimation(rAnimation);
    }

    /**
     * 同步结束后的操作
     *
     * @param state 0 = 成功/1=back取消同步/2-异常触发同步终止
     */
    private void endSynchronize(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //一些变量需要清空，否则bug

                //结束动画
                findViewById(R.id.main_sync_btn).clearAnimation();

                if (state == 0) {
                    //正常结束
                    TNUtilsUi.showNotification(TNMainAct.this, R.string.alert_MainCats_Synchronized, true);
                    //
                    TNSettings settings = TNSettings.getInstance();
                    settings.originalSyncTime = System.currentTimeMillis();
                    settings.savePref(false);
                    mTimeView.setText("上次同步时间：" + TNUtilsUi.formatDate(TNMainAct.this,
                            settings.originalSyncTime / 1000L));
                } else if (state == 1) {
                    TNUtilsUi.showNotification(TNMainAct.this, R.string.alert_Synchronize_Stoped, true);
                } else {
                    TNUtilsUi.showNotification(TNMainAct.this, R.string.alert_SynchronizeCancell, true);
                }
            }
        });

    }

    //=============================================p层调用======================================================

    /**
     * 数据同步
     */
    private void synchronizeData() {
        // 由p层 处理交互问题
        syncPresenter.synchronizeData("HOME");
    }

    //检查更新
    private void findUpgrade() {
        mainPresenter.pUpgrade();
    }

    private void downloadNewAPK(String url) {
        mainPresenter.pDownload(url, progressListener);
    }

    //监听下载文件进度,包括文件大小
    FileProgressListener progressListener = new FileProgressListener() {

        @Override
        public void onFileProgressing(int progress) {
            upgradeDialog.setProgress(progress);
        }

    };


    //=============================================接口结果回调(成对的success+failed)======================================================


    @Override
    public void onUpgradeSuccess(Object obj) {

        MainUpgradeBean bean = (MainUpgradeBean) obj;

        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
            MLog.d(TAG, info.versionCode + "," + info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int newSize = bean.getSize();
        String newVersionName = bean.getVersion();
        String description = bean.getContent();
        mDownLoadAPKPath = bean.getUrl();
        MLog.d(newVersionName, newSize);
        int newVersionCode = 0;
        if (bean.getVersionCode() == 0) {
            newVersionCode = -1;
        } else {
            newVersionCode = bean.getVersionCode();
        }
        //这里需要加判断更新的字段,判断是否需要更新且只更新一次
        if (mSettings.version.equals(newVersionName)) {
            return;
        }
        mSettings.version = newVersionName;
        mSettings.savePref(false);

        //
        if (newVersionCode > info.versionCode) {

            upgradeDialog = new UpdateDialog(this, info.versionName, newVersionName, description, new UpdateDialog.DialogCallBack() {
                @Override
                public void sureBack() {
                    upgradeDialog.setButtonClick(false);
                    //下载接口
                    downloadNewAPK(mDownLoadAPKPath);
                }

                @Override
                public void cancelBack() {
                    upgradeDialog.dismiss();
                }
            });
            upgradeDialog.show();
        } else {
            TNUtilsUi.showToast("当前版本已是最新");
        }
    }

    //下载完成
    @Override
    public void onUpgradeFailed(String msg, Exception e) {
        if (isDestory) {
            return;
        }
        MLog.e(msg);
        endSynchronize(2);
//        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onDownloadSuccess(File filePath) {
        if (isDestory) {
            return;
        }
        upgradeDialog.dismiss();
        MLog.d("下载完成--apk路径：" + filePath);
        installFile = filePath;
        if (filePath != null) {
            /**
             * 判断是否是8.0,8.0需要处理未知应用来源权限问题,否则直接安装
             */
            checkIsAndroidO();
        }
    }

    /**
     * 判断是否是8.0,8.0需要处理未知应用来源权限问题,否则直接安装
     */
    private void checkIsAndroidO() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean b = getPackageManager().canRequestPackageInstalls();
            if (b) {
                TNUtilsUi.openFile(this, installFile);//安装应用的逻辑(写自己的就可以)
            } else {
                //请求安装未知应用来源的权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, 10001);
            }
        } else {
            TNUtilsUi.openFile(this, installFile);
        }

    }

    @Override
    public void onDownloadFailed(String msg, Exception e) {
        if (isDestory) {
            return;
        }
        MLog.e(msg);
        endSynchronize(2);
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onSyncSuccess(String obj) {
        if (isDestory) {
            return;
        }
        if (obj.equals("同步取消")) {
            endSynchronize(2);
        } else {
            endSynchronize(0);
        }
    }

    @Override
    public void onSyncFailed(Exception e, String msg) {
        if (isDestory) {
            return;
        }
        endSynchronize(2);
    }

    //如下回调不使用
    @Override
    public void onSyncEditSuccess() {

    }


}
