package com.thinkernote.ThinkerNote.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.thinkernote.ThinkerNote.utils.MLog;
import com.thinkernote.ThinkerNote.base.TNActBase;

/**
 * 广播通知--显示界面
 * sjy 0625
 */
public class TNRemindAct extends TNActBase {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i = getIntent();		
		if(i.hasExtra("Type")){
			if(i.getStringExtra("Type").equals("MsgRemaind")){
				MLog.i(TAG, "to setRemindTime");
//				TNAction.runActionAsync(TNActionType.SetRemindTime, i.getLongExtra("RemindTime", 0));
				//TODO 更新显示
			}
		}
		    
    	Uri uri = Uri.parse("http://www.qingbiji.cn/home");   
    	i = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(i);
    	finish();
	}
	
}
