package com.thinkernote.ThinkerNote._constructer.listener.v;

/**
 *  v层 只有一个接口 通用回调
 */
public interface OnTagInfoListener {
    void onSuccess();

    void onFailed(String msg, Exception e);

}
