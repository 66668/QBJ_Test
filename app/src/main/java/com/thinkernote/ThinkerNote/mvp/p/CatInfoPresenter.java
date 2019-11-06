package com.thinkernote.ThinkerNote.mvp.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.mvp.listener.v.OnCatInfoListener;
import com.thinkernote.ThinkerNote.mvp.m.FolderModel;

/**
 * 登录 p层 具体实现
 */
public class CatInfoPresenter implements  OnCatInfoListener {
    private Context context;
    private OnCatInfoListener onView;
    //p层调用M层方法
    private FolderModel model;

    public CatInfoPresenter(Context context, OnCatInfoListener logListener) {
        this.context = context;
        this.onView = logListener;
        model = new FolderModel(context);
    }


    //============================p层重写，用于调用m层方法============================
    public void pSetDefaultFolder(long catId) {
        model.mSetDefaultFolder(this, catId);
    }

    public void pDeleteCat(long catId) {
        model.mCatDelete(this, catId);
    }

    //==========================结果回调==============================
    @Override
    public void onSuccess(Object obj) {
        onView.onSuccess(obj);
    }

    @Override
    public void onFailed(String msg, Exception e) {
        onView.onFailed(msg, e);
    }

    @Override
    public void onDeleteFolderSuccess(Object obj, long catId) {
        onView.onDeleteFolderSuccess(obj, catId);
    }

    @Override
    public void onDeleteFolderFailed(String msg, Exception e) {
        onView.onDeleteFolderFailed(msg, e);
    }
}
