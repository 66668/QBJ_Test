package com.thinkernote.ThinkerNote.mvp.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.mvp.m.LogModule;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnUserinfoListener;

/**
 * 主页--设置界面 p层 具体实现
 */

public class UserInfoPresenter implements OnUserinfoListener {
    private Context context;
    private OnUserinfoListener onView;
    //p层调用M层方法
    private LogModule module;

    public UserInfoPresenter(Context context, OnUserinfoListener userinfoListener) {
        this.context = context;
        this.onView = userinfoListener;

        module = new LogModule(context);
    }


    //============================p层重写，用于调用m层方法============================
    public void pLogout() {
        module.mLogout(this);
    }

    //==========================结果回调==============================

    @Override
    public void onLogoutSuccess(Object obj) {
        onView.onLogoutSuccess(obj);
    }

    @Override
    public void onLogoutFailed(String msg, Exception e) {
        onView.onLogoutFailed(msg, e);
    }

}
