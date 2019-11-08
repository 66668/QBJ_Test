package com.thinkernote.ThinkerNote.mvp.p;

import com.thinkernote.ThinkerNote.mvp.http.fileprogress.FileProgressListener;
import com.thinkernote.ThinkerNote.mvp.listener.m.IUpgradeModelListener;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnUpgradeListener;
import com.thinkernote.ThinkerNote.mvp.m.UpgradeModel;

import java.io.File;

/**
 * 通用 p层
 *
 * 注册 p层 具体实现
 * 调用顺讯按编号执行
 */
public class UpgradePresenter implements IUpgradeModelListener {
    private static final String TAG = "MainPresenter";
    private OnUpgradeListener onView;

     UpgradeModel upgradeModel;

    public UpgradePresenter(OnUpgradeListener logListener) {
        this.onView = logListener;
        inject();
        upgradeModel = new UpgradeModel();
    }

    /**
     *
     */
    private void inject(){
//        DaggerUpgradeComponent.builder()
//                .upgradeModule(new UpgradeModule())
//                .build()
//                .inject(this);
    }

    //===========================p层，非同步块的数据=================================
    //更新检查
    public void pUpgrade() {
        upgradeModel.mUpgrade(this);
    }

    //下载
    public void pDownload(String url, FileProgressListener progressListener) {
        upgradeModel.mDownload(this, url, progressListener);
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
