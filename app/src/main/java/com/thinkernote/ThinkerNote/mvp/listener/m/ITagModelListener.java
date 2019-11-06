package com.thinkernote.ThinkerNote.mvp.listener.m;

/**
 * m层interface
 * tag回调给p层相关
 */
public interface ITagModelListener {

    /**
     * 第一次同步 文件夹
     */
    void onAddDefaultTagSuccess();

    void onAddTagSuccess();

    void onAddTagFailed(Exception e, String msg);

    //
    void onTagRenameSuccess();

    void onTagRenameFailed(Exception e, String msg);

    //获取标签
    void onGetTagSuccess();

    void onGetTagFailed(Exception e, String msg);

    //获取列表数据（非同步块回调）
    void onGetTagListSuccess();

    void onGetTagListFailed(Exception e, String msg);

    //
    void onDeleteTagSuccess();

    void onDeleteTagFailed(Exception e, String msg);


}
