package com.thinkernote.ThinkerNote._constructer.listener.v;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;

import java.util.List;

/**
 *  v层 只有一个接口 通用回调
 */
public interface OnSynchronizeDataListener {

    //第一次登录同步1
    //1
    void onSyncFolderAddSuccess(Object obj, int position, int arraySize);

    void onSyncFolderAddFailed(String msg, Exception e, int position, int arraySize);

    //2
    void onSyncTagAddSuccess(Object obj, int position, int arraySize);

    void onSyncTagAddFailed(String msg, Exception e, int position, int arraySize);

    //3
    void onSyncGetFolderSuccess(Object obj);

    void onSyncGetFolderFailed(String msg, Exception e);

    //4
    void onSyncGetFoldersByFolderIdSuccess(Object obj, long catID, int startPos, List<AllFolderItemBean> beans);

    void onSyncGetFoldersByFolderIdFailed(String msg, Exception e, long catID, int posstartPosition, List<AllFolderItemBean> beans);

    //5
    void onSyncFirstFolderAddSuccess(Object obj, int workPos, int workSize, long catID,String name, int catPos, int flag);

    void onSyncFirstFolderAddFailed(String msg, Exception e, int workPos, int workSize, long catID, String name, int catPos, int flag);


    //正常同步2


    //2-1
    void onSyncProfileSuccess(Object obj);

    void onSyncProfileAddFailed(String msg, Exception e);

    //2-2
    void onSyncOldNotePicSuccess(Object obj, int picPos, int picArry, int notePos, int noteArry, TNNoteAtt tnNoteAtt);

    void onSyncOldNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry);

    //2-3
    void onSyncOldNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb);

    void onSyncOldNoteAddFailed(String msg, Exception e, int position, int arraySize);


    //2-4
    void onSyncTagListSuccess(Object obj);

    void onSyncTagListAddFailed(String msg, Exception e);


    //2-5
    void onSyncNewNotePicSuccess(Object obj, int picPos, int picArry, int notePos, int noteArry,TNNoteAtt tnNoteAtt);

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
    void onSyncAllNotesIdSuccess(Object obj);

    void onSyncAllNotesIdAddFailed(String msg, Exception e);

    //2-10-1
    void onSyncEditNotePicSuccess(Object obj, int cloudsPos, int attsPos, TNNote tnNote);

    void onSyncEditNotePicFailed(String msg, Exception e, int cloudsPos, int attsPos, TNNote tnNote);

    //2-11-1
    void onSyncEditNoteSuccess(Object obj, int position, TNNote note);

    void onSyncEditNoteAddFailed(String msg, Exception e);

    //2-11-2
    void onSyncpGetNoteByNoteIdSuccess(Object obj, int position,boolean is12);

    void onSyncpGetNoteByNoteIdFailed(String msg, Exception e);


    //2-12
    void onSyncpGetAllTrashNoteIdsSuccess(Object obj);

    void onSyncpGetAllTrashNoteIdsFailed(String msg, Exception e);

}
