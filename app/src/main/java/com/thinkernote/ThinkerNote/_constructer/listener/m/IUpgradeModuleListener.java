package com.thinkernote.ThinkerNote._constructer.listener.m;

import com.thinkernote.ThinkerNote.bean.login.ProfileBean;

import java.io.File;

/**
 * 主界面 其他回调
 */
public interface IUpgradeModuleListener {

    void onUpgradeSuccess(Object obj);

    void onUpgradeFailed(String msg, Exception e);

    void onDownloadSuccess(File file);

    void onDownloadFailed(String msg, Exception e);

}
