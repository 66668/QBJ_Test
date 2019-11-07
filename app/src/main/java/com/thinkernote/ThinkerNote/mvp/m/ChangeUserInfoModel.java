package com.thinkernote.ThinkerNote.mvp.m;

import android.content.Context;

import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.MLog;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnChangeUserInfoListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.mvp.http.url_main.MyHttpService;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * 登录 m层 具体实现
 */
public class ChangeUserInfoModel {

    private static final String TAG = "SJY";

    public ChangeUserInfoModel() {
    }

    public void mChangePs(final OnChangeUserInfoListener listener, String oldPs, final String newPs) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .changePs(oldPs, newPs, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d("mChangePs--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mChangePs--异常onError:" + e.toString());
                        listener.onChangePsFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("mChangePs-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onChangePsSuccess(bean, newPs);
                        } else {
                            listener.onChangePsFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    public void mChangeNameOrEmail(final OnChangeUserInfoListener listener, final String nameOrEmail, final String type, String userPs) {
        if (type.equals("userName")) {
            //修改name
            TNSettings settings = TNSettings.getInstance();
            MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                    .changeUserName(nameOrEmail, userPs, settings.token)//接口方法
                    .subscribeOn(Schedulers.io())//固定样式
                    .unsubscribeOn(Schedulers.io())//固定样式
                    .observeOn(AndroidSchedulers.mainThread())//固定样式
                    .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                        @Override
                        public void onComplete() {
                            MLog.d("mChangeNameOrEmail--onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            MLog.e("mChangeNameOrEmail--异常onError:" + e.toString());
                            listener.onChangeNameOrEmailFailed("异常", new Exception("接口异常！"));
                        }

                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(CommonBean bean) {
                            MLog.d("mChangeNameOrEmail-onNext--" + bean.toString());

                            //处理返回结果
                            if (bean.getCode() == 0) {
                                listener.onChangeNameOrEmailSuccess(bean, nameOrEmail, type);
                            } else {
                                listener.onChangeNameOrEmailFailed(bean.getMessage(), null);
                            }
                        }

                    });
        } else {
            //修改email
            TNSettings settings = TNSettings.getInstance();
            MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                    .changeUserEmail(nameOrEmail, userPs, settings.token)//接口方法
                    .subscribeOn(Schedulers.io())//固定样式
                    .unsubscribeOn(Schedulers.io())//固定样式
                    .observeOn(AndroidSchedulers.mainThread())//固定样式
                    .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                        @Override
                        public void onComplete() {
                            MLog.d("mChangeNameOrEmail--onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            MLog.e("mChangeNameOrEmail--异常onError:" + e.toString());
                            listener.onChangeNameOrEmailFailed("异常", new Exception("接口异常！"));
                        }

                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(CommonBean bean) {
                            MLog.d("mChangeNameOrEmail-onNext--" + bean.toString());

                            //处理返回结果
                            if (bean.getCode() == 0) {
                                listener.onChangeNameOrEmailSuccess(bean, nameOrEmail, type);
                            } else {
                                listener.onChangeNameOrEmailFailed(bean.getMessage(), null);
                            }
                        }

                    });
        }
    }


    /**
     * 更新userinfo
     *
     * @param listener
     */
    public void mProfile(final OnChangeUserInfoListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .LogNormalProfile(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean2<ProfileBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d(TAG, "mProfile--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "mProfile--异常onError:" + e.toString());
                        listener.onProfileFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CommonBean2<ProfileBean> bean) {
                        MLog.d(TAG, "mProfile-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "mProfile-成功");
                            listener.onProfileSuccess(bean.getProfile());
                        } else {
                            listener.onProfileFailed(bean.getMsg(), null);
                        }
                    }

                });
    }


}
