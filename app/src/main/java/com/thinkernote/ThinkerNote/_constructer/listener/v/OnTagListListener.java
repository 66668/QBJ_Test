package com.thinkernote.ThinkerNote._constructer.listener.v;

/**
 *  v层 只有一个接口 通用回调
 */
public interface OnTagListListener {
    void onTagListSuccess();

    void onTagListFailed(String msg, Exception e);

}
