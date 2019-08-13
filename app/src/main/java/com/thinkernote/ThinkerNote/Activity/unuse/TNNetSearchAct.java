package com.thinkernote.ThinkerNote.Activity.unuse;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.General.TNHandleError;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.other.HorizontalPager;
import com.thinkernote.ThinkerNote.other.HorizontalPager.OnScreenSwitchListener;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.base.TNActBase;

/**
 * TODO 未使用
 */
public class TNNetSearchAct extends TNActBase implements OnClickListener, OnScreenSwitchListener,
						OnKeyListener {
	private HorizontalPager mPager;
	private Dialog mSyncProjectDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_layout);
		mPager = (HorizontalPager) findViewById(R.id.tablelayout_horizontalPager);
		mPager.setOnScreenSwitchListener(this);		
		setViews();
		
//		TNAction.regResponder(TNActionType.GetProject, this, "respondGetProject");
		
		findViewById(R.id.tablelayout_btn_page1).setOnClickListener(this);
		findViewById(R.id.tablelayout_btn_page2).setOnClickListener(this);
		findViewById(R.id.tablelayout_btn_page3).setVisibility(View.GONE);
		findViewById(R.id.table_toolbar_layout_notes).setVisibility(View.GONE);
		findViewById(R.id.table_toolbar_layout_cats).setVisibility(View.GONE);
		findViewById(R.id.table_toolbar_layout_tags).setVisibility(View.GONE);
		findViewById(R.id.table_home).setOnClickListener(this);
		findViewById(R.id.netsearch_id_edittext).setOnKeyListener(this);
		findViewById(R.id.netsearch_name_edittext).setOnKeyListener(this);
		
		mSyncProjectDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
	}

	@Override
	protected void configView() {
		
	}

	@Override
	protected void setViews() {
		TNUtilsSkin.setViewBackground(this, null, R.id.tablelayout_toolbar_layout, R.drawable.toolbg);
		TNUtilsSkin.setViewBackground(this, null, R.id.tablelayout_page, R.drawable.page_bg);
		
		addSearchPage();
	}

	@Override
	public void onDestroy() {
		mSyncProjectDialog.dismiss();
		super.onDestroy();
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_ENTER 
				&& event.getRepeatCount() == 0 
				&& event.getAction() == 1){
			switch (v.getId()) {
			case R.id.netsearch_id_edittext:
				search(0);
				break;

			case R.id.netsearch_name_edittext:
				search(1);
				break;
				
			default:
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.table_home:
			finish();
			break;
			
		case R.id.tablelayout_btn_page1:
			if(mPager.getCurrentScreen() != 0)
				mPager.setCurrentScreen(0, true);
			break;

		case R.id.tablelayout_btn_page2:
			if(mPager.getCurrentScreen() != 1)
				mPager.setCurrentScreen(1, true);
			break;
		
		case R.id.netsearch_id_searchbtn:
			search(0);
			break;
		
		case R.id.netsearch_name_searchbtn:
			search(1);
			break;
		}
	}
	
	//type: 0 id搜索; 1  名称搜索
	private void search(int type){
		String content = "";
		if(type == 0)
			content = ((EditText)findViewById(R.id.netsearch_id_edittext)).getText().toString().trim();
		else
			content = ((EditText)findViewById(R.id.netsearch_name_edittext)).getText().toString().trim();
		
		if(content.length() == 0){
			if(type == 0)
				TNUtilsUi.alert(this, R.string.alert_netsearch_id_blank);
			else
				TNUtilsUi.alert(this, R.string.alert_netsearch_name_blank);
			return;
		}else if(content.length() > 20){
			TNUtilsUi.alert(this, R.string.alert_ProjectInfo_IdNotExist);
			return;
		}
		
		
		if(type == 0){
			if(content.length() > 9){
				TNUtilsUi.alert(this, R.string.alert_ProjectInfo_IdNotExist);
				return;
			}
			int id = -1;
			try {
				id = Integer.valueOf(content);
			} catch (NumberFormatException e) {
				TNUtilsUi.alert(this, R.string.alert_netsearch_id_wrong);
				return;
			}
			if(id <= 0){
				TNUtilsUi.alert(this, R.string.alert_netsearch_id_wrong);
				return;
			}
		}
		
//		TNAction.runActionAsync(TNActionType.GetProject,
//				content, type);
		mSyncProjectDialog.show();
	}
	
	private void addSearchPage(){
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout searchPageID = (LinearLayout)layoutInflater.inflate(R.layout.net_search_id, null);
		searchPageID.findViewById(R.id.netsearch_id_searchbtn).setOnClickListener(this);
		mPager.addView(searchPageID);
		
		LinearLayout searchPageName = (LinearLayout)layoutInflater.inflate(R.layout.net_search_name, null);
		searchPageName.findViewById(R.id.netsearch_name_searchbtn).setOnClickListener(this);
		mPager.addView(searchPageName);
	}

	@Override
	public void onScreenSwitched(int screen) {
		if(screen == 0){
			findViewById(R.id.netsearch_id_edittext).requestFocus();
			RadioButton rb = (RadioButton)findViewById(R.id.tablelayout_btn_page1);
			rb.setChecked(true);
			rb.setTextColor(0xFFFFFFFF);
			((RadioButton)findViewById(R.id.tablelayout_btn_page2)).setTextColor(0xFF8E8E8E);
			TNUtilsUi.showKeyBoard(this, R.id.netsearch_id_edittext);
		}else{
			findViewById(R.id.netsearch_name_edittext).requestFocus();
			RadioButton rb = (RadioButton)findViewById(R.id.tablelayout_btn_page2);
			rb.setChecked(true);
			rb.setTextColor(0xFFFFFFFF);
			((RadioButton)findViewById(R.id.tablelayout_btn_page1)).setTextColor(0xFF8E8E8E);
			TNUtilsUi.showKeyBoard(this, R.id.netsearch_name_edittext);
			
		}
	}
	
	@SuppressWarnings("unchecked")
	public void respondGetProject(TNAction aAction){
		MLog.i(TAG, "respondGetProject");
		mSyncProjectDialog.hide();
		if(!TNHandleError.handleResult(this, aAction, true)){
			int type = (Integer)aAction.inputs.get(1);
			if(type == 0){
//				TNProject project = (TNProject)aAction.outputs.get(0);
//					Bundle b = new Bundle();
//					b.putSerializable("Project", project);
//					runActivity("TNProjectGetAct", b);
			}else{
//				Bundle b = new Bundle();
//				Vector<TNProject> projects = (Vector<TNProject>)aAction.outputs.get(0);
//				if(projects.size() > 0){
//					b.putSerializable("Projects", (Serializable)projects);
//					runActivity("TNProjectInfoListAct", b);
//				}else{
//					TNUtilsUi.alert(this, R.string.alert_netsearch_name_notexist);
//				}
			}
		}
	}

}
