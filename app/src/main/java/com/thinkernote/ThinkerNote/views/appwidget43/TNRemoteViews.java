package com.thinkernote.ThinkerNote.views.appwidget43;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.thinkernote.ThinkerNote.bean.localdata.TNNote;
import com.thinkernote.ThinkerNote.base.TNConst;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.TNUtils;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsAtt;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsHtml;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.utils.MLog;

/**
 * 自定义 appWidget布局更新设置
 */
public class TNRemoteViews extends RemoteViews {
    private static final String TAG = "AppWidget";

    private Context mContext;
    private AppWidgetManager mAppWidgetManager;
    private int[] mAppWidgetIds;//TNAppWidget43中onUpdate的参数保存

    public TNRemoteViews(Context context) {
        super(context.getPackageName(), R.layout.widget_main43);
        mContext = context;
        this.mAppWidgetManager = AppWidgetManager.getInstance(mContext);
        this.mAppWidgetIds = getAppWidgetIds();
    }

    public int[] getAppWidgetIds() {
        ComponentName provider = new ComponentName(mContext, TNAppWidget43.class);
        return mAppWidgetManager.getAppWidgetIds(provider);
    }

    private Intent getProviderIntent() {
        return new Intent(mContext, TNAppWidget43.class);
    }

    /**
     * logo跳转
     */
    public void setOnLogoClickPendingIntent() {
        MLog.d(TAG, "添加--logo跳转PendingIntent");
        Intent intent = getProviderIntent();
        intent.setAction(TNAppWidget43.APP_START);
        PendingIntent logoPendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        this.setOnClickPendingIntent(R.id.logo, logoPendingIntent);
    }

    /**
     * 新笔记
     */
    public void setOnAddClickPendingIntent() {
        MLog.d(TAG, "添加--新笔记跳转PendingIntent");
        Intent intent = getProviderIntent();
        intent.setAction(TNAppWidget43.APP_ADD);
        PendingIntent logoPendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        this.setOnClickPendingIntent(R.id.img_add, logoPendingIntent);
    }

    /**
     * 刷新
     */
    public void setOnRefreshClickPendingIntent() {
        MLog.d(TAG, "添加--刷新跳转PendingIntent");
        Intent intent = getProviderIntent();
        intent.setAction(TNAppWidget43.APP_REFRESH);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        setOnClickPendingIntent(R.id.btn_sync, refreshPendingIntent);
    }

    /**
     * 绑定listView
     */
    public void bindListViewAdapter() {
        MLog.d(TAG, "添加--list跳转PendingIntent");

        Intent serviceIntent = new Intent(mContext, TNWidgetService.class);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        // rv.setEmptyView(listViewResId, R.id.tv_empty);//指定集合view为空时显示的view
        setRemoteAdapter(R.id.listView, serviceIntent);

        // 设置响应 ListView 的intent模板
        // (01) 通过 setPendingIntentTemplate设置"intent模板"
        // (02) 然后在处理该"集合控件"的RemoteViewsFactory类的getViewAt()接口中通过 setOnClickFillInIntent设置"集合控件的某一项的数据"
        Intent listItemIntent = getProviderIntent();
        listItemIntent.setAction(TNAppWidget43.APP_ITEM_DETAIL);
        listItemIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, R.id.listView);
        PendingIntent pendingIntentTemplate = PendingIntent.getBroadcast(mContext, 0, listItemIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        // 设置intent模板
        setPendingIntentTemplate(R.id.listView, pendingIntentTemplate);
    }

    // 更新appWidget的ListView
    public void notifyAppWidgetViewDataChanged() {
        int[] appIds = getAppWidgetIds();
        mAppWidgetManager.notifyAppWidgetViewDataChanged(appIds, R.id.listView);
    }

    /**
     * 获取list的item view
     *
     * @param note
     * @return
     */
    public RemoteViews applyItemView(final TNNote note) {
        MLog.d(TAG, "list的item view");
        if (note == null) {
            return null;
        }

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_item43);
        views.setTextViewText(R.id.appwidget_item_title, note.title);
        if (TNSettings.getInstance().sort == TNConst.UPDATETIME) {
            views.setTextViewText(R.id.appwidget_item_date, TNUtils.formatDateToWeeks(note.lastUpdate));
        } else {
            views.setTextViewText(R.id.appwidget_item_date, TNUtils.formatDateToWeeks(note.createTime));
        }
        views.setTextViewText(R.id.appwidget_item_content, TNUtilsHtml.decodeHtml(note.shortContent.trim()));

//		thumbnailView.setTag(null);
        if (note.attCounts > 0) {
            if (!TextUtils.isEmpty(note.thumbnail) && !("null").equals(note.thumbnail)) {
                views.setImageViewResource(R.id.item_thumbnail_bg, View.INVISIBLE);
                views.setImageViewBitmap(R.id.item_thumbnail1, TNUtilsAtt.getImage(note.thumbnail, 90));

            } else {
                views.setImageViewResource(R.id.item_thumbnail_bg, View.INVISIBLE);
                views.setImageViewResource(R.id.item_thumbnail1, R.drawable.notelist_thumbnail_att);
            }
        } else {
            views.setImageViewResource(R.id.item_thumbnail_bg, View.INVISIBLE);
            views.setImageViewResource(R.id.item_thumbnail1, R.drawable.notelist_thumbnail_note);
        }

        Intent fillInIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putLong(TNAppWidegtConst.SCHEME_ITEMKEY, note.noteLocalId);
        fillInIntent.putExtras(extras);
        // 设置 第position位的"视图"对应的响应事件, api 11
        views.setOnClickFillInIntent(R.id.appwidget_item, fillInIntent);

        return views;
    }

    /**
     * 同步 完成
     */
    public void syncComplete() {
        setViewVisibility(R.id.btn_sync, View.VISIBLE);
        mAppWidgetManager.updateAppWidget(mAppWidgetIds, this);
    }

    /**
     * 同步 开始
     */
    public void syncStart() {
        setViewVisibility(R.id.btn_sync, View.VISIBLE);
        mAppWidgetManager.updateAppWidget(mAppWidgetIds, this);
    }


}
