package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;
import android.os.Environment;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.IUserInfoModule;
import com.thinkernote.ThinkerNote._interface.v.OnUserinfoListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean1;
import com.thinkernote.ThinkerNote.bean.main.MainUpgradeBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;
import com.thinkernote.ThinkerNote.http.fileprogress.FileProgressListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 注册 m层 具体实现
 */
public class UserInfoModuleImpl implements IUserInfoModule {

    private Context context;
    private static final String TAG = "SJY";

    public UserInfoModuleImpl(Context context) {
        this.context = context;
    }

    @Override
    public void mLogout(final OnUserinfoListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .logout(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d("验证码--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("验证码--异常onError:" + e.toString());
                        listener.onLogoutFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("验证码-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d("验证码-成功");
                            listener.onLogoutSuccess(bean);
                        } else {
                            listener.onLogoutFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    @Override
    public void mUpgrade(final OnUserinfoListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .upgrade(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean1<MainUpgradeBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "upgrade--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("upgrade 异常onError:" + e.toString());
                        listener.onUpgradeFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean1<MainUpgradeBean> bean) {
                        MLog.d(TAG, "upgrade-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "upgrade-成功" + bean.getData().toString());
                            listener.onUpgradeSuccess(bean.getData());
                        } else {
                            listener.onUpgradeFailed(bean.getMsg(), null);
                        }
                    }

                });
    }

    @Override
    public void mDownload(final OnUserinfoListener listener, String url, final FileProgressListener progressListener) {
        //自定义路径
        final File filePath = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS), "qingbiji.apk");

        MyHttpService.DownloadBuilder.getFileServer(progressListener)//固定样式，可自定义其他网络
                .download(url)//接口方法
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(new Func1<ResponseBody, InputStream>() {

                    @Override
                    public InputStream call(ResponseBody responseBody) {
                        return responseBody.byteStream();
                    }
                })
                .observeOn(Schedulers.computation()) // 用于计算任务
                .doOnNext(new Action1<InputStream>() {
                    @Override
                    public void call(InputStream inputStream) {
                        writeFile(inputStream, filePath);//保存下载文件
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//返回主线程
                .subscribe(new Subscriber() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "upgrade--onCompleted");
                        listener.onDownloadSuccess(filePath);
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mDownload 异常onError:" + e.toString());
                        listener.onDownloadFailed("下载失败", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(Object o) {

                    }

                });
    }

    /**
     * 将输入流写入文件
     *
     * @param inputString
     * @param file
     */
    private void writeFile(InputStream inputString, File file) {

        if (file.exists()) {
            file.delete();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);

            byte[] b = new byte[1024];

            int len;
            while ((len = inputString.read(b)) != -1) {
                fos.write(b, 0, len);
            }
            inputString.close();
            fos.close();

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

    }


}
