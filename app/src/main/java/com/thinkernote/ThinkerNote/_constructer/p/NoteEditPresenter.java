package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote._constructer.m.NoteEditModule;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnNoteEditListener;

/**
 * 登录 p层 具体实现
 */
public class NoteEditPresenter implements  OnNoteEditListener {
    private Context context;
    private OnNoteEditListener onView;
    //p层调用M层方法
    private NoteEditModule module;

    public NoteEditPresenter(Context context, OnNoteEditListener logListener) {
        this.context = context;
        this.onView = logListener;
        module = new NoteEditModule(context);
    }


    //============================p层重写，用于调用m层方法============================

    //2-5
    public void pNewNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        module.mNewNotePic(this, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    //2-6
    public void pNewNote(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {
        module.mNewNote(this, position, arraySize, tnNote, isNewDb, content);
    }

    //2-7-1
    public void pRecoveryNote(long noteID, int position, int arrySize) {
        module.mRecoveryNote(this, noteID, position, arrySize);
    }

    //2-7-2
    public void pRecoveryNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        module.mRecoveryNotePic(this, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    //2-7-3
    public void pRecoveryNoteAdd(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {
        module.mRecoveryNoteAdd(this, position, arraySize, tnNote, isNewDb, content);
    }

    //2-8
    public void pDeleteNote(long noteId, int position) {
        module.mDeleteNote(this, noteId, position);
    }

    //2-9
    public void pDeleteRealNotes(long noteId, int position) {
        module.mDeleteRealNotes(this, noteId, position);
    }

    //2-10
    public void pGetAllNotesId() {
        module.mGetAllNotesId(this);
    }

    //2-10-1
    public void pEditNotePic(int cloudsPos, int attrPos, TNNote note) {
        module.mEditNotePic(this, cloudsPos, attrPos, note);

    }

    //2-11-1
    public void pEditNote(int position, TNNote note) {
        //
        if (note.catId == -1) {
            note.catId = TNSettings.getInstance().defaultCatId;
        }

        //m层
        module.mEditNote(this, position, note);

    }

    //2-11-2
    public void pGetNoteByNoteId(int position, long noteId, boolean is12) {
        module.mGetNoteByNoteId(this, position, noteId, is12);
    }

    //==========================接口结果回调==============================

    //2-5
    @Override
    public void onSyncNewNotePicSuccess(Object obj, int picPos, int picArry, int notePos, int noteArry, TNNoteAtt tnNoteAtt) {
        onView.onSyncNewNotePicSuccess(obj, picPos, picArry, notePos, noteArry, tnNoteAtt);
    }

    @Override
    public void onSyncNewNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
        onView.onSyncNewNotePicFailed(msg, e, picPos, picArry, notePos, noteArry);


    }

    //2-6
    @Override
    public void onSyncNewNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {
        onView.onSyncNewNoteAddSuccess(obj, position, arraySize, isNewDb);
    }

    @Override
    public void onSyncNewNoteAddFailed(String msg, Exception e, int position, int arraySize) {
        onView.onSyncNewNoteAddFailed(msg, e, position, arraySize);
    }

    //2-7-1
    @Override
    public void onSyncRecoverySuccess(Object obj, long noteId, int position) {
        onView.onSyncRecoverySuccess(obj, noteId, position);
    }

    @Override
    public void onSyncRecoveryFailed(String msg, Exception e) {
        onView.onSyncRecoveryFailed(msg, e);
    }

    //2-7-2
    @Override
    public void onSyncRecoveryNotePicSuccess(Object obj, int picPos, int picArry, int notePos, int noteArry, TNNoteAtt tnNoteAtt) {
        onView.onSyncRecoveryNotePicSuccess(obj, picPos, picArry, notePos, noteArry, tnNoteAtt);
    }

    @Override
    public void onSyncRecoveryNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
        onView.onSyncRecoveryNotePicFailed(msg, e, picPos, picArry, notePos, noteArry);

    }

    //2-7-3
    @Override
    public void onSyncRecoveryNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {
        onView.onSyncRecoveryNoteAddSuccess(obj, position, arraySize, isNewDb);
    }

    @Override
    public void onSyncRecoveryNoteAddFailed(String msg, Exception e, int position, int arraySize) {
        onView.onSyncRecoveryNoteAddFailed(msg, e, position, arraySize);
    }

    //2-8
    @Override
    public void onSyncDeleteNoteSuccess(Object obj, long noteId, int position) {
        onView.onSyncDeleteNoteSuccess(obj, noteId, position);
    }

    @Override
    public void onSyncDeleteNoteFailed(String msg, Exception e) {
        onView.onSyncDeleteNoteFailed(msg, e);
    }

    //2-9-1
    @Override
    public void onSyncpDeleteRealNotes1Success(Object obj, long noteId, int position) {
        onView.onSyncpDeleteRealNotes1Success(obj, noteId, position);
    }

    @Override
    public void onSyncDeleteRealNotes1Failed(String msg, Exception e, int position) {
        onView.onSyncDeleteRealNotes1Failed(msg, e, position);
    }

    //2-9-2
    @Override
    public void onSyncDeleteRealNotes2Success(Object obj, long noteId, int position) {
        onView.onSyncDeleteRealNotes2Success(obj, noteId, position);
    }

    @Override
    public void onSyncDeleteRealNotes2Failed(String msg, Exception e, int position) {
        onView.onSyncDeleteRealNotes2Failed(msg, e, position);
    }

    //2-10
    @Override
    public void onSyncAllNotesIdSuccess(Object obj) {
        onView.onSyncAllNotesIdSuccess(obj);
    }

    @Override
    public void onSyncAllNotesIdAddFailed(String msg, Exception e) {
        onView.onSyncAllNotesIdAddFailed(msg, e);
    }

    //2-10-1
    @Override
    public void onSyncEditNotePicSuccess(Object obj, int cloudsPos, int attsPos, TNNote tnNote) {
        onView.onSyncEditNotePicSuccess(obj, cloudsPos, attsPos, tnNote);
    }

    @Override
    public void onSyncEditNotePicFailed(String msg, Exception e, int cloudsPos, int attsPos, TNNote tnNote) {
        onView.onSyncEditNotePicFailed(msg, e, cloudsPos, attsPos, tnNote);
    }


    //2-11-1
    @Override
    public void onSyncEditNoteSuccess(Object obj, int position, TNNote note) {
        onView.onSyncEditNoteSuccess(obj, position, note);
    }

    @Override
    public void onSyncEditNoteAddFailed(String msg, Exception e) {
        onView.onSyncEditNoteAddFailed(msg, e);
    }

    //2-11-2
    @Override
    public void onSyncpGetNoteByNoteIdSuccess(Object obj, int position, boolean is12) {
        onView.onSyncpGetNoteByNoteIdSuccess(obj, position, is12);
    }

    @Override
    public void onSyncpGetNoteByNoteIdFailed(String msg, Exception e) {
        onView.onSyncpGetNoteByNoteIdFailed(msg, e);
    }
}
