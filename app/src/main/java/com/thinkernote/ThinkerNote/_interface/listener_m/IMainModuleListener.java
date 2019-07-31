package com.thinkernote.ThinkerNote._interface.listener_m;

import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;

import java.io.File;

/**
 * 主界面 其他回调
 */
public interface IMainModuleListener {

    void onUpgradeSuccess(Object obj);

    void onUpgradeFailed(String msg, Exception e);

    void onDownloadSuccess(File file);

    void onDownloadFailed(String msg, Exception e);

    //
    void onProfileSuccess(ProfileBean bean);

    void onProfileFailed(String msg, Exception e);

}
