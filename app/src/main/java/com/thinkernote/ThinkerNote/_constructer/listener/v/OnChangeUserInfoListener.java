package com.thinkernote.ThinkerNote._constructer.listener.v;

/**
 * v层 共view/act使用
 */
public interface OnChangeUserInfoListener {
    void onChangePsSuccess(Object obj,String newPs);

    void onChangePsFailed(String msg, Exception e);

    void onChangeNameOrEmailSuccess(Object obj, String nameOrEmail, String type);

    void onChangeNameOrEmailFailed(String msg, Exception e);

    void onProfileSuccess(Object obj);

    void onProfileFailed(String msg, Exception e);

}
