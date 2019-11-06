package com.thinkernote.ThinkerNote.Activity;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.utils.BitmapUtils;
import com.thinkernote.ThinkerNote.base.TNActBase;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

/**
 * 设置--关于我们--反馈--照片预览
 */
public class ViewImageActivity extends TNActBase {
	
	private ImageView mImageView;
	private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        Bundle bundle = getIntent().getExtras();
        String path = bundle.getString("uri");
        
        mImageView = (ImageView) findViewById(R.id.view_image);
        mBitmap = BitmapUtils.addImage(path, 2);
        mImageView.setImageBitmap(mBitmap);
        
        findViewById(R.id.view_image_delete).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mImageView.setImageBitmap(null);
				mBitmap.recycle();
				finish();
			}
		});
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	mImageView.setImageBitmap(null);
		mBitmap.recycle();
		finish();
    }
}
