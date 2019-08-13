package com.thinkernote.ThinkerNote.other;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;

import com.thinkernote.ThinkerNote.Utils.MLog;

public class TuyaView extends View {
//    private static final float MINP = 0.25f;
//    private static final float MAXP = 0.75f;
    
    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;

    private Paint   mPaint;
    
    private Stack<TuyaPP> mUndoStack;
    private Stack<TuyaPP> mRedoStack;
    

    public TuyaView(Context c, Paint paint, int width, int height) {
        super(c);
        
        mPaint = paint;
        
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mBitmap.eraseColor(DRAWING_CACHE_QUALITY_AUTO);
        mCanvas.drawColor(Color.WHITE);
        
        mUndoStack = new Stack<TuyaPP>();
        mRedoStack = new Stack<TuyaPP>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        MLog.i("onSizeChanged", w+","+h+","+oldw+","+oldh);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        
        canvas.drawPath(mPath, mPaint);
    }
    
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    
    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }
    private void touch_up() {
        mPath.lineTo(mX, mY);
        
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        
        mUndoStack.push(new TuyaPP(mPath, mPaint));
        mRedoStack.clear();
        
        // kill this so we don't double draw
        mPath.reset(); 
    }
    
    public void undo(){
    	mBitmap.eraseColor(DRAWING_CACHE_QUALITY_AUTO);
    	if( !mUndoStack.empty()){
    		TuyaPP last = mUndoStack.pop();
    		mRedoStack.push(last);

    		for(int i=0; i < mUndoStack.size(); i ++){
    			TuyaPP p = mUndoStack.get(i);
    	        mCanvas.drawPath(p.mPath, p.mPaint);
    		}
    	}   	
    	invalidate();
    }
    
    public void clear(){
    	mUndoStack.clear();
    	mRedoStack.clear();
    	undo();
    }
    
    public void redo(){
    	if( !mRedoStack.empty() ){
    		TuyaPP p = mRedoStack.pop();
    		mUndoStack.push(p);
    		
    		mCanvas.drawPath(p.mPath, p.mPaint);
        	invalidate();
    	}
    }
    
    public String saveImage(){
    	try {
    		String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.thinkernote.ThinkerNote/files/temp/" +
    				System.currentTimeMillis() + ".jpg";
        	File file = new File(path);
        	if(!file.exists()){
        		file.getParentFile().mkdirs();
        		file.createNewFile();
        	}
			FileOutputStream out = new FileOutputStream(file);
			mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
	    	return path;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }
    
    private class TuyaPP{
        public Path    mPath;
        public Paint   mPaint;
        
        TuyaPP(Path path, Paint paint){
        	mPath = new Path(path);
        	mPaint = new Paint(paint);
        }
    }

}
