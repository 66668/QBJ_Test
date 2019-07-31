package com.thinkernote.ThinkerNote._constructer.listener.v;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;

import java.util.Vector;

/**
 * 我的笔记 v层 只有一个接口 通用回调
 */
public interface OnPagerListener {
    void onDefaultFolderSuccess(Object obj);

    void onDefaultFolderFailed(String msg, Exception e);

    void onFolderDeleteSuccess(Object obj);

    void onFolderDeleteFailed(String msg, Exception e);

    void onTagDeleteSuccess(Object obj, long tagID);

    void onTagDeleteFailed(String msg, Exception e);

    void onDeleteFolderSuccess(Object obj,long catId);

    void onDeleteFolderFailed(String msg, Exception e);

    //==============syncDataByNoteId=================
    //syncDataByNoteId，与2-12-1共用
    void onGetDataByNoteIdSuccess(Object obj, long noteId, int catPos, boolean isCats);

    void onGetDataByNoteIdFailed(String msg, Exception e);

    //    syncDataByNoteId，与2-12-2共用
    void onSyncNoteAttrSuccess(Object obj, int position, Vector<TNNoteAtt> atts, long noteId, int catPos, boolean isCats);

    void onSyncNoteAttrFailed(String msg, Exception e);


    //=================syncCats 开始===================
    //2-5
    void onSyncNewNotePicSuccess(Object obj, int picPos, int picArry, int notePos, int noteArry, TNNoteAtt tnNoteAtt);

    void onSyncNewNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry);

    //2-6
    void onSyncNewNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb);

    void onSyncNewNoteAddFailed(String msg, Exception e, int position, int arraySize);

    //2-7
    void onSyncRecoverySuccess(Object obj, long noteId, int position);

    void onSyncRecoveryFailed(String msg, Exception e);

    //2-7
    void onSyncRecoveryNotePicSuccess(Object obj, int picPos, int picArry, int notePos, int noteArry, TNNoteAtt tnNoteAtt);

    void onSyncRecoveryNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry);

    //2-7
    void onSyncRecoveryNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb);

    void onSyncRecoveryNoteAddFailed(String msg, Exception e, int position, int arraySize);

    //2-8
    void onSyncDeleteNoteSuccess(Object obj, long noteId, int position);

    void onSyncDeleteNoteFailed(String msg, Exception e);


    //2-9-1
    void onSyncpDeleteRealNotes1Success(Object obj, long noteId, int position);

    void onSyncDeleteRealNotes1Failed(String msg, Exception e, int position);


    //2-9-2
    void onSyncDeleteRealNotes2Success(Object obj, long noteId, int position);

    void onSyncDeleteRealNotes2Failed(String msg, Exception e, int position);


    //2-10
    void onpGetFolderNoteIdsSuccess(Object obj);

    void onpGetFolderNoteIdsFailed(String msg, Exception e);

    //2-10-1
    void onSyncEditNotePicSuccess(Object obj, int cloudsPos, int attsPos, TNNote tnNote);

    void onSyncEditNotePicFailed(String msg, Exception e, int cloudsPos, int attsPos, TNNote tnNote);

    //2-11-1
    void onSyncEditNoteSuccess(Object obj, int position, TNNote note);

    void onSyncEditNoteAddFailed(String msg, Exception e);

    //2-11-2
    void onSyncpGetNoteByNoteIdSuccess(Object obj, int position, boolean is12);

    void onSyncpGetNoteByNoteIdFailed(String msg, Exception e);


    //=================syncCats 结束===================
}
