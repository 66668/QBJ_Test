package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.m.SettingsModule;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnSettingsListener;

/**
 * p层 具体实现
 */
public class SettingsPresenter implements  OnSettingsListener {
    private Context context;
    private OnSettingsListener onView;
    //p层调用M层方法
    private SettingsModule module;

    public SettingsPresenter(Context context, OnSettingsListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new SettingsModule(context);
    }

    //============================p层重写，用于调用m层方法============================
    public void pGetProfile() {
        module.mgetProfile(this);

    }

    public void verifyEmail() {
        module.mVerifyEmail(this);
    }

    public void setDefaultFolder(long pid) {
        module.mSetDefaultFolder(this,pid);
    }

    //==========================结果回调==============================


    @Override
    public void onDefaultFolderSuccess(Object obj,long pid) {
        onView.onDefaultFolderSuccess(obj,pid);
    }

    @Override
    public void onDefaultFoldeFailed(String msg, Exception e) {
        onView.onDefaultFoldeFailed(msg, e);
    }

    @Override
    public void onVerifyEmailSuccess(Object obj) {
        onView.onVerifyEmailSuccess(obj);
    }

    @Override
    public void onVerifyEmailFailed(String msg, Exception e) {
        onView.onVerifyEmailFailed(msg, e);
    }

    @Override
    public void onProfileSuccess(Object obj) {
        onView.onProfileSuccess(obj);
    }

    @Override
    public void onProfileFailed(String msg, Exception e) {
        onView.onProfileFailed(msg, e);
    }


}
