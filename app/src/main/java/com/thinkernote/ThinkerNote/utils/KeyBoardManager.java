package com.thinkernote.ThinkerNote.utils;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyBoardManager {
	private static Activity mAct;
	private static View mView;
	
	private static Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 10001)
				showKeyBoard(mAct, mView);
			super.handleMessage(msg);
		}
	};

    /**
     * 寤惰繜time寮瑰嚭閿洏
     * @param act
     * @param view
     * @param time
     */
	public static void delayShowKeyBoard(Activity act, View view, int time){
		mAct = act;
		mView = view;
		TimerTask mTimerTask = new TimerTask() {
			public void run() {
				mHandler.sendEmptyMessage(10001);
			}
		};
		new Timer().schedule(mTimerTask, time);
	}

    /**
     * 闅愯棌閿洏
      * @param act
     * @param viewId
     */
	public static void hideKeyboard(Activity act, int viewId) {
		hideKeyboard(act, act.findViewById(viewId));
	}

    /**
     * 闅愯棌閿洏
     * @param act
     * @param view
     */
	public static void hideKeyboard(Activity act, View view) {
		InputMethodManager imm = (InputMethodManager) act
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(),
				0); // 闅愯棌杞敭鐩?
	}

	/**
	 * 绔嬪嵆鏄剧ず閿洏
	 * @param act activity
	 * @param viewId 闇?瑕佹樉绀洪敭鐩樼殑view
	 */
	public static void showKeyBoard(Activity act, int viewId) {
		showKeyBoard(act, act.findViewById(viewId));
	}

    /**
     * 绔嬪嵆鏄剧ず閿洏
     * @param act
     * @param view
     */
	public static void showKeyBoard(Activity act, View view) {
		view.requestFocus();
		InputMethodManager imm = ((InputMethodManager) act
				.getSystemService(Context.INPUT_METHOD_SERVICE));
		imm.showSoftInput(view, 0);
	}
}
