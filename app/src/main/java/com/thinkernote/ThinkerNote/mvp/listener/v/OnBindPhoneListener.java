package com.thinkernote.ThinkerNote.mvp.listener.v;

/**
 * 主界面使用 绑定手机号 v层回调
 */
public interface OnBindPhoneListener {
    void onVerifyPicSuccess(Object obj);

    void onVerifyPicFailed(String msg, Exception e);

    void onVcodeSuccess(Object obj);

    void onVcodeFailed(String msg, Exception e);

    void onBindSuccess(Object obj,String phone);

    void onBindFailed(String msg, Exception e);

    void onProfileSuccess(Object obj);

    void onProfileFailed(String msg, Exception e);

}
