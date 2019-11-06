/**
 * 
 */
package com.thinkernote.ThinkerNote.views;

import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class MyCalendar extends LinearLayout {
	private DisplayConfig mDisplayConfig = null;
	
	private ArrayList<DateWidgetDayCell> mDays = new ArrayList<DateWidgetDayCell>();
	private ArrayList<DateWidgetDayHeader> mWeeks = new ArrayList<DateWidgetDayHeader>();
	
	// 鏃ユ湡鍙橀噺
	public static Calendar calStartDate = Calendar.getInstance();
	private Calendar calToday = Calendar.getInstance();
	private Calendar calCalendar = Calendar.getInstance();
	private Calendar calSelected = Calendar.getInstance();

	// 褰撳墠鎿嶄綔鏃ユ湡
	private int iMonthViewCurrentMonth = 0;
	private int iMonthViewCurrentYear = 0;
	// private int iFirstDayOfWeek = Calendar.MONDAY;
	private int iFirstDayOfWeek = Calendar.SUNDAY;

	private int Calendar_Width = 0;
	private int Cell_Width = 0;

	private Calendar startDate = null;
	private Calendar endDate = null;
    
    private DateWidgetDayCell mCheckedCell;
    private float mDensity;
    
    
    private OnMonthChangeListener mOnMonthChangeListener;
    private OnDayClickListener mOnDayClickListener;

	/**
	 * @param context
	 */
	public MyCalendar(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public MyCalendar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	@SuppressLint("NewApi")
	public MyCalendar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mDensity = getResources().getDisplayMetrics().density;
		mDisplayConfig = new DisplayConfig(getContext());
		
		// 鑾峰緱灞忓箷瀹藉拰楂橈紝骞惰▓绠楀嚭灞忓箷瀵害鍒嗕竷绛変唤鐨勫ぇ锟�?
		Calendar_Width = getResources().getDisplayMetrics().widthPixels;
		Cell_Width = Calendar_Width / 7 + 1;

		// 璁＄畻鏈湀鏃ュ巻涓殑绗竴锟�?(锟�?鑸槸涓婃湀鐨勬煇锟�?)锛屽苟鏇存柊鏃ュ巻
		setCalendarStartDate();
		
		setOrientation(VERTICAL);
		addView(generateCalendarHeader());
		mDays.clear();

		for (int iRow = 0; iRow < 6; iRow++) {
			addView(generateCalendarRow());
		}
		
		initDisplayConfig();
		DateWidgetDayCell daySelected = updateCalendar();

		if (daySelected != null)
			daySelected.requestFocus();

		startDate = getStartDate();
		calToday = getTodayDate();

		endDate = getEndDate(startDate);
	}
	
	private void initDisplayConfig(){
		for(DateWidgetDayHeader week : mWeeks){
			week.setTextSize(mDisplayConfig.getWeekTextSize());
			week.setBgColor(mDisplayConfig.getWeekBgColor());
			week.setTextColor(mDisplayConfig.getWeekTextColor());
			week.setDividerWdith(mDensity * 1);
		}
//		
		for(DateWidgetDayCell cell : mDays){
//			cell.setTextColor(config.getCellTextColor());
			cell.setTextSize(mDisplayConfig.getCellTextSize());
//			cell.setTagImg(null);
		}
		setBackgroundColor(mDisplayConfig.getCellBgColor());
	}
	
	/**
	 * 鍒濆鍖栨樉绀洪厤锟�?
	 * @param config
	 */
	public void setDisplayConfig(DisplayConfig config){
		mDisplayConfig = config;
		initDisplayConfig();
		updateCalendar();
	}

	private int millisecondsToDays(long intervalMs) {
		return Math.round((intervalMs / (1000 * 86400)));
	}

	private void setTimeToMidnight(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}

	// 鐢熸垚甯冨眬
	private LinearLayout createLayout(int iOrientation) {
		LinearLayout lay = new LinearLayout(getContext());
		lay.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		lay.setOrientation(iOrientation);
		return lay;
	}

	// 鐢熸垚鏃ュ巻澶撮儴
	private View generateCalendarHeader() {
		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);

		for (int iDay = 0; iDay < 7; iDay++) {
			DateWidgetDayHeader week = new DateWidgetDayHeader(getContext(),
					Cell_Width, (int) (24 * mDensity));
			final int iWeekDay = DayStyle.getWeekDay(iDay, iFirstDayOfWeek);
			week.setData(iWeekDay);
			
			mWeeks.add(week);
			layRow.addView(week);
		}
		return layRow;
	}

	// 鐢熸垚鏃ュ巻涓殑锟�?琛岋紝浠呯敾鐭╁舰
	private View generateCalendarRow() {
		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
		for (int iDay = 0; iDay < 7; iDay++) {
			// DateWidgetDayCell dayCell = new DateWidgetDayCell(getActivity(),
			// Cell_Width, Cell_Width);
			DateWidgetDayCell dayCell = new DateWidgetDayCell(getContext(),
					Cell_Width, Cell_Width / 4 * 3);

			dayCell.setItemClick(mOnDayCellClick);
			mDays.add(dayCell);
			layRow.addView(dayCell);
		}
		return layRow;
	}

	private Calendar getTodayDate() {
		Calendar cal_Today = Calendar.getInstance();
		cal_Today.set(Calendar.HOUR_OF_DAY, 0);
		cal_Today.set(Calendar.MINUTE, 0);
		cal_Today.set(Calendar.SECOND, 0);
		cal_Today.setFirstDayOfWeek(Calendar.MONDAY);

		return cal_Today;
	}

	// 寰楀埌褰撳墠鏃ュ巻涓殑绗竴锟�?
	private Calendar getStartDate() {
		int iDay = 0;
		Calendar cal_Now = Calendar.getInstance();
		cal_Now.set(Calendar.DAY_OF_MONTH, 1);
		cal_Now.set(Calendar.HOUR_OF_DAY, 0);
		cal_Now.set(Calendar.MINUTE, 0);
		cal_Now.set(Calendar.SECOND, 0);
		cal_Now.setFirstDayOfWeek(Calendar.MONDAY);

		iDay = cal_Now.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;

		if (iDay < 0) {
			iDay = 6;
		}

		cal_Now.add(Calendar.DAY_OF_WEEK, -iDay);

		return cal_Now;
	}

	private Calendar getEndDate(Calendar startDate) {
		// Calendar end = GetStartDate(enddate);
		Calendar endDate = (Calendar) startDate.clone();
		endDate.add(Calendar.DAY_OF_MONTH, 41);
		return endDate;
	}

	// 璁剧疆褰撳ぉ鏃ユ湡鍜岃閫変腑鏃ユ湡
	private void setCalendarStartDate() {
		calToday.setTimeInMillis(System.currentTimeMillis());
		calToday.setFirstDayOfWeek(iFirstDayOfWeek);

		if (calSelected.getTimeInMillis() == 0) {
			calStartDate.setTimeInMillis(System.currentTimeMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		} else {
			calStartDate.setTimeInMillis(calSelected.getTimeInMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		}

		updateStartDateForMonth();
	}

	// 鐢变簬鏈棩鍘嗕笂鐨勬棩鏈熼兘鏄粠鍛ㄤ竴锟�?濮嬬殑锛屾鏂规硶鍙帹绠楀嚭涓婃湀鍦ㄦ湰鏈堟棩鍘嗕腑鏄剧ず鐨勫ぉ锟�?
	private void updateStartDateForMonth() {
		iMonthViewCurrentMonth = calStartDate.get(Calendar.MONTH);
		iMonthViewCurrentYear = calStartDate.get(Calendar.YEAR);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.HOUR_OF_DAY, 0);
		calStartDate.set(Calendar.MINUTE, 0);
		calStartDate.set(Calendar.SECOND, 0);
		// update days for week
		if(mOnMonthChangeListener != null){
			mOnMonthChangeListener.onMonthChange(calStartDate.get(Calendar.YEAR), calStartDate.get(Calendar.MONTH));
		}
		int iDay = 0;
		int iStartDay = iFirstDayOfWeek;

		if (iStartDay == Calendar.MONDAY) {
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
			if (iDay < 0)
				iDay = 6;
		}

		if (iStartDay == Calendar.SUNDAY) {
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
			if (iDay < 0)
				iDay = 6;
		}

		calStartDate.add(Calendar.DAY_OF_WEEK, -iDay);
	}

	// 鏇存柊鏃ュ巻
	private DateWidgetDayCell updateCalendar() {
		DateWidgetDayCell daySelected = null;
		boolean bSelected = false;
		final boolean bIsSelection = (calSelected.getTimeInMillis() != 0);
		final int iSelectedYear = calSelected.get(Calendar.YEAR);
		final int iSelectedMonth = calSelected.get(Calendar.MONTH);
		final int iSelectedDay = calSelected.get(Calendar.DAY_OF_MONTH);
		calCalendar.setTimeInMillis(calStartDate.getTimeInMillis());

		for (int i = 0; i < mDays.size(); i++) {
			final int iYear = calCalendar.get(Calendar.YEAR);
			final int iMonth = calCalendar.get(Calendar.MONTH);
			final int iDay = calCalendar.get(Calendar.DAY_OF_MONTH);
			final int iDayOfWeek = calCalendar.get(Calendar.DAY_OF_WEEK);
			DateWidgetDayCell dayCell = mDays.get(i);

			// 鍒ゆ柇鏄惁褰撳ぉ
			boolean bToday = false;

			if (calToday.get(Calendar.YEAR) == iYear) {
				if (calToday.get(Calendar.MONTH) == iMonth) {
					if (calToday.get(Calendar.DAY_OF_MONTH) == iDay) {
						bToday = true;
					}
				}
			}

			// check holiday
			boolean bHoliday = false;
			if ((iDayOfWeek == Calendar.SATURDAY)
					|| (iDayOfWeek == Calendar.SUNDAY))
				bHoliday = true;
			if ((iMonth == Calendar.JANUARY) && (iDay == 1))
				bHoliday = true;

			// 鏄惁琚拷?锟戒腑
			bSelected = false;

			if (bIsSelection && mCheckedCell != null) {
				if ((iSelectedDay == iDay) && (iSelectedMonth == iMonth)
						&& (iSelectedYear == iYear)) {
					bSelected = true;
				}
			}
//			dayCell.setSelected(bSelected);

			// 鍒ゆ柇鏄惁瀛樺湪鏁版嵁锟�?

			// 鏄惁鏈夎锟�?
			boolean hasRecord = false;

			if (bSelected)
				daySelected = dayCell;

			dayCell.setData(iYear, iMonth, iDay, bToday, bHoliday,
					iMonthViewCurrentMonth, hasRecord);
			
			dayCell.setTextColor(mDisplayConfig.getCellTextColor());
			dayCell.setTagImg(null);
			if(bHoliday){
	        	
	        }
			if(bToday){
				dayCell.setTextColor(mDisplayConfig.getTodayTextColor());
	        }
			if(hasRecord){
				dayCell.setTextColor(mDisplayConfig.getHasRecordTextColor());
				dayCell.setTagImg(mDisplayConfig.getHasRecordImg());
	        }
			if(iMonth != iMonthViewCurrentMonth){//闈炲綋鍓嶆湀锟�?
				dayCell.setTextColor(mDisplayConfig.getOtherMonthTextcolor());
	        }
			if(bSelected){
				dayCell.setTextColor(mDisplayConfig.getCheckedTextColor());
				dayCell.setTagImg(mDisplayConfig.getCheckedBg());
	        }

			dayCell.invalidate();
			calCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		invalidate();

		return daySelected;
	}

	// 鐐瑰嚮鏃ュ巻锛岃Е鍙戜簨锟�?
	private DateWidgetDayCell.OnItemClick mOnDayCellClick = new DateWidgetDayCell.OnItemClick() {
		public void OnClick(DateWidgetDayCell item) {
			mCheckedCell = item;
			calSelected.setTimeInMillis(item.getDate().getTimeInMillis());
//			int position = GetNumFromDate(calSelected, startDate);

			item.setSelected(true);
			updateCalendar();

			Boolean isToNextMoth = null;
			if (calSelected.get(Calendar.YEAR) > iMonthViewCurrentYear) {
				isToNextMoth = true;
			} else if (calSelected.get(Calendar.YEAR) < iMonthViewCurrentYear) {
				isToNextMoth = false;
			} else if (calSelected.get(Calendar.MONTH) > iMonthViewCurrentMonth) {
				isToNextMoth = true;
			} else if (calSelected.get(Calendar.MONTH) < iMonthViewCurrentMonth) {
				isToNextMoth = false;
			}

			if (isToNextMoth != null) {
				if(isToNextMoth.booleanValue()){
					toNextMoth();
				}else{
					toUpMonth();
				}
			}
			
			if(mOnDayClickListener != null){
				mOnDayClickListener.onDayClick(calSelected.get(Calendar.YEAR), calSelected.get(Calendar.MONTH), calSelected.get(Calendar.DAY_OF_MONTH));
			}
		}
	};
	
	/**
	 * 
	 * @param year
	 * @param month 锟�?0锟�?锟�?
	 * @param day
	 * @param tagImg
	 * @param textColor
	 */
	public void setOneDayDisplay(int year, int month, int day, Bitmap tagImg, int textColor){
		for(DateWidgetDayCell cell : mDays){
			if(cell.compare(year, month, day)){
				cell.setTagImg(tagImg);
				cell.setTextColor(textColor);
				cell.invalidate();
			}
		}
	}

	/**
	 * 寰楀埌褰撳ぉ鍦ㄦ棩鍘嗕腑鐨勫簭锟�?
	 * @param now
	 * @param returnDate
	 * @return
	 */
	public int GetNumFromDate(Calendar now, Calendar returnDate) {
		Calendar cNow = (Calendar) now.clone();
		Calendar cReturnDate = (Calendar) returnDate.clone();
		setTimeToMidnight(cNow);
		setTimeToMidnight(cReturnDate);

		long todayMs = cNow.getTimeInMillis();
		long returnMs = cReturnDate.getTimeInMillis();
		long intervalMs = todayMs - returnMs;
		int index = millisecondsToDays(intervalMs);

		return index;
	}
	
	/**
	 * 鑾峰彇閫変腑鐨勬棩锟�?
	 * @return 0涓烘湭閫変腑浠讳綍
	 */
	public long getCheckedDate(){
		if(mCheckedCell != null)
			return mCheckedCell.getDate().getTimeInMillis();
		
		return 0;
	}
	
	/**
	 * 鍒囨崲鍒颁笅锟�?涓湀
	 */
	public void toNextMoth(){
		iMonthViewCurrentMonth++;
		if (iMonthViewCurrentMonth == 12) {
			iMonthViewCurrentMonth = 0;
			iMonthViewCurrentYear++;
		}

		calStartDate.setTimeInMillis(0);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);

		updateStartDateForMonth();
		startDate = (Calendar) calStartDate.clone();
		endDate = getEndDate(startDate);

		updateCalendar();
	}
	
	/**
	 * 鍒囨崲鍒颁笂锟�?涓湀
	 */
	public void toUpMonth(){
		iMonthViewCurrentMonth--;
		if (iMonthViewCurrentMonth == -1) {
			iMonthViewCurrentMonth = 11;
			iMonthViewCurrentYear--;
		}
		calStartDate.setTimeInMillis(0);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);

		updateStartDateForMonth();
		startDate = (Calendar) calStartDate.clone();
		endDate = getEndDate(startDate);

		updateCalendar();
	}
	
	public interface OnMonthChangeListener{
		/**
		 * 
		 * @param year
		 * @param month 锟�?0锟�?锟�?
		 */
		public void onMonthChange(int year, int month);
	}
	
	/**
	 * 鏈堜唤鍒囨崲浜嬩欢
	 * @param l
	 */
	public void setOnMonthChangeListener(OnMonthChangeListener l){
		mOnMonthChangeListener = l;
	}
	
	public interface OnDayClickListener{
		/**
		 * 
		 * @param year 锟�?
		 * @param month 锟�? 锟�?0锟�?锟�?
		 * @param day 锟�?
		 */
		public void onDayClick(int year, int month, int day);
	}
	
	/**
	 * 璁剧疆澶╃殑鐐瑰嚮鏁堟灉
	 * @param l
	 */
	/**
	 * @param l
	 */
	public void setOnDayClickListener(OnDayClickListener l){
		mOnDayClickListener = l;
	}

}
