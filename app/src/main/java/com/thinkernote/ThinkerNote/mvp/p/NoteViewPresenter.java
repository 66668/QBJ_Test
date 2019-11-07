package com.thinkernote.ThinkerNote.mvp.p;

import com.thinkernote.ThinkerNote.mvp.listener.v.OnNoteViewListener;
import com.thinkernote.ThinkerNote.mvp.m.NoteViewModel;

/**
 * 笔记详情 p层 具体实现
 */
public class NoteViewPresenter implements OnNoteViewListener {
    private OnNoteViewListener onView;
    //p层调用M层方法
    private NoteViewModel model;

    public NoteViewPresenter( OnNoteViewListener logListener) {
        this.onView = logListener;

        model = new NoteViewModel();
    }


    //============================p层重写，用于调用m层方法============================

    public void pGetNote(long noteID) {
        model.mGetNote(this, noteID);
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
