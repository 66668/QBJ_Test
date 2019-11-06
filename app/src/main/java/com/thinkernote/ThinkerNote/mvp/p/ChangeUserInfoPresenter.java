package com.thinkernote.ThinkerNote.mvp.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.mvp.m.ChangeUserInfoModel;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnChangeUserInfoListener;

/**
 * p层 具体实现
 */
public class ChangeUserInfoPresenter implements OnChangeUserInfoListener {
    private Context context;
    private OnChangeUserInfoListener onView;
    //p层调用M层方法
    private ChangeUserInfoModel model;

    public ChangeUserInfoPresenter(Context context, OnChangeUserInfoListener logListener) {
        this.context = context;
        this.onView = logListener;
        model = new ChangeUserInfoModel(context);
    }


    //============================p层重写，用于调用m层方法============================


    public void pChangePs(String oldPs, String newPs) {
        model.mChangePs(this, oldPs, newPs);
    }

    public void pChangeNameOrEmail(String nameOrEmail, String type, String userPs) {
        model.mChangeNameOrEmail(this, nameOrEmail, type, userPs);
    }

    //更新
    public void pProfile() {
        model.mProfile(this);
    }

    //==========================结果回调==============================

    @Override
    public void onChangePsSuccess(Object obj, String newPs) {
        onView.onChangePsSuccess(obj, newPs);
    }

    @Override
    public void onChangePsFailed(String msg, Exception e) {
        onView.onChangePsFailed(msg, e);
    }

    @Override
    public void onChangeNameOrEmailSuccess(Object obj, String nameOrEmail, String type) {
        onView.onChangeNameOrEmailSuccess(obj, nameOrEmail, type);
    }

    @Override
    public void onChangeNameOrEmailFailed(String msg, Exception e) {
        onView.onChangeNameOrEmailFailed(msg, e);
    }

    //
    @Override
    public void onProfileSuccess(Object obj) {
        onView.onProfileSuccess(obj);
    }

    @Override
    public void onProfileFailed(String msg, Exception e) {
        onView.onProfileFailed(msg, e);
    }


    //========================================================
}
