package com.thinkernote.ThinkerNote._constructer.listener.v;

import java.io.File;

public interface OnMainViewListener {
    void onUpgradeSuccess(Object obj);

    void onUpgradeFailed(String msg, Exception e);

    void onDownloadSuccess(File file);

    void onDownloadFailed(String msg, Exception e);


}
