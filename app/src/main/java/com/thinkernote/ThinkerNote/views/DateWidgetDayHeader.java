package com.thinkernote.ThinkerNote.views;
import com.thinkernote.ThinkerNote.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;

/**
 * 鏃ュ巻鎺т欢澶撮儴缁樺埗绫?
 * @Descriptio.n: 鏃ュ巻鎺т欢澶撮儴缁樺埗绫?
 */
public class DateWidgetDayHeader extends View {
    // 瀛椾綋澶у皬
    private Paint mPaint = new Paint();
    private RectF mRect = new RectF();
    private int mWeekDay = -1;
    
    private int mTextSize = 12;
    private int mBgColor = getResources().getColor(R.color.white);
    private int mTextColor = getResources().getColor(R.color.blue);
    private int mDividerColor = getResources().getColor(R.color.divider);
    private float mDividerWidth = getResources().getDisplayMetrics().density * 1;

    public DateWidgetDayHeader(Context context, int iWidth, int iHeight) {
        super(context);
        setLayoutParams(new LayoutParams(iWidth, iHeight));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 璁剧疆鐭╁舰澶у皬
        mRect.set(0, 0, this.getWidth(), this.getHeight());
       	mRect.inset(0, 0);//鍋忕Щ

        // 缁樺埗鏃ュ巻澶撮儴
        drawDayHeader(canvas);
    }
    
    private int changeTextSize(int size) {  
    	Resources r = getResources();  
    	return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,  
    	        size, r.getDisplayMetrics());  
    } 

    private void drawDayHeader(Canvas canvas) {
        // 鐢荤煩褰紝骞惰缃煩褰㈢敾绗旂殑棰滆壊
        mPaint.setColor(mBgColor);
        canvas.drawRect(mRect, mPaint);

        // 鍐欏叆鏃ュ巻澶撮儴锛岃缃敾绗斿弬鏁?
        mPaint.setTypeface(null);
        mPaint.setTextSize(changeTextSize(mTextSize));
        mPaint.setAntiAlias(true);
//        pt.setFakeBoldText(true);
        mPaint.setFakeBoldText(false);
        mPaint.setColor(mTextColor);

        // draw day name
        final String sDayName = DayStyle.getWeekDayName(mWeekDay);
        final int iPosX = (int) mRect.left + ((int) mRect.width() >> 1)
                - ((int) mPaint.measureText(sDayName) >> 1);
        final int iPosY = (int) (this.getHeight()
                - (this.getHeight() - getTextHeight()) / 2 - mPaint
                .getFontMetrics().bottom);
        canvas.drawText(sDayName, iPosX, iPosY, mPaint);
        
        
    	Paint paint = new Paint();
//    	paint.setTypeface(null);
//    	paint.setTextSize(mTextSize);
//    	paint.setAntiAlias(true);
//    	paint.setFakeBoldText(true);
    	paint.setColor(mDividerColor);
    	paint.setStrokeWidth(mDividerWidth);
//    	canvas.drawLine(0, 0, this.getWidth(), 0, paint);
    	canvas.drawLine(0, this.getHeight(), this.getWidth(), this.getHeight(), paint);
    }

    // 寰楀埌瀛椾綋楂樺害
    private int getTextHeight() {
        return (int) (-mPaint.ascent() + mPaint.descent());
    }

    // 寰楀埌涓?鏄熸湡鐨勭鍑犲ぉ鐨勬枃鏈爣璁?
    public void setData(int iWeekDay) {
        this.mWeekDay = iWeekDay;
    }
    
    public void setTextSize(int size){
    	mTextSize = size;
    }
    
    public void setTextColor(int color){
    	mTextColor = color;
    }
    
    public void setBgColor(int color){
    	mBgColor = color;
    }
    
    public void setDividerColor(int color){
    	mDividerColor = color;
    }
    
    public void setDividerWdith(float wdith){
    	mDividerWidth = wdith;
    }
}