package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;
import android.os.Environment;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.IMainModule;
import com.thinkernote.ThinkerNote._interface.v.OnMainListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean1;
import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.CommonBean3;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.GetNoteByNoteIdBean;
import com.thinkernote.ThinkerNote.bean.main.MainUpgradeBean;
import com.thinkernote.ThinkerNote.bean.main.OldNoteAddBean;
import com.thinkernote.ThinkerNote.bean.main.OldNotePicBean;
import com.thinkernote.ThinkerNote.bean.main.TagListBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;
import com.thinkernote.ThinkerNote.http.RequestBodyUtil;
import com.thinkernote.ThinkerNote.http.URLUtils;
import com.thinkernote.ThinkerNote.http.fileprogress.FileProgressListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 注册 m层 具体实现
 */
public class MainModuleImpl implements IMainModule {

    private Context context;
    private static final String TAG = "SJY";

    public MainModuleImpl(Context context) {
        this.context = context;
    }

    @Override
    public void mUpgrade(final OnMainListener listener) {
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


    //下载文件 实时进度
    @Override
    public void mDownload(final OnMainListener listener, String url, final FileProgressListener progressListener) {
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

    //01-01第一次登录同步 folder
    @Override
    public void mfolderAdd(final OnMainListener listener, final int position, final int arraySize, String name) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncFolderAdd(name, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mfolderAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mfolderAdd 异常onError:" + e.toString());
                        listener.onSyncFolderAddFailed("异常", new Exception("接口异常！"), position, arraySize);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mfolderAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncFolderAddSuccess(bean, position, arraySize);
                        } else {
                            listener.onSyncFolderAddFailed(bean.getMessage(), null, position, arraySize);
                        }
                    }

                });
    }

    //01-02第一次登录同步 tag
    @Override
    public void mTagAdd(final OnMainListener listener, final int position, final int arraySize, String name) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncTagAdd(name, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mTagAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mTagAdd 异常onError:" + e.toString());
                        listener.onSyncTagAddFailed("异常", new Exception("接口异常！"), position, arraySize);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mTagAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncTagAddSuccess(bean, position, arraySize);
                        } else {
                            listener.onSyncTagAddFailed(bean.getMessage(), null, position, arraySize);
                        }
                    }

                });
    }

    // 01-3
    @Override
    public void GetFolder(final OnMainListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncGetFolder(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllFolderBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "GetFolder--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("GetFolder 异常onError:" + e.toString());
                        listener.onSyncGetFolderFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllFolderBean bean) {
                        MLog.d(TAG, "GetFolder-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncGetFolderSuccess(bean);
                        } else {
                            listener.onSyncGetFolderFailed(bean.getMsg(), null);
                        }
                    }
                });
    }


    //01-4
    @Override
    public void mGetFoldersByFolderId(final OnMainListener listener, final long id, final int position, final List<AllFolderItemBean> beans) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncGetFolderByFodlerId(id, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllFolderBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "GetFoldersByFolderId--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("GetFoldersByFolderId 异常onError:" + e.toString());
                        listener.onSyncGetFoldersByFolderIdFailed("异常", new Exception("接口异常！"), id, position, beans);
                    }

                    @Override
                    public void onNext(AllFolderBean bean) {
                        MLog.d(TAG, "GetFoldersByFolderId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncGetFoldersByFolderIdSuccess(bean, id, position, beans);
                        } else {
                            listener.onSyncGetFoldersByFolderIdFailed(bean.getMsg(), null, id, position, beans);
                        }
                    }

                });
    }


    //1-5
    @Override
    public void mFirstFolderAdd(final OnMainListener listener, final int workPos, final int workSize, final long catID, final String name, final int catPos, final int flag) {
        TNSettings settings = TNSettings.getInstance();
        if (catID == -1L) {
            MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                    .folderAdd(name, settings.token)//接口方法
                    .subscribeOn(Schedulers.io())//固定样式
                    .unsubscribeOn(Schedulers.io())//固定样式
                    .observeOn(AndroidSchedulers.mainThread())//固定样式
                    .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                        @Override
                        public void onCompleted() {
                            MLog.d(TAG, "FolderAdd--onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            MLog.e("FolderAdd 异常onError:" + e.toString());
                            listener.onSyncFirstFolderAddFailed("异常", new Exception("接口异常！"), workPos, workSize, catID, name, catPos, flag);
                        }

                        @Override
                        public void onNext(CommonBean bean) {
                            MLog.d(TAG, "FolderAdd-onNext");

                            //处理返回结果
                            if (bean.getCode() == 0) {
                                listener.onSyncFirstFolderAddSuccess(bean, workPos, workSize, catID, name, catPos, flag);
                            } else {
                                //文件已存在
                                MLog.e(bean.getMessage());
                                listener.onSyncFirstFolderAddFailed(bean.getMessage(), null, workPos, workSize, catID, name, catPos, flag);
                            }
                        }

                    });
        } else {
            MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                    .folderAdd(name, catID, settings.token)//接口方法
                    .subscribeOn(Schedulers.io())//固定样式
                    .unsubscribeOn(Schedulers.io())//固定样式
                    .observeOn(AndroidSchedulers.mainThread())//固定样式
                    .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                        @Override
                        public void onCompleted() {
                            MLog.d(TAG, "upgrade--onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            MLog.e("upgrade 异常onError:" + e.toString());
                            listener.onSyncFirstFolderAddFailed("异常", new Exception("接口异常！"), workPos, workSize, catID, name,catPos, flag);
                        }

                        @Override
                        public void onNext(CommonBean bean) {
                            MLog.d(TAG, "upgrade-onNext");

                            //处理返回结果
                            if (bean.getCode() == 0) {
                                listener.onSyncFirstFolderAddSuccess(bean, workPos, workSize, catID, name, catPos, flag);
                            } else {
                                MLog.e(bean.getMessage());
                                listener.onSyncFirstFolderAddFailed(bean.getMessage(), null, workPos, workSize, catID, name,catPos, flag);
                            }
                        }

                    });
        }

    }

    //2-1
    @Override
    public void mProfile(final OnMainListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .LogNormalProfile(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean2<ProfileBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mProFile--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mProFile 异常onError:" + e.toString());
                        listener.onSyncProfileAddFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean2<ProfileBean> bean) {
                        MLog.d(TAG, "mProFile-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncProfileSuccess(bean.getProfile());
                        } else {
                            listener.onSyncProfileAddFailed(bean.getMsg(), null);
                        }
                    }
                });
    }


    //02-02 OldNotePic
    @Override
    public void mUploadOldNotePic(final OnMainListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, final TNNoteAtt tnNoteAtt) {
        String filename = tnNoteAtt.attName;
        String filePath = tnNoteAtt.path;
        long fileId = tnNoteAtt.attId;
        TNSettings settings = TNSettings.getInstance();

        //多个文件上传
        // 需要加入到MultipartBody中，而不是作为参数传递
//        MultipartBody.Builder builder = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)//表单类型
//                .addFormDataPart("token", settings.token);
//        for(File file:files){
//            RequestBody photoRequestBody = RequestBody.create(MediaType.parse("image/*"), file);//TODO multipart/form-data /image/*
//            builder.addFormDataPart("file", file.getName(), photoRequestBody);
//            List<MultipartBody.Part> parts = builder.build().parts();
//        }

        //单个文件上传
        File file = new File(filePath);
        RequestBody requestFile = RequestBodyUtil.getRequest(filePath, file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        //拼接url(本app后台特殊嗜好，蛋疼):
        String url = URLUtils.API_BASE_URL + URLUtils.Home.UPLOAD_PIC + "?" + "filename=" + file.getName() + "&session_token=" + settings.token;
        MLog.d("FeedBackPic", "url=" + url + "\nfilename=" + file.toString() + "---" + file.getName());
        url = url.replace(" ", "%20");//文件名有空格

        //http调用
        MyHttpService.UpLoadBuilder.UploadServer()//固定样式，可自定义其他网络
                .uploadPic(url, part)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNotePicBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "FeedBackPic--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("OldNotePic 异常onError:" + e.toString());
                        listener.onSyncOldNotePicFailed("异常", new Exception("接口异常！"), picPos, picArrySize, notePos, noteArrySize);
                    }

                    @Override
                    public void onNext(OldNotePicBean bean) {
                        MLog.d(TAG, "OldNotePic-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncOldNotePicSuccess(bean, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
                        } else {
                            listener.onSyncOldNotePicFailed(bean.getMessage(), null, picPos, picArrySize, notePos, noteArrySize);
                        }
                    }

                });

    }


    //02-03 OldNoteAdd
    @Override
    public void mOldNoteAdd(final OnMainListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {

        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncOldNoteAdd(note.title, content, note.tagStr, note.catId, note.createTime, note.lastUpdate, note.lbsLongitude, note.lbsLatitude, note.lbsAddress, note.lbsRadius, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNoteAddBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mfolderAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mfolderAdd 异常onError:" + e.toString());
                        listener.onSyncOldNoteAddFailed("异常", new Exception("接口异常！"), position, arraySize);
                    }

                    @Override
                    public void onNext(OldNoteAddBean bean) {
                        MLog.d(TAG, "mfolderAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncOldNoteAddSuccess(bean, position, arraySize, isNewDb);
                        } else {
                            listener.onSyncOldNoteAddFailed(bean.getMessage(), null, position, arraySize);
                        }
                    }

                });
    }

    //2-4
    @Override
    public void mGetTagList(final OnMainListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncTagList(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<TagListBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mGetTagList--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetTagList 异常onError:" + e.toString());
                        listener.onSyncTagListAddFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(TagListBean bean) {
                        MLog.d(TAG, "mGetTagList-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncTagListSuccess(bean);
                        } else {
                            listener.onSyncTagListAddFailed(bean.getMsg(), null);
                        }
                    }

                });
    }


    //2-5
    @Override
    public void mNewNotePic(final OnMainListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, final TNNoteAtt tnNoteAtt) {
        String filePath = tnNoteAtt.path;

        TNSettings settings = TNSettings.getInstance();

        //多个文件上传
        // 需要加入到MultipartBody中，而不是作为参数传递
//        MultipartBody.Builder builder = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)//表单类型
//                .addFormDataPart("token", settings.token);
//        for(File file:files){
//            RequestBody photoRequestBody = RequestBody.create(MediaType.parse("image/*"), file);//TODO multipart/form-data /image/*
//            builder.addFormDataPart("file", file.getName(), photoRequestBody);
//            List<MultipartBody.Part> parts = builder.build().parts();
//        }

        //单个文件上传
        File file = new File(filePath);
        RequestBody requestFile = RequestBodyUtil.getRequest(filePath, file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        //拼接url(本app后台特殊嗜好，蛋疼):
        String url = URLUtils.API_BASE_URL + URLUtils.Home.UPLOAD_PIC + "?" + "filename=" + file.getName() + "&session_token=" + settings.token;
        MLog.d("FeedBackPic", "url=" + url + "\nfilename=" + file.toString() + "---" + file.getName());
        url = url.replace(" ", "%20");//文件名有空格

        //http调用
        MyHttpService.UpLoadBuilder.UploadServer()//固定样式，可自定义其他网络
                .uploadPic(url, part)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNotePicBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mNewNotePic 异常onError:" + e.toString());
                        listener.onSyncNewNotePicFailed("异常", new Exception("接口异常！"), picPos, picArrySize, notePos, noteArrySize);
                    }

                    @Override
                    public void onNext(OldNotePicBean bean) {
                        MLog.d(TAG, "mNewNotePic-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncNewNotePicSuccess(bean, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
                        } else {
                            listener.onSyncNewNotePicFailed(bean.getMessage(), null, picPos, picArrySize, notePos, noteArrySize);
                        }
                    }

                });
    }

    //2-6
    @Override
    public void mNewNote(final OnMainListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {
        TNSettings settings = TNSettings.getInstance();
      Subscription subscription = MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncNewNoteAdd(note.title, content, note.tagStr, note.catId, note.createTime, note.lastUpdate, note.lbsLongitude, note.lbsLatitude, note.lbsAddress, note.lbsRadius, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNoteAddBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mNewNote--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("MainModuleImple---mNewNote 异常onError:" + e.toString());
                        listener.onSyncNewNoteAddFailed("异常", new Exception("接口异常！"), position, arraySize);
                    }

                    @Override
                    public void onNext(OldNoteAddBean bean) {
                        MLog.d(TAG, "mNewNote-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncNewNoteAddSuccess(bean, position, arraySize, isNewDb);
                        } else {
                            listener.onSyncNewNoteAddFailed(bean.getMessage(), null, position, arraySize);
                        }
                    }

                });
    }

    //2-7-1
    @Override
    public void mRecoveryNote(final OnMainListener listener, final long noteID, final int position, int arrySize) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncRecoveryNote(noteID, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNote--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNote 异常onError:" + e.toString());
                        listener.onSyncRecoveryFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mRecoveryNote-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncRecoverySuccess(bean, noteID, position);
                        } else {
                            listener.onSyncRecoveryFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    //2-7-2
    @Override
    public void mRecoveryNotePic(final OnMainListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, final TNNoteAtt tnNoteAtt) {
        TNSettings settings = TNSettings.getInstance();
        String filename = tnNoteAtt.attName;
        String filePath = tnNoteAtt.path;
        long fileId = tnNoteAtt.attId;

        //多个文件上传
        // 需要加入到MultipartBody中，而不是作为参数传递
//        MultipartBody.Builder builder = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)//表单类型
//                .addFormDataPart("token", settings.token);
//        for(File file:files){
//            RequestBody photoRequestBody = RequestBody.create(MediaType.parse("image/*"), file);//TODO multipart/form-data /image/*
//            builder.addFormDataPart("file", file.getName(), photoRequestBody);
//            List<MultipartBody.Part> parts = builder.build().parts();
//        }

        //单个文件上传
        File file = new File(filePath);
        RequestBody requestFile = RequestBodyUtil.getRequest(filePath, file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        //拼接url(本app后台特殊嗜好，蛋疼):
        String url = URLUtils.API_BASE_URL + URLUtils.Home.UPLOAD_PIC + "?" + "filename=" + file.getName() + "&session_token=" + settings.token;
        MLog.d("FeedBackPic", "url=" + url + "\nfilename=" + file.toString() + "---" + file.getName());
        url = url.replace(" ", "%20");//文件名有空格

        //http调用
        MyHttpService.UpLoadBuilder.UploadServer()//固定样式，可自定义其他网络
                .uploadPic(url, part)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNotePicBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNotePic--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNotePic 异常onError:" + e.toString());
                        listener.onSyncRecoveryNotePicFailed("异常", new Exception("接口异常！"), picPos, picArrySize, notePos, noteArrySize);
                    }

                    @Override
                    public void onNext(OldNotePicBean bean) {
                        MLog.d(TAG, "mRecoveryNotePic-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncRecoveryNotePicSuccess(bean, picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
                        } else {
                            listener.onSyncRecoveryNotePicFailed(bean.getMessage(), null, picPos, picArrySize, notePos, noteArrySize);
                        }
                    }
                });
    }

    //2-7-3
    @Override
    public void mRecoveryNoteAdd(final OnMainListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncRecoveryNoteAdd(note.title, content, note.tagStr, note.catId, note.createTime, note.lastUpdate, note.lbsLongitude, note.lbsLatitude, note.lbsAddress, note.lbsRadius, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNoteAddBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNoteAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNoteAdd 异常onError:" + e.toString());
                        listener.onSyncRecoveryNoteAddFailed("异常", new Exception("接口异常！"), position, arraySize);
                    }

                    @Override
                    public void onNext(OldNoteAddBean bean) {
                        MLog.d(TAG, "mRecoveryNoteAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncRecoveryNoteAddSuccess(bean, position, arraySize, isNewDb);
                        } else {
                            listener.onSyncRecoveryNoteAddFailed(bean.getMessage(), null, position, arraySize);
                        }
                    }

                });
    }

    //2-8
    @Override
    public void mDeleteNote(final OnMainListener listener, final long noteId, final int poistion) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncDeleteNote(noteId, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNoteAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNoteAdd 异常onError:" + e.toString());
                        listener.onSyncDeleteNoteFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mRecoveryNoteAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncDeleteNoteSuccess(bean, noteId, poistion);
                        } else {
                            listener.onSyncDeleteNoteFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    //2-9
    @Override
    public void mDeleteRealNotes(final OnMainListener listener, final long noteId, final int poistion) {
        TNSettings settings = TNSettings.getInstance();
        //2-9-1
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncDeleteRealNote1(noteId, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNoteAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNoteAdd 异常onError:" + e.toString());
                        listener.onSyncDeleteRealNotes1Failed("异常", new Exception("接口异常！"), poistion);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mRecoveryNoteAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncpDeleteRealNotes1Success(bean, noteId, poistion);
                        } else {
                            listener.onSyncDeleteRealNotes1Failed(bean.getMessage(), null, poistion);
                        }
                    }

                });

        //2-9-2
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncDeleteRealNote2(noteId, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNoteAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNoteAdd 异常onError:" + e.toString());
                        listener.onSyncDeleteRealNotes2Failed("异常", new Exception("接口异常！"), poistion);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mRecoveryNoteAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncDeleteRealNotes2Success(bean, noteId, poistion);
                        } else {
                            listener.onSyncDeleteRealNotes2Failed(bean.getMessage(), null, poistion);
                        }
                    }

                });
    }

    //2-10
    @Override
    public void mGetAllNotesId(final OnMainListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncAllNotsId(settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllNotesIdsBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mRecoveryNoteAdd--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mRecoveryNoteAdd 异常onError:" + e.toString());
                        listener.onSyncAllNotesIdAddFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllNotesIdsBean bean) {
                        MLog.d(TAG, "mRecoveryNoteAdd-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncAllNotesIdSuccess(bean.getNote_ids());
                        } else {
                            listener.onSyncAllNotesIdAddFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    //2-10-1
    @Override
    public void mEditNotePic(final OnMainListener listener, final int cloudsPos, final int attrPos, final TNNote note) {
        String filePath = note.atts.get(attrPos).path;
        TNSettings settings = TNSettings.getInstance();

        //多个文件上传
        // 需要加入到MultipartBody中，而不是作为参数传递
//        MultipartBody.Builder builder = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)//表单类型
//                .addFormDataPart("token", settings.token);
//        for(File file:files){
//            RequestBody photoRequestBody = RequestBody.create(MediaType.parse("image/*"), file);//TODO multipart/form-data /image/*
//            builder.addFormDataPart("file", file.getName(), photoRequestBody);
//            List<MultipartBody.Part> parts = builder.build().parts();
//        }

        //单个文件上传
        File file = new File(filePath);
        RequestBody requestFile = RequestBodyUtil.getRequest(filePath, file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        //拼接url(本app后台特殊嗜好，蛋疼):
        String url = URLUtils.API_BASE_URL + URLUtils.Home.UPLOAD_PIC + "?" + "filename=" + file.getName() + "&session_token=" + settings.token;
        MLog.d("FeedBackPic", "url=" + url + "\nfilename=" + file.toString() + "---" + file.getName());
        url = url.replace(" ", "%20");//文件名有空格

        //http调用
        MyHttpService.UpLoadBuilder.UploadServer()//固定样式，可自定义其他网络
                .uploadPic(url, part)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNotePicBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mNewNotePic 异常onError:" + e.toString());
                        listener.onSyncEditNotePicFailed("异常", new Exception("接口异常！"), cloudsPos, attrPos, note);
                    }

                    @Override
                    public void onNext(OldNotePicBean bean) {
                        MLog.d(TAG, "mNewNotePic-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncEditNotePicSuccess(bean, cloudsPos, attrPos, note);
                        } else {
                            listener.onSyncEditNotePicFailed(bean.getMessage(), null, cloudsPos, attrPos, note);
                        }

                    }

                });

    }

    //2-11-1
    @Override
    public void mEditNote(final OnMainListener listener, final int position, final TNNote note) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncEditNote(note.noteId, note.title, note.content, note.tagStr, note.catId, note.createTime, note.lastUpdate, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mEditNote--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mEditNote 异常onError:" + e.toString());
                        listener.onSyncEditNoteAddFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mEditNote-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncEditNoteSuccess(bean, position, note);
                        } else {
                            listener.onSyncEditNoteAddFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    //2-11-2
    @Override
    public void mGetNoteByNoteId(final OnMainListener listener, final int position, long id, final boolean is13) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .GetNoteByNoteId(id, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean3<GetNoteByNoteIdBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mGetNoteByNoteId--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetNoteByNoteId 异常onError:" + e.toString());
                        listener.onSyncpGetNoteByNoteIdFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean3<GetNoteByNoteIdBean> bean) {
                        MLog.d(TAG, "mGetNoteByNoteId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncpGetNoteByNoteIdSuccess(bean.getNote(), position, is13);
                        } else {
                            listener.onSyncpGetNoteByNoteIdFailed(bean.getMsg(), null);
                        }
                    }


                });
    }

    //2-12
    @Override
    public void mGetAllTrashNoteIds(final OnMainListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .GetTrashNoteIds(settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllNotesIdsBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mGetAllTrashNoteIds--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetAllTrashNoteIds 异常onError:" + e.toString());
                        listener.onSyncpGetAllTrashNoteIdsFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllNotesIdsBean bean) {
                        MLog.d(TAG, "mGetAllTrashNoteIds-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncpGetAllTrashNoteIdsSuccess(bean.getNote_ids());
                        } else {
                            listener.onSyncpGetAllTrashNoteIdsFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

}
