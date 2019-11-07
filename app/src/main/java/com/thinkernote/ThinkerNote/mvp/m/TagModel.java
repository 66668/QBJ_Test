package com.thinkernote.ThinkerNote.mvp.m;

import android.content.Context;
import android.text.TextUtils;

import com.thinkernote.ThinkerNote.db.TagDbHelper;
import com.thinkernote.ThinkerNote.db.Database.TNDb;
import com.thinkernote.ThinkerNote.db.Database.TNSQLString;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.TNUtils;
import com.thinkernote.ThinkerNote.utils.MLog;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.main.TagItemBean;
import com.thinkernote.ThinkerNote.bean.main.TagListBean;
import com.thinkernote.ThinkerNote.mvp.http.url_main.MyHttpService;
import com.thinkernote.ThinkerNote.mvp.listener.m.ITagModelListener;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnTagListListener;
import com.thinkernote.ThinkerNote.mvp.listener.v.SyncDisposableListener;

import org.json.JSONObject;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * 具体实现:
 * m层 标签相关
 */
public class TagModel {

    private static final String TAG = "Tags";
    final TNSettings settings;

    public TagModel() {
        settings = TNSettings.getInstance();
    }


    //================================================接口相关================================================

    /**
     * 第一次登陆，创建默认标签（向后台发送要创建的文件夹）
     *
     * @param arrayTag
     * @param listener
     */
    public void createTagByFirstLaunch(String[] arrayTag, final ITagModelListener listener, final SyncDisposableListener disposableListener) {
        final String[] mFolderName = {""};
        //创建默认的tag
        Observable.fromArray(arrayTag)
                .subscribeOn(Schedulers.io())
                .concatMap(new Function<String, Observable<CommonBean>>() {
                    @Override
                    public Observable<CommonBean> apply(String folderName) {
                        mFolderName[0] = folderName;
                        //拿到list的item数据 访问接口
                        return MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                                .addNewTag(folderName, settings.token)//接口方法
                                .subscribeOn(Schedulers.io())//固定样式
                                .unsubscribeOn(Schedulers.io())//固定样式;
                                .observeOn(AndroidSchedulers.mainThread());//固定样式
                    }
                }).subscribe(new Observer<CommonBean>() {
            @Override
            public void onComplete() {
                MLog.d(TAG, "addNewTag--onCompleted");
                listener.onAddDefaultTagSuccess();
            }

            @Override
            public void onError(Throwable e) {
                listener.onAddTagFailed(new Exception(e.toString()), null);
            }

            @Override
            public void onSubscribe(Disposable d) {
                disposableListener.add(d);
            }

            @Override
            public void onNext(CommonBean bean) {
                MLog.d(TAG, "addNewTag--onNext" + bean.getMessage());
                if (bean.getCode() == 0) {

                } else if (bean.getMessage().equals("标签已存在")) {
                    //不处理
                } else {
                }
            }
        });

    }

    /**
     * 获取所有tags
     *
     * @param listener
     */
    public void getAllTags(final ITagModelListener listener, final SyncDisposableListener disposableListener) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncTagList(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .doOnNext(new Consumer<TagListBean>() {
                    @Override
                    public void accept(TagListBean tagListBean) {
                        //数据库处理
                        insertTagsSQL(tagListBean);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<TagListBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d(TAG, "getAllTags--onCompleted");
                        listener.onGetTagSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "getAllTags--onError:" + e.toString());
                        listener.onGetTagFailed(new Exception(e.toString()), "异常");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposableListener.add(d);
                    }

                    @Override
                    public void onNext(TagListBean bean) {
                        MLog.d(TAG, "getAllTags-onNext" + bean.getCode() + "--" + bean.getMsg());
                        //处理返回结果
                        if (bean.getCode() == 0) {
                        } else {
                            listener.onGetTagFailed(new Exception(bean.getMsg()), bean.getMsg());
                        }
                    }

                });

    }

    /**
     * 获取所有tags(和上没有区别，只是回调界面不同)
     *
     * @param listener
     */
    public void getAllTagsBySingle(final ITagModelListener listener) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncTagList(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .doOnNext(new Consumer<TagListBean>() {
                    @Override
                    public void accept(TagListBean tagListBean) {
                        //数据库处理
                        insertTagsSQL(tagListBean);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<TagListBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d(TAG, "getAllTags--onCompleted");
                        listener.onGetTagListSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "getAllTags--onError:" + e.toString());
                        listener.onGetTagListFailed(new Exception(e.toString()), "异常");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(TagListBean bean) {
                        MLog.d(TAG, "getAllTags-onNext" + bean.getCode() + "--" + bean.getMsg());
                        //处理返回结果
                        if (bean.getCode() == 0) {
                        } else {
                            listener.onGetTagListFailed(new Exception(bean.getMsg()), bean.getMsg());
                        }
                    }

                });

    }

    /**
     * 获取标签列表
     *
     * @param listener
     */
    public void getTagList(final OnTagListListener listener) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getTagList(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .doOnNext(new Consumer<TagListBean>() {
                    @Override
                    public void accept(TagListBean baseBean) {
                        List<TagItemBean> beans = baseBean.getTags();
                        updateTagsSQL(beans);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<TagListBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d(TAG, "getTagList--onCompleted");
                        listener.onTagListSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("getTagList--onError:" + e.toString());
                        listener.onTagListFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(TagListBean bean) {
                        MLog.d(TAG, "getTagList-onNext" + bean.getCode());
                    }

                });
    }

    public void deleteTag(final long pid, final ITagModelListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .deleteTag(pid, settings.token)//接口方法
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        TNDb.getInstance().execSQL(TNSQLString.TAG_REAL_DELETE, pid);
                    }
                })
                .subscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d(TAG, "deleteTag--onCompleted");
                        listener.onDeleteTagSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("deleteTag--onError:" + e.toString());
                        listener.onDeleteTagFailed(new Exception("接口异常！"), "异常");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "deleteTag-onNext" + bean.getCode());

                    }
                });
    }


    public void renameTag(final long pid, final String text, final ITagModelListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .tagRename(text, pid, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .doOnNext(new Consumer<CommonBean>() {
                    @Override
                    public void accept(CommonBean commonBean) {
                        //数据库
                        TNDb.getInstance().execSQL(TNSQLString.TAG_RENAME, text, TNUtils.getPingYinIndex(text), pid);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d(TAG, "renameTag--onCompleted");
                        listener.onTagRenameSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("renameTag--onError:" + e.toString());
                        listener.onTagRenameFailed(new Exception(e.toString()), null);

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "renameTag-onNext");
                    }

                });
    }


    public void addTag(String text, final ITagModelListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .addNewTag(text, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d(TAG, "addTag--onCompleted");
                        listener.onAddTagSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("addTag--onError:" + e.toString());
                        listener.onAddTagFailed(new Exception(e.toString()), null);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CommonBean bean) {

                    }

                });
    }

    //================================================处理相关================================================


    /**
     * 数据库保存
     *
     * @param tagListBean
     */
    private void insertTagsSQL(TagListBean tagListBean) {
        List<TagItemBean> beans = tagListBean.getTags();
        //
        TNSettings settings = TNSettings.getInstance();
        TagDbHelper.clearTags();

        for (TagItemBean itemBean : beans) {
            String tagName = itemBean.getName();
            if (TextUtils.isEmpty(tagName)) {
                tagName = "无";
            }
            JSONObject tempObj = TNUtils.makeJSON(
                    "tagName", tagName,
                    "userId", settings.userId,
                    "trash", 0,
                    "tagId", itemBean.getId(),
                    "strIndex", TNUtils.getPingYinIndex(tagName),
                    "count", itemBean.getCount()
            );
            TagDbHelper.addOrUpdateTag(tempObj);
            MLog.d(TAG, "标签存储成功：" + tagName);
        }

    }

    /**
     *
     */
    private void updateTagsSQL(List<TagItemBean> beans) {
        TagDbHelper.clearTags();
        for (int i = 0; i < beans.size(); i++) {
            TagItemBean bean = beans.get(i);
            String tagName = bean.getName();
            if (TextUtils.isEmpty(tagName)) {
                tagName = "无";
            }
            JSONObject tempObj = TNUtils.makeJSON(
                    "tagName", tagName,
                    "userId", TNSettings.getInstance().userId,
                    "trash", 0,
                    "tagId", bean.getId(),
                    "strIndex", TNUtils.getPingYinIndex(tagName),
                    "count", bean.getCount()
            );
            TagDbHelper.addOrUpdateTag(tempObj);
            //
        }
    }
}
