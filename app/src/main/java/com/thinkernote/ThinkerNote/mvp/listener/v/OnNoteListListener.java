package com.thinkernote.ThinkerNote.mvp.listener.v;

import com.thinkernote.ThinkerNote.bean.main.NoteListBean;

public interface OnNoteListListener {

    //获取id下笔记回调act
    void onNoteListByIdSuccess();

    void onNoteListByIdNext(NoteListBean bean);

    void onNoteListByIdFailed(Exception e, String msg);

    //完全同步一条笔记
    void onDownloadNoteSuccess();


    void onDownloadNoteFailed(Exception e, String msg);




}
