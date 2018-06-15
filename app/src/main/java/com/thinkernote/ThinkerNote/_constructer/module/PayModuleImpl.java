package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.IPayModule;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnPayListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean1;
import com.thinkernote.ThinkerNote.bean.main.AlipayBean;
import com.thinkernote.ThinkerNote.bean.main.WxpayBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * m层 具体实现
 */
public class PayModuleImpl implements IPayModule {

    private Context context;
    private static final String TAG = "SJY";

    public PayModuleImpl(Context context) {
        this.context = context;
    }


    @Override
    public void mAlipay(final OnPayListener listener, String mAmount, String mType) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .alipay(mAmount,mType)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean1<AlipayBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mAlipay--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mAlipay 异常onError:" + e.toString());
                        listener.onAlipayFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean1<AlipayBean> bean) {
                        MLog.d(TAG, "mAlipay-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onAlipaySuccess(bean.getData());
                        } else {
                            listener.onAlipayFailed(bean.getMsg(), null);
                        }
                    }
                });
    }

    @Override
    public void mWxpay(final OnPayListener listener, String mAmount, String mType) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .wxpay(mAmount,mType)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<WxpayBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mWxpay--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mWxpay 异常onError:" + e.toString());
                        listener.onWxpayFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(WxpayBean bean) {
                        MLog.d(TAG, "mWxpay-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onWxpaySuccess(bean);
                        } else {
                            listener.onWxpayFailed(bean.getMessage(), null);
                        }
                    }
                });
    }
}
