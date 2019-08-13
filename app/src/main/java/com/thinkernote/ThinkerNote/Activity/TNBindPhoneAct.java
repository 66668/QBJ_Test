package com.thinkernote.ThinkerNote.Activity;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.DBHelper.UserDbHelper;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.mvp.p.BindPhonePresenter;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnBindPhoneListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.login.VerifyPicBean;

import org.json.JSONObject;

/**
 * 主界面-绑定新手机号/设置-修改手机号
 * 说明：登录进入主界面后，仍未绑定手机号，则再次绑定
 * sjy 0625
 */
public class TNBindPhoneAct extends TNActBase implements OnClickListener,OnBindPhoneListener {
	
	private EditText mPhoneView, mVerifyView, mPswView, mCodeView;
	private ImageView mCodeImageView;
	private TextView mTitleView;
	private String mPhone, mVCode, mPassword;
	private Button mVerifyBtn;
	private TimeCount mTime;
	private String mType;
	private Dialog mProgressDialog = null;
	private String mNonce;
	private String mHashKey;
	private String mAnswer;

	//
	private BindPhonePresenter presener;
	private VerifyPicBean verifyPicBean;//验证码数据

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bind_phone);
		setViews();	
		
		initView();
		presener = new BindPhonePresenter(this, this);

		getVerifyPic();

	}
	
	private void initView() {
		mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
		
		mTitleView = (TextView) findViewById(R.id.bind_back);
		mPhoneView = (EditText) findViewById(R.id.bind_phone);
		mVerifyView = (EditText) findViewById(R.id.bind_verticode);
		mPswView = (EditText) findViewById(R.id.bind_password);
		mCodeView = (EditText) findViewById(R.id.image_code_edit);
		mCodeImageView = (ImageView) findViewById(R.id.image_code);
		mVerifyBtn = (Button) findViewById(R.id.get_verify_code);
		mVerifyBtn.setOnClickListener(this);
		mCodeImageView.setOnClickListener(this);
		findViewById(R.id.bind_confirm).setOnClickListener(this);
		mTitleView.setOnClickListener(this);
		
		mType = getIntent().getStringExtra("type");
		if ("change".equals(mType)) {
			mTitleView.setText("修改手机号");
		}
		
	}
	
	@Override
	protected void setViews() {
		TNUtilsSkin.setViewBackground(this, null, R.id.bind_toolbg_framelayout, R.drawable.toolbg);
	}
	
	@Override
	protected void configView() {
		super.configView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.get_verify_code:{//验证码
			if( !TNUtils.checkNetwork(this))
				break;
			
			mPhone = mPhoneView.getText().toString().trim();
			if (TextUtils.isEmpty(mPhone)) {
				TNUtilsUi.showToast("手机号不能为空");
				break;
			}
			if (mPhone.length() != 11) {
				TNUtilsUi.showToast("手机号格式不正确");
				break;
			}
			mAnswer = mCodeView.getText().toString().trim();
			if (TextUtils.isEmpty(mAnswer)) {
				TNUtilsUi.showToast("图形验证码不能为空");
				break;
			}
			//p
			getVcode();


			}
			break;
		case R.id.bind_confirm:{
			if( !TNUtils.checkNetwork(this))
				break;
			
			if (check()) {
				mProgressDialog.show();
				submit();
			}}
			break;
		case R.id.bind_back:{
			View view = getWindow().peekDecorView();
			if (view != null) {
				InputMethodManager inputManager = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
			finish();
			}
			break;
		case R.id.image_code://更换图片验证码
			getVerifyPic();
			break;

		default:
			break;
		}
		
	}

	private boolean check() {
		mPhone = mPhoneView.getText().toString().trim();
		mVCode = mVerifyView.getText().toString().trim();
		mPassword = mPswView.getText().toString().trim();
		mAnswer = mCodeView.getText().toString().trim();
		if (TextUtils.isEmpty(mPhone)){
			TNUtilsUi.showToast("手机号码不能为空");
			return false;
		}
		if (TextUtils.isEmpty(mAnswer)) {
			TNUtilsUi.showToast("图形验证码不能为空");
			return false;
		}
		if (TextUtils.isEmpty(mVCode)){
			TNUtilsUi.showToast("验证码不能为空");
			return false;
		}
		if (TextUtils.isEmpty(mPassword)){
			TNUtilsUi.showToast("用户密码不能为空");
			return false;
		}
		return true;
	}

	class TimeCount extends CountDownTimer {
		
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		
		@Override
		public void onFinish() {// 计时完毕
			mVerifyBtn.setText("获取验证码");
			mVerifyBtn.setClickable(true);
		}
		
		@Override
		public void onTick(long millisUntilFinished) {// 计时过程
			mVerifyBtn.setClickable(false);//防止重复点击
			mVerifyBtn.setText(millisUntilFinished / 1000 + "s后重新获取");
		}
	}

	//--------------------------------p层调用--------------------------------------

	private void getVcode() {
		presener.pVcode(mPhone,"change",mAnswer,mNonce,mHashKey);

	}

	private void getVerifyPic() {
		presener.pVerifyPic();
	}

	private void submit() {
		presener.pSubmit(mPhone,mVCode,mPassword);
	}

	private void pProfile() {
		presener.pRrofile();
	}


	//--------------------------------接口回调--------------------------------------
	@Override
	public void onVerifyPicSuccess(Object obj) {
		verifyPicBean = (VerifyPicBean) obj;
		mNonce = verifyPicBean.getNonce();
		mHashKey = verifyPicBean.getHashkey();

		String bitmapStr = verifyPicBean.getCaptcha();
		Bitmap bitmap = TNUtils.stringToBitmap(bitmapStr);
		mCodeImageView.setImageBitmap(TNUtils.changeBitmapSize(bitmap, 1.5));
	}

	@Override
	public void onVerifyPicFailed(String msg, Exception e) {
		//最好用别的图替换
		TNUtilsUi.showToast(msg);
	}

	@Override
	public void onVcodeSuccess(Object obj) {
		CommonBean bean = (CommonBean) obj;
		String msg = bean.getMessage();
		TNUtilsUi.showToast(msg);

		mTime = new TimeCount(60000, 1000);
		mTime.start();
	}

	@Override
	public void onVcodeFailed(String msg, Exception e) {
		TNUtilsUi.showToast(msg);
	}

	@Override
	public void onBindSuccess(Object obj,String phone) {

		TNSettings settings = TNSettings.getInstance();

		settings.loginname = mPhone;
		settings.phone = phone;
		settings.savePref(false);
		TNDb.getInstance().execSQL(TNSQLString.USER_UPDATE_PHONE, phone, settings.userId);
		TNUtilsUi.showToast("修改成功！");
		//
		mProgressDialog.show();
		pProfile();
	}

	@Override
	public void onBindFailed(String msg, Exception e) {
		mProgressDialog.hide();
	}

	@Override
	public void onProfileSuccess(Object obj) {
		mProgressDialog.hide();
		ProfileBean profileBean = (ProfileBean) obj;
		//
		TNSettings settings = TNSettings.getInstance();
		long userId = TNDbUtils.getUserId(settings.username);

		settings.phone = profileBean.getPhone();
		settings.email = profileBean.getEmail();
		settings.defaultCatId = profileBean.getDefault_folder();

		if (userId != settings.userId) {
			//清空user表
			UserDbHelper.clearUsers();
		}

		JSONObject user = TNUtils.makeJSON(
				"username", settings.username,
				"password", settings.password,
				"userEmail", settings.email,
				"phone", settings.phone,
				"userId", settings.userId,
				"emailVerify", profileBean.getEmailverify(),
				"totalSpace", profileBean.getTotal_space(),
				"usedSpace", profileBean.getUsed_space());

		//更新user表
		UserDbHelper.addOrUpdateUser(user);

		//
		settings.isLogout = false;
		settings.firstLaunch = false;//在此处设置 false
		settings.savePref(false);
		if (!"change".equals(mType)) {
			Bundle b = new Bundle();
			b.putInt("FLAG", 1);
			startToMain(TNMainAct.class, b);
		}
		finish();
	}

	@Override
	public void onProfileFailed(String msg, Exception e) {
		mProgressDialog.hide();

		TNUtilsUi.showToast(msg);
	}

}
