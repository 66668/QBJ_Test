package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnLogListener;
import com.thinkernote.ThinkerNote._constructer.m.LogModule;

/**
 * 登录 p层 具体实现
 */
public class LogPresenter implements  OnLogListener {
    private Context context;
    private OnLogListener onLogView;
    //p层调用M层方法
    private LogModule logModule;

    public LogPresenter(Context context, OnLogListener logListener) {
        this.context = context;
        this.onLogView = logListener;
        logModule = new LogModule(context);
    }

    //============================p层重写，用于调用m层方法============================
    public void loginNormal(String name, String ps) {
        logModule.loginNomal(this, name, ps);
    }

    public void loginThird(int aArray, String unionId, long currentTime, String accessToken, String refreshToken, String name) {
        String sign = "bid=" + unionId + "&btype=" + aArray + "&stamp=" + currentTime + "qingbiji";
        logModule.loginThird(this
                , aArray
                , unionId
                , currentTime
                , TNUtils.toMd5(sign).toLowerCase()
                , accessToken
                , refreshToken
                , name);
    }

    //更新
    public void pUpdataProfile() {
        logModule.mProfile(this);
    }

    public void getQQUnionId(String url, String accessToken, String refreshToken) {
        logModule.mGetQQUnionId(this, url, accessToken, refreshToken);
    }

    //==========================结果回调==============================
    @Override
    public void onLoginNormalSuccess(Object obj) {
        onLogView.onLoginNormalSuccess(obj);
    }

    @Override
    public void onLoginNormalFailed(String msg, Exception e) {
        onLogView.onLoginNormalFailed(msg, e);
    }

    @Override
    public void onQQUnionIdSuccess(Object obj, String accessToken, String refreshToken) {
        onLogView.onQQUnionIdSuccess(obj, accessToken, refreshToken);
    }

    @Override
    public void onQQUnionIdFailed(String msg, Exception e) {
        onLogView.onQQUnionIdFailed(msg, e);
    }

    @Override
    public void onLoginThirdSuccess(Object obj) {
        onLogView.onLoginThirdSuccess(obj);
    }

    @Override
    public void onLoginThirdFailed(String msg, Exception e, String bid, int btype, long currentTime, String accessToken, String refreshToken, String name) {
        onLogView.onLoginThirdFailed(msg, e, bid, btype, currentTime, accessToken, refreshToken, name);
    }

    @Override
    public void onLogProfileSuccess(Object obj) {
        onLogView.onLogProfileSuccess(obj);
    }

    @Override
    public void onLogProfileFailed(String msg, Exception e) {
        onLogView.onLogProfileFailed(msg, e);
    }
    //========================================================
}
