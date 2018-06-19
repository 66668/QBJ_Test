package com.thinkernote.ThinkerNote.base;

import java.util.Vector;

import com.baidu.mobstat.StatService;
import com.thinkernote.ThinkerNote.Activity.TNActivityManager;
import com.thinkernote.ThinkerNote.Activity.TNLockAct;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.Views.MenuDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class TNActBase extends Activity {
	private static final String kThinkerNotePackage = TNSettings.kThinkerNotePackage;
	private static final String kActivityPackage = TNSettings.kActivityPackage;

	protected final String TAG = getClass().getSimpleName();
	protected boolean isInFront;
	protected int createStatus; // 0 firstCreate, 1 resume, 2 reCreate
	private Vector<Dialog> dialogs;
	public MenuDialog.Builder mMenuBuilder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MLog.d(TAG, "onCreate:" + savedInstanceState);
		super.onCreate(savedInstanceState);	
		System.gc();

		TNActivityManager.getInstance().addActivity(this);
		
		dialogs = new Vector<Dialog>();
		
		createStatus = (savedInstanceState==null) ? 0 : 2;
		
		//进入动画
		overridePendingTransition(R.anim.pull_in_from_right, R.anim.hold);
		
	}
	
	protected void onStart(){
		MLog.d(TAG, "onStart");
		super.onStart();
		
		TNSettings settings = TNSettings.getInstance();
		
		if(TAG.equals("TNMainAct")){
			if(settings.isLogout){
				finish();
				return;
			}
		}else if(TAG.equals("TNLoginAct") || TAG.equals("TNSplashAct")
				|| TAG.equals("TNAboutAct")){
			settings.isLogout = false;
		}else{
			if(settings.isLogout /*|| settings.isGoHome*/){
				if(TAG.equals("TNNoteEditAct") && TNUtilsUi.isCallFromOutside(this)){
					settings.isLogout = false;
				}
				finish();
				return;
			}
		}
	}
	

	@Override 
	public void finish () {
		MLog.d(TAG, "finish");
		TNAction.unregister(this);
		super.finish();
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		createStatus = 2;
	}
	
	@Override
	protected void onStop() {
		MLog.d(TAG, "onStop");
		super.onStop();
	}

	protected void onResume(){
		MLog.d(TAG, "onResume");
		
		super.onResume();
		
		TNSettings settings = TNSettings.getInstance();

		//启动另一个activity时，如果在本activity尚未onStop时新起的activity被finish了,
		//这时重启本activity时不会调用onStart,而是直接调onResume
//		if(settings.isLogout && !TAG.equals("TNLoginAct")){
//			finish();
//			return;
//		}

		//百度
		StatService.onResume(this);

		isInFront = true;
		TNSettings.getInstance().topAct = this;

		// 直接安装包启动，再home退出，再进入，将分别产生2个task。一个task退出将导致另一个task出错。
		if( !TAG.equals("TNSplashAct") && !TAG.equals("TNLoginAct") && !TAG.equals("TNMainAct")
				&& !TAG.equals("TNAboutAct") && settings.userId <= 0 && !isFinishing()
				&& !TAG.equals("TNRegistAct") && !TAG.equals("TNFindPasswordAct") && !TAG.equals("TNBindAccountAct")){
			finish();
			return;
		}
		
		configView();
		createStatus = 1;
		
		if (!(TAG.equals("TNLoginAct")) && !(TAG.equals("TNSplashAct"))) {
			TNUtilsUi.checkLockScreen(this);
			if( settings.needShowLock && !isFinishing() && !TNSettings.getInstance().isLogout){
				if( !(TAG.equals("TNLockAct") && getTitle().equals("lock")) 
						&& settings.lockPattern.size() > 0){
					MLog.i(TAG, "show lock");
					Bundle b = new Bundle();
					b.putInt("Type", 2);
					b.putString("OriginalPath", settings.lockPattern.toString());
					startActivity(TNLockAct.class, b);
				}
			}
		}
				
	}
	
	protected void configView(){
	}
	
	protected void setViews(){
	}

	protected void onPause(){
		MLog.d(TAG, "onPause");
		overridePendingTransition(R.anim.hold, R.anim.push_out_to_right);
		super.onPause();

		//百度
		StatService.onPause(this);
		
		isInFront = false;
		
		TNSettings settings = TNSettings.getInstance();
		if( settings.topAct == this )
			settings.topAct = null;
	}
	
	@Override
	public void onDestroy(){
		MLog.d(TAG, "onDestroy");
		TNAction.unregister(this);
		
		for(Dialog dialog : dialogs){
			MLog.e(TAG, "dismiss:" + dialog + " showing:" +  dialog.isShowing());
			dialog.dismiss();
		}
//		dialogs.clear();
		super.onDestroy();
	}
	
	//-------------------------------------------------------------------------------
	/**
	 * 添加弹框    resource为布局文件
	 * @param resource
	 */
	public View addMenu(int resource){
		mMenuBuilder = new MenuDialog.Builder(this);  
		LayoutInflater inflater = LayoutInflater.from(getBaseContext());
		View view = inflater.inflate(resource, null);
		mMenuBuilder.setContentView(view);
		mMenuBuilder.create().show(); 
		return view;
	}

	public boolean runExtraIntent(){
		Intent it = getIntent();
		if( it.hasExtra(Intent.EXTRA_INTENT) ){
			startActivity(((Intent) it.getExtras().get(Intent.EXTRA_INTENT)));
			return true;
		}
		return false;
	}

	//===================================新版 跳转 --开始==========================================

	public void startActivity(Class clz){
        Intent i = new Intent(this,clz);//推荐显示调用
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

	}

	public void startActivity(Class clz, Bundle aBundle){
        Intent i = new Intent(this,clz);//推荐显示调用
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if(aBundle != null){
            i.putExtras(aBundle);
        }
        startActivity(i);

	}

	public void startActForResult(Class aActName, Bundle aBundle, int requestCode){
		Intent i = new Intent(this,aActName);//推荐显示调用
		if(aBundle != null)
			i.putExtras(aBundle);
		startActivityForResult(i, requestCode);
	}
    //===================================新版 跳转 --结束==========================================

	public void RespondChangeSkin(TNAction aAction){
		MLog.i(TAG, "RespondChangeSkin");
		setViews();
	}
	
	public void addDialog(AlertDialog dialog){
		MLog.e(TAG, "dialog: " + dialog);
		dialogs.add(dialog);
	}
}
