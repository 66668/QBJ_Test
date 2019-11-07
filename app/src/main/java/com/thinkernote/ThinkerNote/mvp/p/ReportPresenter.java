package com.thinkernote.ThinkerNote.mvp.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.mvp.m.ReportModel;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnReportListener;

import java.io.File;

/**
 * p层 具体实现
 */
public class ReportPresenter implements  OnReportListener {
    private OnReportListener onView;
    //p层调用M层方法
    private ReportModel model;

    public ReportPresenter( OnReportListener logListener) {
        this.onView = logListener;

        model = new ReportModel();
    }


    //============================p层重写，用于调用m层方法============================

    public void pFeedBackPic(File mFiles, String content, String email) {
        model.mFeedBackPic(this, mFiles,content,email);
    }

    public void pFeedBack(String content, long pid, String email) {
        model.mFeedBack(this, content, pid, email);
    }

    //==========================结果回调==============================


    @Override
    public void onPicSuccess(Object obj, String content, String email) {
        onView.onPicSuccess(obj, content, email);
    }

    @Override
    public void onPicFailed(String msg, Exception e) {
        onView.onPicFailed(msg, e);
    }

    @Override
    public void onSubmitSuccess(Object obj) {
        onView.onSubmitSuccess(obj);
    }

    @Override
    public void onSubmitFailed(String msg, Exception e) {
        onView.onSubmitFailed(msg, e);
    }
}
