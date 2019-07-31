package com.thinkernote.ThinkerNote._constructer.listener.v;

import com.thinkernote.ThinkerNote.Data.TNNoteAtt;

import java.util.Vector;

/**
 * v层 只有一个接口 通用回调
 */
public interface OnNoteListListener {
    void onListByFolderIdSuccess(Object obj, long mListDetail, final int mPageNum, final int pageSize, String sort);

    void onListByFolderIdFailed(String msg, Exception e);

    void onNoteListByTagIdSuccess(Object obj, long mListDetail, final int mPageNum, final int pageSize, String sort);

    void onNoteListByTagIdFailed(String msg, Exception e);

    void onGetDataByNoteIdSuccess(Object obj, long noteId);

    void onGetDataByNoteIdFailed(String msg, Exception e);

    void onSyncNoteAttrSuccess(Object obj, int position,Vector<TNNoteAtt> atts, long noteId);

    void onSyncNoteAttrFailed(String msg, Exception e);

}
