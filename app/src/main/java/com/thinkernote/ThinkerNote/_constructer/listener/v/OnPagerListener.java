package com.thinkernote.ThinkerNote._constructer.listener.v;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;

import java.util.Vector;

/**
 * 我的笔记 v层 只有一个接口 通用回调
 */
public interface OnPagerListener {
    //删除文件夹
    void onDeleteFolderSuccess();

    void onDeleteFolderFailed(String msg, Exception e);

    //删除tag
    void onTagDeleteSuccess();

    void onTagDeleteFailed(String msg, Exception e);
    //
    void onDefaultFolderSuccess();

    void onDefaultFolderFailed(String msg, Exception e);

    //
    void onFolderDeleteSuccess(Object obj);

    void onFolderDeleteFailed(String msg, Exception e);
    //
    void onDownloadNoteSuccess();

    void onDownloadNoteFailed(String msg, Exception e);

}
