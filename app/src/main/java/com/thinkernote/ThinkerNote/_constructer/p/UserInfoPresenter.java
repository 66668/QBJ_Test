package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.m.UserInfoModule;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnUserinfoListener;
import com.thinkernote.ThinkerNote.http.fileprogress.FileProgressListener;

import java.io.File;

/**
 * 主页--设置界面 p层 具体实现
 */
public class UserInfoPresenter implements  OnUserinfoListener {
    private Context context;
    private OnUserinfoListener onView;
    //p层调用M层方法
    private UserInfoModule module;

    public UserInfoPresenter(Context context, OnUserinfoListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new UserInfoModule(context);
    }


    //============================p层重写，用于调用m层方法============================
    public void pLogout() {
        module.mLogout(this);
    }

    public void pUpgrade() {
        module.mUpgrade(this);
    }

    public void pDownload(String url, FileProgressListener listener) {
        module.mDownload(this, url, listener);
    }


    //==========================结果回调==============================

    @Override
    public void onLogoutSuccess(Object obj) {
        onView.onLogoutSuccess(obj);
    }

    @Override
    public void onLogoutFailed(String msg, Exception e) {
        onView.onLogoutFailed(msg, e);
    }

    @Override
    public void onUpgradeSuccess(Object obj) {
        onView.onUpgradeSuccess(obj);
    }

    @Override
    public void onUpgradeFailed(String msg, Exception e) {
        onView.onUpgradeFailed(msg, e);
    }

    @Override
    public void onDownloadSuccess(File file) {
        onView.onDownloadSuccess(file);
    }

    @Override
    public void onDownloadFailed(String msg, Exception e) {
        onView.onDownloadFailed(msg, e);
    }
}