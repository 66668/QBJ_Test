package com.thinkernote.ThinkerNote.PullToRefresh;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;


public class LoadingLayout extends FrameLayout {

	static final int DEFAULT_ROTATION_ANIMATION_DURATION = 150;

	private final ImageView mHeaderImage;
	private final ProgressBar mHeaderProgress;
	private final TextView mHeaderText;
	private final TextView mHeaderTextTime;

	private String mPullLabel;
	private String mRefreshingLabel;
	private String mReleaseLabel;
	private String mUpdateTime;
	private Activity mAct;

	private final Animation mRotateAnimation, mResetRotateAnimation;

	public LoadingLayout(Context context, final int mode, String releaseLabel, String pullLabel, String refreshingLabel,String updateTime) {
		super(context);
		mAct = (Activity) context;
		ViewGroup header = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header, this);
		mHeaderText = (TextView) header.findViewById(R.id.pull_to_refresh_text);
		mHeaderImage = (ImageView) header.findViewById(R.id.pull_to_refresh_image);
		mHeaderProgress = (ProgressBar) header.findViewById(R.id.pull_to_refresh_progress);
		mHeaderTextTime = (TextView) header.findViewById(R.id.pull_to_refresh_text_time);

		final Interpolator interpolator = new LinearInterpolator();
		mRotateAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mRotateAnimation.setInterpolator(interpolator);
		mRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
		mRotateAnimation.setFillAfter(true);

		mResetRotateAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f,
		        Animation.RELATIVE_TO_SELF, 0.5f);
		mResetRotateAnimation.setInterpolator(interpolator);
		mResetRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
		mResetRotateAnimation.setFillAfter(true);

		mReleaseLabel = releaseLabel;
		mPullLabel = pullLabel;
		mRefreshingLabel = refreshingLabel;
		mUpdateTime = updateTime;

		switch (mode) {
			case PullToRefreshBase.MODE_PULL_UP_TO_REFRESH:
                mHeaderImage.setImageResource(R.drawable.arrow_up);
				break;
			case PullToRefreshBase.MODE_PULL_DOWN_TO_REFRESH:
            default:
                mHeaderImage.setImageResource(R.drawable.arrow_down);
				break;
		}
	}

	public void reset() {
		if (mUpdateTime!=null){
			mHeaderTextTime.setText(mUpdateTime);
			mHeaderTextTime.setVisibility(View.VISIBLE);
		} else {
			mHeaderTextTime.setVisibility(View.GONE);
			if (TNSettings.getInstance().originalSyncTime > 0) {
				mHeaderTextTime.setText("最近同步："
						+ TNUtilsUi.formatDate(mAct, TNSettings.getInstance().originalSyncTime / 1000L));
				mHeaderTextTime.setVisibility(View.VISIBLE);
			}
		}
        mHeaderText.setText(mPullLabel);
		mHeaderImage.setVisibility(View.VISIBLE);
		mHeaderProgress.setVisibility(View.GONE);
	}

	public void releaseToRefresh() {
        mHeaderText.setText(mReleaseLabel);
        mHeaderImage.clearAnimation();
		mHeaderImage.startAnimation(mRotateAnimation);
		if (mUpdateTime!=null){
			mHeaderTextTime.setText(mUpdateTime);
			mHeaderTextTime.setVisibility(View.VISIBLE);
		} else {
			mHeaderTextTime.setVisibility(View.GONE);
			if (TNSettings.getInstance().originalSyncTime > 0) {
				mHeaderTextTime.setText("最近同步："
						+ TNUtilsUi.formatDate(mAct, TNSettings.getInstance().originalSyncTime / 1000L));
				mHeaderTextTime.setVisibility(View.VISIBLE);
			}
		}
	}

	public void setPullLabel(String pullLabel) {
		mPullLabel = pullLabel;
	}

	public void refreshing() {
        mHeaderText.setText(mRefreshingLabel);
        if (mUpdateTime!=null){
			mHeaderTextTime.setText(mUpdateTime);
			mHeaderTextTime.setVisibility(View.VISIBLE);
		} else {
			mHeaderTextTime.setVisibility(View.GONE);
			if (TNSettings.getInstance().originalSyncTime > 0) {
				mHeaderTextTime.setText("最近同步："
						+ TNUtilsUi.formatDate(mAct, TNSettings.getInstance().originalSyncTime / 1000L));
				mHeaderTextTime.setVisibility(View.VISIBLE);
			}
		}
		mHeaderImage.clearAnimation();
		mHeaderImage.setVisibility(View.GONE);
		mHeaderProgress.setVisibility(View.VISIBLE);
	}

	public void setRefreshingLabel(String refreshingLabel) {
		mRefreshingLabel = refreshingLabel;
	}

	public void setReleaseLabel(String releaseLabel) {
	    mReleaseLabel = releaseLabel;
    }
	
	public void setUpdateTimeLabel (String updateTime){
		mUpdateTime = updateTime;
	}

    public void pullToRefresh() {
    	if (mUpdateTime!=null){
			mHeaderTextTime.setText(mUpdateTime);
			mHeaderTextTime.setVisibility(View.VISIBLE);
		} else {
			mHeaderTextTime.setVisibility(View.GONE);
		}
		mHeaderText.setText(mPullLabel);
		mHeaderImage.clearAnimation();
		mHeaderImage.startAnimation(mResetRotateAnimation);
    }

    public void setTextColor(int color) {
		mHeaderText.setTextColor(color);
	}

}