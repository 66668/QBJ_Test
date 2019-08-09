package com.thinkernote.ThinkerNote.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.thinkernote.ThinkerNote.Activity.TNMainFragAct;

import java.lang.ref.WeakReference;

// fragment基类
public class TNChildViewBase {
	public int pageId;
	public TNMainFragAct mActivity;
	public View mChildView;
	public Bundle mBundle;
	
	public TNChildViewBase() {
		mBundle = new Bundle();
	}
	
	public void configView(int createStatus){
		
	}

	public final Handler handler = new WeakRefHandler(this);

	//===================================handler软引用 --开始==========================================


	public class WeakRefHandler extends Handler {

		private final WeakReference<TNChildViewBase> mFragmentReference;

		public WeakRefHandler(TNChildViewBase base) {
			mFragmentReference = new WeakReference<TNChildViewBase>(base);
		}

		@Override
		public void handleMessage(Message msg) {
			final TNChildViewBase base = mFragmentReference.get();
			if (base != null) {
				base.handleMessage(msg);
			}
		}
	}

	/**
	 * @param msg
	 */
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			default:
				break;
		}
	}
	//===================================handler软引用 --结束==========================================
}
