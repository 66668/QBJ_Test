package com.thinkernote.ThinkerNote.mvp.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.mvp.m.NoteViewModule;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnNoteViewListener;

/**
 * 笔记详情 p层 具体实现
 */
public class NoteViewPresenter implements OnNoteViewListener {
    private Context context;
    private OnNoteViewListener onView;
    //p层调用M层方法
    private NoteViewModule module;

    public NoteViewPresenter(Context context, OnNoteViewListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new NoteViewModule(context);
    }


    //============================p层重写，用于调用m层方法============================

    public void pGetNote(long noteID) {
        module.mGetNote(this, noteID);
    }
    //==========================结果回调==============================


    @Override
    public void onGetNoteSuccess(Object obj) {
        onView.onGetNoteSuccess(obj);
    }

    @Override
    public void onGetNoteFailed(String msg, Exception e) {
        onView.onGetNoteFailed(msg, e);
    }
}
