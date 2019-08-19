package com.thinkernote.ThinkerNote.mvp.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.mvp.listener.m.ITagModuleListener;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnFragmentTagListener;
import com.thinkernote.ThinkerNote.mvp.m.TagModule;

/**
 * tagfrag的同步，和notfrag的不同，
 */
public class FragmentTagPresenter implements ITagModuleListener {
    private static final String TAG = "MainPresenter";
    private Context context;
    private OnFragmentTagListener onView;
    //p层调用M层方法
    private TagModule tagsModule;


    public FragmentTagPresenter(Context context, OnFragmentTagListener logListener) {
        this.context = context;
        this.onView = logListener;
        tagsModule = new TagModule(context);
    }
    //============================p层（非同步块）============================

    public void getTagList() {
        tagsModule.getAllTagsBySingle(this);
    }

    //==========================如下回调不使用==============================


    @Override
    public void onGetTagListSuccess() {
        onView.onGetTagListSuccess();
    }

    @Override
    public void onGetTagListFailed(Exception e, String msg) {
        onView.onGetTagListFailed(msg, e);
    }










    // ======================如下回调不用======================
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
    public void onDeleteTagSuccess() {

    }

    @Override
    public void onDeleteTagFailed(Exception e, String msg) {

    }


}