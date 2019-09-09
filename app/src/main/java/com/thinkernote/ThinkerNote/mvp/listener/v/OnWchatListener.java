package com.thinkernote.ThinkerNote.mvp.listener.v;

/**
 * 微信登录 v层
 */
public interface OnWchatListener {
    void onWchatSuccess();

    void onWchatFailed(String msg, Exception e);


}
