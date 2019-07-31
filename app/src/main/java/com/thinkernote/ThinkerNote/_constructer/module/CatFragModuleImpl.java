package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.ICatFragModule;
import com.thinkernote.ThinkerNote._interface.v.OnCatFragListener;
import com.thinkernote.ThinkerNote._interface.v.OnSynchronizeDataListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.CommonBean3;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.GetNoteByNoteIdBean;
import com.thinkernote.ThinkerNote.bean.main.NoteListBean;
import com.thinkernote.ThinkerNote.bean.main.NewNoteBean;
import com.thinkernote.ThinkerNote.bean.main.OldNotePicBean;
import com.thinkernote.ThinkerNote.bean.main.TagListBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;
import com.thinkernote.ThinkerNote.http.RequestBodyUtil;
import com.thinkernote.ThinkerNote.http.URLUtils;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * frag cat m层 具体实现
 */
public class CatFragModuleImpl implements ICatFragModule {

    private Context context;
    private static final String TAG = "SJY";

    public CatFragModuleImpl(Context context) {
        this.context = context;
    }

    //=========================================其他===========================================
    //1
    @Override
    public void mGetParentFolder(final OnCatFragListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getFolder(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllFolderBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mParentFolder--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "mParentFolder--onError:" + e.toString());
                        listener.onGetParentFolderFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllFolderBean bean) {
                        MLog.d(TAG, "mParentFolder-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "mParentFolder-成功");
                            listener.onGetParentFolderSuccess(bean);
                        } else {
                            listener.onGetParentFolderFailed(bean.getMsg(), null);
                        }
                    }

                });
    }

    //2
    @Override
    public void mGetFolderByFolderId(final OnCatFragListener listener, final long catId) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getFolderByFolderID(catId, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllFolderBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "CatFrag--mGetFolderByFolderId--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("GetFoldersByFolderId 异常onError:" + e.toString());
                        listener.onGetFolderByFolderIdFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllFolderBean bean) {
                        MLog.d(TAG, "GetFoldersByFolderId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onGetFolderByFolderIdSuccess(bean, catId);
                        } else {
                            listener.onGetFolderByFolderIdFailed(bean.getMsg(), null);
                        }
                    }

                });
    }

    //3
    @Override
    public void mGetNoteListByTrash(final OnCatFragListener listener, int pagerSize, final int pagenum, final String sortType) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getNoteListByTrash(pagerSize, pagenum, sortType, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<NoteListBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "CatFrag--mGetNoteListByTrash--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("GetFoldersByFolderId 异常onError:" + e.toString());
                        listener.onGetNoteListByTrashFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(NoteListBean bean) {
                        MLog.d(TAG, "GetFoldersByFolderId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onGetNoteListByTrashSuccess(bean, pagenum, sortType);
                        } else {
                            listener.onGetNoteListByTrashFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    //4
    @Override
    public void mGetNotelistByFolderId(final OnCatFragListener listener, final long folderId, final int mPageNum, final int pageSize, final String sort) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getNoteListByFolderId(folderId, mPageNum, TNConst.PAGE_SIZE, sort, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<NoteListBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "upgrmGetNotelistByFolderIdade--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetNotelistByFolderId 异常onError:" + e.toString());
                        listener.onGetNoteListByFolderIdFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(NoteListBean bean) {
                        MLog.d(TAG, "mGetNotelistByFolderId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onGetNoteListByFolderIdSuccess(bean, folderId, mPageNum, sort);
                        } else {
                            listener.onGetNoteListByFolderIdFailed(bean.getMessage(), null);
                        }
                    }
                });
    }

    //=========================================syncData===========================================

    //01-01第一次登录同步 folder
    @Override
    public void mfolderAdd(final OnSynchronizeDataListener listener, final int position, final int arraySize, String name) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .addNewFolder(name, settings.token)//接口方法
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
    public void mTagAdd(final OnSynchronizeDataListener listener, final int position, final int arraySize, String name) {
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
    public void GetFolder(final OnSynchronizeDataListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getFolder(settings.token)//接口方法
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
    public void mGetFoldersByFolderId(final OnSynchronizeDataListener listener, final long id, final int position, final List<AllFolderItemBean> beans) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getFolderByFolderID(id, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllFolderBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
//                        MLog.d(TAG, "CatFrag--GetFoldersByFolderId--onCompleted");
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
    public void mFirstFolderAdd(final OnSynchronizeDataListener listener, final int workPos, final int workSize, final long catID, final String name, final int catPos, final int flag) {
        TNSettings settings = TNSettings.getInstance();
        if (catID == -1L) {
            MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                    .addNewFolder(name, settings.token)//接口方法
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
                                listener.onSyncFirstFolderAddFailed(bean.getMessage(), null, workPos, workSize, catID, name, catPos, flag);
                            }
                        }

                    });
        } else {
            MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                    .addNewFolderByPid(name, catID, settings.token)//接口方法
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
                            listener.onSyncFirstFolderAddFailed("异常", new Exception("接口异常！"), workPos, workSize, catID, name, catPos, flag);
                        }

                        @Override
                        public void onNext(CommonBean bean) {
                            MLog.d(TAG, "upgrade-onNext");

                            //处理返回结果
                            if (bean.getCode() == 0) {
                                listener.onSyncFirstFolderAddSuccess(bean, workPos, workSize, catID, name, catPos, flag);
                            } else {
                                listener.onSyncFirstFolderAddFailed(bean.getMessage(), null, workPos, workSize, catID, name, catPos, flag);
                            }
                        }

                    });
        }

    }


    //2-1
    @Override
    public void mProfile(final OnSynchronizeDataListener listener) {
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
    public void mUploadOldNotePic(final OnSynchronizeDataListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, final TNNoteAtt tnNoteAtt) {
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
        RequestBody requestFile = null;
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") || filePath.endsWith("png")) {
            requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        } else {
            requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file); //TODO multipart/form-data /image/*
        }
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        //拼接url(本app后台特殊嗜好，蛋疼):
        String url = URLUtils.API_BASE_URL + URLUtils.Home.UPLOAD_FILE + "?" + "filename=" + file.getName() + "&session_token=" + settings.token;
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
    public void mOldNoteAdd(final OnSynchronizeDataListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {

        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .addNewNote(note.title, content, note.tagStr, note.catId, note.createTime, note.lastUpdate, note.lbsLongitude, note.lbsLatitude, note.lbsAddress, note.lbsRadius, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<NewNoteBean>() {//固定样式，可自定义其他处理
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
                    public void onNext(NewNoteBean bean) {
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
    public void mGetTagList(final OnSynchronizeDataListener listener) {
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
    public void mNewNotePic(final OnSynchronizeDataListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, final TNNoteAtt tnNoteAtt) {
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
        String url = URLUtils.API_BASE_URL + URLUtils.Home.UPLOAD_FILE + "?" + "filename=" + file.getName() + "&session_token=" + settings.token;
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
    public void mNewNote(final OnSynchronizeDataListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .addNewNote(note.title, content, note.tagStr, note.catId, note.createTime, note.lastUpdate, note.lbsLongitude, note.lbsLatitude, note.lbsAddress, note.lbsRadius, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<NewNoteBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mNewNote--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("CatFragModuleImple--mNewNote 异常onError:" + e.toString());
                        listener.onSyncNewNoteAddFailed("异常", new Exception("接口异常！"), position, arraySize);
                    }

                    @Override
                    public void onNext(NewNoteBean bean) {
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
    public void mRecoveryNote(final OnSynchronizeDataListener listener, final long noteID, final int position, int arrySize) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .putRecoveryNote(noteID, settings.token)
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
    public void mRecoveryNotePic(final OnSynchronizeDataListener listener, final int picPos, final int picArrySize, final int notePos, final int noteArrySize, final TNNoteAtt tnNoteAtt) {
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
        String url = URLUtils.API_BASE_URL + URLUtils.Home.UPLOAD_FILE + "?" + "filename=" + file.getName() + "&session_token=" + settings.token;
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
    public void mRecoveryNoteAdd(final OnSynchronizeDataListener listener, final int position, final int arraySize, TNNote note, final boolean isNewDb, String content) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .addNewNote(note.title, content, note.tagStr, note.catId, note.createTime, note.lastUpdate, note.lbsLongitude, note.lbsLatitude, note.lbsAddress, note.lbsRadius, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<NewNoteBean>() {//固定样式，可自定义其他处理
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
                    public void onNext(NewNoteBean bean) {
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
    public void mDeleteNote(final OnSynchronizeDataListener listener, final long noteId, final int poistion) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .deleteNote(noteId, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mDeleteNote--onCompleted");
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
    public void mDeleteRealNotes(final OnSynchronizeDataListener listener, final long noteId, final int poistion) {
        TNSettings settings = TNSettings.getInstance();
        //2-9-1
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .deleteNote(noteId, settings.token)
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
                .deleteTrashNote2(noteId, settings.token)
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
    public void mGetAllNotesId(final OnSynchronizeDataListener listener) {
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
    public void mEditNotePic(final OnSynchronizeDataListener listener, final int cloudsPos, final int attrPos, final TNNote note) {
        String filename = note.atts.get(attrPos).attName;
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
        RequestBody requestFile = RequestBodyUtil.getRequest(filePath, file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        //拼接url(本app后台特殊嗜好，蛋疼):
        String url = URLUtils.API_BASE_URL + URLUtils.Home.UPLOAD_FILE + "?" + "filename=" + file.getName() + "&session_token=" + settings.token;
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
                        MLog.d("mEditNotePic onCompleted");
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
    public void mEditNote(final OnSynchronizeDataListener listener, final int position, final TNNote note) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .editNote(note.noteId, note.title, note.content, note.tagStr, note.catId, note.createTime, note.lastUpdate, settings.token)
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
    public void mGetNoteByNoteId(final OnSynchronizeDataListener listener, final int position, long id, final boolean is12) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getNoteByNoteId(id, settings.token)
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
    public void mGetAllTrashNoteIds(final OnSynchronizeDataListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getTrashNoteIds(settings.token)
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
