package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.IPagerModule;
import com.thinkernote.ThinkerNote._interface.v.OnPagerListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean3;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.GetNoteByNoteIdBean;
import com.thinkernote.ThinkerNote.bean.main.OldNoteAddBean;
import com.thinkernote.ThinkerNote.bean.main.OldNotePicBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;
import com.thinkernote.ThinkerNote.http.RequestBodyUtil;
import com.thinkernote.ThinkerNote.http.URLUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 我的笔记 m层 具体实现
 */
public class PagerModuleImpl implements IPagerModule {

    private Context context;
    private static final String TAG = "SJY";

    public PagerModuleImpl(Context context) {
        this.context = context;
    }

    @Override
    public void mSetDefaultFolder(final OnPagerListener listener, long folderID) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .setDefaultFolder(folderID, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "SetDefaultFolder--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("SetDefaultFolder 异常onError:" + e.toString());
                        listener.onDefaultFolderFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "SetDefaultFolder-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onDefaultFolderSuccess(bean);
                        } else {
                            listener.onDefaultFolderFailed(bean.getMessage(), null);
                        }
                    }
                });
    }

    @Override
    public void mDeleteTag(final OnPagerListener listener, final long tagID) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .deleteTag(tagID, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mDeleteTag--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mDeleteTag 异常onError:" + e.toString());
                        listener.onTagDeleteFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mDeleteTag-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onTagDeleteSuccess(bean, tagID);
                        } else {
                            listener.onTagDeleteFailed(bean.getMessage(), null);
                        }
                    }
                });
    }

    @Override
    public void mDeleteFolder(final OnPagerListener listener, final long catID) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .folderDelete(catID, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mDeleteFolder--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetNoteByNoteId 异常onError:" + e.toString());
                        listener.onDeleteFolderFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "Pager--mGetNoteByNoteId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onDeleteFolderSuccess(bean,catID);
                        } else {
                            listener.onDeleteFolderFailed(bean.getMessage(), null);
                        }
                    }


                });
    }


    //=========================================syncDataByNoteId===========================================

    @Override
    public void mGetDataByNoteId(final OnPagerListener listener, final long noteId, final int catPos, final boolean isCats) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .GetNoteByNoteId(noteId, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean3<GetNoteByNoteIdBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mGetDataByNoteId--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetNoteByNoteId 异常onError:" + e.toString());
                        listener.onGetDataByNoteIdFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean3<GetNoteByNoteIdBean> bean) {
                        MLog.d(TAG, "mGetNoteByNoteId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onGetDataByNoteIdSuccess(bean.getNote(), noteId, catPos, isCats);
                        } else {
                            listener.onGetDataByNoteIdFailed(bean.getMsg(), null);
                        }
                    }


                });
    }

    @Override
    public void mSynceNoteAttr(final OnPagerListener listener, final int pos, TNNoteAtt att, final Vector<TNNoteAtt> attrs, final long noteId, final int catPos, final boolean isCats) {
        if (TNActionUtils.isDownloadingAtt(att.attId)) {
            listener.onSyncNoteAttrFailed("没有该数据", null);
            return;
        }

        // check file downloadSize
        final String path = TNUtilsAtt.getAttPath(att.attId, att.type);
        MLog.d("下载附件路径：" + path);
        if (path == null) {
            listener.onSyncNoteAttrFailed(TNUtils.getAppContext().getResources().getString(R.string.alert_NoSDCard), null);
            return;
        }
        //方式从服务器下载附件

//        aAction.runChildAction(TNActionType.TNHttpDownloadAtt, ("attachment/" + att.attId), att.attId, path);

        //url绝对路径
        String url = URLUtils.API_BASE_URL + "attachment/" + att.attId+"?session_token="+TNSettings.getInstance().token;
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
                        MLog.d(TAG, "upgrade--onCompleted");
                        listener.onSyncNoteAttrSuccess(null, pos, attrs, noteId, catPos, isCats);
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mDownload 异常onError:" + e.toString());
                        listener.onSyncNoteAttrFailed("下载失败", new Exception("接口异常！"));
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
        }else{
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

    //========================syncCats========================

    //2-5
    @Override
    public void mNewNotePic(final OnPagerListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, final TNNoteAtt tnNoteAtt) {
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
        RequestBody requestFile = RequestBodyUtil.getRequest(filePath,file);
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
    public void mNewNote(final OnPagerListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
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
                        MLog.e("PagerModuleImple------mNewNote 异常onError:" + e.toString());
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
    public void mRecoveryNote(final OnPagerListener listener, final long noteID, final int position, int arrySize) {
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
    public void mRecoveryNotePic(final OnPagerListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, final TNNoteAtt tnNoteAtt) {
        TNSettings settings = TNSettings.getInstance();
        String filePath = tnNoteAtt.path;
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
        RequestBody requestFile = RequestBodyUtil.getRequest(filePath,file);
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
    public void mRecoveryNoteAdd(final OnPagerListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {
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
    public void mDeleteNote(final OnPagerListener listener, final long noteId, final int poistion) {
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
    public void mDeleteRealNotes(final OnPagerListener listener, final long noteId, final int poistion) {
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

    //2-10 与main不同
    @Override
    public void mGetFolderNoteIds(final OnPagerListener listener, long catId) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .GetFolderNoteIds(catId, settings.token)//接口
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllNotesIdsBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mGetFolderNoteIds--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetFolderNoteIds 异常onError:" + e.toString());
                        listener.onpGetFolderNoteIdsFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllNotesIdsBean bean) {
                        MLog.d(TAG, "mGetFolderNoteIds-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onpGetFolderNoteIdsSuccess(bean.getNote_ids());
                        } else {
                            listener.onpGetFolderNoteIdsFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    //2-10-1
    @Override
    public void mEditNotePic(final OnPagerListener listener, final int cloudsPos, final int attrPos, final TNNote note) {
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
        RequestBody requestFile = RequestBodyUtil.getRequest(filePath,file);
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
                        MLog.d(TAG, "mNewNotePic--onCompleted");
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
    public void mEditNote(final OnPagerListener listener, final int position, final TNNote note) {
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
    public void mGetNoteByNoteId(final OnPagerListener listener, final int position, long id, final boolean is12) {
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
                            listener.onSyncpGetNoteByNoteIdSuccess(bean.getNote(), position, is12);
                        } else {
                            listener.onSyncpGetNoteByNoteIdFailed(bean.getMsg(), null);
                        }
                    }


                });
    }

}
