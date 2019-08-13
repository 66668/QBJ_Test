package com.thinkernote.ThinkerNote.mvp.m;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnRegistListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.login.VerifyPicBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 注册 m层 具体实现
 */
public class RegistModule {

    private Context context;
    private static final String TAG = "SJY";

    public RegistModule(Context context) {
        this.context = context;
    }

    public void getVerifyPic(final OnRegistListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getVerifyPic(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<VerifyPicBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "验证码--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("验证码 异常onError:" + e.toString());
                        listener.onPicFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(VerifyPicBean bean) {
                        MLog.d(TAG, "验证码-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "验证码-成功");
                            listener.onPicSuccess(bean);
                        } else {
                            listener.onPicFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    public void phoneVerifyCode(final OnRegistListener listener, String mPhone, String name, String mAnswer, String mNonce, String mHashKey) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .postVerifyCode4(mPhone, name, mAnswer, mNonce, mHashKey,settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d("验证码--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("验证码--异常onError:" + e.toString());
                        listener.onPhoneVCodeFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("验证码-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d("验证码-成功");
                            listener.onPhoneVCodeSuccess(bean);
                        } else {
                            listener.onPhoneVCodeFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    public void submitRegist(final OnRegistListener listener, String phone, String ps, String vcode) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .registSubmit(phone, ps, vcode)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d("submit--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("submit--异常onError:" + e.toString());
                        listener.onSubmitRegistFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("submit-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d("submit-成功");
                            listener.onSubmitRegistSuccess(bean);
                        } else {
                            listener.onSubmitRegistFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    public void submitForgetPs(final OnRegistListener listener, String phone, String ps, String vcode) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .findPsSubmit(phone, ps, vcode,settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d("submit--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("submit--异常onError:" + e.toString());
                        listener.onSubmitFindPsFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("submit-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d("submit-成功");
                            listener.onSubmitFindPsSuccess(bean);
                        } else {
                            listener.onSubmitFindPsFailed(bean.getMessage(), null);
                        }
                    }

                });
    }


    public void bindPhone(final OnRegistListener listener, int mUserType, String bid, String name, String accessToken, String refreshToken, long currentTime, String phone, String vcode, String sign) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .postLoginBindPhone(mUserType, bid,name,accessToken,refreshToken,currentTime,phone,vcode,sign,settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d("bind--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("bind--异常onError:" + e.toString());
                        listener.onBindPhoneFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("bind-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d("bind-成功");
                            listener.onBindPhoneSuccess(bean);
                        } else {
                            listener.onBindPhoneFailed(bean.getMessage(), null);
                        }
                    }

                });
    }
    public void autoLogin(final OnRegistListener listener, String phone, String ps) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .loginNormal(phone, ps)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<LoginBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d("login--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("login--异常onError:" + e.toString());
                        listener.onAutoLoginFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(LoginBean bean) {
                        MLog.d("login-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d("login-成功");
                            listener.onAutoLoginSuccess(bean);
                        } else {
                            listener.onAutoLoginFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    public void mProfile(final OnRegistListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .LogNormalProfile(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean2<ProfileBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mProFile--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mProFile 异常onError:" + e.toString());
                        listener.onProfileFailed("异常", new Exception("接口异常！"));
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
