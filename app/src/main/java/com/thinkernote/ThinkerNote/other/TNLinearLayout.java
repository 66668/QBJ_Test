package com.thinkernote.ThinkerNote.other;

import com.thinkernote.ThinkerNote.Utils.MLog;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class TNLinearLayout extends LinearLayout {

	public interface TNLinearLayoutListener {
		public void onTNLinearLayoutMeasure(int widthMeasureSpec, int heightMeasureSpec);
	}
	private TNLinearLayoutListener listener;
	public void setListener(TNLinearLayoutListener listener) {
		this.listener = listener;
	}

	public TNLinearLayout(Context context) {
		super(context);
	}

	public TNLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		MLog.d("TNLinearLayout", "Handling Keyboard Window shown");

//		int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
//		int actualHeight = getHeight();
//		Log.d("TNLinearLayout", actualHeight + " : " + proposedheight);
//		if (actualHeight > proposedheight){
//			// Keyboard is shown
//
//		} else {
//			// Keyboard is hidden
//		}
		if (listener != null) {
			listener.onTNLinearLayoutMeasure(widthMeasureSpec, heightMeasureSpec);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
//		int height = MeasureSpec.getSize(heightMeasureSpec);
//		Activity activity = (Activity)getContext();
//		Rect rect = new Rect();
//		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
//		int statusBarHeight = rect.top;
//		int screenHeight = activity.getWindowManager().getDefaultDisplay().getHeight();
//		int diff = (screenHeight - statusBarHeight) - height;
//		Log.d("TNLinearLayout", "diff : " + diff);
//		if (listener != null) {
//			listener.onSoftKeyboardShown(diff>128); // assume all soft keyboards are at least 128 pixels high
//		}
//		
//		Configuration config = activity.getResources().getConfiguration();
//		Log.d("TNLinearLayout", "keyboardHidden:" + config.keyboardHidden);
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);  
	}

}
