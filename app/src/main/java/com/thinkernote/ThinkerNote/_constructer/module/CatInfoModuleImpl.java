package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.ICatInfoModule;
import com.thinkernote.ThinkerNote._interface.v.OnCatInfoListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * m层 具体实现
 */
public class CatInfoModuleImpl implements ICatInfoModule {

    private Context context;
    private static final String TAG = "SJY";

    public CatInfoModuleImpl(Context context) {
        this.context = context;
    }


    @Override
    public void mSetDefaultFolder(final OnCatInfoListener listener, final long catId) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .setDefaultFolder(catId, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "登录--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "登录--登录失败异常onError:" + e.toString());
                        listener.onFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "登录-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "登录-成功");
                            TNSettings settings = TNSettings.getInstance();
                            settings.defaultCatId = catId;
                            settings.savePref(false);
                            listener.onSuccess(bean);
                        } else {
                            listener.onFailed(bean.getMessage(), null);
                        }
                    }

                });
    }


    @Override
    public void mCatDelete(final OnCatInfoListener listener, final long catId) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .deleteFolder(catId, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mCatDelete--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetNoteByNoteId 异常onError:" + e.toString());
                        listener.onDeleteFolderFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mGetNoteByNoteId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onDeleteFolderSuccess(bean, catId);
                        } else {
                            listener.onDeleteFolderFailed(bean.getMessage(), null);
                        }
                    }


                });
    }
}
