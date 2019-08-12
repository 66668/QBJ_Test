package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.m.UpgradeModule;
import com.thinkernote.ThinkerNote._constructer.listener.m.IUpgradeModuleListener;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnUpgradeListener;
import com.thinkernote.ThinkerNote.http.fileprogress.FileProgressListener;

import java.io.File;

/**
 * 注册 p层 具体实现
 * 调用顺讯按编号执行
 */
public class UpgradePresenter implements IUpgradeModuleListener {
    private static final String TAG = "MainPresenter";
    private Context context;
    private OnUpgradeListener onView;
    //p层调用M层方法
    private UpgradeModule upgradeModule;

    public UpgradePresenter(Context context, OnUpgradeListener logListener) {
        this.context = context;
        this.onView = logListener;
        upgradeModule = new UpgradeModule(context);

    }

    //===========================p层，非同步块的数据=================================
    //更新检查
    public void pUpgrade() {
        upgradeModule.mUpgrade(this);
    }

    //下载
    public void pDownload(String url, FileProgressListener progressListener) {
        upgradeModule.mDownload(this, url, progressListener);
    }

    //============================================================================
    //==========================接口结果回调,再传递给UI==============================
    //============================================================================

    //===========================更新相关=================================
    //===========非同步块的回调=========

    @Override
    public void onUpgradeSuccess(Object obj) {
        onView.onUpgradeSuccess(obj);
    }

    @Override
    public void onUpgradeFailed(String msg, Exception e) {
        onView.onUpgradeFailed(msg, e);
    }

    //下载
    @Override
    public void onDownloadSuccess(File file) {
        onView.onDownloadSuccess(file);
    }

    @Override
    public void onDownloadFailed(String msg, Exception e) {
        onView.onDownloadFailed(msg, e);
    }


}
