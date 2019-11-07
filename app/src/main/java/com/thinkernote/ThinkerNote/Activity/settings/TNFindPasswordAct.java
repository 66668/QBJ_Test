package com.thinkernote.ThinkerNote.Activity.settings;

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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.Activity.TNMainAct;
import com.thinkernote.ThinkerNote.db.UserDbHelper;
import com.thinkernote.ThinkerNote.db.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.TNUtils;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.mvp.p.FindPsPresenter;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnFindPsListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.login.VerifyPicBean;

import org.json.JSONObject;

/**
 * 忘记密码 sjy 0612
 */
public class TNFindPasswordAct extends TNActBase implements OnClickListener, OnCheckedChangeListener, OnFindPsListener {

    private EditText mPhoneView, mVerifyView, mPswView, mAgainPswView, mEmailView, mCodeView;
    private ImageView mCodeImageView;
    private String mPhone, mPsw, mVCode, mPsw2, mEmail;
    private RadioGroup mGroup;
    private RadioButton mRadioButton1, mRadioButton2;
    private Button mVerifyBtn;
    private TextView mForgetInfo;
    private TimeCount mTime;
    private int mType = 1;//1表示通过手机找回，2表示通过邮箱找回
    private ProgressDialog mProgressDialog;
    private String mNonce;
    private String mHashKey;
    private String mAnswer;
    private View mDividerImageCodeView;
    private LinearLayout mImageCodeView;

    //
    private FindPsPresenter presener;
    private VerifyPicBean verifyPicBean;//验证码数据
    private LoginBean loginBean;
    private ProfileBean profileBean;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_find_psw);
        mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);

        initView();

        // p
        presener = new FindPsPresenter( this);

        //图片
        getVerifyPic();
    }

    private void initView() {
        mForgetInfo = (TextView) findViewById(R.id.forget_info);
        mPhoneView = (EditText) findViewById(R.id.findpsw_phone);
        mEmailView = (EditText) findViewById(R.id.findpsw_email);
        mVerifyView = (EditText) findViewById(R.id.findpsw_verticode);
        mPswView = (EditText) findViewById(R.id.find_new_psw);
        mAgainPswView = (EditText) findViewById(R.id.find_new_psw_again);
        mCodeView = (EditText) findViewById(R.id.image_code_edit);
        mCodeImageView = (ImageView) findViewById(R.id.image_code);
        mDividerImageCodeView = findViewById(R.id.divider_image_code);
        mImageCodeView = (LinearLayout) findViewById(R.id.ll_image_code);
        mVerifyBtn = (Button) findViewById(R.id.get_verify_code);
        mVerifyBtn.setOnClickListener(this);
        mCodeImageView.setOnClickListener(this);
        findViewById(R.id.find_phone_confirm).setOnClickListener(this);
        findViewById(R.id.findpsw_back).setOnClickListener(this);
        String str = "如有找不回密码问题，请加<font color='#309AF2'>QQ1657975956</font>联系我们";
        mForgetInfo.setText(Html.fromHtml(str));

        mGroup = (RadioGroup) findViewById(R.id.find_group);
        mGroup.setOnCheckedChangeListener(this);

        mRadioButton1 = (RadioButton) findViewById(R.id.find_phone_page);
        mRadioButton2 = (RadioButton) findViewById(R.id.find_email_page);

    }

    @Override
    protected void configView() {
        super.configView();
    }

    @Override
    public void onCheckedChanged(RadioGroup arg0, int id) {
        if (id == R.id.find_phone_page) {
            if (mType != 1) {
                if (mTime != null)
                    mTime.cancel();
                mVerifyBtn.setText("获取验证码");
                mVerifyBtn.setClickable(true);
                mVerifyView.setText("");
            }
            mType = 1;
            mRadioButton1.setChecked(true);
            mPhoneView.setVisibility(View.VISIBLE);
            mEmailView.setVisibility(View.GONE);
            mImageCodeView.setVisibility(View.VISIBLE);
            mDividerImageCodeView.setVisibility(View.VISIBLE);
        } else {
            if (mType != 2) {
                if (mTime != null)
                    mTime.cancel();
                mVerifyBtn.setText("获取验证码");
                mVerifyBtn.setClickable(true);
                mVerifyView.setText("");
            }
            mType = 2;
            mRadioButton2.setChecked(true);
            mPhoneView.setVisibility(View.GONE);
            mEmailView.setVisibility(View.VISIBLE);
            mImageCodeView.setVisibility(View.GONE);
            mDividerImageCodeView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_verify_code: {//验证码
                if (!TNUtils.checkNetwork(this))
                    break;

                if (mType == 2) {
                    if (!TNUtils.checkNetwork(this))
                        break;

                    mEmail = mEmailView.getText().toString().trim();
                    if (mEmail.length() == 0) {
                        TNUtilsUi.showToast("邮箱不能为空");
                        break;
                    }
                    if (!TNUtils.checkRegex(TNUtils.FULL_EMAIL_REGEX, mEmail)) {
                        TNUtilsUi.showToast(TNUtils.getAppContext().getResources().getString(R.string.alert_UserInfo_EmailWrong));
                        break;
                    }

                    // 邮箱验证
                    getEmailVCode(mEmail, "forgot");

                } else {
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

                    //手机验证
                    getPhoneVCode(mPhone, "forgot", mAnswer, mNonce, mHashKey);
                }
            }
            break;

            case R.id.find_phone_confirm: {//确定
                if (!TNUtils.checkNetwork(this))
                    break;

                if (check()) {
                    submit();
                }
            }
            break;
            case R.id.findpsw_back: {//返回
                View view = getWindow().peekDecorView();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                finish();
            }
            break;
            case R.id.image_code://切换图片
                getVerifyPic();
                break;

            default:
                break;
        }

    }


    private boolean check() {
        mPsw = mPswView.getText().toString().trim();
        mPsw2 = mAgainPswView.getText().toString().trim();
        mVCode = mVerifyView.getText().toString().trim();
        if (mType == 1) {
            mAnswer = mCodeView.getText().toString().trim();
            if (TextUtils.isEmpty(mAnswer)) {
                TNUtilsUi.showToast("图形验证码不能为空");
                return false;
            }
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
        verifyPicBean = null;
        mCodeImageView.setImageBitmap(null);//清空数据
        presener.getVerifyPic();
    }

    private void getPhoneVCode(String mPhone, String name, String mAnswer, String mNonce, String mHashKey) {
        presener.phoneVerifyCode(mPhone, name, mAnswer, mNonce, mHashKey);
    }

    private void getEmailVCode(String mEmail, String name) {
        presener.mailVerifyCode(mEmail, name);
    }

    private void submit() {

        mProgressDialog.show();
        if (mType == 1) {
            presener.submit(mPhone, mPsw, mVCode);
        } else {
            presener.submit(mEmail, mPsw, mVCode);
        }
    }

    //找回密码 触发登录接口
    private void autoLogin() {
        if (mType == 1) {
            presener.autoLogin(mPhone, mPsw);
        } else {
            presener.autoLogin(mEmail, mPsw);
        }
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
        //最好用别的图替换
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
    public void onMailVCodeSuccess(Object obj) {

        CommonBean bean = (CommonBean) obj;
        String msg = bean.getMessage();
        TNUtilsUi.showToast(msg);

        mTime = new TimeCount(60000, 1000);
        mTime.start();
    }

    @Override
    public void onMailVCodetFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onSubmitSuccess(Object obj) {
        autoLogin();
    }

    @Override
    public void onSubmitFailed(String msg, Exception e) {
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
        //更新
        updateProfile();


    }

    @Override
    public void onAutoLoginFailed(String msg, Exception e) {
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

        //
        settings.loginname = mType == 1 ? mPhone : mEmail;
        settings.password = mPsw;
        settings.savePref(false);
        startActivity(TNMainAct.class);
        finish();

    }

    @Override
    public void onProfileFailed(String msg, Exception e) {
        mProgressDialog.hide();
        TNUtilsUi.showToast(msg);
    }


}
