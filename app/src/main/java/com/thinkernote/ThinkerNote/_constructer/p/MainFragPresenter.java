package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.listener.m.IFolderModuleListener;
import com.thinkernote.ThinkerNote._constructer.listener.m.INoteModuleListener;
import com.thinkernote.ThinkerNote._constructer.listener.m.ITagModuleListener;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnTagInfoListener;
import com.thinkernote.ThinkerNote._constructer.m.FolderModule;
import com.thinkernote.ThinkerNote._constructer.m.NoteModule;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnPagerListener;
import com.thinkernote.ThinkerNote._constructer.m.TagModule;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.NoteListBean;

/**
 * 我的笔记 p层 具体实现
 */
public class MainFragPresenter implements IFolderModuleListener, ITagModuleListener, INoteModuleListener {
    private Context context;
    private OnPagerListener onView;
    //p层调用M层方法
    private NoteModule noteModule;
    private TagModule tagModule;
    private FolderModule folderModule;

    public MainFragPresenter(Context context, OnPagerListener logListener) {
        this.context = context;
        this.onView = logListener;

        noteModule = new NoteModule(context);
        tagModule = new TagModule(context);
        folderModule = new FolderModule(context);
    }

    //============================p层重写，用于调用m层方法============================

    public void setDefaultFolder(long folderId) {
        folderModule.setDefaultFolder(folderId, this);
    }

    public void deleteTag(long tagID) {
        tagModule.deleteTag(tagID, this);
    }

    public void deleteFolder(long catID) {
        folderModule.deleteFolder(catID, this);
    }

    // 同步一条笔记详情
    public void getDetailByNoteId(long noteId) {
        noteModule.getDetailByNoteId(noteId, this);
    }


    //==========================结果回调==============================

    // 删除文件夹
    @Override
    public void onDeleteFolderSuccess() {
        onView.onDeleteFolderSuccess();
    }

    @Override
    public void onDeleteFolderFailed(String msg, Exception e) {
        onView.onDeleteFolderFailed(msg, e);
    }

    // 默认文件夹
    @Override
    public void onDefaultFolderSuccess() {
        onView.onDefaultFolderSuccess();
    }

    @Override
    public void onDeleteTagSuccess() {
        onView.onTagDeleteSuccess();
    }

    @Override
    public void onDeleteTagFailed(Exception e, String msg) {
        onView.onTagDeleteFailed(msg, e);
    }

    @Override
    public void onDefaultFolderFailed(String msg, Exception e) {
        onView.onDefaultFolderFailed(msg, e);
    }

    @Override
    public void onRenameFolderSuccess() {

    }

    @Override
    public void onRenameFolderFailed(Exception e, String msg) {

    }

    //同步一条笔记详情
    @Override
    public void onDownloadNoteSuccess() {
        onView.onDownloadNoteSuccess();
    }

    @Override
    public void onDownloadNoteFailed(Exception e, String msg) {
        onView.onDownloadNoteFailed(msg, e);
    }

    //==========================如下不使用==============================


    @Override
    public void onAddDefaultFolderSuccess() {

    }

    @Override
    public void onAddDefaultFolderIdSuccess() {

    }

    @Override
    public void onAddFolderSuccess() {

    }

    @Override
    public void onAddFolderFailed(Exception e, String msg) {

    }

    @Override
    public void onGetFolderSuccess() {

    }

    @Override
    public void onGetFolderFailed(Exception e, String msg) {

    }

    @Override
    public void onProfileSuccess(ProfileBean bean) {

    }

    @Override
    public void onProfileFailed(String msg, Exception e) {

    }


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
    public void onAddDefaultTagSuccess() {

    }

    @Override
    public void onAddTagSuccess() {

    }

    @Override
    public void onAddTagFailed(Exception e, String msg) {

    }

    @Override
    public void onTagRenameSuccess() {

    }

    @Override
    public void onTagRenameFailed(Exception e, String msg) {

    }

    @Override
    public void onGetTagSuccess() {

    }

    @Override
    public void onGetTagFailed(Exception e, String msg) {

    }

    @Override
    public void onGetTagListSuccess() {

    }

    @Override
    public void onGetTagListFailed(Exception e, String msg) {

    }


}
