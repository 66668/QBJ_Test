package com.thinkernote.ThinkerNote.mvp.m;

import android.content.Context;
import android.os.Environment;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.mvp.listener.m.IUpgradeModuleListener;
import com.thinkernote.ThinkerNote.bean.CommonBean1;
import com.thinkernote.ThinkerNote.bean.main.MainUpgradeBean;
import com.thinkernote.ThinkerNote.mvp.http.MyHttpService;
import com.thinkernote.ThinkerNote.mvp.http.fileprogress.FileProgressListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * 注册 m层 具体实现 ：更新
 */
public class UpgradeModule {

    private Context context;
    private static final String TAG = "SJY";

    public UpgradeModule(Context context) {
        this.context = context;
    }

    public void mUpgrade(final IUpgradeModuleListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .upgrade(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean1<MainUpgradeBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d(TAG, "upgrade--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("upgrade 异常onError:" + e.toString());
                        listener.onUpgradeFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CommonBean1<MainUpgradeBean> bean) {
                        MLog.d(TAG, "upgrade-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "upgrade-成功");
                            listener.onUpgradeSuccess(bean.getData());
                        } else {
                            listener.onUpgradeFailed(bean.getMsg(), null);
                        }
                    }

                });
    }


    //下载文件 实时进度
    public void mDownload(final IUpgradeModuleListener listener, String url, final FileProgressListener progressListener) {
        //自定义路径
        final File filePath = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS), "qingbiji.apk");

        MyHttpService.DownloadBuilder.getFileServer(progressListener)//固定样式，可自定义其他网络
                .download(url)//接口方法
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(new Function<ResponseBody, InputStream>() {

                    @Override
                    public InputStream apply(ResponseBody responseBody) {
                        return responseBody.byteStream();
                    }
                })
                .observeOn(Schedulers.computation()) // 用于计算任务
                .doOnNext(new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream inputStream) {
                        writeFile(inputStream, filePath);//保存下载文件
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//返回主线程
                .subscribe(new Observer<InputStream>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d(TAG, "mDownload--onCompleted");
                        listener.onDownloadSuccess(filePath);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(InputStream inputStream) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mDownload 异常onError:" + e.toString());
                        listener.onDownloadFailed("下载失败", new Exception("接口异常！"));
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
        } else {
            //创建新文件
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
