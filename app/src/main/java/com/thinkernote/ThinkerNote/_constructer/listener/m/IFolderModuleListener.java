package com.thinkernote.ThinkerNote._constructer.listener.m;

import com.thinkernote.ThinkerNote.bean.login.ProfileBean;

/**
 * m层interface
 * 文件夹回调给p层相关
 */
public interface IFolderModuleListener {

    /**
     * 第一次同步 文件夹
     */
    void onAddDefaultFolderSuccess();

    // 默认添加子文件夹
    void onAddDefaultFolderIdSuccess();

    void onAddFolderSuccess();

    void onAddFolderFailed(Exception e, String msg);

    //获取
    void onGetFolderSuccess();

    void onGetFolderFailed(Exception e, String msg);

    //获取所有数据
    void onProfileSuccess(ProfileBean bean);

    void onProfileFailed(String msg, Exception e);

    //删除文件夹
    void onDeleteFolderSuccess();

    void onDeleteFolderFailed(String msg, Exception e);

    //
    void onDefaultFolderSuccess();

    void onDefaultFolderFailed(String msg, Exception e);

}

