package com.thinkernote.ThinkerNote._constructer.m;

import android.content.Context;
import android.text.TextUtils;

import com.thinkernote.ThinkerNote.DBHelper.TagDbHelper;
import com.thinkernote.ThinkerNote.Data.TNTag;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.listener.m.ITagModuleListener;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnTagInfoListener;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnTagListListener;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.main.TagItemBean;
import com.thinkernote.ThinkerNote.bean.main.TagListBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import org.json.JSONObject;

import java.util.List;
import java.util.Vector;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 具体实现:
 * m层 标签相关
 */
public class TagModule {

    private Context context;
    private static final String TAG = "Tags";
    final TNSettings settings;

    public TagModule(Context context) {
        this.context = context;
        settings = TNSettings.getInstance();
    }


    //================================================接口相关================================================

    /**
     * 第一次登陆，创建默认标签（向后台发送要创建的文件夹）
     *
     * @param arrayTag
     * @param listener
     */
    public void createTagByFirstLaunch(String[] arrayTag, final ITagModuleListener listener) {
        final String[] mFolderName = {""};
        //创建默认的tag
        Observable.from(arrayTag)
                .subscribeOn(Schedulers.io())
                .concatMap(new Func1<String, Observable<CommonBean>>() {
                    @Override
                    public Observable<CommonBean> call(String folderName) {
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
            public void onCompleted() {
                MLog.d(TAG, "addNewTag--onCompleted");
                listener.onAddDefaultTagSuccess();
            }

            @Override
            public void onError(Throwable e) {
                listener.onAddTagFailed(new Exception(e.toString()), null);
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
    public void getAllTags(final ITagModuleListener listener) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncTagList(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .doOnNext(new Action1<TagListBean>() {
                    @Override
                    public void call(TagListBean tagListBean) {
                        //数据库处理
                        insertTagsSQL(tagListBean);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<TagListBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "getAllTags--onCompleted");
                        listener.onGetTagSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "getAllTags--onError:" + e.toString());
                        listener.onGetTagFailed(new Exception(e.toString()), "异常");
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
    public void getAllTagsBySingle(final ITagModuleListener listener) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncTagList(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .doOnNext(new Action1<TagListBean>() {
                    @Override
                    public void call(TagListBean tagListBean) {
                        //数据库处理
                        insertTagsSQL(tagListBean);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<TagListBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "getAllTags--onCompleted");
                        listener.onGetTagListSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "getAllTags--onError:" + e.toString());
                        listener.onGetTagListFailed(new Exception(e.toString()), "异常");
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
                .doOnNext(new Action1<TagListBean>() {
                    @Override
                    public void call(TagListBean baseBean) {
                        List<TagItemBean> beans = baseBean.getTags();
                        updateTagsSQL(beans);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<TagListBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "getTagList--onCompleted");
                        listener.onTagListSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("getTagList--onError:" + e.toString());
                        listener.onTagListFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(TagListBean bean) {
                        MLog.d(TAG, "getTagList-onNext" + bean.getCode());
                    }

                });
    }

    public void deleteTag(final long pid, final ITagModuleListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .deleteTag(pid, settings.token)//接口方法
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        TNDb.getInstance().execSQL(TNSQLString.TAG_REAL_DELETE, pid);
                    }
                })
                .subscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "deleteTag--onCompleted");
                        listener.onDeleteTagSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("deleteTag--onError:" + e.toString());
                        listener.onDeleteTagFailed(new Exception("接口异常！"), "异常");
                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d(TAG, "deleteTag-onNext" + bean.getCode());

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
