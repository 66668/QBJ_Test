package com.thinkernote.ThinkerNote.Activity.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.Activity.TNNoteEditAct;
import com.thinkernote.ThinkerNote.Activity.TNNoteListAct;
import com.thinkernote.ThinkerNote.Activity.TNNoteViewAct;
import com.thinkernote.ThinkerNote.Activity.TNMainFragAct;
import com.thinkernote.ThinkerNote.Activity.TNTextEditAct;
import com.thinkernote.ThinkerNote.Adapter.TNCatViewHolder;
import com.thinkernote.ThinkerNote.Adapter.TNNoteViewHolder;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.other.PullToRefreshExpandableListView;
import com.thinkernote.ThinkerNote.other.PullToRefreshExpandableListView.OnRefreshListener;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnSyncListener;
import com.thinkernote.ThinkerNote.mvp.p.SyncPresenter;
import com.thinkernote.ThinkerNote.base.TNChildViewBase;

import java.util.Vector;

/**
 * 我的笔记--文件夹frag
 * 接口：同步
 */
public class FolderFragment extends TNChildViewBase implements
        OnRefreshListener, OnItemLongClickListener, OnChildClickListener, OnSyncListener {

    //
    private String TAG = "TNPageCats";
    private Vector<TNCat> mCatList;
    private Vector<TNNote> mNoteList;
    private Vector<Group> mGroups;
    private TNCat mPCat;
    private PullToRefreshExpandableListView mCatListView;
    private float mScale;
    private TNSettings mSettings;
    private LinearLayout mLoadingView;
    //p
    SyncPresenter presenter;


    private TNCatListAdapter mCatlistAdapter = null;

    public FolderFragment(TNMainFragAct activity) {
        mActivity = activity;
        pageId = R.id.page_cats;
        mSettings = TNSettings.getInstance();
        //p
        presenter = new SyncPresenter(mActivity, this);
        init();
    }

    private void init() {
        mChildView = LayoutInflater.from(mActivity).inflate(
                R.layout.pagechild_catlist, null);

        mCatListView = (PullToRefreshExpandableListView) mChildView
                .findViewById(R.id.folder_listview);
        mCatListView.setOnChildClickListener(this);
        mCatListView.setOnItemLongClickListener(this);
        mCatListView.setonRefreshListener(this);

        // 隐藏软键盘
        mActivity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        DisplayMetrics metric = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScale = metric.scaledDensity;

        mCatList = new Vector<TNCat>();
        mNoteList = new Vector<TNNote>();
        mGroups = new Vector<Group>();

        // cats
        Group g = new Group();
        g.cats = mCatList;
        g.groupId = 0;
        g.notes = null;
        mGroups.add(g);
        // notes
        Group g1 = new Group();
        g1.notes = mNoteList;
        g1.groupId = 1;
        g1.cats = null;
        mGroups.add(g1);

        mCatlistAdapter = new TNCatListAdapter();
        mLoadingView = (LinearLayout) TNUtilsUi.addListHelpInfoFootView(mActivity, mCatListView,
                mActivity.getString(R.string.listfootview_title_catlist),
                mActivity.getString(R.string.listfootview_info_catlist));
        mCatListView.setAdapter(mCatlistAdapter);

        mPCat = new TNCat();
        mPCat.catId = -1;
    }

    @Override
    public void configView(int createStatus) {
        if (mPCat.catId > 0) {
            ((Button) mChildView.findViewById(R.id.folderlist_folder_more)).setText(mPCat.catName);
        } else {
            ((Button) mChildView.findViewById(R.id.folderlist_folder_more)).setText("根目录");
        }

        getNativeCatList();
        notifyExpandList();

        if (mPCat.catId == -1) {
            mActivity.findViewById(R.id.table_cats_newnote).setVisibility(
                    View.INVISIBLE);
        } else {
            mActivity.findViewById(R.id.table_cats_newnote).setVisibility(
                    View.VISIBLE);
        }
    }

    private void notifyExpandList() {
        mGroups.clear();
        if (mCatList.size() > 0) {
            // cats
            Group g = new Group();
            g.cats = mCatList;
            g.groupId = 0;
            g.notes = null;
            mGroups.add(g);
        }

        if (mNoteList.size() > 0) {
            // notes
            Group g1 = new Group();
            g1.notes = mNoteList;
            g1.groupId = 1;
            g1.cats = null;
            mGroups.add(g1);
        }
        mCatlistAdapter.notifyDataSetChanged();
        for (int i = 0; i < mGroups.size(); i++) {
            mCatListView.expandGroup(i);
        }
    }

    public void newFolder() {
        Bundle b = new Bundle();
        b.putString("TextType", "cat_add");
        b.putString("TextHint",
                TNUtils.getAppContext().getString(R.string.textedit_folder));
        b.putString("OriginalText", "");
        b.putLong("ParentId", mPCat.catId);
        mActivity.startActivity(TNTextEditAct.class, b);
    }

    public void newNote() {
        TNNote note = TNNote.newNote();
        note.catId = mPCat.catId;
        Bundle b = new Bundle();
        b.putLong("NoteForEdit", note.noteId);
        b.putSerializable("NOTE", note);
        mActivity.startActivity(TNNoteEditAct.class, b);
    }

    public TNCat getPCat() {
        return mPCat;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
                                int groupPosition, int childPosition, long id) {
        if (mGroups.get(groupPosition).groupId == 0) {
            TNCat cat = mGroups.get(groupPosition).cats.get(childPosition);
            if (cat.catId == -1002) {
                Bundle b = new Bundle();
                b.putInt("ListType", 3);
                b.putInt("count", cat.noteCounts);
                b.putLong("ListDetail", cat.catId);
                MLog.e(TAG, "跳转前", "ListType=" + 3 + "--cat.catId=" + cat.catId, "cat.noteCounts" + cat.noteCounts);
                mActivity.startActivity(TNNoteListAct.class, b);

            } else {
                mPCat = cat.copy();
                configView(0);
                mCatListView.setSelection(0);
            }
        } else {
            Bundle b = new Bundle();
            b.putLong("NoteLocalId", id);
            mActivity.startActivity(TNNoteViewAct.class, b);
        }

        return true;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        long packed = mCatListView.getExpandableListPosition(position);
        int groupPosition = ExpandableListView.getPackedPositionGroup(packed);
        int childPosition = ExpandableListView.getPackedPositionChild(packed);
        MLog.i(TAG, "groupPosition=" + groupPosition + " childPosition="
                + childPosition);
        if (groupPosition < 0 || childPosition < 0) {
            return true;
        }
        Group g = mGroups.get(groupPosition);
        if (g.groupId == 0) {
            TNCat cat = g.cats.get(childPosition);
            if (cat.catId > 0) {
                MLog.i(TAG, "onLongClick:" + cat.catId);
                mBundle.putSerializable("currentCat", cat);
                if (cat.catId == TNSettings.getInstance().defaultCatId)
                    mActivity.addCatMenu(R.layout.menu_folder_two);
                else
                    mActivity.addCatMenu(R.layout.menu_folder_one);
            } else {
                mBundle.putSerializable("currentCat", cat);
                mActivity.addCatMenu(R.layout.menu_folder_recycle);
            }
        } else {
            mBundle.putSerializable("currentNote", g.notes.get(childPosition));
            mActivity.addNoteMenu(R.layout.menu_notelistitem);
        }
        return true;
    }

    @Override
    public void onRefresh() {
        TNUtilsUi.showNotification(mActivity, R.string.alert_NoteView_Synchronizing, false);
        pSynchronizeData();

    }

    public void folderDestory(){

    }

    public void dialogCB() {
        configView(2);
    }

    public void dialogCallBackSyncCancell() {
        mCatListView.onRefreshComplete();
    }

    public boolean onKeyDown() {
        if (mPCat.catId > 0) {
            mPCat = TNDbUtils.getCat(mPCat.pCatId);
            if (mPCat == null) {
                mPCat = new TNCat();
                mPCat.catId = -1;
            }
            configView(1);
            return true;
        }
        return false;
    }


    private class TNCatListAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return mGroups.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            Group g = mGroups.get(groupPosition);
            if (g.groupId == 0) {
                return g.cats.size();
            } else {
                return g.notes.size();
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mGroups.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            Group g = mGroups.get(groupPosition);
            if (g.groupId == 0) {
                return ((Group) getGroup(groupPosition)).cats
                        .get(childPosition);
            } else {
                return ((Group) getGroup(groupPosition)).notes
                        .get(childPosition);
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return ((Group) getGroup(groupPosition)).groupId;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            Group g = mGroups.get(groupPosition);
            if (g.groupId == 0) {
                return ((TNCat) getChild(groupPosition, childPosition)).catId;
            } else {
                return ((TNNote) getChild(groupPosition, childPosition)).noteLocalId;
            }
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            LinearLayout layout = null;
            LayoutInflater layoutInflater = (LayoutInflater) mActivity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = (LinearLayout) layoutInflater.inflate(
                    R.layout.notelistgroup, null);

            Group g = (Group) getGroup(groupPosition);
            if (g.groupId == 0) {
                ((TextView) layout.findViewById(R.id.notelistgroup_title))
                        .setText("文件夹");
                ((TextView) layout.findViewById(R.id.notelistgroup_count))
                        .setText(g.cats.size() + "");
            } else {
                ((TextView) layout.findViewById(R.id.notelistgroup_title))
                        .setText("笔记");
                ((TextView) layout.findViewById(R.id.notelistgroup_count))
                        .setText(g.notes.size() + "");
            }
            return layout;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            Group g = (Group) getGroup(groupPosition);
            if (g.groupId == 0) {
                TNCatViewHolder holder = null;
                if (convertView == null) {
                    holder = new TNCatViewHolder();
                    LayoutInflater layoutInflater = (LayoutInflater) mActivity
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = (LinearLayout) layoutInflater.inflate(
                            R.layout.folder_list_item, null);
                    holder.catName = (TextView) convertView
                            .findViewById(R.id.folder_listitem_foldername);
                    holder.noteCount = (TextView) convertView
                            .findViewById(R.id.folder_listitem_notecount);
                    holder.catIcon = (ImageView) convertView
                            .findViewById(R.id.folder_listitem_caticon);
                    holder.defaultCat = (ImageView) convertView
                            .findViewById(R.id.folder_listitem_isdefault);

                    convertView.setTag(holder);
                } else {
                    try {
                        holder = (TNCatViewHolder) convertView.getTag();
                    } catch (ClassCastException e) {
                        MLog.i(TAG, "cat holder ClassCastException");
                        holder = new TNCatViewHolder();
                        LayoutInflater layoutInflater = (LayoutInflater) mActivity
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = (LinearLayout) layoutInflater.inflate(
                                R.layout.folder_list_item, null);
                        holder.catName = (TextView) convertView
                                .findViewById(R.id.folder_listitem_foldername);
                        holder.noteCount = (TextView) convertView
                                .findViewById(R.id.folder_listitem_notecount);
                        holder.catIcon = (ImageView) convertView
                                .findViewById(R.id.folder_listitem_caticon);
                        holder.defaultCat = (ImageView) convertView
                                .findViewById(R.id.folder_listitem_isdefault);

                        convertView.setTag(holder);
                    }
                }
                setCatChildView(holder, groupPosition,
                        childPosition);
            } else {
                TNNoteViewHolder holder = null;
                if (convertView == null) {
                    holder = new TNNoteViewHolder();
                    LayoutInflater layoutInflater = (LayoutInflater) mActivity
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = (LinearLayout) layoutInflater.inflate(
                            R.layout.notelistitem, null);
                    holder.noteTitle = (TextView) convertView
                            .findViewById(R.id.notelistitem_title);
                    holder.date = (TextView) convertView
                            .findViewById(R.id.notelistitem_date);
                    holder.shortContent = (TextView) convertView
                            .findViewById(R.id.notelistitem_shortcontent);
                    holder.thumbnail = (ImageView) convertView
                            .findViewById(R.id.notelistitem_thumbnail1);
                    holder.thumbnailBg = (ImageView) convertView.findViewById(R.id.notelistitem_thumbnail_bg);

                    convertView.setTag(holder);
                } else {
                    try {
                        holder = (TNNoteViewHolder) convertView.getTag();
                    } catch (ClassCastException e) {
                        MLog.i(TAG, "note holder ClassCastException");
                        holder = new TNNoteViewHolder();
                        LayoutInflater layoutInflater = (LayoutInflater) mActivity
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = (LinearLayout) layoutInflater.inflate(
                                R.layout.notelistitem, null);
                        holder.noteTitle = (TextView) convertView
                                .findViewById(R.id.notelistitem_title);
                        holder.date = (TextView) convertView
                                .findViewById(R.id.notelistitem_date);
                        holder.shortContent = (TextView) convertView
                                .findViewById(R.id.notelistitem_shortcontent);
                        holder.thumbnail = (ImageView) convertView
                                .findViewById(R.id.notelistitem_thumbnail1);
                        holder.thumbnailBg = (ImageView) convertView.findViewById(R.id.notelistitem_thumbnail_bg);

                        convertView.setTag(holder);
                    }
                }
                setNoteChildView(holder, groupPosition,
                        childPosition);
            }
            return convertView;
        }

        private void setCatChildView(TNCatViewHolder holder,
                                     int groupPosition, int childPosition) {
            TNCat cat = (TNCat) getChild(groupPosition, childPosition);
            holder.catName.setText(cat.catName);
            if (cat.catId == -1002) {
                holder.catIcon.setImageResource(R.drawable.folderlistitem_recycle);
                holder.noteCount.setText(Html.fromHtml("<font color=#4485d6>"
                        + cat.noteCounts + " </font>篇笔记"));
            } else {
                holder.catIcon.setImageResource(R.drawable.folderlistitem_cat);
                holder.noteCount.setText(Html.fromHtml("<font color=#4485d6>"
                        + cat.catCounts + "</font> 个文件夹, "
                        + "<font color=#4485d6>" + cat.noteCounts
                        + " </font>篇笔记"));
            }
            if (cat.catId == TNSettings.getInstance().defaultCatId) {
                holder.defaultCat.setVisibility(View.VISIBLE);
                holder.defaultCat.setImageResource(R.drawable.folderlistitem_defaultcat);
            } else {
                holder.defaultCat.setVisibility(View.GONE);
            }
        }

        private void setNoteChildView(TNNoteViewHolder holder,
                                      int groupPosition, int childPosition) {
            TNNote note = (TNNote) getChild(groupPosition,
                    childPosition);
            holder.noteTitle.setText(note.title);
            holder.shortContent.setText(TNUtilsHtml.decodeHtml(note.shortContent.trim()));
            if (TNSettings.getInstance().sort == TNConst.UPDATETIME) {
                holder.date.setText(TNUtils.formatDateToWeeks(note.lastUpdate));
            } else {
                holder.date.setText(TNUtils.formatDateToWeeks(note.createTime));
            }
            ImageView thumbnailView = holder.thumbnail;
            thumbnailView.setTag(null);
            if (note.attCounts > 0) {
                if (!TextUtils.isEmpty(note.thumbnail) && !("null").equals(note.thumbnail) && note.syncState != 1) {
                    holder.thumbnailBg.setVisibility(View.VISIBLE);
                    thumbnailView.setImageBitmap(TNUtilsAtt.getImage(note.thumbnail, 90));
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                            (int) (46 * mScale), (int) (46 * mScale),
                            Gravity.CENTER);
                    holder.thumbnailBg.setLayoutParams(layoutParams);
                    thumbnailView.setLayoutParams(layoutParams);
                } else {
                    holder.thumbnailBg.setVisibility(View.INVISIBLE);
                    thumbnailView.setImageResource(R.drawable.notelist_thumbnail_att);
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                            (int) (46 * mScale), (int) (46 * mScale), Gravity.CENTER);
                    thumbnailView.setLayoutParams(layoutParams);
                }
            } else {
                thumbnailView
                        .setImageResource(R.drawable.notelist_thumbnail_note);
                holder.thumbnailBg.setVisibility(View.INVISIBLE);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        (int) (46 * mScale), (int) (46 * mScale),
                        Gravity.CENTER);
                thumbnailView.setLayoutParams(layoutParams);
            }
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }

    private void getNativeCatList() {
        MLog.i(TAG, "getCatlist");
        mCatList.clear();
        mNoteList.clear();
        if (mPCat.catId == -1) {
            getNativeParentCats();
        } else {
            getNativeCatsByCatId(mPCat.catId);
        }
    }

    private void getNativeParentCats() {
        refreshParentCats();
    }

    private void getNativeCatsByCatId(long catId) {
        refreshCat();
    }


    private void refreshParentCats() {
        mCatList = TNDbUtils.getCatsByCatId(mSettings.userId, -1);
        TNCat recycler = new TNCat();
        recycler.catId = -1002;
        recycler.catName = "回收站";
        recycler.trash = 1;
        recycler.noteCounts = TNDbUtils.getNoteListByTrash(mSettings.userId, mSettings.sort).size();
        mCatList.add(recycler);

        notifyExpandList();
    }

    private void refreshCat() {
        mCatList = TNDbUtils.getCatsByCatId(mSettings.userId, mPCat.catId);
        mNoteList = TNDbUtils.getNoteListByCatId(mSettings.userId, mPCat.catId, mSettings.sort, TNConst.MAX_PAGE_SIZE);
        notifyExpandList();
    }

    private class Group {
        Vector<TNNote> notes;
        Vector<TNCat> cats;
        int groupId;
    }


    /**
     * 同步结束后的操作
     *
     * @param state 0 = 成功/1=back取消同步/2-异常触发同步终止
     */
    private void endSynchronize(final int state) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCatListView.onRefreshComplete();
                if (state == 0) {
                    //正常结束
                    TNUtilsUi.showNotification(mActivity, R.string.alert_MainCats_Synchronized, true);
                    //
                    configView(1);
                    TNSettings settings = TNSettings.getInstance();
                    settings.originalSyncTime = System.currentTimeMillis();
                    settings.savePref(false);
                } else if (state == 1) {
                    TNUtilsUi.showNotification(mActivity, R.string.alert_Synchronize_Stoped, true);
                } else {
                    TNUtilsUi.showNotification(mActivity, R.string.alert_SynchronizeCancell, true);
                }
            }
        });

    }

    //===================================================================================

    //-------------------------------------p层调用 同步所有数据----------------------------------------

    private void pSynchronizeData() {
        presenter.synchronizeData("FOLDER");
    }

    @Override
    public void onSyncSuccess(String obj) {

        endSynchronize(0);
    }

    @Override
    public void onSyncFailed(Exception e, String msg) {
        endSynchronize(2);
    }

    //如下回调不使用
    @Override
    public void onSyncEditSuccess() {

    }


}
