package com.thinkernote.ThinkerNote._constructer.listener.v;

import java.io.File;

/**
 * 主页--设置界面 v层
 */
public interface OnUserinfoListener {
    void onLogoutSuccess(Object obj);

    void onLogoutFailed(String msg, Exception e);

    void onUpgradeSuccess(Object obj);

    void onUpgradeFailed(String msg, Exception e);

    void onDownloadSuccess(File file);

    void onDownloadFailed(String msg, Exception e);
}
