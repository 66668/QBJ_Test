package com.thinkernote.ThinkerNote.mvp.listener.v;

/**
 * 主页--设置界面 v层
 */
public interface OnUserinfoListener {
    void onLogoutSuccess(Object obj);

    void onLogoutFailed(String msg, Exception e);
}
