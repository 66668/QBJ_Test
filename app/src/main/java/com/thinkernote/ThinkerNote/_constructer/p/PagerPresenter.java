package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote._constructer.m.PagerModule;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnPagerListener;

import java.util.Vector;

/**
 * 我的笔记 p层 具体实现
 */
public class PagerPresenter implements  OnPagerListener {
    private Context context;
    private OnPagerListener onView;
    //p层调用M层方法
    private PagerModule module;

    public PagerPresenter(Context context, OnPagerListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new PagerModule(context);
    }


    //============================p层重写，用于调用m层方法============================

    public void pSetDefaultFolder(long tagID) {
        module.mSetDefaultFolder(this, tagID);
    }

    public void pDeleteTag(long tagID) {
        module.mDeleteTag(this, tagID);
    }

    public void pDeleteCat(long catID) {
        module.mDeleteFolder(this, catID);
    }


    //
    public void pGetDataByNoteId(long noteId, int catPos, boolean isCats) {
        module.mGetDataByNoteId(this, noteId, catPos, isCats);
    }

    public void pSynceNoteAttr(int pos, TNNoteAtt attr, Vector<TNNoteAtt> attrs, long noteId, int catPos, boolean isCats) {
        module.mSynceNoteAttr(this, pos, attr, attrs, noteId, catPos, isCats);
    }

    //============syncCats===============
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


    //2-10 与main不同
    public void pGetFolderNoteIds(long catId) {
        module.mGetFolderNoteIds(this, catId);
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


    //==========================结果回调==============================


    @Override
    public void onDefaultFolderSuccess(Object obj) {
        onView.onDefaultFolderSuccess(obj);
    }

    @Override
    public void onDefaultFolderFailed(String msg, Exception e) {
        onView.onDefaultFolderFailed(msg, e);
    }

    @Override
    public void onFolderDeleteSuccess(Object obj) {
        onView.onFolderDeleteSuccess(obj);
    }

    @Override
    public void onFolderDeleteFailed(String msg, Exception e) {
        onView.onFolderDeleteFailed(msg, e);
    }

    @Override
    public void onTagDeleteSuccess(Object obj, long tagID) {
        onView.onTagDeleteSuccess(obj, tagID);
    }

    @Override
    public void onTagDeleteFailed(String msg, Exception e) {
        onView.onTagDeleteFailed(msg, e);
    }

    @Override
    public void onDeleteFolderSuccess(Object obj,long catId) {
        onView.onDeleteFolderSuccess(obj,catId);
    }

    @Override
    public void onDeleteFolderFailed(String msg, Exception e) {
        onView.onDeleteFolderFailed(msg, e);
    }


    //==========================getDataByNoteId,2-12-1+2-12-2,共同调使用==============================
    @Override
    public void onGetDataByNoteIdSuccess(Object obj, long noteId, int catPos, boolean isCats) {
        onView.onGetDataByNoteIdSuccess(obj, noteId, catPos, isCats);
    }

    @Override
    public void onGetDataByNoteIdFailed(String msg, Exception e) {
        onView.onGetDataByNoteIdFailed(msg, e);
    }

    @Override
    public void onSyncNoteAttrSuccess(Object obj, int position, Vector<TNNoteAtt> atts, long noteId, int catPos, boolean isCats) {
        onView.onSyncNoteAttrSuccess(obj, position, atts, noteId, catPos, isCats);
    }

    @Override
    public void onSyncNoteAttrFailed(String msg, Exception e) {
        onView.onSyncNoteAttrFailed(msg, e);
    }

    //==========syncCats===============
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

    //2-10 与main不同
    @Override
    public void onpGetFolderNoteIdsSuccess(Object obj) {
        onView.onpGetFolderNoteIdsSuccess(obj);
    }

    @Override
    public void onpGetFolderNoteIdsFailed(String msg, Exception e) {
        onView.onpGetFolderNoteIdsFailed(msg, e);
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
