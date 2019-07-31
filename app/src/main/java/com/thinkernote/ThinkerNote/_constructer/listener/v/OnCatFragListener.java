package com.thinkernote.ThinkerNote._constructer.listener.v;

/**
 * v层 只有一个接口 通用回调
 */
public interface OnCatFragListener {
    void onGetParentFolderSuccess(Object obj);

    void onGetParentFolderFailed(String msg, Exception e);

    void onGetFolderByFolderIdSuccess(Object obj, long catId);

    void onGetFolderByFolderIdFailed(String msg, Exception e);

    void onGetNoteListByTrashSuccess(Object obj, int pageNum, String sortType);

    void onGetNoteListByTrashFailed(String msg, Exception e);

    void onGetNoteListByFolderIdSuccess(Object obj,long folderid, int pageNum, String sortType);

    void onGetNoteListByFolderIdFailed(String msg, Exception e);


}
