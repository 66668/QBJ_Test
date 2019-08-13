package com.thinkernote.ThinkerNote.mvp.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.mvp.m.PayModule;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnPayListener;

/**
 * p层 具体实现
 */
public class PayPresenter implements OnPayListener {
    private Context context;
    private OnPayListener onView;
    //p层调用M层方法
    private PayModule module;

    public PayPresenter(Context context, OnPayListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new PayModule(context);
    }

    //============================p层重写，用于调用m层方法============================


    public void pAlipay(String mAmount, String mType) {
        module.mAlipay(this, mAmount, mType);
    }

    public void pWxpay(String mAmount, String mType) {
        module.mWxpay(this, mAmount, mType);
    }


    //==========================结果回调==============================


    @Override
    public void onAlipaySuccess(Object obj) {
        onView.onAlipaySuccess(obj);
    }

    @Override
    public void onAlipayFailed(String msg, Exception e) {
        onView.onAlipayFailed(msg, e);
    }

    @Override
    public void onWxpaySuccess(Object obj) {
        onView.onWxpaySuccess(obj);
    }

    @Override
    public void onWxpayFailed(String msg, Exception e) {
        onView.onWxpayFailed(msg, e);
    }
}
