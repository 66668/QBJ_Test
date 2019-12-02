package com.thinkernote.ThinkerNote.mvp.m;

import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.mvp.http.url_main.MyHttpService;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnSplashListener;
import com.thinkernote.ThinkerNote.utils.MLog;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * m层 具体实现
 */
public class SplashModel {

    private static final String TAG = "SJY";

    public SplashModel() {
    }

    public void mLogin(final OnSplashListener listener, String name, String ps) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .loginNormal(name, ps)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<LoginBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d(TAG, "mLogin--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mLogin 异常onError:" + e.toString());
                        listener.onFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(LoginBean bean) {
                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSuccess(bean);
                        } else {
                            listener.onFailed(bean.getMessage(), null);
                        }
                    }
                });
    }

    public void mProFile(final OnSplashListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .LogNormalProfile(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean2<ProfileBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d(TAG, "mProFile--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mProFile 异常onError:" + e.toString());
                        listener.onProfileFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CommonBean2<ProfileBean> bean) {
                        MLog.d(TAG, "mProFile-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onProfileSuccess(bean.getProfile());
                        } else {
                            listener.onProfileFailed(bean.getMsg(), null);
                        }
                    }
                });
    }
}