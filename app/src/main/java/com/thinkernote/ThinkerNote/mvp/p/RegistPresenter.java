package com.thinkernote.ThinkerNote.mvp.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.utils.TNUtils;
import com.thinkernote.ThinkerNote.mvp.m.RegistModel;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnRegistListener;

/**
 * 注册 p层 具体实现
 */
public class RegistPresenter implements OnRegistListener {
    private Context context;
    private OnRegistListener onView;
    //p层调用M层方法
    private RegistModel model;

    public RegistPresenter(Context context, OnRegistListener logListener) {
        this.context = context;
        this.onView = logListener;
        model = new RegistModel(context);
    }

    //============================p层重写，用于调用m层方法============================
    public void getVerifyPic() {
        model.getVerifyPic(this);
    }

    public void phoneVerifyCode(String mPhone, String name, String mAnswer, String mNonce, String mHashKey) {
        model.phoneVerifyCode(this, mPhone, name, mAnswer, mNonce, mHashKey);
    }

    public void submitRegister(String phone, String ps, String vcode) {
        model.submitRegist(this, phone, ps, vcode);
    }

    public void submitForgotPassword(String phone, String ps, String vcode) {
        model.submitForgetPs(this, phone, ps, vcode);
    }


    public void bindPhone(int mUserType, String bid, String name, String accessToken, String refreshToken, long currentTime, String phone, String vcode) {
        String sign = "access_token=" + accessToken + "&bid=" + bid + "&btype=" + mUserType + "&name=" + name + "&phone=" + phone + "&refresh_token=" + refreshToken + "&stamp=" + currentTime + "&vcode=" + vcode + "qingbiji";

        model.bindPhone(this, mUserType, bid, name, accessToken, refreshToken, currentTime, phone, vcode, TNUtils.toMd5(sign).toLowerCase());
    }

    public void autoLogin(String phoneOrEmail, String ps) {
        model.autoLogin(this, phoneOrEmail, ps);
    }

    public void pProfile() {
        model.mProfile(this);
    }

    //==========================结果回调==============================
    @Override
    public void onPicSuccess(Object obj) {
        onView.onPicSuccess(obj);
    }

    @Override
    public void onPicFailed(String msg, Exception e) {
        onView.onPicFailed(msg, e);
    }

    @Override
    public void onPhoneVCodeSuccess(Object obj) {
        onView.onPhoneVCodeSuccess(obj);
    }

    @Override
    public void onPhoneVCodeFailed(String msg, Exception e) {
        onView.onPhoneVCodeFailed(msg, e);
    }

    @Override
    public void onSubmitRegistSuccess(Object obj) {
        onView.onSubmitRegistSuccess(obj);
    }

    @Override
    public void onSubmitRegistFailed(String msg, Exception e) {
        onView.onSubmitRegistFailed(msg, e);
    }

    @Override
    public void onSubmitFindPsSuccess(Object obj) {
        onView.onSubmitFindPsSuccess(obj);
    }

    @Override
    public void onSubmitFindPsFailed(String msg, Exception e) {
        onView.onSubmitFindPsFailed(msg, e);
    }


    @Override
    public void onAutoLoginSuccess(Object obj) {
        onView.onAutoLoginSuccess(obj);
    }

    @Override
    public void onAutoLoginFailed(String msg, Exception e) {
        onView.onAutoLoginFailed(msg, e);
    }

    @Override
    public void onBindPhoneSuccess(Object obj) {
        onView.onBindPhoneSuccess(obj);
    }

    @Override
    public void onBindPhoneFailed(String msg, Exception e) {
        onView.onBindPhoneFailed(msg, e);
    }

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
