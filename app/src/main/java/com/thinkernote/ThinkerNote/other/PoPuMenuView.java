package com.thinkernote.ThinkerNote.other;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;


public class PoPuMenuView extends PopupWindows {
	LayoutInflater inflater;
	private LinearLayout mLayuout;
	
	private OnPoPuMenuItemClickListener mPoPuMenuItemClickListener;

	public PoPuMenuView(Context context) {
		super(context);
		
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setContentView(R.layout.popumenu_layout);
		mLayuout = (LinearLayout)mRootView.findViewById(R.id.popumenu_layout);
	}
	
	public void addItem(int itemId, String str, float scale){
		final int id = itemId;
		View container = inflater.inflate(R.layout.popumenu_item, null);
		((TextView)container.findViewById(R.id.popumenu_item_name)).setText(str);
//		container.setMinimumHeight((int)(44 * scale));
//		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, (int)(44 * scale));
		container.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mPoPuMenuItemClickListener != null){
					mPoPuMenuItemClickListener.onPoPuMenuItemClick(id);
				}
				v.post(new Runnable() {
					
					@Override
					public void run() {
						mWindow.dismiss();
					}
				});
			}
		});
		
		mLayuout.addView(container);
	}
	
	public void show(View anchor){
		preShow();
		
		int[] location = new int[2];

		anchor.getLocationOnScreen(location);

		Rect anchorRect = new Rect(location[0], location[1], location[0]
				+ anchor.getWidth(), location[1] + anchor.getHeight());

		mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		int rootWidth = mRootView.getMeasuredWidth();
		int rootHeight = mRootView.getMeasuredHeight();

		int screenWidth = mWindowManager.getDefaultDisplay().getWidth();

		int xPos = anchorRect.right - rootWidth;
		int yPos = anchorRect.top - rootHeight;

		boolean onBottom = true;

		// display on bottom
		MLog.i("show", "anchorRect=" + anchorRect + ",rootHeight=" + rootHeight
				+ ",anchor.getTop()=" + anchor.getTop());
		if (rootHeight > anchorRect.top) {
			yPos = anchorRect.bottom - 10;
			onBottom = false;
		}

		setAnimationStyle(screenWidth, anchorRect.centerX(), onBottom);

		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
		
//		mWindow.showAsDropDown(v);
	}
	
	private void setAnimationStyle(int screenWidth, int anchorXPos, boolean onBottom){
		if(anchorXPos < screenWidth/2){
			MLog.d("PoPuMenuView", "int left onBottom=" + onBottom);
			mWindow.setAnimationStyle((onBottom) ? R.style.AnimationFade_Bottom_left : R.style.AnimationFade_Top_left );
		}else{
			MLog.d("PoPuMenuView", "int right onBottom=" + onBottom);
			mWindow.setAnimationStyle((onBottom) ? R.style.AnimationFade_Bottom_right : R.style.AnimationFade_Top_right);
		}
	}
	
	public void setOnPoPuMenuItemClickListener(OnPoPuMenuItemClickListener listener){
		this.mPoPuMenuItemClickListener = listener;
	}
	
	public interface OnPoPuMenuItemClickListener{
		public void onPoPuMenuItemClick(int id);
	}

}
