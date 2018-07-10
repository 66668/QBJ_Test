package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.IChangeUserInfoModule;
import com.thinkernote.ThinkerNote._interface.v.OnChangeUserInfoListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 登录 m层 具体实现
 */
public class ChangeUserInfoModuleImpl implements IChangeUserInfoModule {

    private Context context;
    private static final String TAG = "SJY";

    public ChangeUserInfoModuleImpl(Context context) {
        this.context = context;
    }

    @Override
    public void mChangePs(final OnChangeUserInfoListener listener, String oldPs, final String newPs) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .changePs(oldPs, newPs, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d("mChangePs--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mChangePs--异常onError:" + e.toString());
                        listener.onChangePsFailed("异常", new Exception("接口异常！"));
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

    @Override
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
                        public void onCompleted() {
                            MLog.d("mChangeNameOrEmail--onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            MLog.e("mChangeNameOrEmail--异常onError:" + e.toString());
                            listener.onChangeNameOrEmailFailed("异常", new Exception("接口异常！"));
                        }

                        @Override
                        public void onNext(CommonBean bean) {
                            MLog.d("mChangeNameOrEmail-onNext");

                            //处理返回结果
                            if (bean.getCode() == 0) {
                                listener.onChangeNameOrEmailSuccess(bean,nameOrEmail, type);
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
                        public void onCompleted() {
                            MLog.d("mChangeNameOrEmail--onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            MLog.e("mChangeNameOrEmail--异常onError:" + e.toString());
                            listener.onChangeNameOrEmailFailed("异常", new Exception("接口异常！"));
                        }

                        @Override
                        public void onNext(CommonBean bean) {
                            MLog.d("mChangeNameOrEmail-onNext");

                            //处理返回结果
                            if (bean.getCode() == 0) {
                                listener.onChangeNameOrEmailSuccess(bean,nameOrEmail, type);
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
    @Override
    public void mProfile(final OnChangeUserInfoListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .LogNormalProfile(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean2<ProfileBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mProfile--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "mProfile--异常onError:" + e.toString());
                        listener.onProfileFailed("异常", new Exception("接口异常！"));
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
