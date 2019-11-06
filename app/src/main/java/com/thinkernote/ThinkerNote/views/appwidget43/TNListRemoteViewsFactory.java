package com.thinkernote.ThinkerNote.views.appwidget43;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.thinkernote.ThinkerNote.bean.localdata.TNNote;
import com.thinkernote.ThinkerNote.db.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.base.TNConst;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.MLog;

import java.util.ArrayList;
import java.util.List;

/**
 * list处理相关
 */
class TNListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "AppWidget";

    private static final int VIEW_TYPE_COUNT = 1;

    private List<TNNote> mNoteList = new ArrayList<TNNote>();

    private Context mContext;

    public TNListRemoteViewsFactory(Context context, Intent intent) {
        this.mContext = context;
    }

    @Override
    public void onCreate() {
        mNoteList.clear();
        mNoteList = TNDbUtils.getNoteListByCount(TNSettings.getInstance().userId, TNConst.PAGE_SIZE, TNSettings.getInstance().sort);
        MLog.d(TAG,"TNListRemoteViewsFactory--onCreate--"+mNoteList.size());
    }

    @Override
    public int getCount() {
        if (mNoteList == null) {
            return 0;
        }

        return mNoteList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private TNNote getNews(int index) {
        return mNoteList.get(index);
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @SuppressLint("NewApi")
    @Override
    public RemoteViews getViewAt(int position) {
        if (getCount() == 0) {
            return null;
        }
        MLog.d(TAG, "添加--getViewAt" + position);
        TNRemoteViews mRemoteViews = new TNRemoteViews(mContext);
        mRemoteViews.syncComplete();

        TNNote newsItem = getNews(position);
        return mRemoteViews.applyItemView(newsItem);
    }


    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {
        mNoteList.clear();
        //只能获取最近的20篇笔记
        mNoteList = TNDbUtils.getNoteListByCount(TNSettings.getInstance().userId, TNConst.PAGE_SIZE, TNSettings.getInstance().sort);
    }

    @Override
    public void onDestroy() {
        mNoteList.clear();
    }

    /**
     * 设置字体大小
     *
     * @param views
     * @param viewId
     * @param textSize
     */
    @SuppressLint("NewApi")
    private void setRemoteViewsTextSize(RemoteViews views, int viewId, int textSize) {
        views.setTextViewTextSize(viewId, TypedValue.COMPLEX_UNIT_SP, textSize / 2);
    }

}