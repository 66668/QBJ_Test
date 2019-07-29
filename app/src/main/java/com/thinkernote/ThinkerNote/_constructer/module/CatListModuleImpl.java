package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.ICatListModule;
import com.thinkernote.ThinkerNote._interface.v.OnCatListListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * m层 具体实现
 */
public class CatListModuleImpl implements ICatListModule {

    private Context context;
    private static final String TAG = "SJY";

    public CatListModuleImpl(Context context) {
        this.context = context;
    }

    @Override
    public void mParentFolder(final OnCatListListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getFolder(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllFolderBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mParentFolder--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "mParentFolder--onError:" + e.toString());
                        listener.onParentFolderFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllFolderBean bean) {
                        MLog.d(TAG, "mParentFolder-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "mParentFolder-成功");
                            listener.onParentFolderSuccess(bean);
                        } else {
                            listener.onParentFolderFailed(bean.getMsg(), null);
                        }
                    }

                });
    }

    @Override
    public void mGetFolderByFolderId(final OnCatListListener listener, final long catId) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getFolderByFolderID(catId, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllFolderBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mGetFolderByFolderId--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "mGetFolderByFolderId--onError:" + e.toString());
                        listener.onGetFoldersByFolderIdFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllFolderBean bean) {
                        MLog.d(TAG, "mGetFolderByFolderId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onGetFoldersByFolderIdSuccess(bean, catId);
                        } else {
                            listener.onGetFoldersByFolderIdFailed(bean.getMsg(), null);
                        }
                    }

                });
    }

    @Override
    public void mmoveFolder(final OnCatListListener listener, final long catId, long selectId) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .folderMove(catId, selectId, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "moveFolder--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "moveFolder--onError:" + e.toString());
                        listener.onFolderMoveFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "moveFolder-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onFolderMoveSuccess(bean);
                        } else {
                            listener.onFolderMoveFailed(bean.getMessage(), null);
                        }
                    }

                });
    }
}
