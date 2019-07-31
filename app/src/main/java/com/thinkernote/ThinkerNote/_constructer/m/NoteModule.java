package com.thinkernote.ThinkerNote._constructer.m;

import android.content.Context;
import android.text.TextUtils;

import com.thinkernote.ThinkerNote.DBHelper.NoteAttrDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.NoteDbHelper;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.listener.m.INoteModuleListener;
import com.thinkernote.ThinkerNote.base.TNApplication;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean3;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.GetNoteByNoteIdBean;
import com.thinkernote.ThinkerNote.bean.main.NewNoteBean;
import com.thinkernote.ThinkerNote.bean.main.OldNotePicBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;
import com.thinkernote.ThinkerNote.http.RequestBodyUtil;
import com.thinkernote.ThinkerNote.http.URLUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
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

        Observable.from(notes)
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
                                                upDataAttIdSQL(oldNotePicBean.getId(), tnNoteAtt);
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
    }

    /**
     * 笔记更新：上传本地新增笔记
     * 两个接口，一个for循环的图片上传
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     *
     * @param notes    syncState=3 的数据
     * @param listener
     */
    public void updateLocalNewNotes(Vector<TNNote> notes, final INoteModuleListener listener) {

        Observable.from(notes)
                .concatMap(new Func1<TNNote, Observable<TNNote>>() {//
                    @Override
                    public Observable<TNNote> call(final TNNote tnNote) {
                        Vector<TNNoteAtt> oldNotesAtts = tnNote.atts;//文件列表
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
                                                upDataAttIdSQL(oldNotePicBean.getId(), tnNoteAtt);
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
                })
                .concatMap(new Func1<TNNote, Observable<NewNoteBean>>() {//
                    @Override
                    public Observable<NewNoteBean> call(final TNNote note) {
                        return MyHttpService.Builder.getHttpServer()//(接口2，上传note)
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
                        listener.onUpdateLocalNoteSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onUpdateLocalNoteFailed(new Exception(e.getMessage()), null);
                    }

                    @Override
                    public void onNext(NewNoteBean newNoteBean) {//主线程

                    }
                });
    }

    /**
     * 笔记更新：从回收站还原 TODO 需要测试
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     * <p>
     * 3个接口+两层for循环的转换，单层for循环的两个接口判断调用，zip合并不同数据源,统一处理
     *
     * @param notes    syncState=7 的数据
     * @param listener
     */
    public void updateRecoveryNotes(Vector<TNNote> notes, final INoteModuleListener listener) {

        Observable
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
                                .from(oldNotesAtts).concatMap(new Func1<TNNoteAtt, Observable<OldNotePicBean>>() {//先上传每一个文件
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
    public void deleteNotes(Vector<TNNote> notes, final INoteModuleListener listener) {
        Observable.from(notes).concatMap(new Func1<TNNote, Observable<Integer>>() {
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
    public void clearNotes(Vector<TNNote> notes, final INoteModuleListener listener) {
        Observable.from(notes)
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
                                        if (commonBean.getCode() == 0) {
                                            deleteNoteSQL(tnNote.noteId);
                                        }
                                    }
                                })
                                .concatMap(new Func1<CommonBean, Observable<Integer>>() {//转换 结果类型，保持 不同结果类型转后(CommonBean,int)，有相同的结果类型（int）
                                    @Override
                                    public Observable<Integer> call(CommonBean commonBean) {
                                        //再调用第二个接口
                                        Observable<Integer> clearTrashObservable = MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                                                .deleteTrashNote2(tnNote.noteId, settings.token)
                                                .subscribeOn(Schedulers.io())
                                                .doOnNext(new Action1<CommonBean>() {
                                                    @Override
                                                    public void call(CommonBean commonBean) {
                                                        //数据处理
                                                        if (commonBean.getCode() == 0) {
                                                            deleteTrashNoteSQL(tnNote.noteLocalId);
                                                        }
                                                    }
                                                }).concatMap(new Func1<CommonBean, Observable<? extends Integer>>() {
                                                    @Override
                                                    public Observable<? extends Integer> call(CommonBean commonBean) {
                                                        int deleteTrashNoteResult = commonBean.getCode();
                                                        return Observable.just(deleteTrashNoteResult);
                                                    }
                                                });
                                        if (commonBean.getCode() == 0) {
                                            //deleteNote 调用成功后，调用 deleteTrashNote2 接口
                                            return clearTrashObservable;
                                        } else {
                                            int deleteNoteResult = commonBean.getCode();
                                            return Observable.just(deleteNoteResult);
                                        }
                                    }
                                }).subscribeOn(Schedulers.io());

                        if (tnNote.noteId == -1) {
                            // 不调用接口，直接数据库处理
                            int clearSqlResut = deleteLocalNoteSQL(tnNote.noteLocalId);
                            return Observable.just(clearSqlResut);
                        } else {
                            //需要调用接口（两个接口串行执行）
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
                        if (result != 0) {
                            listener.onClearNoteFailed(new Exception("彻底删除笔记异常返回"), null);
                        }
                    }
                });

    }

    /**
     * 笔记更新：获取所有笔记id(不包括回收站笔记)
     * <p>
     *
     * @param listener
     */
    public void getAllNotesId(final INoteModuleListener listener) {

        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncAllNotsId(settings.token)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Action1<AllNotesIdsBean>() {
                    @Override
                    public void call(AllNotesIdsBean bean) {
                        MLog.d(TAG, "getAllNotsId--doOnNext");
                        //数据处理
                        if (bean.getCode() == 0) {
                            synCloudNote(bean);
                        }
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        MLog.d(TAG, "getAllNotsId--doOnError--" + e.toString());
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
                        MLog.d(TAG, "getAllNotsId-onNext--" + bean.getCode() + "--" + bean.getMessage());
                        if (bean.getCode() == 0) {
                            listener.onGetAllNoteIdNext(bean);
                        }
                    }

                });
    }

    /**
     * 更新编辑的笔记 4
     * 流程介绍：
     * 双层for循环,主流程是 note_ids循环下嵌套 editNotes循环，editNotes下根据编辑状态，处理不同状态，先判断是否需要上传图片，在判断是否需要上传笔记。
     * <p>
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     *
     * @param listener
     */
    public void updateEditNotes(List<AllNotesIdsBean.NoteIdItemBean> note_ids, final Vector<TNNote> notes, final INoteModuleListener listener) {
        Observable.from(note_ids)
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

                                                //拼接url(本app后台特殊嗜好，蛋疼):
                                                String url = URLUtils.API_BASE_URL + URLUtils.Home.UPLOAD_FILE + "?" + "filename=" + file.getName() + "&session_token=" + settings.token;
                                                MLog.d("FeedBackPic", "url=" + url + "\nfilename=" + file.toString() + "---" + file.getName());
                                                url = url.replace(" ", "%20");//文件名有空格

                                                // 上传图片的处理，
                                                Observable<TNNote> upResultObservier = MyHttpService.UpLoadBuilder.UploadServer()//(接口1，上传图片)
                                                        .uploadPic(url, part)//接口方法
                                                        .subscribeOn(Schedulers.io())//固定样式
                                                        .doOnNext(new Action1<OldNotePicBean>() {
                                                            @Override
                                                            public void call(OldNotePicBean oldNotePicBean) {
                                                                //（1）更新图片--数据库(意见反馈不走这一块)
                                                                if (oldNotePicBean.getCode() == 0) {
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
                                                    return upResultObservier;
                                                }
                                            }

                                        });
                                //if处理不同情况
                                if (cloudNoteId == editNote.noteId) {//
                                    if (editNote.lastUpdate > lastUpdate) {//用于上传 TNNote
                                        return editNoteFilesObservable;
                                    } else {//
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
    }

    /**
     * 笔记更新：云端笔记同步到本地
     * <p>
     * 双层for循环
     *
     * @param note_ids 云端所有笔记数据
     * @param listener
     */
    public void getCloudNote(List<AllNotesIdsBean.NoteIdItemBean> note_ids, final INoteModuleListener listener) {
        final Vector<TNNote> allNotes = TNDbUtils.getAllNoteList(TNSettings.getInstance().userId);
        Observable.from(note_ids)
                .concatMap(new Func1<AllNotesIdsBean.NoteIdItemBean, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(AllNotesIdsBean.NoteIdItemBean bean) {

                        final long cloudNoteId = bean.getId();
                        final int lastUpdate = bean.getUpdate_at();
                        //处理 note列表
                        return Observable.from(allNotes)
                                .concatMap(new Func1<TNNote, Observable<Boolean>>() {
                                    @Override
                                    public Observable<Boolean> call(TNNote editNote) {//
                                        TNNote mNote = editNote;//避免数据死这里
                                        //if处理不同情况
                                        if (cloudNoteId == mNote.noteId) {//
                                            if (mNote.lastUpdate > lastUpdate) {// 有更新的笔记，则重新更新
                                                //更新笔记
                                                return MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
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
                                                        .subscribeOn(Schedulers.io()).concatMap(new Func1<CommonBean3<GetNoteByNoteIdBean>, Observable<Boolean>>() {
                                                            @Override
                                                            public Observable<Boolean> call(CommonBean3<GetNoteByNoteIdBean> bean) {
                                                                return Observable.just(true);
                                                            }
                                                        });
                                            } else {
                                                return Observable.just(true);
                                            }
                                        } else {
                                            //继续下一个循环
                                            return Observable.just(false);
                                        }
                                    }
                                })
                                .concatMap(new Func1<Boolean, Observable<Integer>>() {//allNotes处理完的结果，继续处理
                                    @Override
                                    public Observable<Integer> call(Boolean aBoolean) {//aBoolean=true表示 本地和远端都存在
                                        if (aBoolean == false) {//本地不存在远端的数据，就更新到本地
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
                                            return Observable.just(0);//表示不需要处理，参数个人定义，无用
                                        }
                                    }
                                });
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
                        MLog.d(TAG, "getCloudNote--onNext");
                    }
                });


    }

    /**
     * 同步回收站的笔记-1：获取所有回收站的笔记id
     * <p>
     * 代码同getAllNotsId一样逻辑
     *
     * @param listener
     */
    public void getTrashNotesId(final INoteModuleListener listener) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getTrashNoteIds(settings.token)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Action1<AllNotesIdsBean>() {
                    @Override
                    public void call(AllNotesIdsBean bean) {
                        //数据处理
                        if (bean.getCode() == 0) {
                            synCloudNote(bean);
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
        Observable.from(note_ids)
                .concatMap(new Func1<AllNotesIdsBean.NoteIdItemBean, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(AllNotesIdsBean.NoteIdItemBean bean) {
                        final long cloudTrashNoteId = bean.getId();
                        //处理 note列表
                        return Observable.from(allNotes)
                                .concatMap(new Func1<TNNote, Observable<Boolean>>() {
                                    @Override
                                    public Observable<Boolean> call(final TNNote editNote) {//
                                        //if处理不同情况
                                        if (cloudTrashNoteId == editNote.noteId) {//
                                            return Observable.just(true);//本地和远端是否都存在
                                        } else {
                                            //继续下一个循环
                                            return Observable.just(false);
                                        }
                                    }
                                })
                                .concatMap(new Func1<Boolean, Observable<Integer>>() {//allNotes处理完的结果，继续处理
                                    @Override
                                    public Observable<Integer> call(Boolean aBoolean) {//aBoolean=true表示 本地和远端都存在
                                        if (aBoolean == false) {//本地不存在远端的数据，就更新到本地
                                            //删除本地，同时下载远端数据
                                            deleteTrashNoteSQL(cloudTrashNoteId);
                                            //
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
                                            return Observable.just(0);//表示不需要处理，参数个人定义，无用
                                        }
                                    }
                                });
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

    }

    //================================================处理相关（数据库处理已经是异步状态）================================================

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
        } finally {
            TNDb.endTransaction();
        }
    }

    /**
     * 更新Note
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
            TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);

            TNDb.setTransactionSuccessful();
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
            TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);
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
        TNDb.beginTransaction();
        try {
            TNNote note = TNDbUtils.getNoteByNoteId(nonteLocalID);
            //
            TNDb.getInstance().execSQL(TNSQLString.NOTE_DELETE_BY_NOTEID, nonteLocalID);

            TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);

            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }

    }


    /**
     * 与云端同步数据比较，以云端为主，本地不存在的就删除（异步中进行的）
     */
    private void synCloudNote(AllNotesIdsBean bean) {
        List<AllNotesIdsBean.NoteIdItemBean> cloudIds = bean.getNote_ids();
        //
        Vector<TNNote> allNotes = TNDbUtils.getAllNoteList(TNSettings.getInstance().userId);
        //
        for (int i = 0; i < allNotes.size(); i++) {
            boolean isExit = false;
            final TNNote note = allNotes.get(i);
            //查询本地是否存在
            for (int j = 0; j < cloudIds.size(); j++) {
                if (note.noteId == cloudIds.get(j).getId()) {
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
                } finally {
                    TNDb.endTransaction();
                }

            }
        }

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
        } finally {
            TNDb.endTransaction();
        }
        MLog.d("数据库--更新编辑笔记状态--成功");
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
        } finally {
            TNDb.endTransaction();
        }
        MLog.d("数据库--更新编辑笔记内容--成功");
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
            MLog.e("2-11-2--updateNote异常：" + e.toString());
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
            //TODO
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
}
