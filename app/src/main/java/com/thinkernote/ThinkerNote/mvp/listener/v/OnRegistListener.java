package com.thinkernote.ThinkerNote.mvp.listener.v;

/**
 * 注册  v层
 */
public interface OnRegistListener {
    void onPicSuccess(Object obj);

    void onPicFailed(String msg, Exception e);

    void onPhoneVCodeSuccess(Object obj);

    void onPhoneVCodeFailed(String msg, Exception e);

    void onSubmitRegistSuccess(Object obj);

    void onSubmitRegistFailed(String msg, Exception e);

    void onSubmitFindPsSuccess(Object obj);

    void onSubmitFindPsFailed(String msg, Exception e);

    void onAutoLoginSuccess(Object obj);

    void onAutoLoginFailed(String msg, Exception e);

    void onBindPhoneSuccess(Object obj);

    void onBindPhoneFailed(String msg, Exception e);

    void onProfileSuccess(Object obj);

    void onProfileFailed(String msg, Exception e);


}
