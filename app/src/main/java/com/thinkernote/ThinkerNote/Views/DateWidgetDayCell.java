package com.thinkernote.ThinkerNote.Views;

import java.util.Calendar;

import com.thinkernote.ThinkerNote.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout.LayoutParams;

/**
 * 鏃ュ巻鎺т欢鍗曞厓鏍肩粯鍒剁被
 * @Description: 鏃ュ巻鎺т欢鍗曞厓鏍肩粯鍒剁被
 */
public class DateWidgetDayCell extends View {

    // 鍩烘湰鍏冪礌
    private OnItemClick itemClick = null;
    private Paint pt = new Paint();
    private RectF rect = new RectF();
    private String sDate = "";

    // 褰撳墠鏃ユ湡
    private int iDateYear = 0;
    private int iDateMonth = 0;
    private int iDateDay = 0;

    private boolean bTouchedDown = false;

    public static int ANIM_ALPHA_DURATION = 100;

    private long touchTime;

    // 瀛椾綋澶у皬
    private int mTextSize = 14;
    //瀛椾綋棰滆壊
    private int mTextColor = getResources().getColor(R.color.ak_text_title);
    
    //楂樹寒鍥?
    private Bitmap mTagImg = null;
    
    public interface OnItemClick {
        public void OnClick(DateWidgetDayCell item);
    }

    // 鏋勯?犲嚱鏁?
    public DateWidgetDayCell(Context context, int iWidth, int iHeight) {
        super(context);
        setFocusable(true);
        setLayoutParams(new LayoutParams(iWidth, iHeight));
    }

    // 鍙栧彉閲忓??
    public Calendar getDate() {
        Calendar calDate = Calendar.getInstance();
        calDate.clear();
        calDate.set(Calendar.YEAR, iDateYear);
        calDate.set(Calendar.MONTH, iDateMonth);
        calDate.set(Calendar.DAY_OF_MONTH, iDateDay);
        return calDate;
    }

    // 璁剧疆鍙橀噺鍊?
    public void setData(int iYear, int iMonth, int iDay, Boolean bToday,
                        Boolean bHoliday, int iActiveMonth, boolean hasRecord) {
        iDateYear = iYear;
        iDateMonth = iMonth;
        iDateDay = iDay;

        this.sDate = Integer.toString(iDateDay);
        
    }

    public void setItemClick(OnItemClick itemClick) {
        this.itemClick = itemClick;
    }

    public boolean IsViewFocused() {
        return (this.isFocused() || bTouchedDown);
    }
    
    // 閲嶈浇缁樺埗鏂规硶
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        rect.set(0, 0, this.getWidth(), this.getHeight());
        rect.inset(0, 0);//鍐呴棿璺?

        final boolean bFocused = IsViewFocused();

        drawDayView(canvas, bFocused);
        drawDayNumber(canvas);
    }

    // 缁樺埗鏃ュ巻鏂规牸
    private void drawDayView(Canvas canvas, boolean bFocused) {

    	//缁樺埗鏂规牸鑳屾櫙
//        if (bSelected || bFocused) {
//            LinearGradient lGradBkg = null;
//
//            if (bFocused) {
//                lGradBkg = new LinearGradient(rect.left, 0, rect.right, 0,
//                        0xffaa5500, 0xffffddbb, Shader.TileMode.CLAMP);
//            }
//
//            if (bSelected) {
//                lGradBkg = new LinearGradient(rect.left, 0, rect.right, 0,
//                        0xff225599, 0xffbbddff, Shader.TileMode.CLAMP);
//            }
//
//            if (lGradBkg != null) {
//                pt.setShader(lGradBkg);
//                canvas.drawRect(rect, pt);
//            }
//
//            pt.setShader(null);
//
//        } else {
//            pt.setColor(getResources().getColor(R.color.ak_divider_color));
//            canvas.drawRect(rect, pt);
//        }
    	if(mTagImg != null)
    		createReminder(canvas, mTagImg);
    }
    
    private int changeTextSize(int size) {  
    	Resources r = getResources();  
    	return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,  
    	        size, r.getDisplayMetrics());  
    }  

    // 缁樺埗鏃ュ巻涓殑鏁板瓧
    private void drawDayNumber(Canvas canvas) {
        // draw day number
        pt.setTypeface(null);
        pt.setAntiAlias(true);
        pt.setShader(null);
        pt.setFakeBoldText(true);
        pt.setTextSize(changeTextSize(mTextSize));
        pt.setColor(mTextColor);

        final int iPosX = (int) rect.left + ((int) rect.width() >> 1)
                - ((int) pt.measureText(sDate) >> 1);

        final int iPosY = (int) (this.getHeight()
                - (this.getHeight() - getTextHeight()) / 2 - pt
                .getFontMetrics().bottom);

        canvas.drawText(sDate, iPosX, iPosY, pt);
    }

    // 寰楀埌瀛椾綋楂樺害
    private int getTextHeight() {
        return (int) (-pt.ascent() + pt.descent());
    }

    private void doItemClick() {
        if (itemClick != null)
            itemClick.OnClick(this);
    }
    
    // 鐐瑰嚮浜嬩欢
    @SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean bHandled = false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchTime = System.currentTimeMillis();
            bHandled = true;
            bTouchedDown = true;
//            invalidate();
            startAlphaAnimIn(DateWidgetDayCell.this);
        }else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            touchTime = 0;
            bHandled = true;
            bTouchedDown = false;
//            invalidate();
        }else if (event.getAction() == MotionEvent.ACTION_UP) {
            bHandled = true;
            bTouchedDown = false;
//            invalidate();
            if((System.currentTimeMillis() - touchTime) < 500) {
                int[] location = new int[2];
                getLocationInWindow(location);
                Rect rect = new Rect(location[0], location[1], location[0] + getWidth(), location[1] + getHeight());
                if(rect.contains((int)event.getRawX(), (int)event.getRawY())){//澶勪簬璇ell鍖哄煙鍐呮椂鎵嶅搷搴旂偣鍑讳簨浠?
                	doItemClick();
                }
            }
        }
        return bHandled;
    }

    // 鐐瑰嚮浜嬩欢
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean bResult = super.onKeyDown(keyCode, event);
        if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                || (keyCode == KeyEvent.KEYCODE_ENTER)) {
            doItemClick();
        }
        return bResult;
    }

    // 鍗婇?忔槑搴︽笎鍙樺姩鐢?
    private void startAlphaAnimIn(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.5F, 1);
        anim.setDuration(ANIM_ALPHA_DURATION);
        anim.startNow();
        view.startAnimation(anim);
    }

    // 璁＄畻鍦嗗楂?
    private void createReminder(Canvas canvas, Bitmap bitmap) {
//        int bX = (int) rect.left + ((int) rect.width() >> 1)
//                + ((int) pt.measureText(sDate) >> 1);
//
//        int bY = (int) (this.getHeight()
//                - (this.getHeight() - getTextHeight()) - pt
//                .getFontMetrics().bottom);
    	
//    	int bX = ((int) rect.width() >> 1) / 2;
//    	int bY = (int) (this.getHeight()) / 4;
//    	int bY = ((int) rect.height() >> 1) / 4;
//    	int bY = (int) (this.getHeight() - (this.getHeight() - getTextHeight()));
    	
    	int bX = (int)((rect.width() - bitmap.getWidth()) / 2);
    	int bY = (int)((rect.height() - bitmap.getHeight()) / 2);
        canvas.drawBitmap(bitmap, bX, bY, pt);
    }

    //鍦ㄥ彸涓婅鐢讳竴涓笁瑙掑舰鏍囪
//	private void createReminder(Canvas canvas, int Color) {
//        pt.setStyle(Paint.Style.FILL_AND_STROKE);
//        pt.setColor(Color);
//        Path path = new Path();
//        path.moveTo(rect.right - rect.width() / 4, rect.top);
//        path.lineTo(rect.right, rect.top);
//        path.lineTo(rect.right, rect.top + rect.width() / 4);
//        path.lineTo(rect.right - rect.width() / 4, rect.top);
//        path.close();
//        canvas.drawPath(path, pt);
//    }


	public void setTextSize(int size) {
		this.mTextSize = size;
	}

	public void setTextColor(int color) {
		this.mTextColor = color;
	}

	public void setTagImg(Bitmap bmp) {
		this.mTagImg = bmp;
	}
	
	public boolean compare(int year, int month, int day){
		return year == iDateYear && month == iDateMonth && day == iDateDay;
	}
    
}