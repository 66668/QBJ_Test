package com.thinkernote.ThinkerNote.mvp.listener.v;

/**
 * v层 只有一个接口 通用回调
 */
public interface OnCatListListener {

    void onParentFolderSuccess(Object obj);

    void onParentFolderFailed(String msg, Exception e);

    void onGetFoldersByFolderIdSuccess(Object obj, long catId);

    void onGetFoldersByFolderIdFailed(String msg, Exception e);

    void onFolderMoveSuccess(Object obj);

    void onFolderMoveFailed(String msg, Exception e);

}
