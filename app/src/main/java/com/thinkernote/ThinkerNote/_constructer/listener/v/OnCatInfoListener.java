package com.thinkernote.ThinkerNote._constructer.listener.v;

/**
 *  v层 只有一个接口 通用回调
 */
public interface OnCatInfoListener {
    void onSuccess(Object obj);

    void onFailed(String msg, Exception e);

    void onDeleteFolderSuccess(Object obj,long catId);

    void onDeleteFolderFailed(String msg, Exception e);
}
