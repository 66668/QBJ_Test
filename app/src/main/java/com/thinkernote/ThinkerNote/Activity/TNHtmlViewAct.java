package com.thinkernote.ThinkerNote.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.base.TNActBase;

/**
 *sjy 0625
 */
public class TNHtmlViewAct extends TNActBase
	implements OnClickListener{
	
	/* Bundle:
	 * HtmlType
	 */
	
	private String mHtmlType;
	
	// Activity methods
	//-------------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.htmlview);
		setViews();
		// initialize
		findViewById(R.id.htmlview_back).setOnClickListener(this);

		mHtmlType = getIntent().getStringExtra("HtmlType");

		WebView wv = (WebView)findViewById(R.id.htmlview_web);		
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setBuiltInZoomControls(true);
	}
	
	
	
	@Override
	protected void setViews() {
		TNUtilsSkin.setViewBackground(this, null, R.id.htmlview_toolbar_layout, R.drawable.toolbg);
		TNUtilsSkin.setViewBackground(this, null, R.id.htmlview_page, R.drawable.page_bg);
	}



	// ConfigView
	//-------------------------------------------------------------------------------
	protected void configView(){
//		TNCache cache = TNCache.update(this);
		if( mHtmlType.equals("contribution_rule")){
			findViewById(R.id.progressBar1).setVisibility(View.GONE);
			WebView wv = (WebView)findViewById(R.id.htmlview_web);  
			TNUtils.showObject(wv);
//			wv.loadDataWithBaseURL("",
//					TNUtilsAtt.readRule(cache.user.contribution, cache.user.conRank), 
//					"text/html", "utf-8", null);
		}
	}

	// implements OnClickListener
	//-------------------------------------------------------------------------------
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.htmlview_back:
			finish();
			break;
		}
	}}
