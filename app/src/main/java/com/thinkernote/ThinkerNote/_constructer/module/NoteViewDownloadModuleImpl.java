package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.ILogModule;
import com.thinkernote.ThinkerNote._interface.m.INoteViewDownloadModule;
import com.thinkernote.ThinkerNote._interface.v.OnLogListener;
import com.thinkernote.ThinkerNote._interface.v.OnNoteViewDownloadListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;
import com.thinkernote.ThinkerNote.http.URLUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 下载文件 m层 具体实现
 */
public class NoteViewDownloadModuleImpl implements INoteViewDownloadModule {

    private Context context;
    private static final String TAG = "SJY";
    private OnNoteViewDownloadListener listener;

    public NoteViewDownloadModuleImpl(Context context, OnNoteViewDownloadListener listener) {
        this.listener = listener;
        this.context = context;
    }


    @Override
    public void listDownload(final TNNoteAtt att, TNNote tnNote, final Vector<TNNoteAtt> tmpList, final int position) {


        // check file downloadSize
        final String path = TNUtilsAtt.getAttPath(att.attId, att.type);
        if (path == null) {
            listener.onListDownloadFailed(TNUtils.getAppContext().getResources().getString(R.string.alert_NoSDCard), new Exception("接口异常！"), att, tmpList, position);
            return;
        }

        //http 方式从服务器下载附件
//        aAction.runChildAction(TNActionType.TNHttpDownloadAtt, ("attachment/" + att.attId), att.attId, path);

        //url绝对路径:https://s.qingbiji.cn/attachment/28498638?session_token=KA6nN3d3eqMRuWJr8gmX6Svw7d27HPr69qmbpBhf
        String url = URLUtils.API_BASE_URL + "attachment/" + att.attId + "?session_token=" + TNSettings.getInstance().token;
        MLog.d("download", "url=" + url + "下载路径：path=" + path);
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .downloadFile(url)//接口方法
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
                        writeFile(inputStream, new File(path));//保存下载文件
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//返回主线程
                .subscribe(new Subscriber() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "listDownload--onCompleted");
                        listener.onListDownloadSuccess(null, att, tmpList, position);
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("listDownload 异常onError:" + e.toString());
                        listener.onListDownloadFailed("下载失败", new Exception("接口异常！"), att, tmpList, position);
                    }

                    @Override
                    public void onNext(Object o) {

                    }

                });
    }

    @Override
    public void singleDownload(final TNNoteAtt att, TNNote tnNote) {

        final String path = TNUtilsAtt.getAttPath(att.attId, att.type);
        if (path == null) {
            listener.onSingleDownloadFailed(TNUtils.getAppContext().getResources().getString(R.string.alert_NoSDCard), new Exception("接口异常！"));
            return;
        }
        //http 方式从服务器下载附件
//        aAction.runChildAction(TNActionType.TNHttpDownloadAtt, ("attachment/" + att.attId), att.attId, path);

        //url绝对路径 要求：https://s.qingbiji.cn/attachment/28498638?session_token=KA6nN3d3eqMRuWJr8gmX6Svw7d27HPr69qmbpBhf
        //                https://s.qingbiji.cn/attachment/28498638?session_token=av8u9gGn6h4YEDbNd3RQKsyrd2X6SjKTu29DW6EU
        String url = URLUtils.API_BASE_URL + "attachment/" + att.attId + "?session_token=" + TNSettings.getInstance().token;
        MLog.d("download", "url=" + url + "下载路径：path=" + path);
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .downloadFile(url)//接口方法
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
                        writeFile(inputStream, new File(path));//保存下载文件
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//返回主线程
                .subscribe(new Subscriber() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "singleDownload--onCompleted");
                        listener.onSingleDownloadSuccess(null, att);
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("singleDownload 异常onError:" + e.toString());
                        listener.onSingleDownloadFailed("下载失败", new Exception("接口异常！"));
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

        } catch (Exception e) {
            MLog.e("download", "error=" + e.toString());
        }

    }

}
