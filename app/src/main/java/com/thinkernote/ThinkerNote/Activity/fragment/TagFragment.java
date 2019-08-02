package com.thinkernote.ThinkerNote.Activity.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.Activity.TNNoteListAct;
import com.thinkernote.ThinkerNote.Activity.TNPagerAct;
import com.thinkernote.ThinkerNote.DBHelper.NoteAttrDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.NoteDbHelper;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Data.TNTag;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.Other.PullToRefreshExpandableListView;
import com.thinkernote.ThinkerNote.Other.PullToRefreshExpandableListView.OnHeadViewVisibleChangeListener;
import com.thinkernote.ThinkerNote.Other.PullToRefreshExpandableListView.OnRefreshListener;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnFragmentTagListener;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnSyncListener;
import com.thinkernote.ThinkerNote._constructer.p.FragmentTagPresenter;
import com.thinkernote.ThinkerNote._constructer.p.SyncPresenter;
import com.thinkernote.ThinkerNote.base.TNChildViewBase;
import com.thinkernote.ThinkerNote.bean.main.GetNoteByNoteIdBean;

import org.json.JSONObject;

import java.util.List;
import java.util.Vector;

/**
 * 我的笔记--标签frag
 */
public class TagFragment extends TNChildViewBase implements
        OnClickListener, OnRefreshListener, OnItemLongClickListener,
        OnScrollListener, OnHeadViewVisibleChangeListener, OnChildClickListener, OnFragmentTagListener, OnSyncListener {
    public static final String TAG = "TagFragment";//1

    private Vector<TNTagGroup> mGroups;
    private Vector<TNTag> mTags;

    private TextView mTopStrIndexText;
    private TextView mTopCountText;
    private TextView mAllTagCountText;

    private PullToRefreshExpandableListView mListview;
    private TNTagsExpandableListAdapter mAdapter = null;
    //p
    private FragmentTagPresenter presenter;
    private SyncPresenter syncPresenter;

    public TagFragment(TNPagerAct activity) {
        mActivity = activity;
        pageId = R.id.page_tags;

        //p
        presenter = new FragmentTagPresenter(mActivity, this);
        syncPresenter = new SyncPresenter(mActivity, this);
        init();
    }

    public void init() {
        mChildView = LayoutInflater.from(mActivity).inflate(
                R.layout.pagechild_taglist, null);

        mGroups = new Vector<TNTagGroup>();
        mListview = (PullToRefreshExpandableListView) mChildView
                .findViewById(R.id.taglist_listview);
        TNUtilsUi.addListHelpInfoFootView(mActivity, mListview,
                TNUtilsUi.getFootViewTitle(mActivity, 7),
                TNUtilsUi.getFootViewInfo(mActivity, 7));
        mAdapter = new TNTagsExpandableListAdapter();
        mListview.setAdapter(mAdapter);

        mTopStrIndexText = (TextView) mChildView
                .findViewById(R.id.taglist_top_strindex);
        mTopCountText = (TextView) mChildView
                .findViewById(R.id.taglist_top_count);
        mAllTagCountText = (TextView) mChildView.findViewById(R.id.taglist_allcount);

        mListview.setOnChildClickListener(this);
        mListview.setOnItemLongClickListener(this);
        mListview.setOnScrollListener(this);
        mListview.setonRefreshListener(this);
        mListview.setOnHeadViewVisibleChangeListener(this);
    }

    @Override
    public void configView(int createStatus) {
        //第一次进来且有网络的情况下从云端获取，否则从本地获取
        if (createStatus == 0 && TNUtils.isNetWork()) {
            pTagList();
        } else {
            mTags = TNDbUtils.getTagList(TNSettings.getInstance().userId);
            notifyExpandList();
        }
    }

    private void notifyExpandList() {
        mGroups.clear();
        TNTagGroup group = null;
        if (mTags.size() > 0) {
            for (TNTag tag : mTags) {
                String index = tag.strIndex.substring(0, 1);
                if (group == null || !group.strIndex.equals(index)) {
                    group = new TNTagGroup();
                    group.strIndex = index;
                    group.tags = new Vector<TNTag>();
                    group.tags.add(tag);
                    mGroups.add(group);
                } else {
                    group.tags.add(tag);
                }
            }
        }

        mAdapter.notifyDataSetChanged();
        if (mGroups.size() > 0) {
            setTopDateAndCount(mListview.getFirstVisiblePosition());
            mChildView.findViewById(R.id.taglist_top_groupinfo).setVisibility(
                    View.VISIBLE);
        } else {
            mChildView.findViewById(R.id.taglist_top_groupinfo).setVisibility(
                    View.INVISIBLE);
        }
        for (int i = 0; i < mGroups.size(); i++) {
            mListview.expandGroup(i);
        }
        mAllTagCountText.setText(mActivity.getString(R.string.pagetags_alltag_count, mTags.size()));
    }


    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
                                int groupPosition, int childPosition, long id) {
        MLog.i(TAG, "onChildClick id = " + id);
        TNTag tag = mGroups.get(groupPosition).tags.get(childPosition);
        Bundle b = new Bundle();
        b.putLong("UserId",
                TNSettings.getInstance().userId);
        b.putInt("ListType", 4);
        b.putLong("ListDetail", tag.tagId);
        b.putInt("count", tag.noteCounts);
        MLog.e(TAG, "跳转前", "ListType=" + 4 + "--tag.tagId=" + tag.tagId, "tag.noteCounts" + tag.noteCounts);
        mActivity.startActivity(TNNoteListAct.class, b);
        return true;
    }

    @Override
    public void onHeadViewVisibleChange(int visible) {
        if (!mGroups.isEmpty()) {
            if (visible == View.VISIBLE) {
                mChildView.findViewById(R.id.taglist_top_groupinfo).setVisibility(
                        View.INVISIBLE);
            } else {
                mChildView.findViewById(R.id.taglist_top_groupinfo).setVisibility(
                        View.VISIBLE);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mListview.onScrollStateChanged(view, scrollState);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        mListview.onScroll(view, firstVisibleItem, visibleItemCount,
                totalItemCount);

        // 至少有2个view，为headView和footView
        if (visibleItemCount <= 2) {
            return;
        }
        // lp 2011-12-23
        // 设置顶部组信息
        setTopDateAndCount(firstVisibleItem);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        long packed = mListview.getExpandableListPosition(position);
        int groupPosition = PullToRefreshExpandableListView
                .getPackedPositionGroup(packed);
        int childPosition = PullToRefreshExpandableListView
                .getPackedPositionChild(packed);
        MLog.i(TAG, "groupPosition=" + groupPosition + " childPosition="
                + childPosition);

        TNTag tag = mGroups.get(groupPosition).tags.get(childPosition);
        mBundle.putSerializable("currentTag", tag);
        mActivity.addTagMenu(R.layout.menu_tag);
        return true;
    }

    @Override
    public void onRefresh() {
        TNUtilsUi.showNotification(mActivity, R.string.alert_NoteView_Synchronizing, false);
        syncPresenter.synchronizeData("TAG");
    }

    public void dialogCallBackSyncCancell() {
        mListview.onRefreshComplete();
    }

    @Override
    public void onClick(View v) {

    }

    private void setTopDateAndCount(int firstVisibleItemPosition) {
        long packed = mListview
                .getExpandableListPosition(firstVisibleItemPosition);
        int groupPosition = PullToRefreshExpandableListView
                .getPackedPositionGroup(packed);
        if (groupPosition < 0) {
            groupPosition = 0;
        } else if (groupPosition >= mGroups.size()) {
            groupPosition = mGroups.size() - 1;
        }
        TNTagGroup group = mGroups.get(groupPosition);
        mTopStrIndexText.setText(group.strIndex);
        mTopCountText.setText(group.tags.size() + "");
    }


    private class TNTagsExpandableListAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return mGroups.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mGroups.get(groupPosition).tags.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mGroups.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mGroups.get(groupPosition).tags.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return mGroups.get(groupPosition).tags.get(childPosition).tagId;
        }

        @Override
        public boolean hasStableIds() {

            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mActivity
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = (LinearLayout) inflater.inflate(
                        R.layout.notelistgroup, null);
            }
            setGroupView(convertView, groupPosition);

            return convertView;
        }

        private void setGroupView(View layout, int groupPosition) {
            TNTagGroup group = (TNTagGroup) getGroup(groupPosition);
            ((TextView) layout.findViewById(R.id.notelistgroup_title))
                    .setText(group.strIndex);
            ((TextView) layout.findViewById(R.id.notelistgroup_count))
                    .setText(group.tags.size() + "");
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            TNTagViewHolder holder = null;
            if (convertView == null) {
                holder = new TNTagViewHolder();
                LayoutInflater inflater = (LayoutInflater) mActivity
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.pagetaglist_item, null);

                holder.tagName = (TextView) convertView.findViewById(R.id.pagetag_listitem_title);
                holder.noteCount = (TextView) convertView.findViewById(R.id.pagetag_listitem_notecount);
                convertView.setTag(holder);
            } else {
                holder = (TNTagViewHolder) convertView.getTag();
            }

            TNTag tag = mGroups.get(groupPosition).tags.get(childPosition);
            holder.tagName.setText(tag.tagName);
            holder.noteCount.setText(Html.fromHtml("共 <font color=#4485d6>"
                    + tag.noteCounts + "</font> 篇笔记使用该标签"));

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }

    private class TNTagViewHolder {
        TextView tagName;
        TextView noteCount;
    }

    private class TNTagGroup {
        public String strIndex;
        public Vector<TNTag> tags;
    }

    /**
     * 同步结束后的操作
     *
     * @param state 0 = 成功/1=back取消同步/2-异常触发同步终止
     */
    private void endSynchronize(int state) {
        mListview.onRefreshComplete();
        if (state == 0) {
            //正常结束
            TNUtilsUi.showNotification(mActivity, R.string.alert_MainCats_Synchronized, true);
            //
            TNSettings settings = TNSettings.getInstance();
            settings.originalSyncTime = System.currentTimeMillis();
            settings.savePref(false);
        } else if (state == 1) {
            TNUtilsUi.showNotification(mActivity, R.string.alert_Synchronize_Stoped, true);
        } else {
            TNUtilsUi.showNotification(mActivity, R.string.alert_SynchronizeCancell, true);
        }
    }

    // ------------------------------------数据库-------------------------------------------


    /**
     * 2-11-2
     * 该处工作环境最恶劣，上千跳接口返回数据走该处执行耗时任务，有必要手动gc处理内存
     *
     * @param bean
     */
    public static void updateNote(GetNoteByNoteIdBean bean) {
        //
        System.gc();
        //
        try {
            long noteId = bean.getId();
            String contentDigest = bean.getContent_digest();
            TNNote note = TNDbUtils.getNoteByNoteId(noteId);//在全部笔记页同步，会走这里，没在首页同步过的返回为null

            int syncState = note == null ? 1 : note.syncState;
            List<GetNoteByNoteIdBean.TagBean> tags = bean.getTags();

            String tagStr = "";
            for (int k = 0; k < tags.size(); k++) {
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
            //TODO getFolder_id可以为负值么
            if (bean.getFolder_id() > 0) {
                catId = bean.getFolder_id();
            }

            JSONObject tempObj = TNUtils.makeJSON(
                    "title", bean.getTitle(),
                    "userId", TNSettings.getInstance().userId,
                    "trash", bean.getTrash(),
                    "source", "android",
                    "catId", catId,
                    "content", TNUtilsHtml.codeHtmlContent(bean.getContent(), true),
                    "createTime", com.thinkernote.ThinkerNote.Utils.TimeUtils.getMillsOfDate(bean.getCreate_at()) / 1000,
                    "lastUpdate", com.thinkernote.ThinkerNote.Utils.TimeUtils.getMillsOfDate(bean.getUpdate_at()) / 1000,
                    "syncState", syncState,
                    "noteId", noteId,
                    "shortContent", TNUtils.getBriefContent(bean.getContent()),
                    "tagStr", tagStr,
                    "lbsLongitude", bean.getLongitude() <= 0 ? 0 : bean.getLongitude(),
                    "lbsLatitude", bean.getLatitude() <= 0 ? 0 : bean.getLatitude(),
                    "lbsRadius", bean.getRadius() <= 0 ? 0 : bean.getRadius(),
                    "lbsAddress", bean.getAddress(),
                    "nickName", TNSettings.getInstance().username,
                    "thumbnail", thumbnail,
                    "contentDigest", contentDigest
            );
            if (note == null)
                NoteDbHelper.addOrUpdateNote(tempObj);
            else
                NoteDbHelper.updateNote(tempObj);
        } catch (Exception e) {
            MLog.e("操作有异常：" + e.toString());
            //该异常只在TNMainAct的2-11-2的数据处理函数使用，这里只使用try catch 不处理即可 sjy
            MLog.e("TNPagerTags--updateNote:" + e.toString());
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

// ------------------------------------p层调用-------------------------------------------

    private void pTagList() {
        presenter.getTagsBySingle();
    }

    //====================================结果回调============================================

    @Override
    public void onSyncSuccess(String obj) {
        endSynchronize(0);
    }

    @Override
    public void onSyncFailed(Exception e, String msg) {
        endSynchronize(2);
    }

    @Override
    public void onGetTagListSuccess() {
        //显示
        mListview.onRefreshComplete();
        mTags = TNDbUtils.getTagList(TNSettings.getInstance().userId);
        notifyExpandList();
    }

    @Override
    public void onGetTagListFailed(String msg, Exception e) {
        endSynchronize(2);
        MLog.e(msg);
    }
    //如下回调不使用
    @Override
    public void onSyncEditSuccess(String obj) {

    }

    @Override
    public void onSyncEditFailed(Exception e, String msg) {

    }
}
