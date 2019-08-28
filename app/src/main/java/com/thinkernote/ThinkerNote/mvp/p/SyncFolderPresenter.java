package com.thinkernote.ThinkerNote.mvp.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.mvp.listener.m.INoteModuleListener;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnSyncListener;
import com.thinkernote.ThinkerNote.mvp.m.NoteModule;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.NoteListBean;

import java.util.List;
import java.util.Vector;

/**
 * 同步块
 * 同步folder下的所有笔记
 */
public class SyncFolderPresenter implements INoteModuleListener {
    private static final String TAG = "MainPresenter";
    private Context context;
    private OnSyncListener onView;
    private long folderId;

    //p层调用M层方法
    private NoteModule noteModule;

    private List<AllNotesIdsBean.NoteIdItemBean> all_note_ids;//获取所有笔记的id（12）
    private List<AllNotesIdsBean.NoteIdItemBean> trash_note_ids;//获取所有回收站笔记id（15）

    public SyncFolderPresenter(Context context, OnSyncListener logListener) {
        this.context = context;
        this.onView = logListener;
        noteModule = new NoteModule(context);
    }

    public void SynchronizeFolder(long folderId) {
        this.folderId = folderId;
        updateLocalNotes();
    }

    //============================p层============================

    /**
     * （8）笔记更新：上传本地新增笔记
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     */
    private void updateLocalNotes() {
        MLog.d(TAG, "同步--上传本地folder下新增笔记");
        Vector<TNNote> localNewNotes = TNDbUtils.getNoteListBySyncStateByCatId(TNSettings.getInstance().userId, 3, folderId);
        if (localNewNotes != null && localNewNotes.size() > 0) {
            noteModule.updateLocalNewNotes(localNewNotes, this,false);
        } else {
            //(9)
            updateRecoveryNotes();
        }
    }

    /**
     * （9）笔记更新：还原回收站笔记
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     */
    private void updateRecoveryNotes() {
        MLog.d(TAG, "同步--还原回收站笔记");
        Vector<TNNote> recoveryNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 7);
        if (recoveryNotes != null && recoveryNotes.size() > 0) {
            noteModule.updateRecoveryNotes(recoveryNotes, this,false);
        } else {
            //（10）
            deleteNotes();
        }
    }

    /**
     * （10）笔记更新：删除到回收站/完全删除
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     */
    private void deleteNotes() {
        MLog.d(TAG, "同步--删除到回收站");
        Vector<TNNote> mDeleteNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 6);
        if (mDeleteNotes != null && mDeleteNotes.size() > 0) {
            noteModule.deleteNotes(mDeleteNotes, this,false);
        } else {
            //（11）
            clearNotes();
        }
    }

    /**
     * （11）笔记更新：彻底删除
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     */
    private void clearNotes() {
        MLog.d(TAG, "同步--彻底删除");
        Vector<TNNote> mClaerNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 5);
        if (mClaerNotes != null && mClaerNotes.size() > 0) {
            noteModule.clearNotes(mClaerNotes, this,false);
        } else {
            //（12）
            getAllNotsId();
        }
    }

    /**
     * （12）获取文件夹下的所有笔记id（和SyncPresenter该处不同）
     */
    private void getAllNotsId() {
        MLog.d(TAG, "同步--获取所有笔记id");
        noteModule.getAllNotesId(folderId, this);
    }


    /**
     * (13)云端的编辑笔记 同步（12的子步骤）
     * <p>
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     */
    private void updateEditNote() {
        MLog.d(TAG, "同步--编辑笔记");
        Vector<TNNote> editNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 4);
        if (editNotes != null && editNotes.size() > 0 && all_note_ids != null && all_note_ids.size() > 0) {
            noteModule.updateEditNotes(all_note_ids, editNotes, this,false);
        } else {
            //(14)
            updateCloudNote();
        }
    }

    /**
     * （14）云端笔记同步到本地（12的子步骤）
     */
    private void updateCloudNote() {
        MLog.d(TAG, "同步--云端笔记同步到本地");
        Vector<TNNote> allNotes = TNDbUtils.getNoteListByCatId(TNSettings.getInstance().userId, folderId, TNSettings.getInstance().sort, TNConst.MAX_PAGE_SIZE);
        if (all_note_ids != null && all_note_ids.size() > 0 && allNotes != null && allNotes.size() > 0) {
            noteModule.getCloudNoteByFolderId(all_note_ids, folderId, this);
        } else {
            //（15）
            onView.onSyncSuccess("同步成功");
        }
    }


    //============================================================================
    //==========================接口结果回调,再传递给UI==============================
    //============================================================================

    //===========同步块的回调,如下会按编号都调用=========

    //===============================note相关回调=============================


    //（8）同步本地数据 3
    @Override
    public void onUpdateLocalNoteSuccess() {
        //(9)
        updateRecoveryNotes();
    }

    @Override
    public void onUpdateLocalNoteFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
    }

    //（9）还原回收站笔记结果
    @Override
    public void onUpdateRecoveryNoteSuccess() {
        //10
        MLog.d(TAG, "还原回收站笔记-成功");
        deleteNotes();
    }

    @Override
    public void onUpdateRecoveryNoteFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
    }

    //(10)删除笔记
    @Override
    public void onDeleteNoteSuccess() {
        //(11) 彻底删除的笔记
        clearNotes();
    }

    @Override
    public void onDeleteNoteFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
    }


    //（11）彻底删除的笔记
    @Override
    public void onClearNoteSuccess() {
        //（12）
        getAllNotsId();
    }

    @Override
    public void onClearNoteFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
    }

    // （12）获取所有笔记ID，用于同步
    @Override
    public void onGetAllNoteIdSuccess() {
        //(13) 更新编辑的笔记
        updateEditNote();
    }

    //(12)
    @Override
    public void onGetAllNoteIdNext(AllNotesIdsBean bean) {
        //(可为空)
        all_note_ids = bean.getNote_ids();
    }

    @Override
    public void onGetAllNoteIdFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
    }


    //(13)同步编辑笔记
    @Override
    public void onUpdateEditNoteSuccess() {
        //（14）加载云端笔记
        updateCloudNote();
    }

    @Override
    public void onUpdateEditNoteFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
    }

    //(14)加载云端笔记
    @Override
    public void onCloudNoteSuccess() {
        onView.onSyncSuccess("同步成功");
    }

    @Override
    public void onCloudNoteFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
    }


    //=====================同步块不走如下回调============================

    // (3)同步老数据
    @Override
    public void onUpdateOldNoteSuccess() {
        //(4)获取所有文件夹数据
    }

    @Override
    public void onUpdateOldNoteFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
    }

    //
    @Override
    public void onNoteListByIdSuccess() {

    }

    @Override
    public void onNoteListByIdNext(NoteListBean bean) {

    }

    @Override
    public void onNoteListByIdFailed(Exception e, String msg) {

    }

    @Override
    public void onDownloadNoteSuccess() {

    }

    @Override
    public void onDownloadNoteFailed(Exception e, String msg) {

    }

    //(15)获取回收站笔记id
    @Override
    public void onGetTrashNoteIdSuccess() {
    }

    @Override
    public void onGetTrashNoteIdNext(AllNotesIdsBean bean) {
    }

    @Override
    public void onGetTrashNoteIdFailed(Exception e, String msg) {
    }

    //（16）同步的最后一个接口
    @Override
    public void onGetTrashNoteSuccess() {
    }

    @Override
    public void onGetTrashNoteFailed(Exception e, String msg) {
    }


}
