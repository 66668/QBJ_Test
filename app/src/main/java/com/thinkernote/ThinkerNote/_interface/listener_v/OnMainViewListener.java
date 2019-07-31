package com.thinkernote.ThinkerNote._interface.listener_v;

import java.io.File;

public interface OnMainViewListener {
    void onUpgradeSuccess(Object obj);

    void onUpgradeFailed(String msg, Exception e);

    void onDownloadSuccess(File file);

    void onDownloadFailed(String msg, Exception e);


    //大块同步，最终返回ui处理的回调
    void onSyncSuccess(String obj);

    void onSyncFailed( Exception e,String msg);


}
