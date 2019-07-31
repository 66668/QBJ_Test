package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.m.TextEditModule;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnTextEditListener;

/**
 *  p层 具体实现
 */
public class TextEditPresenter implements  OnTextEditListener {
    private Context context;
    private OnTextEditListener onView;
    //p层调用M层方法
    private TextEditModule module;

    public TextEditPresenter(Context context, OnTextEditListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new TextEditModule(context);
    }



    //============================p层重写，用于调用m层方法============================
    public void pFolderAdd(long parentID,String text) {
        module.pFolderAdd(this,parentID,text);
    }

    public void pFolderRename(long parentID,String text) {
        module.pFolderRename(this,parentID,text);
    }

    public void pTagAdd(String text) {
        module.pTagAdd(this,text);
    }

    public void pTagRename(long parentID,String text) {
        module.pTagRename(this,parentID,text);
    }

    //==========================结果回调==============================


    @Override
    public void onFolderAddSuccess(Object obj) {
        onView.onFolderAddSuccess(obj);
    }

    @Override
    public void onFolderAddFailed(String msg, Exception e) {
        onView.onFolderAddFailed(msg,e);
    }

    @Override
    public void onFolderRenameSuccess(Object obj,String name,long pid) {
        onView.onFolderRenameSuccess(obj,name,pid);
    }

    @Override
    public void onFolderRenameFailed(String msg, Exception e) {
        onView.onFolderRenameFailed(msg,e);
    }

    @Override
    public void onTagAddSuccess(Object obj) {
        onView.onTagAddSuccess(obj);
    }

    @Override
    public void onTagAddFailed(String msg, Exception e) {
        onView.onTagAddFailed(msg,e);
    }

    @Override
    public void onTagRenameSuccess(Object obj,String name,long pid) {
        onView.onTagRenameSuccess(obj,name,pid);
    }

    @Override
    public void onTagRenameFailed(String msg, Exception e) {
        onView.onTagRenameFailed(msg,e);
    }
}
