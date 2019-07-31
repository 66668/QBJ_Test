package com.thinkernote.ThinkerNote._constructer.m;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnTagInfoListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * m层 具体实现
 */
public class TagInfoModule {

    private Context context;
    private static final String TAG = "SJY";

    public TagInfoModule(Context context) {
        this.context = context;
    }



    public void mTagDelete(final OnTagInfoListener listener, final long pid) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .deleteTag(pid,settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "upgrade--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("upgrade 异常onError:" + e.toString());
                        listener.onFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "upgrade-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSuccess(bean,pid);
                        } else {
                            listener.onFailed(bean.getMessage(), null);
                        }
                    }
                });
    }

}
