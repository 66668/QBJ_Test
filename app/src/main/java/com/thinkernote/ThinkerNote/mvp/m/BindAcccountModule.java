package com.thinkernote.ThinkerNote.mvp.m;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnBindAccountListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.mvp.http.url_main.MyHttpService;


import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

/**
 * 绑定手机号
 */
public class BindAcccountModule {

    private Context context;

    public BindAcccountModule(Context context) {
        this.context = context;
    }

    public void mVerifyCode(final OnBindAccountListener listener, String phone, String t) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()
                .postVerifyCode2(phone, t, settings.token)//接口方法
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d("验证码--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("验证码--登录失败异常onError:" + e.toString());
                        listener.onVerifyCodeFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("验证码-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d("验证码-成功");
                            listener.onVerifyCodeSuccess(bean);
                        } else {
                            listener.onVerifyCodeFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    public void mLoginBind(final OnBindAccountListener listener, int btype, String bid, String name, String accessToken, String refreshToken, long currentTime, String phone, String vcode, String sign) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .postLoginBindPhone(btype, bid, name, accessToken, refreshToken, currentTime, phone, vcode, sign, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d("绑定手机号--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("绑定手机号--登录失败异常onError:" + e.toString());
                        listener.onVerifyCodeFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("绑定手机号-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d("绑定手机号-成功");
                            listener.onVerifyCodeSuccess(bean);
                        } else {
                            listener.onVerifyCodeFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    public void autoLoginAferBind(final OnBindAccountListener listener, int btype, final String bid, final long stamp, String sign) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .loginThird(btype, bid, stamp, sign, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<LoginBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d("绑定登录--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("绑定登录--登录失败异常onError:" + e.toString());
                        listener.onAutoLogFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(LoginBean bean) {
                        MLog.d(TAG, "绑定登录-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "绑定登录-成功");
                            listener.onAutoLogSuccess(bean);
                        } else {
                            listener.onAutoLogFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    public void mProfile(final OnBindAccountListener listener) {
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
                        listener.onLogProfileFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CommonBean2<ProfileBean> bean) {
                        MLog.d(TAG, "mProFile-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onLogProfileSuccess(bean.getProfile());
                        } else {
                            listener.onLogProfileFailed(bean.getMsg(), null);
                        }
                    }
                });
    }
}
