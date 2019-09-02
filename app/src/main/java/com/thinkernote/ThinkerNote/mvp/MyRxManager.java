package com.thinkernote.ThinkerNote.mvp;

import com.thinkernote.ThinkerNote.Utils.MLog;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * 请求管理，主动关闭网络请求
 */
public class MyRxManager {
    private static MyRxManager sInstance = new MyRxManager();

    public boolean isSyncing;//是否在同步中


    public static MyRxManager getInstance() {
        return sInstance;
    }

    private MyRxManager() {

    }

    /**
     * 是否在请求中（目前只用于大块同步中，单个接口不调用）
     *
     * @return
     */
    public boolean isSyncing() {
        return isSyncing;

    }

    /**
     * 同步中
     */
    public void setSyncing(boolean b) {
        this.isSyncing = b;
    }

}
