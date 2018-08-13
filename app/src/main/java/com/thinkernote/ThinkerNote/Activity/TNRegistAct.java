package com.thinkernote.ThinkerNote.Activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.DBHelper.UserDbHelper;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsDialog;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote._constructer.presenter.RegistPresenterImpl;
import com.thinkernote.ThinkerNote._interface.p.IRegistPresenter;
import com.thinkernote.ThinkerNote._interface.v.OnRegistListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.login.VerifyPicBean;

import org.json.JSONObject;

/**
 * 登录 注册
 * sjy 0612
 */
public class TNRegistAct extends TNActBase implements OnClickListener, OnRegistListener {

    private EditText mPhoneView, mVerifyView, mPswView, mAgainPswView, mCodeView;
    private ImageView mCodeImageView;
    private String mPhone, mPsw, mVCode, mPsw2;
    private Button mVerifyBtn;
    private TextView mForgetInfo;
    private TimeCount mTime;
    private String mType;
    private ProgressDialog mProgressDialog;
    private int mUserType = 0;
    private String mNonce;
    private String mHashKey;
    private String mAnswer;

    //
    private IRegistPresenter presener;
    private VerifyPicBean verifyPicBean;
    private LoginBean loginBean;
    private ProfileBean profileBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regist);
        mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
        setViews();

        mType = getIntent().getExtras().getString("type");
        if ("register".equals(mType)) {
            ((TextView) findViewById(R.id.regist_back)).setText("注册");
        } else if ("third".equals(mType)) {
            findViewById(R.id.regist_bind_account).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.regist_back)).setText("注册");
        } else {
            ((TextView) findViewById(R.id.regist_back)).setText("忘记密码");
        }

        initView();

        presener = new RegistPresenterImpl(this, this);

        getVerifyPic();

    }

    private void initView() {
        mForgetInfo = (TextView) findViewById(R.id.forget_info);
        mPhoneView = (EditText) findViewById(R.id.regist_phone);
        mVerifyView = (EditText) findViewById(R.id.regist_verticode);
        mPswView = (EditText) findViewById(R.id.regist_psw);
        mCodeView = (EditText) findViewById(R.id.image_code_edit);
        mCodeImageView = (ImageView) findViewById(R.id.image_code);
        mAgainPswView = (EditText) findViewById(R.id.regist_psw_again);
        mVerifyBtn = (Button) findViewById(R.id.get_verify_code);
        mVerifyBtn.setOnClickListener(this);
        mCodeImageView.setOnClickListener(this);
        findViewById(R.id.regist_confirm).setOnClickListener(this);
        findViewById(R.id.regist_back).setOnClickListener(this);
        findViewById(R.id.regist_bind_account).setOnClickListener(this);

        if ("forgetPwd".equals(mType)) {
            mForgetInfo.setVisibility(View.VISIBLE);
            String str = "如有找不回密码问题，请加<font color='#309AF2'>QQ1657975956</font>联系我们";
            mForgetInfo.setText(Html.fromHtml(str));
        }
    }

    @Override
    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.regist_toolbg_framelayout, R.drawable.toolbg);
    }

    @Override
    protected void configView() {
        super.configView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_verify_code://验证码
                if (!TNUtils.checkNetwork(this))
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
                if ("register".equals(mType) || "third".equals(mType)) {
                    getVCode(mPhone, "register", mAnswer, mNonce, mHashKey);
                } else {
                    getVCode(mPhone, "forgot", mAnswer, mNonce, mHashKey);
                }
                break;
            case R.id.regist_confirm://提交
                if (!TNUtils.checkNetwork(this))
                    break;

                if (check()) {
                    submit();
                }
                break;
            case R.id.regist_back://返回
                View view = getWindow().peekDecorView();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                finish();
                break;
            case R.id.regist_bind_account://绑定原来手机号
                Bundle bundle = getIntent().getExtras();
                startActivity(TNBindAccountAct.class, bundle);
                break;

            case R.id.image_code://验证图
                getVerifyPic();
                break;

            default:
                break;
        }

    }

    private boolean check() {
        mPhone = mPhoneView.getText().toString().trim();
        mPsw = mPswView.getText().toString().trim();
        mPsw2 = mAgainPswView.getText().toString().trim();
        mVCode = mVerifyView.getText().toString().trim();
        mAnswer = mCodeView.getText().toString().trim();
        if (TextUtils.isEmpty(mPhone)) {
            TNUtilsUi.showToast("手机号码不能为空");
            return false;
        }
        if (TextUtils.isEmpty(mAnswer)) {
            TNUtilsUi.showToast("图形验证码不能为空");
            return false;
        }
        if (TextUtils.isEmpty(mVCode)) {
            TNUtilsUi.showToast("验证码不能为空");
            return false;
        }
        if (TextUtils.isEmpty(mPsw)) {
            TNUtilsUi.showToast("密码不能为空");
            return false;
        }
        if (TextUtils.isEmpty(mPsw2)) {
            TNUtilsUi.showToast("确认密码不能为空");
            return false;
        }
        if (!mPsw.equals(mPsw2)) {
            TNUtilsUi.showToast("两次密码不一样");
            return false;
        }
        return true;
    }

    //----------------------------------------------

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

    //-------------------------------------p层调用------------------------------------------

    //图片
    private void getVerifyPic() {
        //清空数据
        verifyPicBean = null;
        mCodeImageView.setImageBitmap(null);
        //
        presener.getVerifyPic();
    }

    //验证码
    private void getVCode(String phone, String type, String mAnswer, String mNonce, String mHashKey) {
        presener.phoneVerifyCode(phone, type, mAnswer, mNonce, mHashKey);
    }

    //提交
    private void submit() {
        mProgressDialog.show();
        if ("register".equals(mType) || "third".equals(mType)) {
            presener.submitRegister(mPhone, mPsw, mVCode);
        } else {
            presener.submitForgotPassword(mPhone, mPsw, mVCode);
        }
    }

    private void autoLogin() {
        presener.autoLogin(mPhone, mPsw);
    }

    //第三方登录，需要绑定手机
    private void bindPhoneToLogin() {
        Bundle b = getIntent().getExtras();
        mUserType = b.getInt("bType");

        //
        presener.bindPhone(mUserType, b.getString("bid"), b.getString("name"), b.getString("accessToken"), b.getString("refreshToken"), b.getLong("stamp"), mPhone, mVCode);
    }

    private void updateProfile() {
        presener.pProfile();
    }


    //-------------------------------------接口回调------------------------------------------

    @Override
    public void onPicSuccess(Object obj) {
        verifyPicBean = (VerifyPicBean) obj;
        mNonce = verifyPicBean.getNonce();
        mHashKey = verifyPicBean.getHashkey();

        String bitmapStr = verifyPicBean.getCaptcha();
        Bitmap bitmap = TNUtils.stringToBitmap(bitmapStr);
        mCodeImageView.setImageBitmap(TNUtils.changeBitmapSize(bitmap, 1.5));
    }

    @Override
    public void onPicFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onPhoneVCodeSuccess(Object obj) {
        CommonBean bean = (CommonBean) obj;
        String msg = bean.getMessage();
        TNUtilsUi.showToast(msg);

        mTime = new TimeCount(60000, 1000);
        mTime.start();
    }

    @Override
    public void onPhoneVCodeFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onSubmitRegistSuccess(Object obj) {
        TNSettings settings = TNSettings.getInstance();
        settings.firstLaunch = true;
        settings.savePref(false);

        if ("third".equals(mType)) {
            //绑定
            bindPhoneToLogin();
        } else {
            autoLogin();
        }
    }

    @Override
    public void onSubmitRegistFailed(String msg, Exception e) {
        mProgressDialog.hide();
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onSubmitFindPsSuccess(Object obj) {
        if ("third".equals(mType)) {
            //绑定
            bindPhoneToLogin();
        } else {
            autoLogin();
        }
    }

    @Override
    public void onSubmitFindPsFailed(String msg, Exception e) {
        mProgressDialog.hide();
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onAutoLoginSuccess(Object obj) {
        loginBean = (LoginBean) obj;
        TNSettings settings = TNSettings.getInstance();
        settings.isLogout = false;

        //
        settings.password = mPsw;
        settings.userId = loginBean.getUser_id();
        settings.username = loginBean.getUsername();
        settings.token = loginBean.getToken();
        settings.expertTime = loginBean.getExpire_at();
        if (TextUtils.isEmpty(settings.loginname)) {
            settings.loginname = loginBean.getUsername();
        }
        settings.savePref(false);
        //更新接口
        updateProfile();


    }

    @Override
    public void onAutoLoginFailed(String msg, Exception e) {
        mProgressDialog.hide();
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onBindPhoneSuccess(Object obj) {
        //登录接口
        autoLogin();
    }

    @Override
    public void onBindPhoneFailed(String msg, Exception e) {
        mProgressDialog.hide();
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onProfileSuccess(Object obj) {
        profileBean = (ProfileBean) obj;
        mProgressDialog.hide();

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
        settings.savePref(false);


        settings.loginname = mPhone;
        settings.password = mPsw;
        settings.userType = mUserType;
        settings.savePref(false);
        startToMain(TNMainAct.class);
        finish();
    }

    @Override
    public void onProfileFailed(String msg, Exception e) {
        mProgressDialog.hide();
        TNUtilsUi.showToast(msg);
    }


}
