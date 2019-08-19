package com.thinkernote.ThinkerNote.mvp.m;

import android.content.Context;
import android.util.Log;

import com.thinkernote.ThinkerNote.DBHelper.CatDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.UserDbHelper;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.mvp.listener.m.IFolderModuleListener;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnCatInfoListener;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnCatListListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;
import com.thinkernote.ThinkerNote.mvp.http.MyHttpService;
import com.thinkernote.ThinkerNote.mvp.http.MyRxManager;

import org.json.JSONObject;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 具体实现:
 * m层 文件夹相关
 */
public class FolderModule {

    private Context context;
    private static final String TAG = "Folder";
    private TNSettings settings;
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public FolderModule(Context context) {
        this.context = context;
        settings = TNSettings.getInstance();
    }

    //================================================接口相关================================================

    /**
     * 第一次登陆，创建默认文件夹（向后台发送要创建的文件夹）
     *
     * @param arrayFolders
     * @param listener
     */
    public void createFolderByFirstLaunch(String[] arrayFolders, long id, final IFolderModuleListener listener) {

        final String[] mFolderName = {""};
        //创建默认的一级文件夹
        Subscription subscription = Observable.from(arrayFolders)
                .concatMap(new Func1<String, Observable<CommonBean>>() {
                    @Override
                    public Observable<CommonBean> call(String folderName) {
                        mFolderName[0] = folderName;
                        //拿到list的item数据 访问接口
                        return MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                                .addNewFolder(folderName, settings.token)
                                .subscribeOn(Schedulers.io());//固定样式
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CommonBean>() {
                    @Override
                    public void onCompleted() {
                        listener.onAddDefaultFolderSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onAddFolderFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "createFolderByFirstLaunch--addNewFolder--onCompleted--" + bean.getMessage());
                        if (bean.getCode() == 0) {
                        } else if (bean.getMessage().equals("文件夹已存在")) {
                            //不处理
                        } else {
                        }
                    }
                });
        MyRxManager.getInstance().add(subscription);
    }

    /**
     * 创建 文件夹下的子文件夹
     */
    public void createFolderByIdByFirstLaunch(Vector<TNCat> cats, final String[] works, final String[] life, final String[] funs, final IFolderModuleListener listener) {
        MLog.d(TAG, "addNewFolder--创建子文件夹");
        Subscription subscription = Observable.from(cats)
                .concatMap(new Func1<TNCat, Observable<CommonBean>>() {
                    @Override
                    public Observable<CommonBean> call(TNCat tnCat) {
                        if (TNConst.GROUP_WORK.equals(tnCat.catName)) {
                            return Observable.from(works)
                                    .concatMap(new Func1<String, Observable<CommonBean>>() {
                                        @Override
                                        public Observable<CommonBean> call(String workName) {
                                            return MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                                                    .addNewFolder(workName, settings.token)//接口方法
                                                    .subscribeOn(Schedulers.io());
                                        }
                                    });
                        } else if (TNConst.GROUP_LIFE.equals(tnCat.catName)) {
                            return Observable.from(life)
                                    .concatMap(new Func1<String, Observable<CommonBean>>() {
                                        @Override
                                        public Observable<CommonBean> call(String lifeName) {
                                            return MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                                                    .addNewFolder(lifeName, settings.token)//接口方法
                                                    .subscribeOn(Schedulers.io())
                                                    .subscribeOn(Schedulers.io());
                                        }
                                    });
                        } else if (TNConst.GROUP_FUN.equals(tnCat.catName)) {
                            return Observable.from(funs)
                                    .concatMap(new Func1<String, Observable<CommonBean>>() {
                                        @Override
                                        public Observable<CommonBean> call(String funsName) {
                                            return MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                                                    .addNewFolder(funsName, settings.token)//接口方法
                                                    .subscribeOn(Schedulers.io());
                                        }
                                    });
                        }
                        return Observable.empty();
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CommonBean>() {
                    @Override
                    public void onCompleted() {
                        listener.onAddDefaultFolderIdSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onAddFolderFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "createFolderByIdByFirstLaunch--addNewFolder--onCompleted" + bean.getMessage());
                        if (bean.getCode() == 0) {
                        } else if (bean.getMessage().equals("文件夹已存在")) {
                            //不处理
                        } else {
                        }
                    }
                });
        MyRxManager.getInstance().add(subscription);

    }

    /**
     * 获取所有数据
     *
     * @param listener
     */
    public void getProfiles(final IFolderModuleListener listener) {

        Subscription subscription = MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .LogNormalProfile(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .doOnNext(new Action1<CommonBean2<ProfileBean>>() {
                    @Override
                    public void call(CommonBean2<ProfileBean> bean) {
                        if (bean.getCode() == 0) {

                        }
                        updateProfileSQL(bean.getProfile());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean2<ProfileBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("getProfiles--onError" + e.toString());
                        listener.onProfileFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean2<ProfileBean> bean) {
                        MLog.d(TAG, "getProfiles-onNext" + bean.getCode() + "--" + bean.getMsg());
                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onProfileSuccess(bean.getProfile());
                        } else {
                            listener.onProfileFailed(bean.getMsg(), null);
                        }
                    }
                });
        MyRxManager.getInstance().add(subscription);
    }

    /**
     * 获取所有文件夹数据（顶层文件夹）
     * 包含多个接口调用
     * 先单一接口，获取list,5层for循环遍历接口
     *
     * @param listener
     */
    public void getAllFolder(final IFolderModuleListener listener) {

        Subscription subscription = MyHttpService.Builder.getHttpServer()
                .getFolder(settings.token) //（1）获取第一个接口数据
                .subscribeOn(Schedulers.io())
                .doOnNext(new Action1<AllFolderBean>() {
                    @Override
                    public void call(AllFolderBean bean) {

                        setFolderResult(bean, -1L);
                        if (bean.getCode() == 0) {
                            //更新文件夹数据库(第一级处理)
                            MLog.d(TAG, "1级文件夹--size=" + bean.getFolders());
                            insertCatsSQL(bean, -1L);
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .concatMap(new Func1<AllFolderBean, Observable<AllFolderItemBean>>() {//（2）拿到第一个总接口数据，将list转item处理
                    @Override
                    public Observable<AllFolderItemBean> call(AllFolderBean allFolderBean) {
                        if (allFolderBean.getFolders() != null && allFolderBean.getFolders().size() > 0) {
                            return Observable.from(allFolderBean.getFolders());
                        } else {
                            //下一个循环
                            return Observable.empty();
                        }

                    }
                })
                .concatMap(new Func1<AllFolderItemBean, Observable<AllFolderBean>>() {//处理每个item下的文件夹
                    @Override
                    public Observable<AllFolderBean> call(final AllFolderItemBean itemBean) {

                        return MyHttpService.Builder.getHttpServer()
                                .getFolderByFolderID(itemBean.getId(), settings.token)
                                .subscribeOn(Schedulers.io())
                                .doOnNext(new Action1<AllFolderBean>() {
                                    @Override
                                    public void call(AllFolderBean bean) {

                                        if (bean.getCode() == 0) {
                                            //第二级处理：更新文件夹数据库
                                            MLog.d(TAG, "2级文件夹--" + itemBean.getName());
                                            insertCatsSQL(bean, itemBean.getId());
                                        }
                                    }
                                }).subscribeOn(Schedulers.io());

                    }
                })
                .concatMap(new Func1<AllFolderBean, Observable<AllFolderItemBean>>() {//（3）item下拿到新的list，将list再转item处理
                    @Override
                    public Observable<AllFolderItemBean> call(AllFolderBean allFolderBean) {
                        if (allFolderBean.getFolders() != null && allFolderBean.getFolders().size() > 0) {
                            return Observable.from(allFolderBean.getFolders());
                        } else {
                            //下一个循环
                            return Observable.empty();
                        }
                    }
                })
                .concatMap(new Func1<AllFolderItemBean, Observable<AllFolderBean>>() {//处理每个item下的文件夹
                    @Override
                    public Observable<AllFolderBean> call(final AllFolderItemBean itemBean) {

                        return MyHttpService.Builder.getHttpServer()
                                .getFolderByFolderID(itemBean.getId(), settings.token)
                                .subscribeOn(Schedulers.io())
                                .doOnNext(new Action1<AllFolderBean>() {
                                    @Override
                                    public void call(AllFolderBean bean) {
                                        if (bean.getCode() == 0) {
                                            //第3级处理：更新文件夹数据库
                                            MLog.d(TAG, "3级文件夹--" + itemBean.getName());
                                            insertCatsSQL(bean, itemBean.getId());
                                        }
                                    }
                                }).subscribeOn(Schedulers.io());

                    }
                })
                .concatMap(new Func1<AllFolderBean, Observable<AllFolderItemBean>>() {//（4）item下拿到新的list，将list再转item处理
                    @Override
                    public Observable<AllFolderItemBean> call(AllFolderBean allFolderBean) {
                        if (allFolderBean.getFolders() != null && allFolderBean.getFolders().size() > 0) {
                            return Observable.from(allFolderBean.getFolders());
                        } else {
                            //下一个循环
                            return Observable.empty();
                        }
                    }
                })
                .concatMap(new Func1<AllFolderItemBean, Observable<AllFolderBean>>() {//处理每个item下的文件夹
                    @Override
                    public Observable<AllFolderBean> call(final AllFolderItemBean itemBean) {

                        return MyHttpService.Builder.getHttpServer()
                                .getFolderByFolderID(itemBean.getId(), settings.token)//接口方法
                                .subscribeOn(Schedulers.io())
                                .doOnNext(new Action1<AllFolderBean>() {
                                    @Override
                                    public void call(AllFolderBean bean) {
                                        if (bean.getCode() == 0) {
                                            //第4级处理：更新文件夹数据库
                                            MLog.d(TAG, "4级文件夹--" + itemBean.getName());
                                            insertCatsSQL(bean, itemBean.getId());
                                        }
                                    }
                                }).subscribeOn(Schedulers.io());

                    }
                })
                .concatMap(new Func1<AllFolderBean, Observable<AllFolderItemBean>>() {//（5）item下拿到新的list，将list再转item处理
                    @Override
                    public Observable<AllFolderItemBean> call(AllFolderBean allFolderBean) {
                        if (allFolderBean.getFolders() != null && allFolderBean.getFolders().size() > 0) {
                            return Observable.from(allFolderBean.getFolders());
                        } else {
                            //下一个循环
                            return Observable.empty();
                        }
                    }
                })
                .concatMap(new Func1<AllFolderItemBean, Observable<AllFolderBean>>() {//处理每个item下的文件夹
                    @Override
                    public Observable<AllFolderBean> call(final AllFolderItemBean itemBean) {

                        return MyHttpService.Builder.getHttpServer()
                                .getFolderByFolderID(itemBean.getId(), settings.token)//接口方法
                                .subscribeOn(Schedulers.io())
                                .doOnNext(new Action1<AllFolderBean>() {
                                    @Override
                                    public void call(AllFolderBean bean) {
                                        if (bean.getCode() == 0) {
                                            //第5级处理：更新文件夹数据库
                                            MLog.d(TAG, "5级文件夹--" + itemBean.getName());
                                            insertCatsSQL(bean, itemBean.getId());
                                        }
                                    }
                                }).subscribeOn(Schedulers.io());

                    }
                }).concatMap(new Func1<AllFolderBean, Observable<AllFolderItemBean>>() {//（5）item下拿到新的list，将list再转item处理
                    @Override
                    public Observable<AllFolderItemBean> call(AllFolderBean allFolderBean) {
                        if (allFolderBean.getFolders() != null && allFolderBean.getFolders().size() > 0) {
                            return Observable.from(allFolderBean.getFolders());
                        } else {
                            //下一个循环
                            return Observable.empty();
                        }
                    }
                })
                .concatMap(new Func1<AllFolderItemBean, Observable<AllFolderBean>>() {//处理每个item下的文件夹
                    @Override
                    public Observable<AllFolderBean> call(final AllFolderItemBean itemBean) {

                        return MyHttpService.Builder.getHttpServer()
                                .getFolderByFolderID(itemBean.getId(), settings.token)//接口方法
                                .subscribeOn(Schedulers.io())
                                .doOnNext(new Action1<AllFolderBean>() {
                                    @Override
                                    public void call(AllFolderBean bean) {
                                        if (bean.getCode() == 0) {
                                            //第5级处理：更新文件夹数据库
                                            MLog.d(TAG, "6级文件夹--" + itemBean.getName());
                                            insertCatsSQL(bean, itemBean.getId());
                                        }
                                    }
                                }).subscribeOn(Schedulers.io());

                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AllFolderBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "getAllFolder--onCompleted");
                        listener.onGetFolderSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("getAllFolder--onError:" + e.toString());
                        listener.onGetFolderFailed(new Exception(e), null);
                    }

                    @Override
                    public void onNext(AllFolderBean bean) {
                        bean.getFolders();
                        MLog.d(TAG, "getAllFolder-onNext" + bean.getCode() + "--" + bean.getMsg());
                        //处理返回结果
                        if (bean.getCode() != 0) {
                            listener.onGetFolderFailed(new Exception(bean.getMsg()), bean.getMsg());
                        }
                    }
                });
        MyRxManager.getInstance().add(subscription);
    }


    /**
     * 删除文件夹
     *
     * @param listener
     */
    public void deleteFolder(final long fodlerId, final IFolderModuleListener listener) {

        Subscription subscription = MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .deleteFolder(fodlerId, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .doOnNext(new Action1<CommonBean>() {
                    @Override
                    public void call(CommonBean commonBean) {
                        if (commonBean.getCode() == 0) {
                            deleteFolderSQL(fodlerId);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "deleteFolder--onCompleted");
                        listener.onDeleteFolderSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("deleteFolder--onError:" + e.toString());
                        listener.onDeleteFolderFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "deleteFolder-onNext");
                    }
                });
    }

    /**
     * 设置默认文件夹
     *
     * @param listener
     */
    public void setDefaultFolder(final long fodlerId, final IFolderModuleListener listener) {

        Subscription subscription = MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .setDefaultFolder(fodlerId, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "setDefaultFolder--onCompleted");
                        listener.onDefaultFolderSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("setDefaultFolder--onError:" + e.toString());
                        listener.onDefaultFolderFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "setDefaultFolder-onNext--" + bean.getCode());

                    }
                });
    }


    public void renameFolder(final long pid, final String text, final IFolderModuleListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .renameFolder(text, pid, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .doOnNext(new Action1<CommonBean>() {
                    @Override
                    public void call(CommonBean commonBean) {
                        TNDb.beginTransaction();
                        try {
                            TNDb.getInstance().execSQL(TNSQLString.CAT_RENAME, text, pid);

                            TNDb.setTransactionSuccessful();
                        } finally {
                            TNDb.endTransaction();
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "renameFolder--onCompleted");
                        listener.onRenameFolderSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("renameFolder--onError:" + e.toString());
                        listener.onRenameFolderFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "FolderRename-onNext");

                    }

                });
    }

    public void addFoler(long pid, String text, final IFolderModuleListener listener) {
        TNSettings settings = TNSettings.getInstance();
        if (pid == -1) {
            MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                    .addNewFolder(text, settings.token)//接口方法
                    .subscribeOn(Schedulers.io())//固定样式
                    .unsubscribeOn(Schedulers.io())//固定样式
                    .observeOn(AndroidSchedulers.mainThread())//固定样式
                    .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                        @Override
                        public void onCompleted() {
                            MLog.d(TAG, "addFoler--onCompleted");
                            listener.onAddFolderSuccess();
                        }

                        @Override
                        public void onError(Throwable e) {
                            MLog.e("addFoler--onError:" + e.toString());
                            listener.onAddFolderFailed(new Exception(e.toString()), null);
                        }

                        @Override
                        public void onNext(CommonBean bean) {

                        }

                    });
        } else {
            MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                    .addNewFolderByPid(text, pid, settings.token)//接口方法
                    .subscribeOn(Schedulers.io())//固定样式
                    .unsubscribeOn(Schedulers.io())//固定样式
                    .observeOn(AndroidSchedulers.mainThread())//固定样式
                    .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                        @Override
                        public void onCompleted() {
                            MLog.d(TAG, "addFoler--onCompleted");
                            listener.onAddFolderSuccess();
                        }

                        @Override
                        public void onError(Throwable e) {
                            MLog.e("addFoler--onError:" + e.toString());
                            listener.onAddFolderFailed(new Exception(e.toString()), null);
                        }

                        @Override
                        public void onNext(CommonBean bean) {
                            MLog.d(TAG, "addFoler--onNext");
                        }

                    });
        }

    }

    public void mSetDefaultFolder(final OnCatInfoListener listener, final long catId) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .setDefaultFolder(catId, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "登录--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "登录--登录失败异常onError:" + e.toString());
                        listener.onFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "登录-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "登录-成功");
                            TNSettings settings = TNSettings.getInstance();
                            settings.defaultCatId = catId;
                            settings.savePref(false);
                            listener.onSuccess(bean);
                        } else {
                            listener.onFailed(bean.getMessage(), null);
                        }
                    }

                });
    }


    public void mCatDelete(final OnCatInfoListener listener, final long catId) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .deleteFolder(catId, settings.token)
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mCatDelete--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mGetNoteByNoteId 异常onError:" + e.toString());
                        listener.onDeleteFolderFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "mGetNoteByNoteId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onDeleteFolderSuccess(bean, catId);
                        } else {
                            listener.onDeleteFolderFailed(bean.getMessage(), null);
                        }
                    }


                });
    }

    public void mParentFolder(final OnCatListListener listener) {
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
                        listener.onParentFolderFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllFolderBean bean) {
                        MLog.d(TAG, "mParentFolder-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "mParentFolder-成功");
                            listener.onParentFolderSuccess(bean);
                        } else {
                            listener.onParentFolderFailed(bean.getMsg(), null);
                        }
                    }

                });
    }

    public void mGetFolderByFolderId(final OnCatListListener listener, final long catId) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getFolderByFolderID(catId, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<AllFolderBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mGetFolderByFolderId--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "mGetFolderByFolderId--onError:" + e.toString());
                        listener.onGetFoldersByFolderIdFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(AllFolderBean bean) {
                        MLog.d(TAG, "mGetFolderByFolderId-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onGetFoldersByFolderIdSuccess(bean, catId);
                        } else {
                            listener.onGetFoldersByFolderIdFailed(bean.getMsg(), null);
                        }
                    }

                });
    }

    public void moveFolder(final OnCatListListener listener, final long catId, long selectId) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .folderMove(catId, selectId, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "moveFolder--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "moveFolder--onError:" + e.toString());
                        listener.onFolderMoveFailed("文件夹移动失败，请稍后重试", new Exception("文件夹移动失败，请稍后重试"));
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "moveFolder-onNext--" + bean.getMessage());
                        listener.onFolderMoveSuccess(bean);
                    }

                });
    }
    //================================================处理相关================================================

    private void deleteFolderSQL(long folderId) {
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().execSQL(TNSQLString.CAT_DELETE_CAT, folderId);
            TNDb.getInstance().execSQL(TNSQLString.NOTE_TRASH_CATID, 2, System.currentTimeMillis() / 1000, TNSettings.getInstance().defaultCatId, folderId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            MLog.e("deleteFolderSQL" + e.toString());
            TNDb.endTransaction();
        } finally {
            TNDb.endTransaction();
        }
    }

    /**
     * 更新user表
     *
     * @param profileBean
     */
    private void updateProfileSQL(final ProfileBean profileBean) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                MLog.d("SQL", "更新user表");
                long userId = TNDbUtils.getUserId(settings.username);

                settings.phone = profileBean.getPhone();
                settings.email = profileBean.getEmail();
                settings.defaultCatId = profileBean.getDefault_folder();

                if (userId != settings.userId) {
                    //清空user表
                    UserDbHelper.clearUsers();
                }
                JSONObject user = TNUtils.makeJSON(
                        "username", settings.username,
                        "password", settings.password,
                        "userEmail", settings.email,
                        "phone", settings.phone,
                        "userId", settings.userId,
                        "emailVerify", profileBean.getEmailverify(),
                        "totalSpace", profileBean.getTotal_space(),
                        "usedSpace", profileBean.getUsed_space());

                //更新user表
                UserDbHelper.addOrUpdateUser(user);

                //
                settings.isLogout = false;
                settings.firstLaunch = false;//在此处设置 false
                settings.savePref(false);
            }
        });
    }

    /**
     * 必须在子线程中处理
     *
     * @param allFolderBean
     * @param catId
     */
    private void insertCatsSQL(final AllFolderBean allFolderBean, final long catId) {
        //耗时操作
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                CatDbHelper.clearCatsByParentId(catId);
                List<AllFolderItemBean> beans = allFolderBean.getFolders();

                for (int i = 0; i < beans.size(); i++) {
                    AllFolderItemBean bean = beans.get(i);
                    JSONObject tempObj = TNUtils.makeJSON(
                            "catName", bean.getName(),
                            "userId", settings.userId,
                            "trash", 0,
                            "catId", bean.getId(),
                            "noteCounts", bean.getCount(),
                            "catCounts", bean.getFolder_count(),
                            "deep", bean.getFolder_count() > 0 ? 1 : 0,
                            "pCatId", catId,
                            "isNew", -1,
                            "createTime", TNUtils.formatStringToTime(bean.getCreate_at()),
                            "lastUpdateTime", TNUtils.formatStringToTime(bean.getUpdate_at()),
                            "strIndex", TNUtils.getPingYinIndex(bean.getName())
                    );
                    //更新数据库
                    CatDbHelper.addOrUpdateCat(tempObj);
                    Log.d("SJY", "添加文件夹:" + bean.getName());
                }
            }
        });

    }

    /**
     * 返回数据的再次处理
     *
     * @param bean
     */
    private void setFolderResult(AllFolderBean bean, long cat_Note_Folder_Id) {
        if ("login required".equals(bean.getMsg())) {
            MLog.e(TAG, bean.getMsg());
            TNUtilsUi.showToast(bean.getMsg());
            TNUtils.goToLogin(context.getApplicationContext());
        } else if ("笔记不存在".equals(bean.getMsg())) {
            try {
                MLog.e(TAG, "笔记不存在：删除");
                TNDb.getInstance().execSQL(TNSQLString.NOTE_DELETE_BY_NOTEID, cat_Note_Folder_Id);
            } catch (Exception e) {
                MLog.e(TAG, "笔记不存在：" + e.toString());
            }
        } else if ("标签不存在".equals(bean.getMsg())) {
            try {
                MLog.e(TAG, "标签不存在：删除");
                TNDb.getInstance().execSQL(TNSQLString.TAG_REAL_DELETE, cat_Note_Folder_Id);
            } catch (Exception e) {
                MLog.e(TAG, "标签不存在：" + e.toString());
            }
        } else if ("文件夹不存在".equals(bean.getMsg())) {
            try {
                MLog.e(TAG, "文件夹不存在：删除");
                TNDb.getInstance().execSQL(TNSQLString.CAT_DELETE_CAT, cat_Note_Folder_Id);
            } catch (Exception e) {
                MLog.e(TAG, "文件夹不存在：" + e.toString());
            }
        } else {
        }

    }

}