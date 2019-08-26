package com.thinkernote.ThinkerNote.Activity.unuse;

import java.util.Vector;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.General.TNHandleError;
import com.thinkernote.ThinkerNote.General.TNUtilsDialog;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsTag;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.base.TNActBase;
/**
 *  未使用
 */
public class TNToEmailAct extends TNActBase
	implements OnClickListener{

	/* Bundle:
	 * NoteId
	 * From
	 * Subject
	 */

	private Dialog mProgressDialog = null;

	// Activity methods
	//-------------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.toemail);
		setViews();
		
		// register action
//		TNAction.regResponder(TNActionType.ShareByEmail, this, "respondShareByEmail");

		findViewById(R.id.toemail_back).setOnClickListener(this);
		findViewById(R.id.toemail_send).setOnClickListener(this);
				
		mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
	}
	
	@Override
	protected void setViews() {
		TNUtilsSkin.setViewBackground(this, null, R.id.toemail_toolbar, R.drawable.toolbg);
		TNUtilsSkin.setImageViewDrawable(this, null, R.id.toemail_send_divide, R.drawable.divide);
		TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null, R.id.toemail_send, R.drawable.savelog);
	}

	@Override
	public void onDestroy(){
		mProgressDialog.dismiss();
		super.onDestroy();
	}
		
	protected void configView(){
		if( createStatus == 0){
			((EditText)findViewById(R.id.toemail_subject)).setText(
					getIntent().getStringExtra("Subject"));
		}
	}
	
	// Implement OnClickListener
	//-------------------------------------------------------------------------------
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.toemail_back:
			finish();
			break;
			
		case R.id.toemail_send:
			send();
			break;
		}
	}
	
	// Action respond methods
	//-------------------------------------------------------------------------------
	public void respondShareByEmail(TNAction aAction){
		if(!this.isFinishing())
			mProgressDialog.hide();
		if( isInFront ){
			if(!TNHandleError.handleResult(this, aAction)){
				TNUtilsUi.showToast(getString(R.string.alert_Share_EmailSent));
				finish();
			}
		}
	}

	// Private methods
	//-------------------------------------------------------------------------------
	private void send(){
		if( !TNUtils.checkNetwork(this))
			return;
		
		String emailStr = ((EditText)findViewById(R.id.toemail_to))
				.getText().toString().trim();
		Vector<String> goodEmail = TNUtilsTag.splitEmailStr(emailStr);
		if( goodEmail.size() <= 0){
			TNUtilsUi.showToast(getString(R.string.alert_Share_BadToAddr));
			return;
		}
		
		long noteId = getIntent().getLongExtra("NoteId", 0);
		String fromEmail = getIntent().getStringExtra("From");
		String subjectStr = ((EditText)findViewById(R.id.toemail_subject))
				.getText().toString().trim();
		String additionStr = ((EditText)findViewById(R.id.toemail_addition))
				.getText().toString().trim();
//		TNAction.runActionAsync(TNActionType.ShareByEmail, 
//				noteId, fromEmail,
//				goodEmail, subjectStr, additionStr);
		mProgressDialog.show();
	}
	
}
