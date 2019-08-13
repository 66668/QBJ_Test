package com.thinkernote.ThinkerNote._constructer.m;

import android.content.Context;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnReportListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.settings.FeedBackBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;
import com.thinkernote.ThinkerNote.http.URLUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 设置--意见反馈 m层 具体实现
 */
public class ReportModule {

    private Context context;
    private static final String TAG = "SJY";

    public ReportModule(Context context) {
        this.context = context;
    }

    /**
     * 纯图片上传 拿到图片id再和内容一起上传
     */
    public void mFeedBackPic(final OnReportListener listener, File file, final String content, final String email) {
        TNSettings settings = TNSettings.getInstance();

        //多个文件上传
        // 需要加入到MultipartBody中，而不是作为参数传递
//        MultipartBody.Builder builder = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)//表单类型
//                .addFormDataPart("token", settings.token);
//        for(File file:files){
//            RequestBody photoRequestBody = RequestBody.create(MediaType.parse("image/*"), file);
//            builder.addFormDataPart("file", file.getName(), photoRequestBody);
//            List<MultipartBody.Part> parts = builder.build().parts();
//        }

        //单个文件上传
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file); // multipart/form-data /image/*
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        //拼接url(本app后台特殊嗜好，蛋疼):
        String url = URLUtils.API_BASE_URL + URLUtils.Home.UPLOAD_FILE + "?" + "filename=" + file.getName() + "&session_token=" + settings.token;
        MLog.d("FeedBackPic", "url=" + url + "\nfilename=" + file.toString() + "---" + file.getName());
        url = url.replace(" ", "%20");//文件名有空格


        //http调用
        MyHttpService.UpLoadBuilder.UploadServer()//固定样式，可自定义其他网络
                .upLoadFeedBackPic(url, part)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<FeedBackBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "FeedBackPic--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("FeedBackPic 异常onError:" + e.toString());
                        listener.onPicFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(FeedBackBean bean) {
                        MLog.d(TAG, "FeedBackPic-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "FeedBackPic-success");
                            listener.onPicSuccess(bean, content, email);
                        } else {
                            listener.onPicFailed(bean.getMessage(), null);
                        }
                    }

                });


    }

    public void mFeedBack(final OnReportListener listener, final String content, long pid, final String email) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .feedBack(content, pid, email, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "FeedBackPic--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("FeedBackPic 异常onError:" + e.toString());
                        listener.onSubmitFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "FeedBackPic-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSubmitSuccess(bean);
                        } else {
                            listener.onSubmitFailed(bean.getMessage(), null);
                        }
                    }

                });
    }
}
