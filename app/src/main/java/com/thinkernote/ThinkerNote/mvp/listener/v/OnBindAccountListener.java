package com.thinkernote.ThinkerNote.mvp.listener.v;

/**
 * 登录使用 绑定手机号 v层m层公用回调
 */
public interface OnBindAccountListener {
    void onVerifyCodeSuccess(Object obj);

    void onVerifyCodeFailed(String msg, Exception e);

    void onBindSuccess(Object obj);

    void onBindFailed(String msg, Exception e);

    void onAutoLogSuccess(Object obj);

    void onAutoLogFailed(String msg, Exception e);

    void onLogProfileSuccess(Object obj);

    void onLogProfileFailed(String msg, Exception e);

}
