package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNTag;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.m.FolderModule;
import com.thinkernote.ThinkerNote._constructer.m.NoteModule;
import com.thinkernote.ThinkerNote._constructer.m.TagModule;
import com.thinkernote.ThinkerNote._constructer.m.UpgradeModule;
import com.thinkernote.ThinkerNote._constructer.listener.m.IFolderModuleListener;
import com.thinkernote.ThinkerNote._constructer.listener.m.IMainModuleListener;
import com.thinkernote.ThinkerNote._constructer.listener.m.INoteModuleListener;
import com.thinkernote.ThinkerNote._constructer.listener.m.ITagModuleListener;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnMainViewListener;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.http.fileprogress.FileProgressListener;

import java.io.File;
import java.util.List;
import java.util.Vector;

/**
 * 注册 p层 具体实现
 * 调用顺讯按编号执行
 */
public class MainPresenter implements IMainModuleListener {
    private static final String TAG = "MainPresenter";
    private Context context;
    private OnMainViewListener onView;
    //p层调用M层方法
    private UpgradeModule upgradeModule;

    public MainPresenter(Context context, OnMainViewListener logListener) {
        this.context = context;
        this.onView = logListener;
        upgradeModule = new UpgradeModule(context);

    }

    //===========================p层，非同步块的数据=================================
    //更新检查
    public void pUpgrade(String home) {
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
