package com.thinkernote.ThinkerNote._constructer.m;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnTextEditListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 注册 m层 具体实现
 */
public class TextEditModule {

    private Context context;
    private static final String TAG = "SJY";

    public TextEditModule(Context context) {
        this.context = context;
    }

    public void pFolderAdd(final OnTextEditListener listener, long pid, String text) {
        TNSettings settings = TNSettings.getInstance();
        if (pid == -1) {
            MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                    .addNewFolder(text, settings.token)//接口方法
                    .subscribeOn(Schedulers.io())//固定样式
                    .unsubscribeOn(Schedulers.io())//固定样式
                    .observeOn(AndroidSchedulers.mainThread())//固定样式
                    .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                        @Override
                        public void onCompleted() {
                            MLog.d(TAG, "FolderAdd--onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            MLog.e("FolderAdd 异常onError:" + e.toString());
                            listener.onFolderAddFailed("异常", new Exception("接口异常！"));
                        }

                        @Override
                        public void onNext(CommonBean bean) {
                            MLog.d(TAG, "FolderAdd-onNext");

                            //处理返回结果
                            if (bean.getCode() == 0) {
                                listener.onFolderAddSuccess(bean);
                            } else {
                                listener.onFolderAddFailed(bean.getMessage(), null);
                            }
                        }

                    });
        } else {
            MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                    .addNewFolderByPid(text, pid, settings.token)//接口方法
                    .subscribeOn(Schedulers.io())//固定样式
                    .unsubscribeOn(Schedulers.io())//固定样式
                    .observeOn(AndroidSchedulers.mainThread())//固定样式
                    .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                        @Override
                        public void onCompleted() {
                            MLog.d(TAG, "pFolderAdd--onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            MLog.e("pFolderAdd 异常onError:" + e.toString());
                            listener.onFolderAddFailed("异常", new Exception("接口异常！"));
                        }

                        @Override
                        public void onNext(CommonBean bean) {
                            MLog.d(TAG, "pFolderAdd-onNext");

                            //处理返回结果
                            if (bean.getCode() == 0) {
                                listener.onFolderAddSuccess(bean);
                            } else {
                                listener.onFolderAddFailed(bean.getMessage(), null);
                            }
                        }

                    });
        }

    }

    public void pFolderRename(final OnTextEditListener listener, final long pid, final String text) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .renameFolder(text, pid, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "FolderRename--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("FolderRename 异常onError:" + e.toString());
                        listener.onFolderRenameFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "FolderRename-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onFolderRenameSuccess(bean, text, pid);
                        } else {
                            listener.onFolderRenameFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    public void pTagAdd(final OnTextEditListener listener, String text) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .addNewTag(text, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "pTagAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("pTagAdd 异常onError:" + e.toString());
                        listener.onTagAddFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "pTagAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onTagAddSuccess(bean);
                        } else {
                            listener.onTagAddFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    public void pTagRename(final OnTextEditListener listener, final long pid, final String text) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .tagRename(text, pid, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "TagRename--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("TagRename 异常onError:" + e.toString());
                        listener.onTagRenameFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "TagRename-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onTagRenameSuccess(bean, text, pid);
                        } else {
                            listener.onTagRenameFailed(bean.getMessage(), null);
                        }
                    }

                });
    }
}
