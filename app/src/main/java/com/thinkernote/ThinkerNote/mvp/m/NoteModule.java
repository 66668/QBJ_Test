package com.thinkernote.ThinkerNote.mvp.m;

import android.content.Context;
import android.text.TextUtils;

import com.thinkernote.ThinkerNote.DBHelper.NoteAttrDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.NoteDbHelper;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.mvp.listener.m.INoteModuleListener;
import com.thinkernote.ThinkerNote.base.TNApplication;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean3;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.GetNoteByNoteIdBean;
import com.thinkernote.ThinkerNote.bean.main.NewNoteBean;
import com.thinkernote.ThinkerNote.bean.main.NoteListBean;
import com.thinkernote.ThinkerNote.bean.main.OldNotePicBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;
import com.thinkernote.ThinkerNote.http.MyRxManager;
import com.thinkernote.ThinkerNote.http.RequestBodyUtil;
import com.thinkernote.ThinkerNote.http.URLUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;

/**
 * 具体实现:
 * m层 笔记相关
 * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
 * <p>
 * 说明：isSync的方法保证，只有SyncPresenter里的方法使用，非SyncPresenter的方法，设置为false
 */
public class NoteModule {

    private Context context;
    private static final String TAG = "Note";
    TNSettings settings;
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public NoteModule(Context context) {
        this.context = context;
        settings = TNSettings.getInstance();
    }


    //================================================接口相关================================================

    /**
     * 添加新笔记（向后台发送要创建的文件夹）
     * 两个接口+两层for循环的转换
     *
     * @param notes
     * @param isNewDb  是否是老数据
     * @param listener
     */
    public void updateOldNote(Vector<TNNote> notes, final boolean isNewDb, final INoteModuleListener listener) {

        Subscription subscription = Observable.from(notes)
                .concatMap(new Func1<TNNote, Observable<TNNote>>() {//
                    @Override
                    public Observable<TNNote> call(final TNNote tnNote) {
                        Vector<TNNoteAtt> oldNotesAtts = tnNote.atts;
                        final String[] content = {tnNote.content};
                        //上传每一个文件
                        Observable<TNNote> fileBeanObservable = Observable.from(oldNotesAtts).concatMap(new Func1<TNNoteAtt, Observable<OldNotePicBean>>() {//先上传文件

                            //拿到每一个文件数据，上传
                            @Override
                            public Observable<OldNotePicBean> call(final TNNoteAtt tnNoteAtt) {
                                //多个文件上传
                                // 需要加入到MultipartBody中，而不是作为参数传递
                                //        MultipartBody.Builder builder = new MultipartBody.Builder()
                                //                .setType(MultipartBody.FORM)//表单类型
                                //                .addFormDataPart("token", settings.token);
                                //        for(File file:files){
                                //            RequestBody photoRequestBody = RequestBody.create(MediaType.parse("image/*"), file);// multipart/form-data /image/*
                                //            builder.addFormDataPart("file", file.getName(), photoRequestBody);
                                //            List<MultipartBody.Part> parts = builder.build().parts();
                                //        }

                                //单个文件上传
                                File file = new File(tnNoteAtt.path);
                                RequestBody requestFile = RequestBodyUtil.getRequest(tnNoteAtt.path, file);
                                MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

                                //拼接url(本app后台特殊嗜好，蛋疼):
                                String url = URLUtils.API_BASE_URL + URLUtils.Home.UPLOAD_FILE + "?" + "filename=" + file.getName() + "&session_token=" + settings.token;
                                MLog.d("FeedBackPic", "url=" + url + "\nfilename=" + file.toString() + "---" + file.getName());
                                url = url.replace(" ", "%20");//文件名有空格


                                return MyHttpService.UpLoadBuilder.UploadServer()//(接口1，上传图片)
                                        .uploadPic(url, part)//接口方法
                                        .subscribeOn(Schedulers.io())//固定样式
                                        .doOnNext(new Action1<OldNotePicBean>() {
                                            @Override
                                            public void call(OldNotePicBean oldNotePicBean) {
                                                //（1）更新图片--数据库
                                                if (oldNotePicBean.getCode() == 0) {
                                                    MLog.d("updateOldNote--upDataAttIdSQL--getId=" + oldNotePicBean.getId() + "--noteId=" + tnNote.noteId);
                                                    upDataAttIdSQL(oldNotePicBean.getId(), tnNoteAtt);
                                                }
                                            }
                                        })
                                        .doOnError(new Action1<Throwable>() {
                                            @Override
                                            public void call(Throwable throwable) {
                                                MLog.e(TAG, "updateNote--uploadPic--doOnError:" + throwable.toString());
                                                listener.onUpdateOldNoteFailed(new Exception(throwable.getMessage()), null);
                                            }
                                        });//固定样式
                            }

                        }).concatMap(new Func1<OldNotePicBean, Observable<TNNote>>() {//OldNotePicBean和tnNote处理后，再转换成tnNote,上传old TNNote
                            @Override
                            public Observable<TNNote> call(OldNotePicBean oldNotePicBean) {
                                String digest = oldNotePicBean.getMd5();
                                long attId = oldNotePicBean.getId();
                                //(2)更新图片--content
                                String s1 = String.format("<tn-media hash=\"%s\" />", digest);
                                String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", digest, attId);
                                content[0] = content[0].replaceAll(s1, s2);
                                //
                                TNNote mNote = tnNote;
                                mNote.content = content[0];
                                if (mNote.catId == -1) {
                                    mNote.catId = TNSettings.getInstance().defaultCatId;
                                }
                                return Observable.just(mNote);
                            }
                        });
                        return fileBeanObservable;
                    }
                }).concatMap(new Func1<TNNote, Observable<NewNoteBean>>() {//
                    @Override
                    public Observable<NewNoteBean> call(final TNNote note) {
                        return MyHttpService.Builder.getHttpServer()//(接口2，上传note)
                                .addNewNote(note.title, note.content, note.tagStr, note.catId, note.createTime, note.lastUpdate, note.lbsLongitude, note.lbsLatitude, note.lbsAddress, note.lbsRadius, settings.token)//接口方法
                                .subscribeOn(Schedulers.io())//固定样式
                                .unsubscribeOn(Schedulers.io())//固定样式
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnNext(new Action1<NewNoteBean>() {
                                    @Override
                                    public void call(NewNoteBean newNoteBean) {
                                        if (isNewDb) {//false时表示老数据库的数据上传，不用在修改本地的数据
                                            upDataNoteLocalIdSQL(newNoteBean, note);
                                        }
                                    }
                                })
                                .doOnError(new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable e) {
                                        MLog.e(TAG, "updateNote--addNewNote--doOnError:" + e.toString());
                                        listener.onUpdateOldNoteFailed(new Exception(e.getMessage()), null);
                                    }
                                });
                    }
                }).subscribe(new Observer<NewNoteBean>() {
                    @Override
                    public void onCompleted() {
                        listener.onUpdateOldNoteSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onUpdateOldNoteFailed(new Exception(e.getMessage()), null);
                    }

                    @Override
                    public void onNext(NewNoteBean newNoteBean) {//主线程

                    }
                });
        MyRxManager.getInstance().add(subscription);
    }

    /**
     * 笔记更新：上传本地新增笔记
     * 两个接口，一个for循环的图片上传
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     *
     * @param notes    syncState=3 的数据
     * @param listener
     */
    public void updateLocalNewNotes(Vector<TNNote> notes, final INoteModuleListener listener, boolean isSync) {

        Subscription subscription = Observable.from(notes)
                .concatMap(new Func1<TNNote, Observable<TNNote>>() {//
                    @Override
                    public Observable<TNNote> call(final TNNote tnNote) {
                        Vector<TNNoteAtt> oldNotesAtts = tnNote.atts;//文件列表


                        //上传每一个文件
                        Observable<TNNote> fileBeanObservable = Observable
                                .from(oldNotesAtts)
                                .concatMap(new Func1<TNNoteAtt, Observable<OldNotePicBean>>() {//先上传文件

                                    //拿到每一个文件数据，上传
                                    @Override
                                    public Observable<OldNotePicBean> call(final TNNoteAtt tnNoteAtt) {
                                        //多个文件上传
                                        // 需要加入到MultipartBody中，而不是作为参数传递
                                        //        MultipartBody.Builder builder = new MultipartBody.Builder()
                                        //                .setType(MultipartBody.FORM)//表单类型
                                        //                .addFormDataPart("token", settings.token);
                                        //        for(File file:files){
                                        //            RequestBody photoRequestBody = RequestBody.create(MediaType.parse("image/*"), file);// multipart/form-data /image/*
                                        //            builder.addFormDataPart("file", file.getName(), photoRequestBody);
                                        //            List<MultipartBody.Part> parts = builder.build().parts();
                                        //        }

                                        //单个文件上传
                                        File file = new File(tnNoteAtt.path);
                                        RequestBody requestFile = RequestBodyUtil.getRequest(tnNoteAtt.path, file);
                                        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

                                        //拼接url(本app后台特殊嗜好，蛋疼):
                                        String url = URLUtils.API_BASE_URL + URLUtils.Home.UPLOAD_FILE + "?" + "filename=" + file.getName() + "&session_token=" + settings.token;
                                        MLog.d("FeedBackPic", "url=" + url + "\nfilename=" + file.toString() + "---" + file.getName());
                                        url = url.replace(" ", "%20");//文件名有空格


                                        return MyHttpService.UpLoadBuilder.UploadServer()//(接口1，上传图片)
                                                .uploadPic(url, part)//接口方法
                                                .subscribeOn(Schedulers.io())//固定样式
                                                .doOnNext(new Action1<OldNotePicBean>() {
                                                    @Override
                                                    public void call(OldNotePicBean oldNotePicBean) {
                                                        //（1）更新图片--数据库
                                                        if (oldNotePicBean.getCode() == 0) {
                                                            MLog.d("updateLocalNewNotes--upDataAttIdSQL--getId=" + oldNotePicBean.getId() + "--noteId=" + tnNote.noteId);
                                                            upDataAttIdSQL(oldNotePicBean.getId(), tnNoteAtt);
                                                        }

                                                    }
                                                })
                                                .doOnError(new Action1<Throwable>() {
                                                    @Override
                                                    public void call(Throwable throwable) {
                                                        MLog.e(TAG, "updateLocalNewNotes--uploadPic--doOnError:" + throwable.toString());
                                                        listener.onUpdateLocalNoteFailed(new Exception(throwable.getMessage()), null);
                                                    }
                                                });//固定样式
                                    }

                                }).concatMap(new Func1<OldNotePicBean, Observable<TNNote>>() {//OldNotePicBean和tnNote处理后，再转换成tnNote,上传old TNNote
                                    @Override
                                    public Observable<TNNote> call(OldNotePicBean oldNotePicBean) {
                                        String digest = oldNotePicBean.getMd5();
                                        String content = tnNote.content;
                                        long attId = oldNotePicBean.getId();
                                        //(2)更新图片--content
                                        String s1 = String.format("<tn-media hash=\"%s\" />", digest);
                                        String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", digest, attId);
                                        content = content.replaceAll(s1, s2);
                                        //
                                        TNNote mNote = tnNote;
                                        mNote.content = content;
                                        if (mNote.catId == -1) {
                                            mNote.catId = TNSettings.getInstance().defaultCatId;
                                        }
                                        return Observable.just(mNote);
                                    }
                                });
                        //判断是否有文件要上传
                        if (oldNotesAtts != null && oldNotesAtts.size() > 0) {
                            MLog.d("先本地笔记的文件");
                            return fileBeanObservable;
                        } else {
                            MLog.d("直接上传本地笔记");
                            return Observable.just(tnNote);
                        }

                    }
                })
                .concatMap(new Func1<TNNote, Observable<NewNoteBean>>() {//
                    @Override
                    public Observable<NewNoteBean> call(final TNNote note) {
                        MLog.e(TAG, "updateLocalNewNotes--noteId=" + note.noteId);
                        return MyHttpService.Builder.getHttpServer()//(接口2，上传note)
                                .addNewNote(note.title, note.content, note.tagStr, note.catId, note.createTime, note.lastUpdate, note.lbsLongitude, note.lbsLatitude, note.lbsAddress, note.lbsRadius, settings.token)//接口方法
                                .subscribeOn(Schedulers.io())//固定样式
                                .doOnNext(new Action1<NewNoteBean>() {
                                    @Override
                                    public void call(NewNoteBean newNoteBean) {
                                        if (newNoteBean.getCode() == 0) {
                                            MLog.d("updateLocalNewNotes--upDataNoteLocalIdSQL");
                                            upDataNoteLocalIdSQL(newNoteBean, note);
                                        }

                                    }
                                })
                                .doOnError(new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable e) {
                                        MLog.e(TAG, "updateLocalNewNotes--addNewNote--doOnError:" + e.toString());
                                        listener.onUpdateLocalNoteFailed(new Exception(e.getMessage()), null);
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<NewNoteBean>() {
                    @Override
                    public void onCompleted() {
                        MLog.d("updateLocalNewNotes--onCompleted");
                        listener.onUpdateLocalNoteSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.d("updateLocalNewNotes--onError");
                        listener.onUpdateLocalNoteFailed(new Exception(e.getMessage()), null);
                    }

                    @Override
                    public void onNext(NewNoteBean newNoteBean) {//主线程
                        MLog.d("updateLocalNewNotes--onNext");
                    }
                });
        if (isSync) {//是否是主界面的同步
            MyRxManager.getInstance().add(subscription);
        }

    }

    /**
     * 笔记更新：从回收站还原
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     * <p>
     * 3个接口+两层for循环的转换，单层for循环的两个接口判断调用，zip合并不同数据源,统一处理
     *
     * @param notes    syncState=7 的数据
     * @param listener
     */
    public void updateRecoveryNotes(Vector<TNNote> notes, final INoteModuleListener listener, boolean isSync) {

        Subscription subscription = Observable
                .from(notes)
                .concatMap(new Func1<TNNote, Observable<List>>() {//
                    @Override
                    public Observable<List> call(final TNNote tnNote) {

                        //修改文件类型 rename(数据源1)
                        Observable<CommonBean> renameNoteObservable = MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                                .putRecoveryNote(tnNote.noteId, settings.token)
                                .subscribeOn(Schedulers.io())
                                .doOnNext(new Action1<CommonBean>() {
                                    @Override
                                    public void call(CommonBean commonBean) {
                                        MLog.d(TAG, "updateRecoveryNotes--doOnNext" + commonBean.getCode() + "--" + commonBean.getMessage());
                                        if (commonBean.getCode() == 0) {
                                            MLog.d(TAG, "更新回收站数据库");
                                            //更新数据库
                                            recoveryPutNoteSQL(tnNote.noteId);
                                        } else {
                                        }

                                    }
                                })
                                .doOnError(new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        MLog.d(TAG, "updateRecoveryNotes--doOnError" + throwable.getMessage());
                                        listener.onUpdateRecoveryNoteFailed(new Exception(throwable.getMessage()), null);
                                    }
                                });

                        //上传文件(数据源2)
                        Vector<TNNoteAtt> oldNotesAtts = tnNote.atts;
                        final String[] content = {tnNote.content};
                        Observable<TNNote> fileBeanObservable = Observable
                                .from(oldNotesAtts)
                                .concatMap(new Func1<TNNoteAtt, Observable<OldNotePicBean>>() {//先上传每一个文件
                                    //拿到每一个文件数据，上传
                                    @Override
                                    public Observable<OldNotePicBean> call(final TNNoteAtt tnNoteAtt) {
                                        //单个文件上传
                                        File file = new File(tnNoteAtt.path);
                                        RequestBody requestFile = RequestBodyUtil.getRequest(tnNoteAtt.path, file);
                                        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

                                        //拼接url(本app后台特殊嗜好，蛋疼):
                                        String url = URLUtils.API_BASE_URL + URLUtils.Home.UPLOAD_FILE + "?" + "filename=" + file.getName() + "&session_token=" + settings.token;
                                        MLog.d(TAG, "url=" + url + "\nfilename=" + file.toString() + "---" + file.getName());
                                        url = url.replace(" ", "%20");//文件名有空格

                                        return MyHttpService.UpLoadBuilder.UploadServer()//(接口1，上传图片)
                                                .uploadPic(url, part)//接口方法
                                                .subscribeOn(Schedulers.io())//固定样式
                                                .doOnNext(new Action1<OldNotePicBean>() {
                                                    @Override
                                                    public void call(OldNotePicBean oldNotePicBean) {
                                                        //（1）更新图片--数据库
                                                        MLog.d("updateRecoveryNotes--upDataAttIdSQL--getId=" + oldNotePicBean.getId() + "--noteId=" + tnNote.noteId);
                                                        upDataAttIdSQL(oldNotePicBean.getId(), tnNoteAtt);
                                                    }
                                                })
                                                .doOnError(new Action1<Throwable>() {
                                                    @Override
                                                    public void call(Throwable throwable) {
                                                        MLog.e(TAG, "updateLocalNewNotes--uploadPic--doOnError:" + throwable.toString());
                                                        listener.onUpdateRecoveryNoteFailed(new Exception(throwable.getMessage()), null);
                                                    }
                                                });//固定样式
                                    }

                                }).concatMap(new Func1<OldNotePicBean, Observable<TNNote>>() {//OldNotePicBean和tnNote处理后，再转换成tnNote,上传old TNNote
                                    @Override
                                    public Observable<TNNote> call(OldNotePicBean oldNotePicBean) {
                                        String digest = oldNotePicBean.getMd5();
                                        long attId = oldNotePicBean.getId();
                                        //(2)更新图片--content
                                        String s1 = String.format("<tn-media hash=\"%s\" />", digest);
                                        String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", digest, attId);
                                        content[0] = content[0].replaceAll(s1, s2);
                                        //
                                        TNNote mNote = tnNote;
                                        mNote.content = content[0];
                                        if (mNote.catId == -1) {
                                            mNote.catId = TNSettings.getInstance().defaultCatId;
                                        }
                                        return Observable.just(mNote);
                                    }
                                });
                        //

                        //使用zip解决不同数据源的统一处理
                        if (tnNote.noteId != -1L) {
                            return Observable.zip(renameNoteObservable, fileBeanObservable, new Func2<CommonBean, TNNote, List>() {

                                @Override
                                public List call(CommonBean commonBean, TNNote tnNote) {
                                    List list = new ArrayList();
                                    list.add(commonBean);
                                    return list;
                                }
                            });
                        } else {
                            return Observable.zip(renameNoteObservable, fileBeanObservable, new Func2<CommonBean, TNNote, List>() {

                                @Override
                                public List call(CommonBean commonBean, TNNote tnNote) {
                                    List list = new ArrayList();
                                    list.add(tnNote);
                                    return list;
                                }
                            });
                        }

                    }
                })
                .concatMap(new Func1<List, Observable<List>>() {//混合数据源的状态处理
                    @Override
                    public Observable<List> call(List list) {

                        if (list.get(0) instanceof CommonBean) {//继续传递一次，结果一起处理
                            CommonBean bean = (CommonBean) list.get(0);
                            Observable beanObservable = Observable.just(bean);
                            return Observable.zip(beanObservable, new FuncN<List>() {// 返回 CommonBean对象
                                @Override
                                public List call(Object... args) {
                                    List list = new ArrayList();
                                    list.add(args);
                                    return list;
                                }
                            });
                        } else {//TNNote
                            final TNNote note = (TNNote) list.get(0);
                            Observable empty = Observable.empty();
                            //上传笔记
                            final Observable noteObservable = MyHttpService.Builder
                                    .getHttpServer()//(接口2，上传note)
                                    .addNewNote(note.title, note.content, note.tagStr, note.catId, note.createTime, note.lastUpdate, note.lbsLongitude, note.lbsLatitude, note.lbsAddress, note.lbsRadius, settings.token)//接口方法
                                    .subscribeOn(Schedulers.io())//固定样式
                                    .doOnNext(new Action1<NewNoteBean>() {
                                        @Override
                                        public void call(NewNoteBean newNoteBean) {
                                            upDataNoteLocalIdSQL(newNoteBean, note);
                                        }
                                    })
                                    .doOnError(new Action1<Throwable>() {
                                        @Override
                                        public void call(Throwable e) {
                                            MLog.e(TAG, "updateLocalNewNotes--addNewNote--doOnError:" + e.toString());
                                            listener.onUpdateRecoveryNoteFailed(new Exception(e.getMessage()), null);
                                        }
                                    });

                            return Observable.zip(noteObservable, new FuncN<List>() {//返回 NewNoteBean对象
                                @Override
                                public List call(Object... args) {
                                    List list = new ArrayList();
                                    list.add(args);
                                    return list;
                                }
                            });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List>() {
                    @Override
                    public void onCompleted() {
                        listener.onUpdateRecoveryNoteSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onUpdateRecoveryNoteFailed(new Exception(e.getMessage()), null);
                    }

                    @Override
                    public void onNext(List list) {

                    }
                });
        if (isSync) {
            MyRxManager.getInstance().add(subscription);
        }

    }

    /**
     * 笔记更新：删除到回收站/完全删除
     * for循环，不同状态处理不同结果，返回的结果用zip统一处理
     * <p>
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     *
     * @param notes
     * @param listener
     */
    public void deleteNotes(Vector<TNNote> notes, final INoteModuleListener listener, boolean isSync) {
        Subscription subscription = Observable.from(notes).concatMap(new Func1<TNNote, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(final TNNote tnNote) {

                Observable<Integer> deleteObservable = MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                        .deleteNote(tnNote.noteId, settings.token)
                        .subscribeOn(Schedulers.io())
                        .doOnNext(new Action1<CommonBean>() {
                            @Override
                            public void call(CommonBean commonBean) {
                                //数据处理
                                if (commonBean.getCode() == 0) {
                                    deleteNoteSQL(tnNote.noteId);
                                }
                            }
                        })
                        .concatMap(new Func1<CommonBean, Observable<Integer>>() {//转换 结果类型，保持 不同结果类型转后(CommonBean,int)，有相同的结果类型（int）
                            @Override
                            public Observable<Integer> call(CommonBean commonBean) {
                                int result = commonBean.getCode();
                                return Observable.just(result);
                            }
                        }).subscribeOn(Schedulers.io());
                /**
                 * 此处处理有判断，不同情况走不同处理，最后统一处理结果并做一下转换，即可统一处理
                 */
                if (tnNote.noteId != -1) {
                    return deleteObservable;
                } else {
                    int deleteSqlResut = deleteLocalNoteSQL(tnNote.noteLocalId);
                    return Observable.just(deleteSqlResut);
                }

            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "deleteNotes--onCompleted");
                        listener.onDeleteNoteSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("deleteNotes--onError:" + e.toString());
                        listener.onDeleteNoteFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onNext(Integer result) {
                        MLog.d(TAG, "deleteNotes-onNext");
                        if (result != 0) {
                            listener.onDeleteNoteFailed(new Exception("删除笔记异常返回"), null);
                        }
                    }

                });
        if (isSync) {
            MyRxManager.getInstance().add(subscription);
        }


    }

    /**
     * 笔记更新：彻底删除
     * 流程介绍：两个接口串行执行
     * <p>
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     *
     * @param notes
     * @param listener
     */
    public void clearNotes(Vector<TNNote> notes, final INoteModuleListener listener, boolean isSync) {
        MLog.d("clearNotes--size=" + notes.size());
        Subscription subscription = Observable.from(notes)
                .concatMap(new Func1<TNNote, Observable<Integer>>() {//list转化item
                    @Override
                    public Observable<Integer> call(final TNNote tnNote) {

                        Observable<Integer> clearObservable = MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                                .deleteNote(tnNote.noteId, settings.token)
                                .subscribeOn(Schedulers.io())
                                .doOnNext(new Action1<CommonBean>() {
                                    @Override
                                    public void call(CommonBean commonBean) {//先调用deleteNote接口
                                        //数据处理
                                        MLog.d("clearNotes--deleteNoteSQL--noteId" + commonBean.toString());
                                        deleteNoteSQL(tnNote.noteId);
                                    }
                                })
                                .concatMap(new Func1<CommonBean, Observable<Integer>>() {//转换 结果类型，保持 不同结果类型转后(CommonBean,int)，有相同的结果类型（int）
                                    @Override
                                    public Observable<Integer> call(CommonBean commonBean) {
                                        //deleteNote 调用成功后，调用 deleteTrashNote2 接口
                                        //再调用第二个接口
                                        return MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                                                .deleteTrashNote2(tnNote.noteId, settings.token)
                                                .subscribeOn(Schedulers.io())
                                                .doOnNext(new Action1<CommonBean>() {
                                                    @Override
                                                    public void call(CommonBean bean) {
                                                        MLog.d("clearNotes--deleteTrashNoteSQL--noteLocalId" + bean.toString());
                                                        deleteTrashNoteSQL(tnNote.noteLocalId);
                                                    }
                                                }).concatMap(new Func1<CommonBean, Observable<? extends Integer>>() {
                                                    @Override
                                                    public Observable<? extends Integer> call(CommonBean commonBean) {
                                                        int deleteTrashNoteResult = commonBean.getCode();
                                                        return Observable.just(deleteTrashNoteResult);
                                                    }
                                                });

                                    }
                                }).subscribeOn(Schedulers.io());

                        if (tnNote.noteId == -1) {
                            // 不调用接口，直接数据库处理
                            MLog.d("彻底删除--直接删除数据库");
                            int clearSqlResut = deleteLocalNoteSQL(tnNote.noteLocalId);
                            return Observable.just(clearSqlResut);
                        } else {
                            //需要调用接口（两个接口串行执行）
                            MLog.d("彻底删除--调用接口");
                            return clearObservable;
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "clearNotes--onCompleted");
                        listener.onClearNoteSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("clearNotes--onError:" + e.toString());
                        listener.onClearNoteFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onNext(Integer result) {
                        MLog.d(TAG, "clearNotes-onNext");
                    }
                });
        if (isSync) {
            MyRxManager.getInstance().add(subscription);
        }


    }

    /**
     * 笔记更新：获取所有笔记id(不包括回收站笔记)
     * <p>
     *
     * @param listener
     */
    public void getAllNotesId(final INoteModuleListener listener) {
        Subscription subscription = MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncAllNotesId(settings.token)
                .doOnNext(new Action1<AllNotesIdsBean>() {
                    @Override
                    public void call(AllNotesIdsBean bean) {
                        if (bean.getCode() == 0) {
                            synCloudNoteById(bean.getNote_ids());
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AllNotesIdsBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "getAllNotsId--onCompleted");
                        listener.onGetAllNoteIdSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("getAllNotsId--onError:" + e.toString());
                        listener.onGetAllNoteIdFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onNext(AllNotesIdsBean bean) {
                        if (bean.getCode() == 0) {
                            listener.onGetAllNoteIdNext(bean);
                        }
                    }

                });
        MyRxManager.getInstance().add(subscription);
    }

    /**
     * 笔记更新：获取文件夹id下所有笔记id
     * <p>
     *
     * @param listener
     */
    public void getAllNotesId(long folderId, final INoteModuleListener listener) {
        Subscription subscription = MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .GetFolderNoteIds(folderId, settings.token)
                .doOnNext(new Action1<AllNotesIdsBean>() {
                    @Override
                    public void call(AllNotesIdsBean bean) {
                        if (bean.getCode() == 0) {
                            synCloudNoteById(bean.getNote_ids());
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AllNotesIdsBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "getAllNotsId--onCompleted");
                        listener.onGetAllNoteIdSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("getAllNotsId--onError:" + e.toString());
                        listener.onGetAllNoteIdFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onNext(AllNotesIdsBean bean) {
                        if (bean.getCode() == 0) {
                            listener.onGetAllNoteIdNext(bean);
                        }
                    }

                });
        MyRxManager.getInstance().add(subscription);
    }

    /**
     * TODO
     * 更新编辑的笔记 4
     * 流程介绍：
     * 双层for循环,主流程是 note_ids循环下嵌套 editNotes循环，editNotes下根据编辑状态，处理不同状态，先判断是否需要上传图片，在判断是否需要上传笔记。
     * <p>
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     *
     * @param listener
     */
    public void updateEditNotes(List<AllNotesIdsBean.NoteIdItemBean> note_ids, final Vector<TNNote> notes, final INoteModuleListener listener, boolean isSync) {
        MLog.d("编辑笔记同步--" + notes.size() + "--note_ids" + note_ids.size());
        Subscription subscription = Observable.from(note_ids)
                .concatMap(new Func1<AllNotesIdsBean.NoteIdItemBean, Observable<CommonBean>>() {
                    @Override
                    public Observable<CommonBean> call(AllNotesIdsBean.NoteIdItemBean bean) {//list转item
                        final long cloudNoteId = bean.getId();
                        final int lastUpdate = bean.getUpdate_at();
                        //处理 notes列表
                        return Observable.from(notes).concatMap(new Func1<TNNote, Observable<TNNote>>() {
                            @Override
                            public Observable<TNNote> call(final TNNote editNote) {//
                                //note下的文件列表上传
                                Vector<TNNoteAtt> oldNotesAtts = editNote.atts;//文件列表

                                //处理 item note下的每一个图片是否上传(请先参考if的判断，先执行if的判断，在执行该处)
                                Observable<TNNote> editNoteFilesObservable = Observable.from(oldNotesAtts)
                                        .concatMap(new Func1<TNNoteAtt, Observable<TNNote>>() {//先上传文件，在上传TNNote
                                            @Override
                                            public Observable<TNNote> call(final TNNoteAtt tnNoteAtt) {
                                                //单图片上传
                                                File file = new File(tnNoteAtt.path);
                                                RequestBody requestFile = RequestBodyUtil.getRequest(tnNoteAtt.path, file);
                                                MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

                                                //拼接url
                                                String url = URLUtils.API_BASE_URL + URLUtils.Home.UPLOAD_FILE + "?" + "filename=" + file.getName() + "&session_token=" + settings.token;
                                                url = url.replace(" ", "%20");//文件名有空格
                                                //
                                                MLog.d("url=" + url + "\nfilename=" + file.toString() + "\nfile.getName()=" + file.getName());

                                                // 上传图片的处理，
                                                Observable<TNNote> upResultObservier = MyHttpService.UpLoadBuilder.UploadServer()//(接口1，上传图片)
                                                        .uploadPic(url, part)//接口方法
                                                        .subscribeOn(Schedulers.io())//固定样式
                                                        .doOnNext(new Action1<OldNotePicBean>() {
                                                            @Override
                                                            public void call(OldNotePicBean oldNotePicBean) {
                                                                //（1）更新图片--数据库(意见反馈不走这一块)
                                                                if (oldNotePicBean.getCode() == 0) {
                                                                    MLog.d("updateEditNotes--upDataAttIdSQL--getId=" + oldNotePicBean.getId() + "--noteId=" + editNote.noteId);
                                                                    upDataAttIdSQL(oldNotePicBean.getId(), tnNoteAtt);
                                                                }
                                                            }
                                                        })
                                                        .doOnError(new Action1<Throwable>() {
                                                            @Override
                                                            public void call(Throwable throwable) {
                                                                MLog.e(TAG, "updateLocalNewNotes--uploadPic--doOnError:" + throwable.toString());
                                                                listener.onUpdateLocalNoteFailed(new Exception(throwable.getMessage()), null);
                                                            }
                                                        }).concatMap(new Func1<OldNotePicBean, Observable<TNNote>>() {//结果需要转换成TNNote，用于上传TNNote
                                                            @Override
                                                            public Observable<TNNote> call(OldNotePicBean oldNotePicBean) {
                                                                //结果doOnNext的进一步处理
                                                                String digest = oldNotePicBean.getMd5();
                                                                long attId = oldNotePicBean.getId();
                                                                //(2)更新图片--content
                                                                String s1 = String.format("<tn-media hash=\"%s\" />", digest);
                                                                String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", digest, attId);
                                                                //笔记的内容修改
                                                                TNNote newNote = editNote;
                                                                newNote.content = newNote.content.replaceAll(s1, s2);
                                                                //
                                                                if (newNote.catId == -1) {
                                                                    newNote.catId = TNSettings.getInstance().defaultCatId;
                                                                }
                                                                return Observable.just(newNote);
                                                            }
                                                        });

                                                //判断每个图片是否需要上传
                                                if (!TextUtils.isEmpty(tnNoteAtt.path) && tnNoteAtt.attId != -1) { //不上传图片，直接返回Note对象
                                                    TNNote newNote = editNote;
                                                    String s1 = String.format("<tn-media hash=\"%s\" />", tnNoteAtt.digest);
                                                    String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", tnNoteAtt.digest, tnNoteAtt.attId);
                                                    newNote.content = newNote.content.replaceAll(s1, s2);
                                                    String s3 = String.format("<tn-media hash=\"%s\"></tn-media>", tnNoteAtt.digest);
                                                    String s4 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", tnNoteAtt.digest, tnNoteAtt.attId);
                                                    newNote.content = newNote.content.replaceAll(s3, s4);
                                                    return Observable.just(newNote);
                                                } else {//上传图片
                                                    MLog.d("编辑笔记同步--上传图片");
                                                    return upResultObservier;
                                                }
                                            }

                                        });
                                //if处理不同情况
                                if (cloudNoteId == editNote.noteId) {//
                                    if (editNote.lastUpdate > lastUpdate) {//用于上传 TNNote
                                        MLog.d("编辑笔记同步--上传笔记更新");
                                        return editNoteFilesObservable;
                                    } else {//
                                        MLog.d("编辑笔记同步--更新数据库");
                                        //更新编辑笔记的状态
                                        updataEditNotesStateSQL(editNote.noteLocalId);
                                        //继续下一个循环
                                        return Observable.empty();
                                    }
                                } else {
                                    //继续下一个循环
                                    return Observable.empty();
                                }
                            }
                        })
                                .concatMap(new Func1<TNNote, Observable<TNNote>>() {//该处来自 editNoteFilesObservable的结果
                                    @Override
                                    public Observable<TNNote> call(TNNote newEditNote) {//editNoteFilesObservable处理后的新的TNNote
                                        TNNote baseNote = newEditNote;//编辑新笔记
                                        String shortContent = TNUtils.getBriefContent(baseNote.content);
                                        String content = baseNote.content;
                                        ArrayList list = new ArrayList();
                                        int index1 = content.indexOf("<tn-media");
                                        int index2 = content.indexOf("</tn-media>");
                                        while (index1 >= 0 && index2 > 0) {
                                            String temp = content.substring(index1, index2 + 11);
                                            list.add(temp);
                                            content = content.replaceAll(temp, "");
                                            index1 = content.indexOf("<tn-media");
                                            index2 = content.indexOf("</tn-media>");
                                        }
                                        for (int i = 0; i < list.size(); i++) {
                                            String temp = (String) list.get(i);
                                            boolean isExit = false;
                                            for (TNNoteAtt att : baseNote.atts) {
                                                String temp2 = String.format("<tn-media hash=\"%s\"></tn-media>", att.digest);
                                                if (temp.equals(temp2)) {
                                                    isExit = true;
                                                }
                                            }
                                            if (!isExit) {
                                                baseNote.content = baseNote.content.replaceAll(temp, "");
                                            }
                                        }
                                        return Observable.just(baseNote);//再次转换处理
                                    }
                                })
                                .concatMap(new Func1<TNNote, Observable<CommonBean>>() {//编辑笔记上传
                                    @Override
                                    public Observable<CommonBean> call(final TNNote note) {
                                        MLog.d("编辑笔记同步--上传笔记");
                                        return MyHttpService.Builder.getHttpServer()//上传笔记
                                                .editNote(note.noteId, note.title, note.content, note.tagStr, note.catId, note.createTime, note.lastUpdate, settings.token)
                                                .subscribeOn(Schedulers.io())
                                                .doOnNext(new Action1<CommonBean>() {
                                                    @Override
                                                    public void call(CommonBean commonBean) {
                                                        //数据处理
                                                        if (commonBean.getCode() == 0) {
                                                            updataEditNoteSQL(note);
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CommonBean>() {
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "updateEditNotes--onCompleted");
                        listener.onUpdateEditNoteSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("updateEditNotes--onError:" + e.toString());
                        listener.onUpdateEditNoteFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "updateEditNotes-onNext--" + bean.getCode() + bean.getMessage());
                    }

                });
        if (isSync) {
            MyRxManager.getInstance().add(subscription);
        }
    }

    /**
     * 笔记更新：云端笔记同步到本地
     * <p>
     * 双层for循环
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     *
     * @param note_ids 云端所有笔记数据
     * @param listener
     */
    public void getCloudNote(List<AllNotesIdsBean.NoteIdItemBean> note_ids, final Vector<TNNote> allNotes, final INoteModuleListener listener) {
        Subscription subscription = Observable.from(note_ids)
                .concatMap(new Func1<AllNotesIdsBean.NoteIdItemBean, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(AllNotesIdsBean.NoteIdItemBean bean) {
                        final long cloudNoteId = bean.getId();
                        final int lastUpdate = bean.getUpdate_at();
                        //处理 note列表
                        boolean exit = false;
                        if (allNotes != null && allNotes.size() > 0) {
                            for (TNNote editNote : allNotes) {
                                if (cloudNoteId == editNote.noteId) {
                                    exit = true;
                                    if (editNote.lastUpdate > lastUpdate) {
                                        //更新笔记
                                        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                                                .getNoteByNoteId(cloudNoteId, settings.token)
                                                .subscribeOn(Schedulers.io())
                                                .doOnNext(new Action1<CommonBean3<GetNoteByNoteIdBean>>() {
                                                    @Override
                                                    public void call(CommonBean3<GetNoteByNoteIdBean> bean) {
                                                        //结果处理
                                                        setNoteResult(bean.getMsg(), cloudNoteId);
                                                        //数据处理
                                                        if (bean.getCode() == 0) {
                                                            GetNoteByNoteIdBean noteBean = bean.getNote();
                                                            updataCloudNoteSQL(noteBean);
                                                        }
                                                    }
                                                })
                                                .subscribeOn(Schedulers.io())
                                                .subscribeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Observer<CommonBean3<GetNoteByNoteIdBean>>() {
                                                    @Override
                                                    public void onCompleted() {

                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        MLog.e(TAG, "getCloudNote--onError--更新笔记失败" + e.toString());
                                                    }

                                                    @Override
                                                    public void onNext(CommonBean3<GetNoteByNoteIdBean> getNoteByNoteIdBeanCommonBean3) {

                                                    }
                                                });
                                    }
                                }

                            }
                        } else {
                            exit = false;
                        }
                        for (TNNote editNote : allNotes) {
                            if (cloudNoteId == editNote.noteId) {
                                exit = true;
                                if (editNote.lastUpdate > lastUpdate) {
                                    //更新笔记
                                    MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                                            .getNoteByNoteId(cloudNoteId, settings.token)
                                            .subscribeOn(Schedulers.io())
                                            .doOnNext(new Action1<CommonBean3<GetNoteByNoteIdBean>>() {
                                                @Override
                                                public void call(CommonBean3<GetNoteByNoteIdBean> bean) {
                                                    //结果处理
                                                    setNoteResult(bean.getMsg(), cloudNoteId);
                                                    //数据处理
                                                    if (bean.getCode() == 0) {
                                                        GetNoteByNoteIdBean noteBean = bean.getNote();
                                                        updataCloudNoteSQL(noteBean);
                                                    }
                                                }
                                            })
                                            .subscribeOn(Schedulers.io())
                                            .subscribeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Observer<CommonBean3<GetNoteByNoteIdBean>>() {
                                                @Override
                                                public void onCompleted() {

                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    MLog.e(TAG, "getCloudNote--onError--更新笔记失败" + e.toString());
                                                }

                                                @Override
                                                public void onNext(CommonBean3<GetNoteByNoteIdBean> getNoteByNoteIdBeanCommonBean3) {

                                                }
                                            });
                                }
                            }

                        }
                        //拿到遍历结果
                        if (exit == false) {//本地不存在远端的数据，就更新到本地
                            return MyHttpService.Builder.getHttpServer()//获取 noteid对应的数据，然后处理
                                    .getNoteByNoteId(cloudNoteId, settings.token)
                                    .subscribeOn(Schedulers.io())
                                    .doOnNext(new Action1<CommonBean3<GetNoteByNoteIdBean>>() {
                                        @Override
                                        public void call(CommonBean3<GetNoteByNoteIdBean> bean) {
                                            //结果处理
                                            setNoteResult(bean.getMsg(), cloudNoteId);
                                            //数据处理
                                            if (bean.getCode() == 0) {
                                                GetNoteByNoteIdBean noteBean = bean.getNote();
                                                updataCloudNoteSQL(noteBean);
                                            }
                                        }
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .concatMap(new Func1<CommonBean3<GetNoteByNoteIdBean>, Observable<Integer>>() {//
                                        @Override
                                        public Observable<Integer> call(CommonBean3<GetNoteByNoteIdBean> bean) {
                                            return Observable.just(bean.getCode());
                                        }
                                    });
                        } else {
                            //下一个循环
                            return Observable.empty();
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "getCloudNote--onCompleted");
                        listener.onCloudNoteSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.d(TAG, "getCloudNote--onError");
                        listener.onCloudNoteFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        MLog.d(TAG, "getCloudNote--onNext" + integer);
                    }
                });
        MyRxManager.getInstance().add(subscription);

    }


    /**
     * 同步回收站的笔记-1：获取所有回收站的笔记id
     * <p>
     * 代码同getAllNotsId一样逻辑
     *
     * @param listener
     */
    public void getTrashNotesId(final INoteModuleListener listener) {
        Subscription subscription = MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getTrashNoteIds(settings.token)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Action1<AllNotesIdsBean>() {
                    @Override
                    public void call(AllNotesIdsBean bean) {
                        //数据处理
                        if (bean.getCode() == 0) {
                            synCloudTrashNoteById(bean.getNote_ids());
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AllNotesIdsBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "getTrashNotesId--onCompleted");
                        listener.onGetTrashNoteIdSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("getTrashNotesId--onError:" + e.toString());
                        listener.onGetTrashNoteIdFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onNext(AllNotesIdsBean bean) {
                        if (bean.getCode() == 0) {
                            listener.onGetTrashNoteIdNext(bean);
                        }
                    }

                });
        MyRxManager.getInstance().add(subscription);
    }

    /**
     * 同步回收站的笔记-2：回收站笔记的处理（同步，删除等操作）
     * 双层for循环，最里层调用数据库和接口
     *
     * @param note_ids
     * @param allNotes
     * @param listener
     */
    public void upateTrashNotes(List<AllNotesIdsBean.NoteIdItemBean> note_ids, final Vector<TNNote> allNotes, final INoteModuleListener listener) {
        MLog.d("upateTrashNotes--回收站笔记--" + note_ids.size());
        Subscription subscription = Observable.from(note_ids)
                .concatMap(new Func1<AllNotesIdsBean.NoteIdItemBean, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(AllNotesIdsBean.NoteIdItemBean bean) {
                        final long cloudTrashNoteId = bean.getId();

                        //处理 note列表
                        boolean exit = false;
                        for (TNNote editNote : allNotes) {
                            if (cloudTrashNoteId == editNote.noteId) {
                                exit = true;
                            }
                        }
                        if (exit == false) {
                            //删除本地，同时下载远端数据
                            deleteTrashNoteSQL(cloudTrashNoteId);
                            return MyHttpService.Builder.getHttpServer()//获取 noteid对应的数据，然后处理
                                    .getNoteByNoteId(cloudTrashNoteId, settings.token)
                                    .subscribeOn(Schedulers.io())
                                    .doOnNext(new Action1<CommonBean3<GetNoteByNoteIdBean>>() {
                                        @Override
                                        public void call(CommonBean3<GetNoteByNoteIdBean> bean) {
                                            //结果处理
                                            setNoteResult(bean.getMsg(), cloudTrashNoteId);
                                            //数据处理
                                            if (bean.getCode() == 0) {
                                                GetNoteByNoteIdBean noteBean = bean.getNote();
                                                updataCloudNoteSQL(noteBean);
                                            }
                                        }
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .concatMap(new Func1<CommonBean3<GetNoteByNoteIdBean>, Observable<Integer>>() {//
                                        @Override
                                        public Observable<Integer> call(CommonBean3<GetNoteByNoteIdBean> bean) {
                                            return Observable.just(bean.getCode());
                                        }
                                    });
                        } else {
                            //下一个循环
                            return Observable.empty();
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "upateTrashNotes--onCompleted");
                        listener.onGetTrashNoteSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.d(TAG, "upateTrashNotes--onError--" + e.getMessage());
                        listener.onGetTrashNoteFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        MLog.d(TAG, "upateTrashNotes--onNext");
                    }
                });
        MyRxManager.getInstance().add(subscription);
    }

    /**
     * 获取文件夹id下的所有笔记（同getNoteListByTagId）
     * <p>
     */
    public void getNoteListByFolderId(final long tagId, final int mPageNum, final int pageSize, final String sort, final INoteModuleListener listener) {
        Subscription subscription = MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getNoteListByFolderId(tagId, mPageNum, pageSize, sort, settings.token)//接口方法
                .subscribeOn(Schedulers.io())
                .doOnNext(new Action1<NoteListBean>() {
                    @Override
                    public void call(NoteListBean bean) {
                        //数据处理
                        if (bean.getCode() == 0) {
                            insertDbNotes(bean, false);//异步
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<NoteListBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "getNoteListByFolderId--onCompleted");
                        listener.onNoteListByIdSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("getNoteListByTagId--onError:" + e.toString());
                        listener.onNoteListByIdFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onNext(NoteListBean bean) {
                        MLog.d(TAG, "getNoteListByFolderId-onNext");
                        //处理返回结果
                        if (bean.getCode() == 0) {
                            if (pageSize == TNConst.MAX_PAGE_SIZE) {
                                int currentCount = mPageNum * TNConst.PAGE_SIZE;
                                int count = bean.getCount();
                                if (count > currentCount) {
                                    int newPageNum = mPageNum;
                                    long newTagid = tagId;
                                    int newPageSize = TNConst.MAX_PAGE_SIZE;
                                    String newSort = sort;
                                    newPageNum++;
                                    getNoteListByFolderId(newTagid, newPageNum, newPageSize, newSort, listener);
                                }
                            }
                            listener.onNoteListByIdNext(bean);
                        }
                    }
                });
    }

    /**
     * 获取标签id下的所有笔记(同getNoteListByFolderId)
     * <p>
     */
    public void getNoteListByTagId(final long tagId, final int mPageNum, final int pageSize, final String sort, final INoteModuleListener listener) {
        Subscription subscription = MyHttpService.Builder.getHttpServer()
                .getNoteListByTagId(tagId, mPageNum, pageSize, sort, settings.token)
                .doOnNext(new Action1<NoteListBean>() {
                    @Override
                    public void call(NoteListBean bean) {
                        //数据处理
                        if (bean.getCode() == 0) {
                            insertDbNotes(bean, false);//异步
                        }
                    }
                })
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<NoteListBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "getNoteListByTagId--onCompleted");
                        listener.onNoteListByIdSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("getNoteListByTagId--onError:" + e.toString());
                        listener.onNoteListByIdFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onNext(NoteListBean bean) {
                        MLog.d(TAG, "getNoteListByTagId-onNext");
                        //处理返回结果
                        if (bean.getCode() == 0) {
                            if (pageSize == TNConst.MAX_PAGE_SIZE) {
                                int currentCount = mPageNum * TNConst.PAGE_SIZE;
                                int count = bean.getCount();
                                if (count > currentCount) {
                                    int newPageNum = mPageNum;
                                    long newTagid = tagId;
                                    int newPageSize = TNConst.MAX_PAGE_SIZE;
                                    String newSort = sort;
                                    newPageNum++;
                                    getNoteListByTagId(newTagid, newPageNum, newPageSize, newSort, listener);
                                }
                            }
                            listener.onNoteListByIdNext(bean);
                        }
                    }
                });
    }

    /**
     * 笔记更新：云端笔记同步到本地(folder文件夹下的同步)
     * <p>
     * 双层for循环
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     *
     * @param note_ids 云端所有笔记数据
     * @param listener
     */
    public void getCloudNoteByFolderId(List<AllNotesIdsBean.NoteIdItemBean> note_ids, final long folderId, final INoteModuleListener listener) {

        Subscription subscription = Observable.from(note_ids)
                .concatMap(new Func1<AllNotesIdsBean.NoteIdItemBean, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(AllNotesIdsBean.NoteIdItemBean bean) {
                        final long cloudNoteId = bean.getId();
                        final int lastUpdate = bean.getUpdate_at();
                        //处理 note列表
                        boolean exit = false;
                        Vector<TNNote> allNotes = TNDbUtils.getNoteListByCatId(TNSettings.getInstance().userId, folderId, TNSettings.getInstance().sort, TNConst.MAX_PAGE_SIZE);
                        for (TNNote editNote : allNotes) {
                            if (cloudNoteId == editNote.noteId) {
                                exit = true;
                                if (editNote.lastUpdate > lastUpdate) {
                                    //更新笔记
                                    MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                                            .getNoteByNoteId(cloudNoteId, settings.token)
                                            .subscribeOn(Schedulers.io())
                                            .doOnNext(new Action1<CommonBean3<GetNoteByNoteIdBean>>() {
                                                @Override
                                                public void call(CommonBean3<GetNoteByNoteIdBean> bean) {
                                                    //结果处理
                                                    setNoteResult(bean.getMsg(), cloudNoteId);
                                                    //数据处理
                                                    if (bean.getCode() == 0) {
                                                        GetNoteByNoteIdBean noteBean = bean.getNote();
                                                        updataCloudNoteSQL(noteBean);
                                                    }
                                                }
                                            })
                                            .subscribeOn(Schedulers.io())
                                            .subscribeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Observer<CommonBean3<GetNoteByNoteIdBean>>() {
                                                @Override
                                                public void onCompleted() {

                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    MLog.e(TAG, "getCloudNote--onError--更新笔记失败" + e.toString());
                                                }

                                                @Override
                                                public void onNext(CommonBean3<GetNoteByNoteIdBean> getNoteByNoteIdBeanCommonBean3) {

                                                }
                                            });
                                }
                            }
                            //
                            if (editNote.syncState == 1) {
                                //更新笔记
                                MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                                        .getNoteByNoteId(cloudNoteId, settings.token)
                                        .subscribeOn(Schedulers.io())
                                        .doOnNext(new Action1<CommonBean3<GetNoteByNoteIdBean>>() {
                                            @Override
                                            public void call(CommonBean3<GetNoteByNoteIdBean> bean) {
                                                //结果处理
                                                setNoteResult(bean.getMsg(), cloudNoteId);
                                                //数据处理
                                                if (bean.getCode() == 0) {
                                                    GetNoteByNoteIdBean noteBean = bean.getNote();
                                                    updataCloudNoteSQL(noteBean);
                                                }
                                            }
                                        })
                                        .subscribeOn(Schedulers.io())
                                        .subscribeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Observer<CommonBean3<GetNoteByNoteIdBean>>() {
                                            @Override
                                            public void onCompleted() {

                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                MLog.e(TAG, "getCloudNote--onError--更新笔记失败" + e.toString());
                                            }

                                            @Override
                                            public void onNext(CommonBean3<GetNoteByNoteIdBean> getNoteByNoteIdBeanCommonBean3) {

                                            }
                                        });
                            }

                        }
                        //拿到遍历结果
                        if (exit == false) {//本地不存在远端的数据，就更新到本地
                            return MyHttpService.Builder.getHttpServer()//获取 noteid对应的数据，然后处理
                                    .getNoteByNoteId(cloudNoteId, settings.token)
                                    .subscribeOn(Schedulers.io())
                                    .doOnNext(new Action1<CommonBean3<GetNoteByNoteIdBean>>() {
                                        @Override
                                        public void call(CommonBean3<GetNoteByNoteIdBean> bean) {
                                            //结果处理
                                            setNoteResult(bean.getMsg(), cloudNoteId);
                                            //数据处理
                                            if (bean.getCode() == 0) {
                                                GetNoteByNoteIdBean noteBean = bean.getNote();
                                                updataCloudNoteSQL(noteBean);
                                            }
                                        }
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .concatMap(new Func1<CommonBean3<GetNoteByNoteIdBean>, Observable<Integer>>() {//
                                        @Override
                                        public Observable<Integer> call(CommonBean3<GetNoteByNoteIdBean> bean) {
                                            return Observable.just(bean.getCode());
                                        }
                                    });
                        } else {
                            //下一个循环
                            return Observable.empty();
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "getCloudNote--onCompleted");
                        listener.onCloudNoteSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.d(TAG, "getCloudNote--onError");
                        listener.onCloudNoteFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        MLog.d(TAG, "getCloudNote--onNext" + integer);
                    }
                });

    }

    /**
     * 一条笔记下载详情
     * 两个接口串行，for循环接口调用
     * <p>
     */
    public void getDetailByNoteId(final long noteId, final INoteModuleListener listener) {

        Subscription subscription = MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getNoteByNoteId(noteId, settings.token)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Action1<CommonBean3<GetNoteByNoteIdBean>>() {
                    @Override
                    public void call(CommonBean3<GetNoteByNoteIdBean> bean) {
                        //更新数据库
                        if (bean.getCode() == 0) {
                            MLog.e("getDetailByNoteId--获取笔记详情--doOnError");
                            updataCloudNoteSQL(bean.getNote());
                        }
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        MLog.e("getDetailByNoteId--获取笔记详情--doOnError");
                    }
                })
                .concatMap(new Func1<CommonBean3<GetNoteByNoteIdBean>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(CommonBean3<GetNoteByNoteIdBean> dataBean) {
                        if (dataBean.getCode() == 0) {
                            TNNote note = TNDbUtils.getNoteByNoteId(noteId);
                            Vector<TNNoteAtt> atts = note.atts;

                            if (atts != null && atts.size() > 0) {
                                //下载文件数据
                                return Observable.from(atts)
                                        .concatMap(new Func1<TNNoteAtt, Observable<InputStream>>() {
                                            @Override
                                            public Observable<InputStream> call(TNNoteAtt att) {
                                                if (TNActionUtils.isDownloadingAtt(att.attId)) {
                                                    //下一个循环
                                                    return Observable.empty();
                                                }
                                                final String path = TNUtilsAtt.getAttPath(att.attId, att.type);
                                                MLog.d("下载附件路径：" + path);
                                                if (path == null) {
                                                    //下一个循环
                                                    return Observable.empty();
                                                }
                                                //方式从服务器下载附件
                                                //url绝对路径
                                                String url = URLUtils.API_BASE_URL + "attachment/" + att.attId + "?session_token=" + TNSettings.getInstance().token;
                                                return MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                                                        .downloadFile(url)//接口方法
                                                        .subscribeOn(Schedulers.io())
                                                        .unsubscribeOn(Schedulers.io())
                                                        .map(new Func1<ResponseBody, InputStream>() {//第一次转换，将流数据转成InputStream类数据
                                                            @Override
                                                            public InputStream call(ResponseBody responseBody) {
                                                                return responseBody.byteStream();
                                                            }
                                                        })
                                                        .observeOn(Schedulers.computation())
                                                        .doOnNext(new Action1<InputStream>() {
                                                            @Override
                                                            public void call(InputStream inputStream) {
                                                                //保存下载文件
                                                                MLog.e("getDetailByNoteId--保存文件--doOnNext");
                                                                writeFile(inputStream, new File(path));
                                                            }
                                                        })
                                                        .doOnError(new Action1<Throwable>() {
                                                            @Override
                                                            public void call(Throwable e) {
                                                                MLog.e("getDetailByNoteId--下载文件--onError--" + e.toString());
                                                            }
                                                        });
                                            }
                                        }).concatMap(new Func1<InputStream, Observable<Boolean>>() {
                                            @Override
                                            public Observable<Boolean> call(InputStream inputStream) {
                                                return Observable.just(true);
                                            }
                                        });

                            } else {
                                //无文件，返回
                                return Observable.empty();
                            }
                        } else {
                            return Observable.empty();
                        }
                    }
                })
                .doOnNext(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        MLog.d(TAG, "getDetailByNoteId--onNext=" + aBoolean);
                        if (aBoolean) {
                            upDataDetailNoteSQL(noteId);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "getDetailByNoteId--onCompleted");
                        listener.onDownloadNoteSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.d(TAG, "getDetailByNoteId--onError");
                        listener.onDownloadNoteFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        MLog.d(TAG, "getDetailByNoteId--onNext=" + aBoolean);

                    }
                });
    }


    //================================================处理相关（数据库处理已经是异步状态）================================================


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

    /**
     * 图片保存，就触发更新db
     *
     * @param attrId
     */
    private void upDataAttIdSQL(final long attrId, final TNNoteAtt tnNoteAtt) {
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().execSQL(TNSQLString.ATT_UPDATE_SYNCSTATE_ATTID, 2, attrId, (int) tnNoteAtt.noteLocalId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            MLog.e("upDataAttIdSQL" + e.toString());
            TNDb.endTransaction();
        } finally {
            TNDb.endTransaction();
        }
    }

    /**
     * 笔记详情，更新保存
     */
    private void upDataDetailNoteSQL(final long noteId) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    TNNote note = TNDbUtils.getNoteByNoteId(noteId);
                    note.syncState = 2;
                    if (note.attCounts > 0) {
                        for (int i = 0; i < note.atts.size(); i++) {
                            TNNoteAtt tempAtt = note.atts.get(i);
                            if (i == 0 && tempAtt.type > 10000 && tempAtt.type < 20000) {
                                TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, tempAtt.path, note.noteLocalId);
                            }
                            if (TextUtils.isEmpty(tempAtt.path) || "null".equals(tempAtt.path)) {
                                note.syncState = 1;
                            }
                        }
                    }
                    TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, note.syncState, note.noteLocalId);
                } catch (Exception e) {
                    MLog.e("upDataDetailNoteSQL" + e.toString());
                }

            }
        });

    }

    /**
     * 更新本地Note到数据库
     */
    private void upDataNoteLocalIdSQL(final NewNoteBean resultBean, final TNNote note) {

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                long id = resultBean.getId();
                TNDb.beginTransaction();
                try {
                    TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_NOTEID_BY_NOTELOCALID, id, note.noteLocalId);

                    TNDb.setTransactionSuccessful();
                } catch (Exception e) {
                    MLog.e("upDataNoteLocalIdSQL" + e.toString());
                    TNDb.endTransaction();
                } finally {
                    TNDb.endTransaction();
                }
            }
        });

    }

    /**
     * 回收站笔记更新 状态更改为 2：完全同步
     */
    private void recoveryPutNoteSQL(long noteId) {
        TNNote note = TNDbUtils.getNoteByNoteId(noteId);
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().execSQL(TNSQLString.NOTE_SET_TRASH, 0, 2, System.currentTimeMillis() / 1000, note.noteLocalId);

            TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);

            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            MLog.e("recoveryPutNoteSQL" + e.toString());
            TNDb.endTransaction();
        } finally {
            TNDb.endTransaction();
        }
    }

    /**
     * deleteNote 接口处理
     *
     * @param noteId
     */
    private void deleteNoteSQL(long noteId) {

        TNNote note = TNDbUtils.getNoteByNoteId(noteId);
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().execSQL(TNSQLString.NOTE_SET_TRASH, 2, 1, System.currentTimeMillis() / 1000, note.noteLocalId);
            if (note != null) {
                TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);
            }
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            MLog.e("deleteNoteSQL--error=" + e.toString());
            TNDb.endTransaction();
        } finally {
            TNDb.endTransaction();
        }
    }

    /**
     * deleteNotes
     * <p>
     */
    private int deleteLocalNoteSQL(final long noteLocalId) {
        //使用异步操作，完成后，执行下一个 position或接口
        TNDb.beginTransaction();
        try {
            //
            TNDb.getInstance().execSQL(TNSQLString.NOTE_SET_TRASH, 2, 6, System.currentTimeMillis() / 1000, noteLocalId);
            //
            TNNote note = TNDbUtils.getNoteByNoteLocalId(noteLocalId);
            if (note != null) {
                TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);
            }
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            MLog.e("deleteLocalNoteSQL" + e.toString());
            TNDb.endTransaction();
            return 0;//0表示成功，返回给rx的处理使用
        } finally {
            TNDb.endTransaction();
        }
        return 0;//0表示成功，返回给rx的处理使用

    }


    /**
     * @param nonteLocalID
     */
    private void deleteTrashNoteSQL(final long nonteLocalID) {
        TNNote note = TNDbUtils.getNoteByNoteId(nonteLocalID);
        if (note != null) {
            TNDb.beginTransaction();
            try {

                //
                TNDb.getInstance().execSQL(TNSQLString.NOTE_DELETE_BY_NOTEID, nonteLocalID);

                TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);

                TNDb.setTransactionSuccessful();
            } catch (Exception e) {
                MLog.e("deleteTrashNoteSQL" + e.toString());
                TNDb.endTransaction();
            } finally {
                TNDb.endTransaction();
            }
        }

    }


    /**
     * 与云端同步数据比较，以云端为主，本地不存在的就删除（异步中进行的）
     */
    private void synCloudNoteById(final List<AllNotesIdsBean.NoteIdItemBean> cloudIds) {

        Vector<TNNote> allNotes = TNDbUtils.getAllNoteList(TNSettings.getInstance().userId);
        for (int i = 0; i < allNotes.size(); i++) {
            boolean isExit = false;
            TNNote note = allNotes.get(i);
            for (int j = 0; j < cloudIds.size(); j++) {
                if (note.noteId == cloudIds.get(j).getId()) {
                    isExit = true;
                    break;
                }
            }
            if (!isExit && note.syncState != 7) {
                TNDb.beginTransaction();
                try {
                    //
                    MLog.d("删除本地笔记--" + note.title);
                    TNDb.getInstance().deleteSQL(TNSQLString.NOTE_DELETE_BY_NOTEID, new Object[]{note.noteId});

                    TNDb.setTransactionSuccessful();
                } catch (Exception e) {
                    MLog.e("synCloudNoteById" + e.toString());
                    TNDb.endTransaction();
                } finally {
                    TNDb.endTransaction();
                }
            }
        }

    }

    /**
     *
     */
    private void synCloudTrashNoteById(final List<AllNotesIdsBean.NoteIdItemBean> cloudTrashIds) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Vector<TNNote> trashNotes = TNDbUtils.getNoteListByTrash(settings.userId, TNConst.CREATETIME);
                //
                for (TNNote note : trashNotes) {
                    boolean isExit = false;
                    //查询本地是否存在
                    for (AllNotesIdsBean.NoteIdItemBean cloudBean : cloudTrashIds) {
                        if (note.noteId == cloudBean.getId()) {
                            isExit = true;
                            break;
                        }
                    }
                    //不存在就删除  /使用异步
                    if (!isExit && note.syncState != 7) {
                        TNDb.beginTransaction();
                        try {
                            //
                            TNDb.getInstance().deleteSQL(TNSQLString.NOTE_DELETE_BY_NOTEID, new Object[]{note.noteId});

                            TNDb.setTransactionSuccessful();
                        } catch (Exception e) {
                            MLog.e("synCloudTrashNoteById" + e.toString());
                            TNDb.endTransaction();
                        } finally {
                            TNDb.endTransaction();
                        }

                    }
                }
            }
        });
    }

    /**
     * 更新编辑笔记的状态
     *
     * @param noteId
     */
    private void updataEditNotesStateSQL(final long noteId) {
        TNDb.beginTransaction();
        try {
            //
            TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, 1, noteId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            MLog.e("updataEditNotesStateSQL" + e.toString());
            TNDb.endTransaction();
        } finally {
            TNDb.endTransaction();
        }
    }

    /**
     * 更新编辑的笔记
     *
     * @param note
     */
    private void updataEditNoteSQL(TNNote note) {
        String shortContent = TNUtils.getBriefContent(note.content);
        TNDb.beginTransaction();
        try {
            //
            TNDb.getInstance().execSQL(TNSQLString.NOTE_SHORT_CONTENT, shortContent, note.noteId);
            //
            TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);

            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            MLog.e("updataEditNoteSQL" + e.toString());
            TNDb.endTransaction();
        } finally {
            TNDb.endTransaction();
        }
    }

    /**
     * 同步云端笔记到本地(getNoteById)，存储到数据库
     *
     * @param bean
     */
    private void updataCloudNoteSQL(GetNoteByNoteIdBean bean) {

        long noteId = bean.getId();
        String contentDigest = bean.getContent_digest();
        TNNote note = TNDbUtils.getNoteByNoteId(noteId);//在全部笔记页同步，会走这里，没在首页同步过的返回为null

        int syncState = note == null ? 1 : note.syncState;
        List<GetNoteByNoteIdBean.TagBean> tags = bean.getTags();

        String tagStr = "";
        for (int k = 0; k < tags.size(); k++) {
            //设置tagStr
            GetNoteByNoteIdBean.TagBean tempTag = tags.get(k);
            String tag = tempTag.getName();
            if ("".equals(tag)) {
                continue;
            }
            if (tags.size() == 1) {
                tagStr = tag;
            } else {
                if (k == (tags.size() - 1)) {
                    tagStr = tagStr + tag;
                } else {
                    tagStr = tagStr + tag + ",";
                }
            }
        }

        String thumbnail = "";
        if (note != null) {
            thumbnail = note.thumbnail;
            Vector<TNNoteAtt> localAtts = TNDbUtils.getAttrsByNoteLocalId(note.noteLocalId);
            List<GetNoteByNoteIdBean.Attachments> atts = bean.getAttachments();
            if (localAtts.size() != 0) {
                //循环判断是否与线上同步，线上没有就删除本地
                for (int k = 0; k < localAtts.size(); k++) {
                    boolean exit = false;
                    TNNoteAtt tempLocalAtt = localAtts.get(k);
                    for (int i = 0; i < atts.size(); i++) {
                        GetNoteByNoteIdBean.Attachments tempAtt = atts.get(i);
                        long attId = tempAtt.getId();
                        if (tempLocalAtt.attId == attId) {
                            exit = true;
                        }
                    }
                    if (!exit) {
                        if (thumbnail.indexOf(String.valueOf(tempLocalAtt.attId)) != 0) {
                            thumbnail = "";
                        }
                        NoteAttrDbHelper.deleteAttById(tempLocalAtt.attId);
                    }
                }
                //循环判断是否与线上同步，本地没有就插入数据
                for (int k = 0; k < atts.size(); k++) {
                    GetNoteByNoteIdBean.Attachments tempAtt = atts.get(k);
                    long attId = tempAtt.getId();
                    boolean exit = false;
                    for (int i = 0; i < localAtts.size(); i++) {
                        TNNoteAtt tempLocalAtt = localAtts.get(i);
                        if (tempLocalAtt.attId == attId) {
                            exit = true;
                        }
                    }
                    if (!exit) {
                        syncState = 1;
                        insertAttr(tempAtt, note.noteLocalId);
                    }
                }
            } else {
                for (int i = 0; i < atts.size(); i++) {
                    GetNoteByNoteIdBean.Attachments tempAtt = atts.get(i);
                    syncState = 1;
                    insertAttr(tempAtt, note.noteLocalId);
                }
            }

            //如果本地的更新时间晚就以本地的为准
            if (note.lastUpdate > (com.thinkernote.ThinkerNote.Utils.TimeUtils.getMillsOfDate(bean.getUpdate_at()) / 1000)) {
                return;
            }

            if (atts.size() == 0) {
                syncState = 2;
            }
        }

        int catId = -1;
        // getFolder_id可以为负值么
        if (bean.getFolder_id() > 0) {
            catId = bean.getFolder_id();
        }
        JSONObject tempObj = new JSONObject();
        try {
            tempObj.put("title", bean.getTitle());
            tempObj.put("userId", TNSettings.getInstance().userId);
            tempObj.put("trash", bean.getTrash());
            tempObj.put("source", "android");
            tempObj.put("catId", catId);
            tempObj.put("createTime", com.thinkernote.ThinkerNote.Utils.TimeUtils.getMillsOfDate(bean.getCreate_at()) / 1000);
            tempObj.put("lastUpdate", com.thinkernote.ThinkerNote.Utils.TimeUtils.getMillsOfDate(bean.getUpdate_at()) / 1000);
            tempObj.put("syncState", syncState);
            tempObj.put("noteId", noteId);
            tempObj.put("shortContent", TNUtils.getBriefContent(bean.getContent()));
            tempObj.put("tagStr", tagStr);
            tempObj.put("lbsLongitude", bean.getLongitude() <= 0 ? 0 : bean.getLongitude());
            tempObj.put("lbsLatitude", bean.getLatitude() <= 0 ? 0 : bean.getLatitude());
            tempObj.put("lbsRadius", bean.getRadius() <= 0 ? 0 : bean.getRadius());
            tempObj.put("lbsAddress", bean.getAddress());
            tempObj.put("nickName", TNSettings.getInstance().username);
            tempObj.put("thumbnail", thumbnail);
            tempObj.put("contentDigest", contentDigest);
            tempObj.put("content", TNUtilsHtml.codeHtmlContent(bean.getContent(), true));

            //等价上边写法，没有问题，不用删
//            JSONObject tempObj = TNUtils.makeJSON(
//                    "title", bean.getTitle(),
//                    "userId", TNSettings.getInstance().userId,
//                    "trash", bean.getTrash(),
//                    "source", "android",
//                    "catId", catId,
//                    "content", TNUtilsHtml.codeHtmlContent(bean.getContent(), true)),
//                    "createTime", com.thinkernote.ThinkerNote.Utils.TimeUtils.getMillsOfDate(bean.getCreate_at()) / 1000),
//                    "lastUpdate", com.thinkernote.ThinkerNote.Utils.TimeUtils.getMillsOfDate(bean.getUpdate_at()) / 1000),
//                    "syncState", syncState,
//                    "noteId", noteId,
//                    "shortContent", TNUtils.getBriefContent(bean.getContent()),
//                    "tagStr", tagStr,
//                    "lbsLongitude", bean.getLongitude() <= 0 ? 0 : bean.getLongitude(),
//                    "lbsLatitude", bean.getLatitude() <= 0 ? 0 : bean.getLatitude(),
//                    "lbsRadius", bean.getRadius() <= 0 ? 0 : bean.getRadius(),
//                    "lbsAddress", bean.getAddress(),
//                    "nickName", TNSettings.getInstance().username,
//                    "thumbnail", thumbnail,
//                    "contentDigest", contentDigest
//            );

            if (note == null)
                NoteDbHelper.addOrUpdateNote(tempObj);
            else
                NoteDbHelper.updateNote(tempObj);
        } catch (Exception e) {
            TNApplication.getInstance().htmlError("笔记:" + bean.getTitle() + "  " + bean.getCreate_at() + "需要到网页版中" + "\n" + "+修改成新版app支持的格式,新版app不支持网页抓去 \n或者删除该笔记");

        }
    }

    public static void insertAttr(GetNoteByNoteIdBean.Attachments tempAtt, long noteLocalId) {
        long attId = tempAtt.getId();
        String digest = tempAtt.getDigest();
        //
        TNNoteAtt noteAtt = TNDbUtils.getAttrById(attId);
        noteAtt.attName = tempAtt.getName();
        noteAtt.type = tempAtt.getType();
        noteAtt.size = tempAtt.getSize();
        noteAtt.syncState = 1;

        JSONObject tempObj = TNUtils.makeJSON(
                "attName", noteAtt.attName,
                "type", noteAtt.type,
                "path", noteAtt.path,
                "noteLocalId", noteLocalId,
                "size", noteAtt.size,
                "syncState", noteAtt.syncState,
                "digest", digest,
                "attId", attId,
                "width", noteAtt.width,
                "height", noteAtt.height
        );
        NoteAttrDbHelper.addOrUpdateAttr(tempObj);
    }

    /**
     * 返回数据的再次处理
     */
    private void setNoteResult(String msg, long noteId) {
        if ("login required".equals(msg)) {
            MLog.e(TAG, msg);
            TNUtilsUi.showToast(msg);
            TNUtils.goToLogin(context.getApplicationContext());
        } else if ("笔记不存在".equals(msg)) {
            try {
                MLog.e(TAG, "笔记不存在：删除");
                TNDb.getInstance().execSQL(TNSQLString.NOTE_DELETE_BY_NOTEID, noteId);
            } catch (Exception e) {
                MLog.e(TAG, "笔记不存在：" + e.toString());
            }
        } else if ("标签不存在".equals(msg)) {
            try {
                MLog.e(TAG, "标签不存在：删除");
                TNDb.getInstance().execSQL(TNSQLString.TAG_REAL_DELETE, noteId);
            } catch (Exception e) {
                MLog.e(TAG, "标签不存在：" + e.toString());
            }
        } else if ("文件夹不存在".equals(msg)) {
            try {
                MLog.e(TAG, "文件夹不存在：删除");
                TNDb.getInstance().execSQL(TNSQLString.CAT_DELETE_CAT, noteId);
            } catch (Exception e) {
                MLog.e(TAG, "文件夹不存在：" + e.toString());
            }
        } else {
            MLog.d(TAG, "setNoteResult--" + msg);
        }

    }

    /**
     * 某id下笔记存储（tag下笔记，回收站笔记）
     *
     * @param bean
     * @param isTrash
     */
    public static void insertDbNotes(final NoteListBean bean, final boolean isTrash) {
        List<NoteListBean.NoteItemBean> notesObj = bean.getNotes();
        if (notesObj == null || notesObj.size() <= 0) {
            return;
        }
        int trash = isTrash ? 2 : 0;
        for (int i = 0; i < notesObj.size(); i++) {
            NoteListBean.NoteItemBean obj = notesObj.get(i);
            long noteId = obj.getId();
            long lastUpdate = TNUtils.formatStringToTime((obj.getUpdate_at()) + "") / 1000;

            List<NoteListBean.NoteItemBean.TagItemBean> tags = obj.getTags();
            String tagStr = "";
            for (int k = 0; k < tags.size(); k++) {
                NoteListBean.NoteItemBean.TagItemBean tempTag = tags.get(k);
                String tag = tempTag.getName();
                if ("".equals(tag)) {
                    continue;
                }
                if (tags.size() == 1) {
                    tagStr = tag;
                } else {
                    if (k == (tags.size() - 1)) {
                        tagStr = tagStr + tag;
                    } else {
                        tagStr = tagStr + tag + ",";
                    }
                }
            }

            int catId = -1;
            if (obj.getFolder_id() > 0) {
                catId = obj.getFolder_id();
            } else {
                catId = -1;
            }

            int syncState = 1;
            TNNote note = TNDbUtils.getNoteByNoteId(noteId);
            if (note != null) {
                if (note.lastUpdate > lastUpdate) {
                    continue;
                } else {
                    syncState = note.syncState;
                }
            }
            JSONObject tempObj = TNUtils.makeJSON(
                    "title", obj.getTitle(),
                    "userId", TNSettings.getInstance().userId,
                    "trash", trash,
                    "source", "android",
                    "catId", catId,
                    "content", obj.getSummary(),
                    "createTime", com.thinkernote.ThinkerNote.Utils.TimeUtils.getMillsOfDate(obj.getCreate_at()) / 1000,
                    "lastUpdate", lastUpdate,
                    "syncState", syncState,
                    "noteId", noteId,
                    "shortContent", obj.getSummary(),
                    "tagStr", tagStr,
                    "lbsLongitude", 0,
                    "lbsLatitude", 0,
                    "lbsRadius", 0,
                    "lbsAddress", "",
                    "nickName", TNSettings.getInstance().username,
                    "thumbnail", "",
                    "contentDigest", obj.getContent_digest()
            );
            NoteDbHelper.addOrUpdateNote(tempObj);
        }
    }
}
