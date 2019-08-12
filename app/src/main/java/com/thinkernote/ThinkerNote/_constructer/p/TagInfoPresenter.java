package com.thinkernote.ThinkerNote._constructer.p;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.listener.m.ITagModuleListener;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnTagInfoListener;
import com.thinkernote.ThinkerNote._constructer.m.TagModule;

/**
 * p层 具体实现
 */
public class TagInfoPresenter implements ITagModuleListener {
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
        module.deleteTag(pid, this);
    }
    //==========================回调============================
    @Override
    public void onDeleteTagSuccess() {
        onView.onSuccess();
    }

    @Override
    public void onDeleteTagFailed(Exception e, String msg) {
        onView.onFailed(msg, e);
    }

    //==========================如下回调不使用==============================


    @Override
    public void onAddDefaultTagSuccess() {

    }

    @Override
    public void onAddTagSuccess() {

    }

    @Override
    public void onAddTagFailed(Exception e, String msg) {

    }

    @Override
    public void onTagRenameSuccess() {

    }

    @Override
    public void onTagRenameFailed(Exception e, String msg) {

    }

    @Override
    public void onGetTagSuccess() {

    }

    @Override
    public void onGetTagFailed(Exception e, String msg) {

    }

    @Override
    public void onGetTagListSuccess() {

    }

    @Override
    public void onGetTagListFailed(Exception e, String msg) {

    }


}
