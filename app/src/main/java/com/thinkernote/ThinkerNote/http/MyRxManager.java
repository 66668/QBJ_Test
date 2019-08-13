package com.thinkernote.ThinkerNote.http;

import com.thinkernote.ThinkerNote.Utils.MLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import rx.Subscription;

/**
 * 请求管理，主动关闭网络请求
 */
public class MyRxManager {
    private static MyRxManager sInstance = new MyRxManager();

    private Vector<Subscription> list;//保存请求(线程安全)
    public boolean isSyncing;//是否在同步中


    public static MyRxManager getInstance() {
        return sInstance;
    }

    private MyRxManager() {
        list = new Vector<>();
    }

    public void add(Subscription subscription) {
        list.add(subscription);
    }

    /**
     * 是否在请求中（目前只用于大块同步中，单个接口不调用）
     *
     * @return
     */
    public boolean isSyncing() {
        isSyncing = false;
        for (Subscription subscription : list) {
            if (subscription.isUnsubscribed()) {
                return isSyncing = true;
            } else {
                list.remove(subscription);
            }
        }
        return isSyncing;
    }

    /**
     * 同步中
     */
    public void setSyncing() {
        isSyncing = true;
    }

    /**
     * 同步完成
     */
    public void syncOver() {
        list.clear();
        isSyncing = false;
    }

    public void remove(Subscription tag) {
        list.remove(tag);
    }

    public void removeAll() {
        list.clear();
    }

    /**
     * 不管用list还是Vector
     */
    public void cancelAll() {
        if (list.size() <= 0) {
            return;
        }
        isSyncing = false;//先终止，避免继续执行
        synchronized (MyRxManager.this) {//加锁，多线程变单线程处理
            Vector<Subscription> cloneList = (Vector<Subscription>) list.clone();
            for (Subscription subscription : cloneList) {
                if (subscription.isUnsubscribed()) {
                    subscription.unsubscribe();
                }
            }
        }
        syncOver();
    }
}