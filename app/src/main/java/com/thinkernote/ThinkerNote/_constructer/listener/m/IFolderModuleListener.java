package com.thinkernote.ThinkerNote._constructer.listener.m;

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
}
