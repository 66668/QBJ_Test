package com.thinkernote.ThinkerNote.Activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.EditText;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.base.TNActBase;

/**
 * 搜索界面
 * 只是中间界面，将结果传递给并跳转到TNNoteListAct处理
 * sjy 0613
 */
public class TNSearchAct extends TNActBase implements OnClickListener, OnKeyListener{
	private EditText mEditeText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		
		mEditeText = (EditText)findViewById(R.id.serch_edittext);
		findViewById(R.id.serch_serch_btn).setOnClickListener(this);
		mEditeText.setOnKeyListener(this);
	}

	@Override
	protected void configView() {
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.serch_serch_btn:
			serch();
			break;
		}
	}
	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_ENTER 
				&& event.getRepeatCount() == 0 
				&& event.getAction() == 1){
			serch();
			return true;
		}
		return false;
	}

	private void serch(){
		String word = mEditeText.getText().toString().trim();
		//保存
		TNSettings.getInstance().searchWord = word;
		TNSettings.getInstance().savePref(true);

		if( word.length() > 0){
			Bundle b = new Bundle();
			b.putLong("UserId", TNSettings.getInstance().userId);
			b.putInt("ListType", 5);
			b.putString("ListDetail", word);
			//跳转
			startActivity(TNNoteListAct.class, b);
			finish();
		}
	}

}
