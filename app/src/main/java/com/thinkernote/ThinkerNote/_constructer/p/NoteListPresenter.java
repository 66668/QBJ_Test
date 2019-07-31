package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote._constructer.m.NoteListModule;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnNoteListListener;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnSynchronizeDataListener;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnSynchronizeEditListener;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;

import java.util.List;
import java.util.Vector;

/**
 * p层 具体实现
 * syncData+syncEdit+OnNoteListListener
 */
public class NoteListPresenter implements OnNoteListListener {
    private Context context;
    private OnNoteListListener onView;
    private OnSynchronizeDataListener onDataView;
    private OnSynchronizeEditListener onEditView;
    //p层调用M层方法
    private NoteListModule module;

    public NoteListPresenter(Context context, OnNoteListListener logListener, OnSynchronizeDataListener onDataView, OnSynchronizeEditListener onEditView) {
        this.context = context;
        this.onView = logListener;
        this.onDataView = onDataView;
        this.onEditView = onEditView;

        module = new NoteListModule(context);
    }


    //============================p层重写，用于调用m层方法============================

    public void pGetNoteListByFolderID(long mListDetail, int mPageNum, int size, String sort) {
        module.mGetNotelistByFolderId(this, mListDetail, mPageNum, size, sort);
    }

    public void pGetNoteListByTagID(long mListDetail, int mPageNum, int size, String sort) {
        module.mGetNotelistByTagId(this, mListDetail, mPageNum, size, sort);
    }

    //
    public void pGetDataByNoteId(long noteId) {
        module.mGetDataByNoteId(this, noteId);
    }

    public void pSynceNoteAttr(int pos, TNNoteAtt attr, Vector<TNNoteAtt> attrs, long noteId) {
        module.mSynceNoteAttr(this, pos, attr, attrs, noteId);
    }


    //=========================================syncData===========================================

    //=第一次登录的同步方法=
    //1
    public void folderAdd(int position, int arraySize, String folderName) {
        module.mfolderAdd(dataListener, position, arraySize, folderName);
    }

    //2
    public void tagAdd(int position, int arraySize, String tagName) {
        module.mTagAdd(dataListener, position, arraySize, tagName);
    }

    //3
    public void pGetFolder() {
        module.GetFolder(dataListener);
    }


    //4
    public void pGetFoldersByFolderId(long id, int position, List<AllFolderItemBean> beans) {
        module.mGetFoldersByFolderId(dataListener, id, position, beans);
    }

    //5
    public void pFirstFolderAdd(int workPos, int workSize, long catID, String name, int catPos, int flag) {
        module.mFirstFolderAdd(dataListener, workPos, workSize, catID, name, catPos, flag);
    }


    //=正常登录的同步方法=

    //2-1
    public void pProfile() {
        module.mProfile(dataListener);
    }


    //2-2
    public void pUploadOldNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        module.mUploadOldNotePic(dataListener, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }


    //2-3
    public void pOldNoteAdd(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {
        module.mOldNoteAdd(dataListener, position, arraySize, tnNote, isNewDb, content);
    }


    //2-4
    public void pGetTagList() {
        module.mGetTagList(dataListener);
    }

    //2-5
    public void pNewNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        module.mNewNotePic(dataListener, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    //2-6
    public void pNewNote(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {
        module.mNewNote(dataListener, position, arraySize, tnNote, isNewDb, content);
    }

    //2-7-1
    public void pRecoveryNote(long noteID, int position, int arrySize) {
        module.mRecoveryNote(dataListener, noteID, position, arrySize);
    }

    //2-7-2
    public void pRecoveryNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        module.mRecoveryNotePic(dataListener, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    //2-7-3
    public void pRecoveryNoteAdd(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {
        module.mRecoveryNoteAdd(dataListener, position, arraySize, tnNote, isNewDb, content);
    }

    //2-8
    public void pDeleteNote(long noteId, int position) {
        module.mDeleteNote(dataListener, noteId, position);
    }

    //2-9
    public void pDeleteRealNotes(long noteId, int position) {
        module.mDeleteRealNotes(dataListener, noteId, position);
    }

    //2-10
    public void pGetAllNotesId() {
        module.mGetAllNotesId(dataListener);
    }

    //2-10-1
    public void pEditNotePic(int cloudsPos, int attrPos, TNNote note) {
        module.mEditNotePic(dataListener, cloudsPos, attrPos, note);

    }

    //2-11-1
    public void pEditNote(int position, TNNote note) {
        //
        if (note.catId == -1) {
            note.catId = TNSettings.getInstance().defaultCatId;
        }

        //m层
        module.mEditNote(dataListener, position, note);

    }

    //2-11-2
    public void pGetNoteByNoteId(int position, long noteId, boolean is12) {
        module.mGetNoteByNoteId(dataListener, position, noteId, is12);
    }

    public void pGetAllTrashNoteIds() {
        module.mGetAllTrashNoteIds(dataListener);
    }


    //=========================================syncEdit===========================================

    //2-5
    public void pNewNotePic2(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        module.mNewNotePic(editListener, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    //2-6
    public void pNewNote2(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {
        module.mNewNote(editListener, position, arraySize, tnNote, isNewDb, content);
    }

    //2-7-1
    public void pRecoveryNote2(long noteID, int position, int arrySize) {
        module.mRecoveryNote(editListener, noteID, position, arrySize);
    }

    //2-7-2
    public void pRecoveryNotePic2(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        module.mRecoveryNotePic(editListener, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    //2-7-3
    public void pRecoveryNoteAdd2(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content) {
        module.mRecoveryNoteAdd(editListener, position, arraySize, tnNote, isNewDb, content);
    }

    //2-8
    public void pDeleteNote2(long noteId, int position) {
        module.mDeleteNote(editListener, noteId, position);
    }

    //2-9
    public void pDeleteRealNotes2(long noteId, int position) {
        module.mDeleteRealNotes(editListener, noteId, position);
    }

    //2-10
    public void pGetAllNotesId2() {
        module.mGetAllNotesId(editListener);
    }

    //2-10-1
    public void pEditNotePic2(int cloudsPos, int attrPos, TNNote note) {
        module.mEditNotePic(editListener, cloudsPos, attrPos, note);

    }

    //2-11-1
    public void pEditNote2(int position, TNNote note) {
        //
        if (note.catId == -1) {
            note.catId = TNSettings.getInstance().defaultCatId;
        }

        //m层
        module.mEditNote(editListener, position, note);

    }

    //2-11-2
    public void pGetNoteByNoteId2(int position, long noteId, boolean is12) {
        module.mGetNoteByNoteId(editListener, position, noteId, is12);
    }

    public void pGetAllTrashNoteIds2() {
        module.mGetAllTrashNoteIds(editListener);
    }

    //==========================结果回调==============================

    //1-1
    @Override
    public void onListByFolderIdSuccess(Object obj, long tagId, int mPageNum, int pageSize, String sort) {
        onView.onListByFolderIdSuccess(obj, tagId, mPageNum, pageSize, sort);
    }

    @Override
    public void onListByFolderIdFailed(String msg, Exception e) {
        onView.onListByFolderIdFailed(msg, e);
    }

    //1-2
    @Override
    public void onNoteListByTagIdSuccess(Object obj, long tagId, int mPageNum, int pageSize, String sort) {
        onView.onNoteListByTagIdSuccess(obj, tagId, mPageNum, pageSize, sort);
    }

    @Override
    public void onNoteListByTagIdFailed(String msg, Exception e) {
        onView.onNoteListByTagIdFailed(msg, e);
    }

    //==========================getDataByNoteId==============================
    @Override
    public void onGetDataByNoteIdSuccess(Object obj, long noteId) {
        onView.onGetDataByNoteIdSuccess(obj, noteId);
    }

    @Override
    public void onGetDataByNoteIdFailed(String msg, Exception e) {
        onView.onGetDataByNoteIdFailed(msg, e);
    }

    @Override
    public void onSyncNoteAttrSuccess(Object obj, int position, Vector<TNNoteAtt> atts, long noteId) {
        onView.onSyncNoteAttrSuccess(obj, position, atts, noteId);
    }

    @Override
    public void onSyncNoteAttrFailed(String msg, Exception e) {
        onView.onSyncNoteAttrFailed(msg, e);
    }

    //==========================syncData==============================
    DataListener dataListener = new DataListener();

    class DataListener implements OnSynchronizeDataListener {

        //1 创建folder
        @Override
        public void onSyncFolderAddSuccess(Object obj, int position, int arraySize) {
            onDataView.onSyncFolderAddSuccess(obj, position, arraySize);
        }

        @Override
        public void onSyncFolderAddFailed(String msg, Exception e, int position, int arraySize) {
            onDataView.onSyncFolderAddFailed(msg, e, position, arraySize);
        }

        //2 创建tag
        @Override
        public void onSyncTagAddSuccess(Object obj, int position, int arraySize) {
            onDataView.onSyncTagAddSuccess(obj, position, arraySize);
        }

        @Override
        public void onSyncTagAddFailed(String msg, Exception e, int position, int arraySize) {
            onDataView.onSyncTagAddFailed(msg, e, position, arraySize);
        }

        //3 getAllFolder
        @Override
        public void onSyncGetFolderSuccess(Object obj) {
            onDataView.onSyncGetFolderSuccess(obj);
        }

        @Override
        public void onSyncGetFolderFailed(String msg, Exception e) {
            onDataView.onSyncGetFolderFailed(msg, e);
        }

        //4
        @Override
        public void onSyncGetFoldersByFolderIdSuccess(Object obj, long id, int position, List<AllFolderItemBean> beans) {
            onDataView.onSyncGetFoldersByFolderIdSuccess(obj, id, position, beans);
        }

        @Override
        public void onSyncGetFoldersByFolderIdFailed(String msg, Exception e, long id, int position, List<AllFolderItemBean> beans) {
            onDataView.onSyncGetFoldersByFolderIdFailed(msg, e, id, position, beans);
        }

        //5 TNCat
        @Override
        public void onSyncFirstFolderAddSuccess(Object obj, int workPos, int workSize, long catID, String name, int catPos, int flag) {
            onDataView.onSyncFirstFolderAddSuccess(obj, workPos, workSize, catID, name, catPos, flag);
        }

        @Override
        public void onSyncFirstFolderAddFailed(String msg, Exception e, int workPos, int workSize, long catID, String name, int catPos, int flag) {
            onDataView.onSyncFirstFolderAddFailed(msg, e, workPos, workSize, catID, name, catPos, flag);
        }


        //===正常登录的回调=
        //2-1
        @Override
        public void onSyncProfileSuccess(Object obj) {
            onDataView.onSyncProfileSuccess(obj);
        }

        @Override
        public void onSyncProfileAddFailed(String msg, Exception e) {
            onDataView.onSyncProfileAddFailed(msg, e);
        }

        //2-2 pic
        @Override
        public void onSyncOldNotePicSuccess(Object obj, int picPos, int picArry, int notePos, int noteArry, TNNoteAtt tnNoteAtt) {
            onDataView.onSyncOldNotePicSuccess(obj, picPos, picArry, notePos, noteArry, tnNoteAtt);
        }

        @Override
        public void onSyncOldNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
            onDataView.onSyncOldNotePicFailed(msg, e, picPos, picArry, notePos, noteArry);
        }

        //2-3 oldNoteAdd
        @Override
        public void onSyncOldNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {
            onDataView.onSyncOldNoteAddSuccess(obj, position, arraySize, isNewDb);
        }

        @Override
        public void onSyncOldNoteAddFailed(String msg, Exception e, int position, int arraySize) {
            onDataView.onSyncOldNoteAddFailed(msg, e, position, arraySize);
        }

        //2-4
        @Override
        public void onSyncTagListSuccess(Object obj) {
            onDataView.onSyncTagListSuccess(obj);
        }

        @Override
        public void onSyncTagListAddFailed(String msg, Exception e) {
            onDataView.onSyncTagListAddFailed(msg, e);
        }

        //2-5
        @Override
        public void onSyncNewNotePicSuccess(Object obj, int picPos, int picArry, int notePos, int noteArry, TNNoteAtt tnNoteAtt) {
            onDataView.onSyncNewNotePicSuccess(obj, picPos, picArry, notePos, noteArry, tnNoteAtt);
        }

        @Override
        public void onSyncNewNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
            onDataView.onSyncNewNotePicFailed(msg, e, picPos, picArry, notePos, noteArry);


        }

        //2-6
        @Override
        public void onSyncNewNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {
            onDataView.onSyncNewNoteAddSuccess(obj, position, arraySize, isNewDb);
        }

        @Override
        public void onSyncNewNoteAddFailed(String msg, Exception e, int position, int arraySize) {
            onDataView.onSyncNewNoteAddFailed(msg, e, position, arraySize);
        }

        //2-7-1
        @Override
        public void onSyncRecoverySuccess(Object obj, long noteId, int position) {
            onDataView.onSyncRecoverySuccess(obj, noteId, position);
        }

        @Override
        public void onSyncRecoveryFailed(String msg, Exception e) {
            onDataView.onSyncRecoveryFailed(msg, e);
        }

        //2-7-2
        @Override
        public void onSyncRecoveryNotePicSuccess(Object obj, int picPos, int picArry, int notePos, int noteArry, TNNoteAtt tnNoteAtt) {
            onDataView.onSyncRecoveryNotePicSuccess(obj, picPos, picArry, notePos, noteArry, tnNoteAtt);
        }

        @Override
        public void onSyncRecoveryNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
            onDataView.onSyncRecoveryNotePicFailed(msg, e, picPos, picArry, notePos, noteArry);

        }

        //2-7-3
        @Override
        public void onSyncRecoveryNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {
            onDataView.onSyncRecoveryNoteAddSuccess(obj, position, arraySize, isNewDb);
        }

        @Override
        public void onSyncRecoveryNoteAddFailed(String msg, Exception e, int position, int arraySize) {
            onDataView.onSyncRecoveryNoteAddFailed(msg, e, position, arraySize);
        }

        //2-8
        @Override
        public void onSyncDeleteNoteSuccess(Object obj, long noteId, int position) {
            onDataView.onSyncDeleteNoteSuccess(obj, noteId, position);
        }

        @Override
        public void onSyncDeleteNoteFailed(String msg, Exception e) {
            onDataView.onSyncDeleteNoteFailed(msg, e);
        }

        //2-9-1
        @Override
        public void onSyncpDeleteRealNotes1Success(Object obj, long noteId, int position) {
            onDataView.onSyncpDeleteRealNotes1Success(obj, noteId, position);
        }

        @Override
        public void onSyncDeleteRealNotes1Failed(String msg, Exception e, int position) {
            onDataView.onSyncDeleteRealNotes1Failed(msg, e, position);
        }

        //2-9-2
        @Override
        public void onSyncDeleteRealNotes2Success(Object obj, long noteId, int position) {
            onDataView.onSyncDeleteRealNotes2Success(obj, noteId, position);
        }

        @Override
        public void onSyncDeleteRealNotes2Failed(String msg, Exception e, int position) {
            onDataView.onSyncDeleteRealNotes2Failed(msg, e, position);
        }

        //2-10
        @Override
        public void onSyncAllNotesIdSuccess(Object obj) {
            onDataView.onSyncAllNotesIdSuccess(obj);
        }

        @Override
        public void onSyncAllNotesIdAddFailed(String msg, Exception e) {
            onDataView.onSyncAllNotesIdAddFailed(msg, e);
        }

        //2-10-1
        @Override
        public void onSyncEditNotePicSuccess(Object obj, int cloudsPos, int attsPos, TNNote tnNote) {
            onDataView.onSyncEditNotePicSuccess(obj, cloudsPos, attsPos, tnNote);
        }

        @Override
        public void onSyncEditNotePicFailed(String msg, Exception e, int cloudsPos, int attsPos, TNNote tnNote) {
            onDataView.onSyncEditNotePicFailed(msg, e, cloudsPos, attsPos, tnNote);
        }


        //2-11-1
        @Override
        public void onSyncEditNoteSuccess(Object obj, int position, TNNote note) {
            onDataView.onSyncEditNoteSuccess(obj, position, note);
        }

        @Override
        public void onSyncEditNoteAddFailed(String msg, Exception e) {
            onDataView.onSyncEditNoteAddFailed(msg, e);
        }

        //2-11-2
        @Override
        public void onSyncpGetNoteByNoteIdSuccess(Object obj, int position, boolean is12) {
            onDataView.onSyncpGetNoteByNoteIdSuccess(obj, position, is12);
        }

        @Override
        public void onSyncpGetNoteByNoteIdFailed(String msg, Exception e) {
            onDataView.onSyncpGetNoteByNoteIdFailed(msg, e);
        }

        //2-12
        @Override
        public void onSyncpGetAllTrashNoteIdsSuccess(Object obj) {
            onDataView.onSyncpGetAllTrashNoteIdsSuccess(obj);
        }

        @Override
        public void onSyncpGetAllTrashNoteIdsFailed(String msg, Exception e) {
            onDataView.onSyncpGetAllTrashNoteIdsFailed(msg, e);
        }
    }

    //==========================syncEdit==============================
    EditListener editListener = new EditListener();

    class EditListener implements OnSynchronizeEditListener {
        //2-5
        @Override
        public void onSyncNewNotePicSuccess2(Object obj, int picPos, int picArry, int notePos, int noteArry, TNNoteAtt tnNoteAtt) {
            onEditView.onSyncNewNotePicSuccess2(obj, picPos, picArry, notePos, noteArry, tnNoteAtt);
        }

        @Override
        public void onSyncNewNotePicFailed2(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
            onEditView.onSyncNewNotePicFailed2(msg, e, picPos, picArry, notePos, noteArry);


        }

        //2-6
        @Override
        public void onSyncNewNoteAddSuccess2(Object obj, int position, int arraySize, boolean isNewDb) {
            onEditView.onSyncNewNoteAddSuccess2(obj, position, arraySize, isNewDb);
        }

        @Override
        public void onSyncNewNoteAddFailed2(String msg, Exception e, int position, int arraySize) {
            onEditView.onSyncNewNoteAddFailed2(msg, e, position, arraySize);
        }

        //2-7-1
        @Override
        public void onSyncRecoverySuccess2(Object obj, long noteId, int position) {
            onEditView.onSyncRecoverySuccess2(obj, noteId, position);
        }

        @Override
        public void onSyncRecoveryFailed2(String msg, Exception e) {
            onEditView.onSyncRecoveryFailed2(msg, e);
        }

        //2-7-2
        @Override
        public void onSyncRecoveryNotePicSuccess2(Object obj, int picPos, int picArry, int notePos, int noteArry, TNNoteAtt tnNoteAtt) {
            onEditView.onSyncRecoveryNotePicSuccess2(obj, picPos, picArry, notePos, noteArry, tnNoteAtt);
        }

        @Override
        public void onSyncRecoveryNotePicFailed2(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
            onEditView.onSyncRecoveryNotePicFailed2(msg, e, picPos, picArry, notePos, noteArry);

        }

        //2-7-3
        @Override
        public void onSyncRecoveryNoteAddSuccess2(Object obj, int position, int arraySize, boolean isNewDb) {
            onEditView.onSyncRecoveryNoteAddSuccess2(obj, position, arraySize, isNewDb);
        }

        @Override
        public void onSyncRecoveryNoteAddFailed2(String msg, Exception e, int position, int arraySize) {
            onEditView.onSyncRecoveryNoteAddFailed2(msg, e, position, arraySize);
        }

        //2-8
        @Override
        public void onSyncDeleteNoteSuccess2(Object obj, long noteId, int position) {
            onEditView.onSyncDeleteNoteSuccess2(obj, noteId, position);
        }

        @Override
        public void onSyncDeleteNoteFailed2(String msg, Exception e) {
            onEditView.onSyncDeleteNoteFailed2(msg, e);
        }

        //2-9-1
        @Override
        public void onSyncpDeleteRealNotes1Success2(Object obj, long noteId, int position) {
            onEditView.onSyncpDeleteRealNotes1Success2(obj, noteId, position);
        }

        @Override
        public void onSyncDeleteRealNotes1Failed2(String msg, Exception e, int position) {
            onEditView.onSyncDeleteRealNotes1Failed2(msg, e, position);
        }

        //2-9-2
        @Override
        public void onSyncDeleteRealNotes2Success2(Object obj, long noteId, int position) {
            onEditView.onSyncDeleteRealNotes2Success2(obj, noteId, position);
        }

        @Override
        public void onSyncDeleteRealNotes2Failed2(String msg, Exception e, int position) {
            onEditView.onSyncDeleteRealNotes2Failed2(msg, e, position);
        }

        //2-10
        @Override
        public void onSyncAllNotesIdSuccess2(Object obj) {
            onEditView.onSyncAllNotesIdSuccess2(obj);
        }

        @Override
        public void onSyncAllNotesIdAddFailed2(String msg, Exception e) {
            onEditView.onSyncAllNotesIdAddFailed2(msg, e);
        }

        //2-10-1
        @Override
        public void onSyncEditNotePicSuccess2(Object obj, int cloudsPos, int attsPos, TNNote tnNote) {
            onEditView.onSyncEditNotePicSuccess2(obj, cloudsPos, attsPos, tnNote);
        }

        @Override
        public void onSyncEditNotePicFailed2(String msg, Exception e, int cloudsPos, int attsPos, TNNote tnNote) {
            onEditView.onSyncEditNotePicFailed2(msg, e, cloudsPos, attsPos, tnNote);
        }


        //2-11-1
        @Override
        public void onSyncEditNoteSuccess2(Object obj, int position, TNNote note) {
            onEditView.onSyncEditNoteSuccess2(obj, position, note);
        }

        @Override
        public void onSyncEditNoteAddFailed2(String msg, Exception e) {
            onEditView.onSyncEditNoteAddFailed2(msg, e);
        }

        //2-11-2
        @Override
        public void onSyncpGetNoteByNoteIdSuccess2(Object obj, int position, boolean is12) {
            onEditView.onSyncpGetNoteByNoteIdSuccess2(obj, position, is12);
        }

        @Override
        public void onSyncpGetNoteByNoteIdFailed2(String msg, Exception e) {
            onEditView.onSyncpGetNoteByNoteIdFailed2(msg, e);
        }

        //2-12
        @Override
        public void onSyncpGetAllTrashNoteIdsSuccess2(Object obj) {
            onEditView.onSyncpGetAllTrashNoteIdsSuccess2(obj);
        }

        @Override
        public void onSyncpGetAllTrashNoteIdsFailed2(String msg, Exception e) {
            onEditView.onSyncpGetAllTrashNoteIdsFailed2(msg, e);
        }
    }
}
