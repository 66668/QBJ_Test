package com.thinkernote.ThinkerNote.mvp.listener.v;

import java.io.File;

public interface OnUpgradeListener {
    void onUpgradeSuccess(Object obj);

    void onUpgradeFailed(String msg, Exception e);

    void onDownloadSuccess(File file);

    void onDownloadFailed(String msg, Exception e);


}
