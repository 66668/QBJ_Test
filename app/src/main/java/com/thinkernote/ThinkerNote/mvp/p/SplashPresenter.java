package com.thinkernote.ThinkerNote.mvp.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.mvp.m.SplashModel;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnSplashListener;

/**
 * p层 具体实现
 */
public class SplashPresenter implements  OnSplashListener {
    private Context context;
    private OnSplashListener onView;
    //p层调用M层方法
    private SplashModel model;

    public SplashPresenter(Context context, OnSplashListener logListener) {
        this.context = context;
        this.onView = logListener;

        model = new SplashModel(context);
    }


    //============================p层重写，用于调用m层方法============================

    public void plogin(String name, String ps) {
        model.mLogin(this, name, ps);

    }

    public void pUpdataProfile() {
        model.mProFile(this);
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
