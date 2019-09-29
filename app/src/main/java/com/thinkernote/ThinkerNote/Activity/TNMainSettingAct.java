package com.thinkernote.ThinkerNote.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.thinkernote.ThinkerNote.Activity.settings.TNCommonSettingsAct;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.main.MainUpgradeBean;
import com.thinkernote.ThinkerNote.dialog.CommonDialog;
import com.thinkernote.ThinkerNote.dialog.UpdateDialog;
import com.thinkernote.ThinkerNote.mvp.http.fileprogress.FileProgressListener;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnUpgradeListener;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnUserinfoListener;
import com.thinkernote.ThinkerNote.mvp.p.UpgradePresenter;
import com.thinkernote.ThinkerNote.mvp.p.UserInfoPresenter;

import java.io.File;
import java.util.LinkedList;

/**
 * 主页--设置
 * 样式：recylerView
 * sjy 0614
 */
public class TNMainSettingAct extends TNActBase implements OnClickListener, OnUserinfoListener, OnUpgradeListener {

    private String mDownLoadAPKPath = "";
    private TNSettings mSettings = TNSettings.getInstance();
    File installFile;//安装包file
    //
    private UserInfoPresenter presener;
    private UpgradePresenter upgradePresenter;


    //更新弹窗的自定义监听（确定按钮的监听）
    private UpdateDialog upgradeDialog;

    //--------------------控件相关----------------------------
    LinearLayout ly_update, ly_set, ly_user, ly_space, ly_pay, ly_about;
    TextView tv_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_settings_main);
        setViews();
        initView();
        //
        presener = new UserInfoPresenter(this, this);
        upgradePresenter = new UpgradePresenter(this, this);
    }

    @Override
    protected void configView() {
    }

    @Override
    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.userinfo_page,
                R.drawable.page_bg);
        TNUtilsSkin.setViewBackground(this, null, R.id.userinfo_toolbar_layout,
                R.drawable.toolbg);

        findViewById(R.id.userinfo_back).setOnClickListener(this);
        findViewById(R.id.userinfo_logout).setOnClickListener(this);
    }

    /**
     *
     */
    private void initView() {
        ly_about = findViewById(R.id.ly_about);
        ly_pay = findViewById(R.id.ly_pay);
        ly_update = findViewById(R.id.ly_update);
        ly_user = findViewById(R.id.ly_user);
        ly_space = findViewById(R.id.ly_space);
        ly_set = findViewById(R.id.ly_set);
        tv_version = findViewById(R.id.tv_version);

        ly_about.setOnClickListener(this);
        ly_user.setOnClickListener(this);
        ly_update.setOnClickListener(this);
        ly_space.setOnClickListener(this);
        ly_pay.setOnClickListener(this);
        ly_set.setOnClickListener(this);
        //
        tv_version.setText("当前版本：" + getAppVersionName(this));

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.userinfo_back://
                finish();
                break;

            case R.id.userinfo_logout://
                logout();

                break;
            case R.id.ly_update://检查更新
                upgrade();
                break;
            case R.id.ly_pay://打赏
                startActivity(TNPayTipAct.class);
                break;
            case R.id.ly_about://关于我们
                Bundle b1 = new Bundle();
                b1.putString("Type", "ABOUT_US");
                startActivity(TNCommonSettingsAct.class, b1);
                break;
            case R.id.ly_set://个性设置
                Bundle b2 = new Bundle();
                b2.putString("Type", "SETTING");
                startActivity(TNCommonSettingsAct.class, b2);
                break;
            case R.id.ly_user://用户
                Bundle b3 = new Bundle();
                b3.putString("Type", "USER_INFO");
                startActivity(TNCommonSettingsAct.class, b3);
                break;
            case R.id.ly_space://空间
                Bundle b4 = new Bundle();
                b4.putString("Type", "SPACE_INFO");
                startActivity(TNCommonSettingsAct.class, b4);
                break;

        }


    }

    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public String getAppVersionName(Context context) {
        String appVersionName = "";
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            appVersionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            MLog.e(e.getMessage());
        }
        return appVersionName;
    }

    // ----------------------------------------------------------------------------------


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 10001:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MLog.d("4");
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
            case 10002://
                MLog.d("6");
                checkIsAndroidO();
                break;
            default:
                break;
        }
    }

    //-----------------------------------------p层调用--------------------------------------------


    private void logout() {
        presener.pLogout();
    }

    private void upgrade() {
        if (TNUtils.checkNetwork(this)) {
            upgradePresenter.pUpgrade();

        }
    }


    private void download(String url) {
        upgradePresenter.pDownload(url, progressListener);
    }

    //监听下载文件进度,包括文件大小
    FileProgressListener progressListener = new FileProgressListener() {

        @Override
        public void onFileProgressing(int progress) {
            upgradeDialog.setProgress(progress);
        }

    };

    //-----------------------------------------接口回调--------------------------------------------
    @Override
    public void onLogoutSuccess(Object obj) {

        TNSettings settings = TNSettings.getInstance();
        settings.isLogout = true;
        settings.lockPattern = new LinkedList<Integer>();
        settings.userId = -1;
        settings.username = "";
        settings.phone = "";
        settings.email = "";
        settings.password = "";
        settings.savePref(true);

        startActivity(TNLoginAct.class);
        finish();
    }

    @Override
    public void onLogoutFailed(String msg, Exception e) {
        MLog.e(msg);

        TNSettings settings = TNSettings.getInstance();
        settings.isLogout = true;
        settings.lockPattern = new LinkedList<Integer>();
        settings.userId = -1;
        settings.username = "";
        settings.phone = "";
        settings.email = "";
        settings.password = "";
        settings.savePref(true);

        startActivity(TNLoginAct.class);
        finish();
    }

    @Override
    public void onUpgradeSuccess(Object obj) {
        MainUpgradeBean bean = (MainUpgradeBean) obj;
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            MLog.d(TAG, info.versionCode + "," + info.versionName);
            String newVersionName = bean.getVersion();
            int newVersionCode = bean.getVersionCode() != 0 ? bean.getVersionCode() : -1;
            int newSize = bean.getSize();
            String description = bean.getContent();
            mDownLoadAPKPath = bean.getUrl();

            MLog.d(TAG, newVersionName + "," + newSize);

            if (newVersionCode > info.versionCode) {
                //需要判断手机是否支持最低版本
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    CommonDialog dialog = new CommonDialog(this, "有最新版，您的手机版本过低，新版本软件不支持升级!,建议在android6.0以上的手机更新最新版本", new CommonDialog.DialogCallBack() {
                        @Override
                        public void sureBack() {

                        }

                        @Override
                        public void cancelBack() {

                        }
                    });
                    dialog.show();
                    return;
                }
                upgradeDialog = new UpdateDialog(this, info.versionName, newVersionName, description, new UpdateDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        upgradeDialog.setButtonClick(false);
                        //下载接口
                        download(mDownLoadAPKPath);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //下载完成
    @Override
    public void onUpgradeFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onDownloadSuccess(File filePath) {
        upgradeDialog.dismiss();
        MLog.d("下载完成--apk路径：" + filePath.getParentFile());
        if (filePath != null) {
            installFile = filePath;
            if (filePath != null) {
                /**
                 * 判断是否是8.0,8.0需要处理未知应用来源权限问题,否则直接安装
                 */
                checkIsAndroidO();
            }
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
        upgradeDialog.dismiss();
        TNUtilsUi.showToast(msg);
    }

}
