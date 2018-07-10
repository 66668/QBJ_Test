package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.INoteEditModule;
import com.thinkernote.ThinkerNote._interface.v.OnNoteEditListener;
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

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 登录 m层 具体实现
 */
public class NoteEditModuleImpl implements INoteEditModule {

    private Context context;
    private static final String TAG = "SJY";

    public NoteEditModuleImpl(Context context) {
        this.context = context;
    }

    //2-5
    @Override
    public void mNewNotePic(final OnNoteEditListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, final TNNoteAtt tnNoteAtt) {
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
    public void mNewNote(final OnNoteEditListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {
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
                        MLog.e("mNewNote 异常onError:" + e.toString());
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
    public void mRecoveryNote(final OnNoteEditListener listener, final long noteID, final int position, int arrySize) {
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
    public void mRecoveryNotePic(final OnNoteEditListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, final TNNoteAtt tnNoteAtt) {
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
    public void mRecoveryNoteAdd(final OnNoteEditListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {
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
    public void mDeleteNote(final OnNoteEditListener listener, final long noteId, final int poistion) {
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
                        MLog.d(TAG, "mDeleteNote-onNext");

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
    public void mDeleteRealNotes(final OnNoteEditListener listener, final long noteId, final int poistion) {
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
                        MLog.d(TAG, "mDeleteRealNotes--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mDeleteRealNotes 异常onError:" + e.toString());
                        listener.onSyncDeleteRealNotes1Failed("异常", new Exception("接口异常！"), poistion);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mDeleteRealNotes-onNext");

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
                        MLog.d(TAG, "mDeleteRealNotes--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mDeleteRealNotes 异常onError:" + e.toString());
                        listener.onSyncDeleteRealNotes2Failed("异常", new Exception("接口异常！"), poistion);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mDeleteRealNotes-onNext");

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
    public void mGetAllNotesId(final OnNoteEditListener listener) {
        TNSettings settings = TNSettings.getInstance();
        //2-9-1
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncAllNotsId(settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllNotesIdsBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mGetAllNotesId--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetAllNotesId 异常onError:" + e.toString());
                        listener.onSyncAllNotesIdAddFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllNotesIdsBean bean) {
                        MLog.d(TAG, "mGetAllNotesId-onNext");

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
    public void mEditNotePic(final OnNoteEditListener listener, final int cloudsPos, final int attrPos, final TNNote note) {
        String filePath = note.atts.get(attrPos).path;
        long fileId = note.atts.get(attrPos).attId;

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
                        MLog.e("mEditNotePic 异常onError:" + e.toString());
                        listener.onSyncEditNotePicFailed("异常", new Exception("接口异常！"), cloudsPos, attrPos, note);
                    }

                    @Override
                    public void onNext(OldNotePicBean bean) {
                        MLog.d(TAG, "mEditNotePic-onNext");

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
    public void mEditNote(final OnNoteEditListener listener, final int position, final TNNote note) {
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
    public void mGetNoteByNoteId(final OnNoteEditListener listener, final int position, long id, final boolean is12) {
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

    //2-12
    @Override
    public void mGetAllTrashNoteIds(final OnNoteEditListener listener) {
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
