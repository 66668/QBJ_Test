package com.thinkernote.ThinkerNote._interface.v;

/**
 *  v层 只有一个接口 通用回调
 */
public interface OnTagInfoListener {
    void onSuccess(Object obj,long pid);

    void onFailed(String msg, Exception e);

}
