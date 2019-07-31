package com.thinkernote.ThinkerNote._constructer.listener.m;

import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;

/**
 * m层interface
 * note回调给p层相关
 */
public interface INoteModuleListener {


    //更新老note
    void onUpdateOldNoteSuccess();

    void onUpdateOldNoteFailed(Exception e, String msg);


    //更新 本地新增note
    void onUpdateLocalNoteSuccess();

    void onUpdateLocalNoteFailed(Exception e, String msg);

    //更新 回收站还原note
    void onUpdateRecoveryNoteSuccess();

    void onUpdateRecoveryNoteFailed(Exception e, String msg);

    // 删除笔记
    void onDeleteNoteSuccess();

    void onDeleteNoteFailed(Exception e, String msg);

    // 更新本地编辑笔记
    void onUpdateEditNoteSuccess();

    void onUpdateEditNoteFailed(Exception e, String msg);

    // 彻底删除笔记
    void onClearNoteSuccess();

    void onClearNoteFailed(Exception e, String msg);


    // 获取所有笔记id(除回收站笔记)
    void onGetAllNoteIdSuccess();

    void onGetAllNoteIdNext(AllNotesIdsBean bean);

    void onGetAllNoteIdFailed(Exception e, String msg);


    // 获取回收站笔记id
    void onGetTrashNoteIdSuccess();

    void onGetTrashNoteIdNext(AllNotesIdsBean bean);

    void onGetTrashNoteIdFailed(Exception e, String msg);

    //更新远端回收站笔记到本地
    void onGetTrashNoteSuccess();

    void onGetTrashNoteFailed(Exception e, String msg);

    // 加载云端笔记
    void onCloudNoteSuccess();

    void onCloudNoteFailed(Exception e, String msg);


}
