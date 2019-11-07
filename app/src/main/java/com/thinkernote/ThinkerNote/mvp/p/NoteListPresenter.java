package com.thinkernote.ThinkerNote.mvp.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.mvp.listener.m.INoteModelListener;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnNoteListListener;
import com.thinkernote.ThinkerNote.mvp.m.NoteModel;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.NoteListBean;

/**
 * 注册 p层 具体实现
 */
public class NoteListPresenter implements INoteModelListener {
    private static final String TAG = "MainPresenter";
    private OnNoteListListener onView;
    //p层调用M层方法

    private NoteModel noteModel;

    public NoteListPresenter( OnNoteListListener onNoteListListener) {
        this.onView = onNoteListListener;
        noteModel = new NoteModel();
    }


    //============================p层（非同步块）============================

    /**
     * 单独调用，获取文件夹下的笔记列表
     */
    public void getNoteListByFolderid(long tagId, int mPageNum, int size, String sort) {
        noteModel.getNoteListByFolderId(tagId, mPageNum, size, sort, this);
    }

    /**
     * 单独调用，获取tag下的笔记列表
     */
    public void getNoteListByTagId(long tagId, int mPageNum, int size, String sort) {
        noteModel.getNoteListByTagId(tagId, mPageNum, size, sort, this);
    }

    /**
     * 单独调用，获取tag下的笔记列表
     */
    public void getDetailByNoteId(long noteId) {
        noteModel.getDetailByNoteId(noteId, this);
    }


    //============================================================================
    //==========================接口结果回调,再传递给UI==============================
    //============================================================================


    // 获取tag下/文件夹下 笔记列表（非同步块回调）
    @Override
    public void onNoteListByIdSuccess() {
        onView.onNoteListByIdSuccess();
    }

    @Override
    public void onNoteListByIdNext(NoteListBean bean) {
        onView.onNoteListByIdNext(bean);
    }

    @Override
    public void onNoteListByIdFailed(Exception e, String msg) {
        onView.onNoteListByIdFailed(e, msg);
    }

    //笔记详情下载保存
    @Override
    public void onDownloadNoteSuccess() {
        onView.onDownloadNoteSuccess();
    }

    @Override
    public void onDownloadNoteFailed(Exception e, String msg) {
        onView.onDownloadNoteFailed(e, msg);
    }


    //==========================接口结果回调,如下不使用==============================
    @Override
    public void onUpdateOldNoteSuccess() {

    }

    @Override
    public void onUpdateOldNoteFailed(Exception e, String msg) {

    }

    @Override
    public void onUpdateLocalNoteSuccess() {

    }

    @Override
    public void onUpdateLocalNoteFailed(Exception e, String msg) {

    }

    @Override
    public void onUpdateRecoveryNoteSuccess() {

    }

    @Override
    public void onUpdateRecoveryNoteFailed(Exception e, String msg) {

    }

    @Override
    public void onDeleteNoteSuccess() {

    }

    @Override
    public void onDeleteNoteFailed(Exception e, String msg) {

    }

    @Override
    public void onUpdateEditNoteSuccess() {

    }

    @Override
    public void onUpdateEditNoteFailed(Exception e, String msg) {

    }

    @Override
    public void onClearNoteSuccess() {

    }

    @Override
    public void onClearNoteFailed(Exception e, String msg) {

    }

    @Override
    public void onGetAllNoteIdSuccess() {

    }

    @Override
    public void onGetAllNoteIdNext(AllNotesIdsBean bean) {

    }

    @Override
    public void onGetAllNoteIdFailed(Exception e, String msg) {

    }

    @Override
    public void onGetTrashNoteIdSuccess() {

    }

    @Override
    public void onGetTrashNoteIdNext(AllNotesIdsBean bean) {

    }

    @Override
    public void onGetTrashNoteIdFailed(Exception e, String msg) {

    }

    @Override
    public void onGetTrashNoteSuccess() {

    }

    @Override
    public void onGetTrashNoteFailed(Exception e, String msg) {

    }

    @Override
    public void onCloudNoteSuccess() {

    }

    @Override
    public void onCloudNoteFailed(Exception e, String msg) {

    }


}
