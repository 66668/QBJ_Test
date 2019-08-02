package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.listener.v.OnTagListListener;
import com.thinkernote.ThinkerNote._constructer.m.TagModule;

/**
 * p层 具体实现
 */
public class TagListPresenter implements OnTagListListener {
    private Context context;
    private OnTagListListener onView;
    //p层调用M层方法
    private TagModule module;

    public TagListPresenter(Context context, OnTagListListener logListener) {
        this.context = context;
        this.onView = logListener;

        module = new TagModule(context);
    }


    //============================p层重写，用于调用m层方法============================

    public void pTagList() {
        module.getTagList(this);
    }

    //==========================结果回调==============================


    @Override
    public void onTagListSuccess() {
        onView.onTagListSuccess();
    }

    @Override
    public void onTagListFailed(String msg, Exception e) {
        onView.onTagListFailed(msg, e);
    }
}
