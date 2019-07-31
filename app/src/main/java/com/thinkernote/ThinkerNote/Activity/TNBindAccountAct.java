package com.thinkernote.ThinkerNote.Activity;

import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.DBHelper.UserDbHelper;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote._constructer.p.BindAccountPresenter;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnBindAccountListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;

import org.json.JSONObject;

/**
 * 使用第三方登录，需要绑定手机号 sjy 06-11
 */
public class TNBindAccountAct extends TNActBase implements OnClickListener, OnBindAccountListener {

    private EditText mAccountView, mVerifyView;
    private TextView mTitleView;
    private String mPhone;
    private String vCode;
    private Button mVerifyBtn;
    private TimeCount mTime;
    private Dialog mProgressDialog = null;
    private Bundle mBundle;


    //
    private BindAccountPresenter presener;
    private LoginBean loginBean;
    private ProfileBean profileBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_account);
        mBundle = getIntent().getExtras();

        setViews();

        initView();

        presener = new BindAccountPresenter(this, this);
    }

    private void initView() {
        mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);

        mTitleView = (TextView) findViewById(R.id.bind_back);
        mAccountView = (EditText) findViewById(R.id.bind_account);
        findViewById(R.id.bind_account_confirm).setOnClickListener(this);
        mVerifyView = (EditText) findViewById(R.id.bind_verticode);
        mVerifyBtn = (Button) findViewById(R.id.get_verify_code);
        mVerifyBtn.setOnClickListener(this);
        mTitleView.setOnClickListener(this);
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
            case R.id.get_verify_code: {//获取验证码
                if (!TNUtils.checkNetwork(this))
                    break;

                mPhone = mAccountView.getText().toString().trim();
                if (TextUtils.isEmpty(mPhone)) {
                    TNUtilsUi.showToast("手机号不能为空");
                    break;
                }
                if (mPhone.length() != 11) {
                    TNUtilsUi.showToast("手机号格式不正确");
                    break;
                }
                //
                pVerifyCode(mPhone, "bind");
            }
            break;
            case R.id.bind_account_confirm: {//绑定
                if (!TNUtils.checkNetwork(this))
                    break;

                if (check()) {
//                    mProgressDialog.show();
                    submitBind();
                }
            }
            break;
            case R.id.bind_back: {//返回
                View view = getWindow().peekDecorView();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                finish();
            }
            break;

            default:
                break;
        }

    }

    //-----------------------------------私有方法--------------------------------------------

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

    private boolean check() {
        mPhone = mAccountView.getText().toString().trim();
        if (TextUtils.isEmpty(mPhone)) {
            TNUtilsUi.showToast("手机号不能为空");
            return false;
        }
        vCode = mVerifyView.getText().toString().trim();
        if (TextUtils.isEmpty(vCode)) {
            TNUtilsUi.showToast("验证码不能为空");
            return false;
        }
        return true;
    }


    //-----------------------------------p层调用接口--------------------------------------------

    //验证码
    private void pVerifyCode(String mPhone, String t) {
        presener.pPostVerifyCode(mPhone, t);
    }

    //绑定按钮
    private void submitBind() {
        presener.pBind(mBundle.getInt("bType")
                , mBundle.getString("bid")
                , mBundle.getString("name")
                , mBundle.getString("accessToken")
                , mBundle.getString("refreshToken")
                , mBundle.getLong("stamp")
                , mPhone
                , vCode
                , "bindAccount");
    }

    //绑定手机后自动登录
    private void autoLogin() {
        presener.pAutoLogAfterBind(mBundle.getInt("bType")
                , mBundle.getString("bid")
                , mBundle.getString("name")
                , mBundle.getString("accessToken")
                , mBundle.getString("refreshToken")
                , mBundle.getLong("stamp")
                , "bindAccount");

    }

    //更新登录信息
    private void updateProfile() {
        presener.pProfile();
    }
    //-----------------------------------接口返回的回调--------------------------------------------

    @Override
    public void onVerifyCodeSuccess(Object obj) {
        mTime = new TimeCount(60000, 1000);
        mTime.start();
        TNUtilsUi.showToast("短信已发送！");
    }

    @Override
    public void onVerifyCodeFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onBindSuccess(Object obj) {
        TNSettings settings = TNSettings.getInstance();
        settings.firstLaunch = true;
        settings.savePref(false);

        autoLogin();
    }

    @Override
    public void onBindFailed(String msg, Exception e) {
        TNUtilsUi.alert(this, msg);
    }

    @Override
    public void onAutoLogSuccess(Object obj) {
        loginBean = (LoginBean) obj;

        TNSettings settings = TNSettings.getInstance();
        settings.isLogout = false;

        //
        settings.password = mBundle.getString("password");
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
    public void onAutoLogFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onLogProfileSuccess(Object obj) {
        profileBean = (ProfileBean) obj;
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
        //
        settings.savePref(false);
        startToMain(TNMainAct.class);
        finish();
    }

    @Override
    public void onLogProfileFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }

}
