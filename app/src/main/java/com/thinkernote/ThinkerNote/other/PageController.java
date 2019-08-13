package com.thinkernote.ThinkerNote.other;

import com.thinkernote.ThinkerNote.R;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PageController {
	public static void setCount(ViewGroup pager, int count, float scale){
		pager.removeAllViews();
		
		for(int i=0; i<count; i ++){
			LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, 
					LinearLayout.LayoutParams.WRAP_CONTENT,
					Gravity.NO_GRAVITY);
			imageParams.setMargins((int)(3*scale), 0, (int)(3*scale), 0); //left, top, right, bottom

			ImageView image = new ImageView(pager.getContext());
			//image.setImageResource(R.drawable.dot);
			image.setLayoutParams(imageParams);
			pager.addView(image);
		}
	}
	
	public static void setCurrent(ViewGroup pager, int current, boolean hasSearch){
		for(int i=0; i< pager.getChildCount(); i ++){
			ImageView image = (ImageView) pager.getChildAt(i);
			if( i == 0 && hasSearch){
				if( i == current)
					image.setImageResource(R.drawable.dot_search_cur);
				else
					image.setImageResource(R.drawable.dot_search);
			}else {
				if( i == current)
					image.setImageResource(R.drawable.dot_cur);
				else
					image.setImageResource(R.drawable.dot);
			}
		}
	}
}
