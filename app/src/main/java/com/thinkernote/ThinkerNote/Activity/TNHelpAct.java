package com.thinkernote.ThinkerNote.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsSkin;
import com.thinkernote.ThinkerNote.views.HorizontalPager;
import com.thinkernote.ThinkerNote.views.HorizontalPager.OnScreenSwitchListener;
import com.thinkernote.ThinkerNote.views.PageController;
import com.thinkernote.ThinkerNote.base.TNActBase;

/**
 * 主页--帮助
 * 首次打开软件，进入帮助界面
 * sjy 0613
 */
public class TNHelpAct extends TNActBase
	implements OnClickListener,OnScreenSwitchListener{

	private HorizontalPager mPager;
	private LinearLayout mController;
	
	// Activity methods
	//-------------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		
		mPager = (HorizontalPager) findViewById(R.id.help_pager);
		mController=(LinearLayout)findViewById(R.id.help_pagercontroller);
		findViewById(R.id.help_back).setOnClickListener(this);
		mPager.setOnScreenSwitchListener(this);
		
//		TNSettings settings = TNSettings.getInstance();
//		if(settings.firstLaunch){
//			settings.firstLaunch = false;
//			settings.savePref(true);
//		}
	}
	
	protected void configView(){
		if( createStatus == 0 || createStatus == 2){
			ViewGroup.LayoutParams imgParams = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT, 
					ViewGroup.LayoutParams.FILL_PARENT);
			PageController.setCount(mController, 3, 1);
			ImageView img = new ImageView(this);
			img.setLayoutParams(imgParams);
			TNUtilsSkin.setViewBackground(this, img, R.drawable.help01);
			mPager.addView(img);
			img=null;
			
			img=new ImageView(this);
			img.setLayoutParams(imgParams);
			TNUtilsSkin.setViewBackground(this, img, R.drawable.help02);
			mPager.addView(img);
			img=null;
			
			img=new ImageView(this);
			img.setLayoutParams(imgParams);
			TNUtilsSkin.setViewBackground(this, img, R.drawable.help03);
			mPager.addView(img);
			img=null;
			PageController.setCurrent(mController, 0, false);
		}
	}

	@Override
	public void onClick(View v) {
		finish();
	}

	@Override
	public void onScreenSwitched(int screen) {
		PageController.setCurrent(mController, screen, false);
	}

	
}
