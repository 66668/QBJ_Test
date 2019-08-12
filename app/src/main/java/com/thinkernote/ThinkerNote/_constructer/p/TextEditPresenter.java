package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.listener.m.IFolderModuleListener;
import com.thinkernote.ThinkerNote._constructer.listener.m.ITagModuleListener;
import com.thinkernote.ThinkerNote._constructer.m.FolderModule;
import com.thinkernote.ThinkerNote._constructer.m.TagModule;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnTextEditListener;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;

/**
 * p层 具体实现
 */
public class TextEditPresenter implements ITagModuleListener, IFolderModuleListener {
    private Context context;
    private OnTextEditListener onView;
    //p层调用M层方法
    private TagModule tagModule;
    private FolderModule folderModule;

    public TextEditPresenter(Context context, OnTextEditListener logListener) {
        this.context = context;
        this.onView = logListener;
        tagModule = new TagModule(context);
        folderModule = new FolderModule(context);
    }


    //============================p层重写，用于调用m层方法============================
    public void pFolderAdd(long parentID, String text) {
        folderModule.addFoler(parentID, text, this);
    }

    public void pFolderRename(long parentID, String text) {
        folderModule.renameFolder(parentID, text, this);
    }


    public void pTagAdd(String text) {
        tagModule.addTag(text, this);
    }

    public void pTagRename(long parentID, String text) {
        tagModule.renameTag(parentID, text, this);
    }

    //==========================结果回调==============================


    @Override
    public void onAddFolderSuccess() {
        onView.onFolderAddSuccess();
    }

    @Override
    public void onAddFolderFailed(Exception e, String msg) {
        onView.onFolderAddFailed(msg, e);
    }

    @Override
    public void onRenameFolderSuccess() {
        onView.onFolderRenameSuccess();
    }

    @Override
    public void onRenameFolderFailed(Exception e, String msg) {
        onView.onFolderRenameFailed(msg, e);
    }

    @Override
    public void onAddTagSuccess() {
        onView.onTagAddSuccess();
    }

    @Override
    public void onAddTagFailed(Exception e, String msg) {
        onView.onTagAddFailed(msg, e);
    }

    //
    @Override
    public void onTagRenameSuccess() {
        onView.onTagRenameSuccess();
    }

    @Override
    public void onTagRenameFailed(Exception e, String msg) {
        onView.onTagRenameFailed(e, msg);
    }


    //============================如下接口不使用===================================
    @Override
    public void onAddDefaultTagSuccess() {

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

    @Override
    public void onDeleteTagSuccess() {

    }

    @Override
    public void onDeleteTagFailed(Exception e, String msg) {

    }

    @Override
    public void onAddDefaultFolderSuccess() {

    }

    @Override
    public void onAddDefaultFolderIdSuccess() {

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
    public void onDeleteFolderSuccess() {

    }

    @Override
    public void onDeleteFolderFailed(String msg, Exception e) {

    }

    @Override
    public void onDefaultFolderSuccess() {

    }

    @Override
    public void onDefaultFolderFailed(String msg, Exception e) {

    }

}
