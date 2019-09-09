package com.thinkernote.ThinkerNote.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.thinkernote.ThinkerNote.General.TNRunner;
import com.thinkernote.ThinkerNote.Adapter.TNPreferenceAdapter;
import com.thinkernote.ThinkerNote.Data.TNPreferenceChild;
import com.thinkernote.ThinkerNote.Data.TNPreferenceGroup;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtilsDialog;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.base.TNActBase;

import java.util.Vector;

/**
 * 关于我们
 */
public class TNAboutAct extends TNActBase implements OnClickListener,
		OnGroupClickListener, OnChildClickListener, OnItemLongClickListener {
	private ExpandableListView mListView;
	private Vector<TNPreferenceGroup> mGroups;
	private TNPreferenceChild mCurrChild;
	private int clickCount = 0;
	private long currTime = 0;
	private Dialog mProgressDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		setViews();
		mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
		
		mGroups = new Vector<TNPreferenceGroup>();
		
		mListView = (ExpandableListView)findViewById(R.id.about_expandablelistview);
		mListView.setAdapter(new TNPreferenceAdapter(this, mGroups));
		
		mListView.setOnGroupClickListener(this);
		mListView.setOnChildClickListener(this);
		mListView.setOnItemLongClickListener(this);
		getSettings(false);
	}

	@Override
	protected void configView() {
		((BaseExpandableListAdapter)mListView.getExpandableListAdapter()).notifyDataSetChanged();
		for(int i=0; i<mGroups.size(); i++){
			mListView.expandGroup(i);
		}
	}
	
	private void getSettings(boolean addLog){
		TNSettings settings = TNSettings.getInstance();
		mGroups.clear();
		TNPreferenceGroup group = null;
		
		//关于轻笔记
		group = new TNPreferenceGroup(getString(R.string.about_company));
		{	//意见或建议
			group.addChild(new TNPreferenceChild(getString(R.string.about_advice), null, true, new TNRunner(this, "advice")));
			//给个好评吧
			group.addChild(new TNPreferenceChild(getString(R.string.about_comment), null, true, new TNRunner(this, "comment")));
			//轻笔记主页
			group.addChild(new TNPreferenceChild(getString(R.string.about_homepage), getString(R.string.about_homepage_url), true, new TNRunner(this, "homepage")));
			//联系我们
			group.addChild(new TNPreferenceChild(getString(R.string.about_contact), getString(R.string.about_contact_email), true, new TNRunner(this, "contact")));
		}
		mGroups.add(group);
		
		//其他
		group = new TNPreferenceGroup(getString(R.string.about_other));
		{	//软件版本
			try {
				PackageInfo info = getPackageManager().getPackageInfo(
						getPackageName(), 0);
				String revision = info.versionName;
				group.addChild(new TNPreferenceChild(getString(R.string.about_revision), revision, false, null));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		mGroups.add(group);
		
//		//合作伙伴
//		group = new TNPreferenceGroup(getString(R.string.about_friends));
//		{	//360安全卫士
//			group.addChild(new TNPreferenceChild(getString(R.string.about_qihu360), null, true, new TNRunner(this, "friends")));
//		}
//		mGroups.add(group);
	}

	@Override
	protected void setViews() {
		TNUtilsSkin.setViewBackground(this, null, R.id.about_toolbg_framelayout, R.drawable.toolbg);
		TNUtilsSkin.setViewBackground(this, null, R.id.about_page, R.drawable.page_bg);
		
		findViewById(R.id.about_back).setOnClickListener(this);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		mCurrChild = mGroups.get(groupPosition).getChilds().get(childPosition);
		if(mCurrChild.getChildName().equals(getString(R.string.about_revision))){
			shortClick();
		}

		if(mCurrChild.getTargetMethod() != null){
			mCurrChild.getTargetMethod().run();
			return true;
		}
		return false;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		long packed = mListView.getExpandableListPosition(position);
		int groupPosition = ExpandableListView.getPackedPositionGroup(packed);
		int childPosition = ExpandableListView.getPackedPositionChild(packed);
		if(groupPosition < 0 || childPosition < 0){
			return false;
		}
		mCurrChild = mGroups.get(groupPosition).getChilds().get(childPosition);
		if(mCurrChild.getChildName().equals(getString(R.string.about_revision))){
			longClick();
		}
		
		return true;
	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v,
			int groupPosition, long id) {
		return true;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.about_back:
			finish();
			break;
		}
	}
	
	@Override
	public void onDestroy() {
		mProgressDialog.dismiss();
		super.onDestroy();
	}
	

	
	private void shortClick(){
		long t = System.currentTimeMillis();
		if(clickCount > 0){
			if((t - currTime) > 3000){
				clickCount = 0;
			}
		}
		MLog.d(TAG, "shortClick:count=" + clickCount + " time" + t + " -" + currTime);
		currTime = t;
		if(clickCount == 2){
			clickCount = 0;
			return;
		}
		clickCount++;
		if(clickCount > 4){
			clickCount = 0;
			MLog.d(TAG, "Open debug mode");
			MLog.DEBUG = true;
			getSettings(true);
			configView();
		}
	}
	
	private void longClick(){
		long t = System.currentTimeMillis();
		if(clickCount > 0){
			if((t - currTime) > 3000){
				clickCount = 0;
			}
		}
		Log.d(TAG, "longClick:count=" + clickCount + " time" + t + " -" + currTime);
		currTime = t;
		if(clickCount != 2){
			clickCount = 0;
			return;
		}
		clickCount ++;
	}

	public void advice(){
		startActivity(TNReportAct.class);
	}
	
	public void comment(){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id="+ getPackageName()));
		TNUtilsDialog.startIntent(this, intent, 
				R.string.alert_About_CantOpenComment);
	}
	
	public void homepage(){
		Intent intent = new Intent(Intent.ACTION_VIEW, 
				Uri.parse("http://" + mCurrChild.getInfo()));
		TNUtilsDialog.startIntent(this, intent, 
				R.string.alert_About_CantOpenWeb);
	}
	
	public void contact(){
		Intent intent = new Intent(Intent.ACTION_SENDTO, 
				Uri.parse("mailto:info@qingbiji.cn"));
		TNUtilsDialog.startIntent(this, intent, 
				R.string.alert_About_CantSendEmail);
	}
	
	public void friends(){
		Intent intent = new Intent(Intent.ACTION_VIEW, 
				Uri.parse("http://shouji.360.cn/partner.and.php?id=101089"));
		TNUtilsDialog.startIntent(this, intent, 
				R.string.alert_About_CantOpenWeb);
	}
	
}
