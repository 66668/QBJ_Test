package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.m.SplashModule;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnSplashListener;

/**
 * p层 具体实现
 */
public class SplashPresenter implements  OnSplashListener {
    private Context context;
    private OnSplashListener onView;
    //p层调用M层方法
    private SplashModule module;

    public SplashPresenter(Context context, OnSplashListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new SplashModule(context);
    }


    //============================p层重写，用于调用m层方法============================

    public void plogin(String name, String ps) {
        module.mLogin(this, name, ps);

    }

    public void pUpdataProfile() {
        module.mProFile(this);
    }
    //==========================结果回调==============================

    @Override
    public void onSuccess(Object obj) {
        onView.onSuccess(obj);
    }

    @Override
    public void onFailed(String msg, Exception e) {
        onView.onFailed(msg, e);
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