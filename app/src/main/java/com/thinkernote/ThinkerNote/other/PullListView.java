package com.thinkernote.ThinkerNote.other;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.R;

public class PullListView extends ListView{
	private View mHeaderContainer = null;
    private View mHeaderView = null;
    private float mY = 0;
    private float mHistoricalY = 0;
    private int mHistoricalTop = 0;
    private int mInitialHeight = 0;
    private boolean mFlag = false;
    private int mHeaderHeight = 0;
    private OnViewChangeListener mViewChangeListener = null;

    private final static int RATIO = 3;
    public static final int HEADER_HEIGHT_DP = 240;
    private int maxHeight;

    public PullListView(final Context context) {
        super(context);
        initialize();
    }

    public PullListView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public PullListView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }
    
    public void setHeaderInfo(String title, String msg){
    	((TextView)mHeaderView.findViewById(R.id.pull_head_title)).setText(title);
//    	((TextView)mHeaderView.findViewById(R.id.pull_head_msg)).setText(msg);
    }
    
    public void setOnViewChangeListener(final OnViewChangeListener l){
    	mViewChangeListener = l;
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHandler.removeMessages(1);
                mY = mHistoricalY = ev.getY();
                if (mHeaderContainer.getLayoutParams() != null) {
                    mInitialHeight = mHeaderContainer.getLayoutParams().height;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mHistoricalTop = getChildAt(0).getTop();
                break;
            case MotionEvent.ACTION_UP:
            	int height = (int) (ev.getY() - mY) / RATIO + mInitialHeight;
            	if(height > maxHeight){
            		height = maxHeight;
            	}
                mHandler.sendMessage(mHandler.obtainMessage(1, height, 0));
                mFlag = false;
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE && getFirstVisiblePosition() == 0) {
            float direction = ev.getY() - mHistoricalY;
            int height = (int) (ev.getY() - mY) / RATIO + mInitialHeight;
            if (height < 0) {
                height = 0;
            }
            
            if(height > maxHeight){
            	try{
            		return super.dispatchTouchEvent(ev);
            	}catch (Exception e) {
					return false;
				}
            }

            float deltaY = Math.abs(mY - ev.getY());
            ViewConfiguration config = ViewConfiguration.get(getContext());
            if (deltaY > config.getScaledTouchSlop()) {

                // Scrolling downward
                if (direction > 0) {
                	if(getChildAt(0)!=null){

                        // Extends refresh bar
                        setHeaderHeight(height);
                        
                		if (getChildAt(0).getTop() == 0) {
                            if (mHistoricalTop < 0) {

                                // mY = ev.getY();
                                // this?mHistoricalTop = 0;
                            }

                            // Stop list scroll to prevent the list from
                            // overscrolling
                            ev.setAction(MotionEvent.ACTION_CANCEL);
                            mFlag = false;
                        }
                	}
                    
                } else if (direction < 0) {
                    // Scrolling upward
                	if(getChildAt(0)!=null){
//                		 if (getChildAt(0).getTop() == 0) {
                             setHeaderHeight(height);

                             // If scroll reaches top of the list, list scroll is
                             // enabled
                             if (getChildAt(1) != null && getChildAt(1).getTop() <= 1 && !mFlag) {
                                 ev.setAction(MotionEvent.ACTION_DOWN);
                                 mFlag = true;
                             }
//                         }
                	}
                }
            }

            mHistoricalY = ev.getY();
        }
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean performItemClick(final View view, final int position, final long id) {
        if (position == 0) {
            // This is the refresh header element
            return true;
        } else {
            return super.performItemClick(view, position - 1, id);
        }
    }

    private void initialize() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mHeaderContainer = inflater.inflate(R.layout.pull_headview, null);
        mHeaderView = mHeaderContainer.findViewById(R.id.pull_head);
        addHeaderView(mHeaderContainer, null, false);

        float density = getContext().getResources().getDisplayMetrics().density;
        mHeaderHeight = (int) (HEADER_HEIGHT_DP * density);
        maxHeight = mHeaderHeight * 2;
        setHeaderHeight(mHeaderHeight);
    }

    private void setHeaderHeight(final int height) {
    	if(height < mHeaderHeight){
    		return;
    	}
//        // Extends refresh bar
        LayoutParams lp = (LayoutParams) mHeaderContainer.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        }
        lp.height = height;
        mHeaderContainer.setLayoutParams(lp);
        if(mViewChangeListener != null){
        	mViewChangeListener.onViewChange(height);
        }
    }

	private final Handler mHandler = new Handler() {

		@Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);

            int limit = mHeaderHeight;

            // Elastic scrolling
            if (msg.arg1 >= limit) {
                setHeaderHeight(msg.arg1);
                int displacement = (msg.arg1 - limit) / 1;
                if (displacement == 0) {
                    mHandler.sendMessage(mHandler.obtainMessage(msg.what, msg.arg1 - 25, 0));
                } else {
                    mHandler.sendMessage(mHandler.obtainMessage(msg.what, msg.arg1 - displacement,
                            0));
                }
            }
        }

    };
    
    public interface OnViewChangeListener {
        public void onViewChange(int movedY);
    }

}
