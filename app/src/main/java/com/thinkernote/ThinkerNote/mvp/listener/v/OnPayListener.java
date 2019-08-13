package com.thinkernote.ThinkerNote.mvp.listener.v;

/**
 * v层 只有一个接口 通用回调
 */
public interface OnPayListener {
    void onAlipaySuccess(Object obj);

    void onAlipayFailed(String msg, Exception e);

    void onWxpaySuccess(Object obj);

    void onWxpayFailed(String msg, Exception e);

}
