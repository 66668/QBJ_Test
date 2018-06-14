package com.thinkernote.ThinkerNote.Activity.fragment;

import java.util.Vector;

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
import com.thinkernote.ThinkerNote.base.TNChildViewBase;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNHandleError;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Data.TNTag;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.Log;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.Other.PullToRefreshExpandableListView;
import com.thinkernote.ThinkerNote.Other.PullToRefreshExpandableListView.OnHeadViewVisibleChangeListener;
import com.thinkernote.ThinkerNote.Other.PullToRefreshExpandableListView.OnRefreshListener;

/**
 * 我的笔记--标签frag
 */
public class TNPageTags extends TNChildViewBase implements
OnClickListener, OnRefreshListener, OnItemLongClickListener,
OnScrollListener, OnHeadViewVisibleChangeListener, OnChildClickListener {
	private static final String TAG = "TNPageChildTags";
	private TNSettings mSettings = TNSettings.getInstance();

	private Vector<TNTagGroup> mGroups;
	private Vector<TNTag> mTags;

	private TextView mTopStrIndexText;
	private TextView mTopCountText;
	private TextView mAllTagCountText;

	private PullToRefreshExpandableListView mListview;
	private TNTagsExpandableListAdapter mAdapter = null;

	public TNPageTags(TNPagerAct activity) {
		mActivity = activity;
		pageId = R.id.page_tags;
		
		TNAction.regResponder(TNActionType.GetTagList, this, "respondGetTagList");
		TNAction.regResponder(TNActionType.Synchronize, this, "respondSynchronize");

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
			TNAction.runActionAsync(TNActionType.GetTagList, "page");
		} else {
			mTags = TNDbUtils.getTagList(TNSettings.getInstance().userId);
			notifyExpandList();
		}
	}
	
	private void notifyExpandList() {
		mGroups.clear();
		TNTagGroup group = null;
		if (mTags.size() > 0) {
			for(TNTag tag : mTags){
				String index = tag.strIndex.substring(0, 1);
				if(group == null || !group.strIndex.equals(index)){
					group = new TNTagGroup();
					group.strIndex = index;
					group.tags = new Vector<TNTag>();
					group.tags.add(tag);
					mGroups.add(group);
				}else{
					group.tags.add(tag);
				}
			}
		}

		mAdapter.notifyDataSetChanged();
		if(mGroups.size() > 0){
			setTopDateAndCount(mListview.getFirstVisiblePosition());
			mChildView.findViewById(R.id.taglist_top_groupinfo).setVisibility(
					View.VISIBLE);
		}else{
			mChildView.findViewById(R.id.taglist_top_groupinfo).setVisibility(
					View.INVISIBLE);
		}
		for (int i = 0; i < mGroups.size(); i++) {
			mListview.expandGroup(i);
		}
		mAllTagCountText.setText(mActivity.getString(R.string.pagetags_alltag_count, mTags.size()));
	}

	// respond Action
	// -------------------------------------------------------------------------------
	public void respondSynchronize(TNAction aAction){
		if (aAction.inputs.size() > 0 && !aAction.inputs.get(0).equals("Tags")) {
			return;
		}

		if (aAction.result == TNAction.TNActionResult.Cancelled) {
			TNUtilsUi.showNotification(mActivity, R.string.alert_SynchronizeCancell, true);
		} else if (!TNHandleError.handleResult(mActivity, aAction, false)) {
			mListview.onRefreshComplete();
			configView(1);
			TNUtilsUi.showNotification(mActivity, R.string.alert_MainCats_Synchronized, true);
			if (TNActionUtils.isSynchroniz(aAction)) {
				mSettings.originalSyncTime = System.currentTimeMillis();
				mSettings.savePref(false);
			}
		} else {
			mListview.onRefreshComplete();
			TNUtilsUi.showNotification(mActivity,
					R.string.alert_Synchronize_Stoped, true);
		}
	}

	public void respondGetTagList(TNAction aAction) {
		//判断是不是本页的注册响应事件
		if (aAction.inputs.size() < 1) {
			return;
		}
		// 共用该响应函数
		mListview.onRefreshComplete();
		mTags = TNDbUtils.getTagList(TNSettings.getInstance().userId);
		notifyExpandList();
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Log.i(TAG, "onChildClick id = " + id);
		TNTag tag = mGroups.get(groupPosition).tags.get(childPosition);
		Bundle b = new Bundle();
		b.putLong("UserId",
				TNSettings.getInstance().userId);
		b.putInt("ListType", 4);
		b.putLong("ListDetail", tag.tagId);
		b.putInt("count", tag.noteCounts);
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
		Log.i(TAG, "groupPosition=" + groupPosition + " childPosition="
				+ childPosition);

		TNTag  tag = mGroups.get(groupPosition).tags.get(childPosition);
		mBundle.putSerializable("currentTag", tag);
		mActivity.addTagMenu(R.layout.menu_tag);
		return true;
	}

	@Override
	public void onRefresh() {
		Vector<TNNote> notes = TNDbUtils.getAllNoteList(TNSettings.getInstance().userId);
//		if (notes.size() == 0) {
			TNUtilsUi.showNotification(mActivity, R.string.alert_NoteView_Synchronizing, false);
			TNAction.runActionAsync(TNActionType.Synchronize, "Tags");
//		} else {
//			TNAction.runActionAsync(TNActionType.GetTagList, "page");
//		}
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
			if(convertView == null){
				holder = new TNTagViewHolder();
				LayoutInflater inflater = (LayoutInflater) mActivity
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.pagetaglist_item, null);
				
				holder.tagName = (TextView)convertView.findViewById(R.id.pagetag_listitem_title);
				holder.noteCount = (TextView)convertView.findViewById(R.id.pagetag_listitem_notecount);
				convertView.setTag(holder);
			}else{
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
	
	private class TNTagViewHolder{
		TextView tagName;
		TextView noteCount;
	}

	private class TNTagGroup {
		public String strIndex;
		public Vector<TNTag> tags;
	}

}
