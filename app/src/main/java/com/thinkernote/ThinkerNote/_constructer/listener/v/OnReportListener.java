package com.thinkernote.ThinkerNote._constructer.listener.v;

/**
 *  v层 只有一个接口 通用回调
 */
public interface OnReportListener {

    void onPicSuccess(Object obj,String content,String email);

    void onPicFailed(String msg, Exception e);

    void onSubmitSuccess(Object obj);

    void onSubmitFailed(String msg, Exception e);

}
