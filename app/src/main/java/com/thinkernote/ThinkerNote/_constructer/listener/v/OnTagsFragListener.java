package com.thinkernote.ThinkerNote._constructer.listener.v;

/**
 * v层 只有一个接口 通用回调
 */
public interface OnTagsFragListener {
    void onGetTagListSuccess(Object obj);

    void onGetTagListFailed(String msg, Exception e);
}
