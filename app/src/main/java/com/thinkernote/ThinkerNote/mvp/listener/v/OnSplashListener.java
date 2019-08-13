package com.thinkernote.ThinkerNote.mvp.listener.v;

/**
 * v层 只有一个接口 通用回调
 */
public interface OnSplashListener {
    void onSuccess(Object obj);

    void onFailed(String msg, Exception e);

    void onProfileSuccess(Object obj);

    void onProfileFailed(String msg, Exception e);

}
