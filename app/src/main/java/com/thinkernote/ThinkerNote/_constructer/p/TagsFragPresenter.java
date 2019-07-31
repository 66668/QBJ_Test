package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.m.TagsFragModule;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnTagsFragListener;

/**
 * 登录 p层 具体实现
 */
public class TagsFragPresenter implements OnTagsFragListener {
    private Context context;
    private OnTagsFragListener onView;
    //p层调用M层方法
    private TagsFragModule module;

    public TagsFragPresenter(Context context, OnTagsFragListener logListener) {
        this.context = context;
        this.onView = logListener;
        module = new TagsFragModule(context);
    }

    //============================p层重写，用于调用m层方法============================

    public void pTagList() {
        module.mGetTagList(this);
    }

    //==========================结果回调==============================

    @Override
    public void onGetTagListSuccess(Object obj) {
        onView.onGetTagListSuccess(obj);
    }

    @Override
    public void onGetTagListFailed(String msg, Exception e) {
        onView.onGetTagListFailed(msg, e);
    }
    //========================================================
}
