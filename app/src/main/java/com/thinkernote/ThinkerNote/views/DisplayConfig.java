/**
 * 
 */
package com.thinkernote.ThinkerNote.views;

import com.thinkernote.ThinkerNote.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * LiuPeng 2015-5-14
 * 
 */
public class DisplayConfig {
	private int mWeekTextSize = 12;
	private int mWeekBgColor = 0;
	private int mWeekTextColor = 0;
	private int mDividerColor = 0;

	private int mCellTextSize = 14;
	// 瀛椾綋棰滆壊
	private int mCellTextColor = 0;
	private int mOtherMonthTextcolor = 0;
	private int mTodayTextColor = 0;
	private int mCheckedTextColor = 0;
	private int mHasRecordTextColor = 0;
	private int mCellBgColor = 0;

	// 楂樹寒鍥?
	private Bitmap mHasRecordImg = null;
	private Bitmap mCheckedBg = null;
	
	public DisplayConfig(Context context){
		mWeekTextSize = 12;
		mWeekBgColor = context.getResources().getColor(R.color.white);
		mWeekTextColor = context.getResources().getColor(R.color.blue);
		mDividerColor = context.getResources().getColor(R.color.divider);

		mCellTextSize = 14;
		// 瀛椾綋棰滆壊
		mCellTextColor = context.getResources().getColor(R.color.ak_text_title);
		mOtherMonthTextcolor = context.getResources().getColor(
				R.color.ak_text_info);
		mTodayTextColor = context.getResources().getColor(R.color.blue);
		mCheckedTextColor = context.getResources().getColor(
				R.color.white);
		mHasRecordTextColor = context.getResources().getColor(
				R.color.ak_text_title);
		mCellBgColor = context.getResources().getColor(R.color.white);
		
    	BitmapFactory.Options opts = new BitmapFactory.Options();
//      hasSelectTag = BitmapFactory.decodeResource(getResources(), mCheckedBg, opts);
		
		mHasRecordImg = BitmapFactory.decodeResource(context.getResources(), R.drawable.ak_calendar_nol, opts);
		mCheckedBg = BitmapFactory.decodeResource(context.getResources(), R.drawable.ak_calendar_press, opts);
	}

	public int getWeekTextSize() {
		return mWeekTextSize;
	}

	public void setWeekTextSize(int size) {
		this.mWeekTextSize = size;
	}

	public int getWeekBgColor() {
		return mWeekBgColor;
	}

	public void setWeekBgColor(int color) {
		this.mWeekBgColor = color;
	}

	public int getWeekTextColor() {
		return mWeekTextColor;
	}

	public void setWeekTextColor(int color) {
		this.mWeekTextColor = color;
	}

	public int getDividerColor() {
		return mDividerColor;
	}

	public void setDividerColor(int color) {
		this.mDividerColor = color;
	}

	public int getCellTextSize() {
		return mCellTextSize;
	}

	public void setCellTextSize(int size) {
		this.mCellTextSize = size;
	}

	public int getCellTextColor() {
		return mCellTextColor;
	}

	public void setCellTextColor(int color) {
		this.mCellTextColor = color;
	}

	public int getOtherMonthTextcolor() {
		return mOtherMonthTextcolor;
	}

	public void setOtherMonthTextcolor(int color) {
		this.mOtherMonthTextcolor = color;
	}

	public int getTodayTextColor() {
		return mTodayTextColor;
	}

	public void setTodayTextColor(int color) {
		this.mTodayTextColor = color;
	}

	public int getCheckedTextColor() {
		return mCheckedTextColor;
	}

	public void setCheckedTextColor(int color) {
		this.mCheckedTextColor = color;
	}

	public int getHasRecordTextColor() {
		return mHasRecordTextColor;
	}

	public void setHasRecordColor(int color) {
		this.mHasRecordTextColor = color;
	}

	public Bitmap getHasRecordImg() {
		return mHasRecordImg;
	}

	public void setHasRecordImg(Bitmap bmp) {
		this.mHasRecordImg = bmp;
	}

	public Bitmap getCheckedBg() {
		return mCheckedBg;
	}

	public void setCheckedBg(Bitmap bmp) {
		this.mCheckedBg = bmp;
	}

	public int getCellBgColor() {
		return mCellBgColor;
	}

	public void setCellBgColor(int color) {
		this.mCellBgColor = color;
	}

}
