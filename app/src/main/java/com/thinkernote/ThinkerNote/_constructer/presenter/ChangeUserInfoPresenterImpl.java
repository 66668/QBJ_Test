package com.thinkernote.ThinkerNote._constructer.presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote._constructer.module.ChangeUserInfoModuleImpl;
import com.thinkernote.ThinkerNote._constructer.module.FindPsModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.IChangeUserInfoModule;
import com.thinkernote.ThinkerNote._interface.m.IFindPsModule;
import com.thinkernote.ThinkerNote._interface.p.IChangeUserInfoPresener;
import com.thinkernote.ThinkerNote._interface.p.IFindPsPresener;
import com.thinkernote.ThinkerNote._interface.v.OnChangeUserInfoListener;
import com.thinkernote.ThinkerNote._interface.v.OnFindPsListener;

/**
 * p层 具体实现
 */
public class ChangeUserInfoPresenterImpl implements IChangeUserInfoPresener, OnChangeUserInfoListener {
    private Context context;
    private OnChangeUserInfoListener onView;
    //p层调用M层方法
    private IChangeUserInfoModule module;

    public ChangeUserInfoPresenterImpl(Context context, OnChangeUserInfoListener logListener) {
        this.context = context;
        this.onView = logListener;
        module = new ChangeUserInfoModuleImpl(context);
    }


    //============================p层重写，用于调用m层方法============================


    @Override
    public void pChangePs(String oldPs, String newPs) {
        module.mChangePs(this, oldPs, newPs);
    }

    @Override
    public void pChangeNameOrEmail(String nameOrEmail, String type, String userPs) {
        module.mChangeNameOrEmail(this, nameOrEmail, type, userPs);
    }

    //更新
    @Override
    public void pProfile() {
        module.mProfile(this);
    }

    //==========================结果回调==============================

    @Override
    public void onChangePsSuccess(Object obj,String newPs) {
        onView.onChangePsSuccess(obj,newPs);
    }

    @Override
    public void onChangePsFailed(String msg, Exception e) {
        onView.onChangePsFailed(msg, e);
    }

    @Override
    public void onChangeNameOrEmailSuccess(Object obj, String nameOrEmail, String type) {
        onView.onChangeNameOrEmailSuccess(obj, nameOrEmail,type);
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
