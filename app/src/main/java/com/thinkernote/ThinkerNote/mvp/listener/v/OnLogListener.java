package com.thinkernote.ThinkerNote.mvp.listener.v;

/**
 * 登录 v层
 */
public interface OnLogListener {
    void onLoginNormalSuccess(Object obj);

    void onLoginNormalFailed(String msg, Exception e);

    void onQQUnionIdSuccess(Object obj,String accessToken,String refreshToken);

    void onQQUnionIdFailed(String msg, Exception e);

    void onLoginThirdSuccess(Object obj);

    void onLoginThirdFailed(String msg, Exception e, String bid, int btype, long currentTime, String accessToken, String refreshToken, String name);

    void onLogProfileSuccess(Object obj);

    void onLogProfileFailed(String msg, Exception e);


}
