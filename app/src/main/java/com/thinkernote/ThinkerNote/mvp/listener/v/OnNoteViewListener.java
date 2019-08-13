package com.thinkernote.ThinkerNote.mvp.listener.v;

/**
 * 笔记详情 v层 只有一个接口 通用回调
 */
public interface OnNoteViewListener {
    void onGetNoteSuccess(Object obj);

    void onGetNoteFailed(String msg, Exception e);

}
