package com.thinkernote.ThinkerNote._constructer.listener.v;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;

/**
 * v层 只有一个接口 通用回调
 */
public interface OnSynchronizeEditListener {
    //2-5
    void onSyncNewNotePicSuccess2(Object obj, int picPos, int picArry, int notePos, int noteArry, TNNoteAtt tnNoteAtt);

    void onSyncNewNotePicFailed2(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry);

    //2-6
    void onSyncNewNoteAddSuccess2(Object obj, int position, int arraySize, boolean isNewDb);

    void onSyncNewNoteAddFailed2(String msg, Exception e, int position, int arraySize);

    //2-7
    void onSyncRecoverySuccess2(Object obj, long noteId, int position);

    void onSyncRecoveryFailed2(String msg, Exception e);

    //2-7
    void onSyncRecoveryNotePicSuccess2(Object obj, int picPos, int picArry, int notePos, int noteArry, TNNoteAtt tnNoteAtt);

    void onSyncRecoveryNotePicFailed2(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry);

    //2-7
    void onSyncRecoveryNoteAddSuccess2(Object obj, int position, int arraySize, boolean isNewDb);

    void onSyncRecoveryNoteAddFailed2(String msg, Exception e, int position, int arraySize);

    //2-8
    void onSyncDeleteNoteSuccess2(Object obj, long noteId, int position);

    void onSyncDeleteNoteFailed2(String msg, Exception e);


    //2-9-1
    void onSyncpDeleteRealNotes1Success2(Object obj, long noteId, int position);

    void onSyncDeleteRealNotes1Failed2(String msg, Exception e, int position);


    //2-9-2
    void onSyncDeleteRealNotes2Success2(Object obj, long noteId, int position);

    void onSyncDeleteRealNotes2Failed2(String msg, Exception e, int position);

    //2-10
    void onSyncAllNotesIdSuccess2(Object obj);

    void onSyncAllNotesIdAddFailed2(String msg, Exception e);

    //2-10-1
    void onSyncEditNotePicSuccess2(Object obj, int cloudsPos, int attsPos, TNNote tnNote);

    void onSyncEditNotePicFailed2(String msg, Exception e, int cloudsPos, int attsPos, TNNote tnNote);

    //2-11-1
    void onSyncEditNoteSuccess2(Object obj, int position, TNNote note);

    void onSyncEditNoteAddFailed2(String msg, Exception e);

    //2-11-2
    void onSyncpGetNoteByNoteIdSuccess2(Object obj, int position, boolean is12);

    void onSyncpGetNoteByNoteIdFailed2(String msg, Exception e);


    //2-12
    void onSyncpGetAllTrashNoteIdsSuccess2(Object obj);

    void onSyncpGetAllTrashNoteIdsFailed2(String msg, Exception e);

}
