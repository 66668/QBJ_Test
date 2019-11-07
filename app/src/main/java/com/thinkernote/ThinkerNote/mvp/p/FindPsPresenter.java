package com.thinkernote.ThinkerNote.mvp.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.mvp.m.FindPsModel;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnFindPsListener;

/**
 * 登录 p层 具体实现
 */
public class FindPsPresenter implements  OnFindPsListener {
    private OnFindPsListener onView;
    //p层调用M层方法
    private FindPsModel model;

    public FindPsPresenter( OnFindPsListener logListener) {
        this.onView = logListener;
        model = new FindPsModel();
    }


    //============================p层重写，用于调用m层方法============================

    //图片验证
    public void getVerifyPic() {
        model.getVerifyPic(this);
    }

    //短信验证码
    public void phoneVerifyCode(String mPhone, String name, String mAnswer, String mNonce, String mHashKey) {
        model.phoneVerifyCode(this, mPhone, name, mAnswer, mNonce, mHashKey);
    }

    //邮箱验证
    public void mailVerifyCode(String mEmail, String name) {
        model.mailVerifyCode(this, mEmail, name);
    }

    //修改密码提交
    public void submit(String phone, String ps, String vcode) {
        model.submit(this, phone, ps, vcode);
    }

    //登录
    public void autoLogin(String phoneOrEmail, String ps) {
        model.autoLogin(this, phoneOrEmail, ps);
    }

    //更新
    public void pProfile() {
        model.mProfile(this);
    }

    //==========================结果回调==============================
    //
    @Override
    public void onPicSuccess(Object obj) {
        onView.onPicSuccess(obj);
    }

    @Override
    public void onPicFailed(String msg, Exception e) {
        onView.onPicFailed(msg, e);
    }

    //
    @Override
    public void onPhoneVCodeSuccess(Object obj) {
        onView.onPhoneVCodeSuccess(obj);
    }

    @Override
    public void onPhoneVCodeFailed(String msg, Exception e) {
        onView.onPhoneVCodeFailed(msg, e);
    }

    //
    @Override
    public void onMailVCodeSuccess(Object obj) {
        onView.onMailVCodeSuccess(obj);
    }

    @Override
    public void onMailVCodetFailed(String msg, Exception e) {
        onView.onMailVCodetFailed(msg, e);
    }

    //
    @Override
    public void onSubmitSuccess(Object obj) {
        onView.onSubmitSuccess(obj);
    }

    @Override
    public void onSubmitFailed(String msg, Exception e) {
        onView.onSubmitFailed(msg, e);
    }

    //
    @Override
    public void onAutoLoginSuccess(Object obj) {
        onView.onAutoLoginSuccess(obj);
    }

    @Override
    public void onAutoLoginFailed(String msg, Exception e) {
        onView.onAutoLoginFailed(msg, e);
    }

    //
    @Override
    public void onProfileSuccess(Object obj) {
        onView.onProfileSuccess(obj);
    }

    @Override
    public void onProfileFailed(String msg, Exception e) {
        onView.onProfileFailed(msg, e);
    }


    //========================================================
}
