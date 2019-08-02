package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.listener.v.OnTagInfoListener;
import com.thinkernote.ThinkerNote._constructer.m.TagModule;

/**
 *  p层 具体实现
 */
public class TagInfoPresenter implements OnTagInfoListener {
    private Context context;
    private OnTagInfoListener onView;
    //p层调用M层方法
    private TagModule module;

    public TagInfoPresenter(Context context, OnTagInfoListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new TagModule(context);
    }


    //============================p层重写，用于调用m层方法============================
    public void pTagDelete(long pid) {
        module.deleteTag(this,pid);
    }

    //==========================结果回调==============================

    @Override
    public void onSuccess() {
        onView.onSuccess();
    }

    @Override
    public void onFailed(String msg, Exception e) {
        onView.onFailed(msg, e);
    }


}
