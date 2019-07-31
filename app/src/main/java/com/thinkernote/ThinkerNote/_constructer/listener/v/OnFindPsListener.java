package com.thinkernote.ThinkerNote._constructer.listener.v;

/**
 * 找回密码 v层
 */
public interface OnFindPsListener {
    void onPicSuccess(Object obj);

    void onPicFailed(String msg, Exception e);

    void onPhoneVCodeSuccess(Object obj);

    void onPhoneVCodeFailed(String msg, Exception e);

    void onMailVCodeSuccess(Object obj);

    void onMailVCodetFailed(String msg, Exception e);


    void onSubmitSuccess(Object obj);

    void onSubmitFailed(String msg, Exception e);

    void onAutoLoginSuccess(Object obj);

    void onAutoLoginFailed(String msg, Exception e);

    void onProfileSuccess(Object obj);

    void onProfileFailed(String msg, Exception e);

}
