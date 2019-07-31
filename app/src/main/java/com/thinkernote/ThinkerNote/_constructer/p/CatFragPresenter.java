package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.m.CatFragModule;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnCatFragListener;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnSynchronizeDataListener;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;

import java.util.List;

/**
 * p层 具体实现
 */
public class CatFragPresenter implements OnCatFragListener {
    private Context context;
    private OnCatFragListener onView;
    private OnSynchronizeDataListener onDataView;
    //p层调用M层方法
    private CatFragModule module;

    public CatFragPresenter(Context context, OnCatFragListener listener, OnSynchronizeDataListener syncListener) {
        this.context = context;
        this.onView = listener;
        this.onDataView = syncListener;
        module = new CatFragModule(context);
    }


    //============================p层重写，用于调用m层方法============================

    public void pGetParentFolder() {
        module.mGetParentFolder(this);
    }

    public void pGetFolderByFolderId(long catId) {
        module.mGetFolderByFolderId(this, catId);
    }

    public void pGetNoteListByTrash(int pagerSize, int pagenum, String sortType) {
        module.mGetNoteListByTrash(this, pagerSize, pagenum, sortType);
    }

    public void pGetNoteListByFolderId(long folderId, int pagerSize, int pagenum, String sortType) {
        module.mGetNotelistByFolderId(this, folderId, pagerSize, pagenum, sortType);
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
        MLog.d("2-10-1 presenter实现");
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


    //==========================结果回调==============================
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
    //===========================其他=============================

    @Override
    public void onGetParentFolderSuccess(Object obj) {
        onView.onGetParentFolderSuccess(obj);
    }

    @Override
    public void onGetParentFolderFailed(String msg, Exception e) {
        onView.onGetParentFolderFailed(msg, e);
    }

    @Override
    public void onGetFolderByFolderIdSuccess(Object obj, long catId) {
        onView.onGetFolderByFolderIdSuccess(obj, catId);
    }

    @Override
    public void onGetFolderByFolderIdFailed(String msg, Exception e) {
        onView.onGetFolderByFolderIdFailed(msg, e);
    }

    @Override
    public void onGetNoteListByTrashSuccess(Object obj, int pageNum, String sortType) {
        onView.onGetNoteListByTrashSuccess(obj, pageNum, sortType);
    }

    @Override
    public void onGetNoteListByTrashFailed(String msg, Exception e) {
        onView.onGetNoteListByTrashFailed(msg, e);
    }

    @Override
    public void onGetNoteListByFolderIdSuccess(Object obj, long folderid, int pageNum, String sortType) {
        onView.onGetNoteListByFolderIdSuccess(obj, folderid, pageNum, sortType);
    }

    @Override
    public void onGetNoteListByFolderIdFailed(String msg, Exception e) {
        onView.onGetNoteListByFolderIdFailed(msg, e);
    }

}
