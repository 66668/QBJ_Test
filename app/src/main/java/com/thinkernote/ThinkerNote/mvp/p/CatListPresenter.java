package com.thinkernote.ThinkerNote.mvp.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.mvp.listener.v.OnCatListListener;
import com.thinkernote.ThinkerNote.mvp.m.FolderModel;

/**
 *
 */
public class CatListPresenter implements  OnCatListListener {
    private OnCatListListener onView;
    //p层调用M层方法
    private FolderModel model;

    public CatListPresenter( OnCatListListener logListener) {
        this.onView = logListener;
        model = new FolderModel();
    }


    //============================p层重写，用于调用m层方法============================
    public void pParentFodler() {
        model.mParentFolder(this);
    }

    public void pGetFolderByFolderId(long catId) {
        model.mGetFolderByFolderId(this, catId);
    }

    public void pFolderMove(long catId, long selectId) {
        model.moveFolder(this, catId, selectId);
    }

    //==========================结果回调==============================

    @Override
    public void onParentFolderSuccess(Object obj) {
        onView.onParentFolderSuccess(obj);
    }

    @Override
    public void onParentFolderFailed(String msg, Exception e) {
        onView.onParentFolderFailed(msg, e);
    }

    @Override
    public void onGetFoldersByFolderIdSuccess(Object obj, long catId) {
        onView.onGetFoldersByFolderIdSuccess(obj,catId);
    }

    @Override
    public void onGetFoldersByFolderIdFailed(String msg, Exception e) {
        onView.onGetFoldersByFolderIdFailed(msg, e);
    }


    @Override
    public void onFolderMoveSuccess(Object obj) {
        onView.onFolderMoveSuccess(obj);
    }

    @Override
    public void onFolderMoveFailed(String msg, Exception e) {
        onView.onFolderMoveFailed(msg, e);
    }


}
