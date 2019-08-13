package com.thinkernote.ThinkerNote.other;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.Action.TNAction.TNRunner;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;

public class SeekBarPreference extends DialogPreference implements  
	OnSeekBarChangeListener {  
	
	private SeekBar seekBar;
	private String initText;
	private int initMax;
	private int initProgress;
	private TextView textView;
	private TNRunner callback;
	
	public SeekBarPreference(Context context, AttributeSet attrs) {  
		super(context, attrs);  
	}  
	
	@Override  
	protected void onBindDialogView(View view) {  
		super.onBindDialogView(view);
		
		seekBar = (SeekBar) view.findViewById(R.id.seekbar_dialog_seekbar);  
		textView = (TextView) view.findViewById(R.id.seekbar_dialog_textview);  
		seekBar.setOnSeekBarChangeListener(this);  
		seekBar.setMax(initMax);
		seekBar.setProgress(initProgress);
		textView.setText(initText + " " + initProgress);
	}  
	
	@Override  
	protected void onDialogClosed(boolean positiveResult) {  
		if (positiveResult) {
			MLog.i("Dialog closed", "You click positive button");
			if(callback != null){
				callback.run(seekBar.getProgress());
			}
		} else {
			MLog.i("Dialog closed", "You click negative button");
		}  
	}  
	
	@Override  
	public void onProgressChanged(SeekBar seekBar, int progress,  
			boolean fromUser) {
		textView.setText(initText + " " + progress);
	}  
	
	@Override  
	public void onStartTrackingTouch(SeekBar seekBar) {  
	
	}  
	
	@Override  
	public void onStopTrackingTouch(SeekBar seekBar) {  
	} 
	
	public void init(String text, int progress, int max ){
		initText = text;
		initMax = max;
		initProgress = progress;
	}
	
	public void setCallback(TNRunner cb){
		callback = cb;
	}

}
