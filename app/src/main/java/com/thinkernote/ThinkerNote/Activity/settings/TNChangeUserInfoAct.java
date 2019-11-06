package com.thinkernote.ThinkerNote.Activity.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.db.UserDbHelper;
import com.thinkernote.ThinkerNote.db.Database.TNDb;
import com.thinkernote.ThinkerNote.db.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.db.Database.TNSQLString;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.TNUtils;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.utils.MLog;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnChangeUserInfoListener;
import com.thinkernote.ThinkerNote.mvp.p.ChangeUserInfoPresenter;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 主页--设置--用户信息--具体修改界面（密码 用户名 邮箱等）
 * sjy 0626
 */
public class TNChangeUserInfoAct extends TNActBase implements OnClickListener, OnChangeUserInfoListener {
    private static final int NEXT_PROFILE = 101;

    private TextView mTitleView;
    private String mType;
    private EditText mUserNameView, mUserNamePswView, mNewPswView, mNewPswAgainView, mOldPswView, mEmailView, mEmailPswView;
    private Dialog mProgressDialog = null;

    //p
    ChangeUserInfoPresenter presener;
    ProfileBean profileBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_change_userinfo);
        //p
        presener = new ChangeUserInfoPresenter(this, this);
        initView();
    }

    private void initView() {
        mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);

        mTitleView = (TextView) findViewById(R.id.change_userinfo_back);
        mUserNameView = (EditText) findViewById(R.id.change_username);
        mUserNamePswView = (EditText) findViewById(R.id.old_password_username);
        mNewPswView = (EditText) findViewById(R.id.new_password);
        mNewPswAgainView = (EditText) findViewById(R.id.new_password_again);
        mOldPswView = (EditText) findViewById(R.id.old_password);
        mEmailView = (EditText) findViewById(R.id.change_email);
        mEmailPswView = (EditText) findViewById(R.id.old_password_email);

        mTitleView.setOnClickListener(this);
        findViewById(R.id.change_confirm).setOnClickListener(this);

        mType = getIntent().getStringExtra("type");
        if ("username".equals(mType)) {
            mTitleView.setText("修改用户名");
            findViewById(R.id.ll_username).setVisibility(View.VISIBLE);
        } else if ("password".equals(mType)) {
            mTitleView.setText("修改密码");
            findViewById(R.id.ll_password).setVisibility(View.VISIBLE);
        } else {
            mTitleView.setText("修改邮箱");
            findViewById(R.id.ll_email).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void configView() {
        super.configView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_userinfo_back:
                finish();
                break;
            case R.id.change_confirm:
                change();
                break;

            default:
                break;
        }
    }

    private void change() {
        if ("username".equals(mType)) {
            String newUserName = mUserNameView.getText()
                    .toString().trim();
            if (newUserName.length() == 0) {
                TNUtilsUi.showToast(R.string.alert_UserInfo_NewUserNameBlank);
                return;
            }

            if (!TNUtils.checkRegex(TNUtils.USERNAME_REGEX, newUserName)) {
                TNUtilsUi.showToast(R.string.alert_Reg_UsernameWrong);
                return;
            }

            String userPwd = mUserNamePswView.getText().toString();
            if (userPwd.length() == 0) {
                TNUtilsUi.showToast(R.string.alert_UserInfo_UserPwdBlank);
                return;
            }
            mProgressDialog.show();
            //p
            changeUserNameOrEmail(newUserName, "userName", userPwd);

        } else if ("password".equals(mType)) {
            String newPwd = mNewPswView.getText().toString().trim();
            if (newPwd.length() == 0) {
                TNUtilsUi.showToast(R.string.alert_UserInfo_NewPwdBlank);
                return;
            }
            if (newPwd.length() > 20) {
                TNUtilsUi.showToast(R.string.alert_UserInfo_Password_Long);
                return;
            }
            String confirmPwd = mNewPswAgainView.getText()
                    .toString().trim();
            if (!confirmPwd.equals(newPwd)) {
                TNUtilsUi.showToast(R.string.alert_UserInfo_ConfirmPwdUnmatch);
                return;
            }

            String oldPwd = mOldPswView.getText()
                    .toString().trim();
            if (oldPwd.length() == 0) {
                TNUtilsUi.showToast(R.string.alert_UserInfo_OldPwdBlank);
                return;
            }
//			if (!TNSettings.getInstance().password.equals(oldPwd)){
//            TNUtilsUi.showToast(R.string.alert_UserInfo_UserPwdWrong);
//				return;
//			}
            // try modify password
            mProgressDialog.show();
            //
            changePs(oldPwd, newPwd);

        } else {
            String newEmail = mEmailView.getText().toString().trim();
            if (newEmail.length() == 0) {
                TNUtilsUi.showToast(R.string.alert_UserInfo_NewEmailBlank);
                return;
            }
            if (!TNUtils.checkRegex(TNUtils.FULL_EMAIL_REGEX, newEmail)) {
                TNUtilsUi.showToast(R.string.alert_UserInfo_EmailWrong);
                return;
            }

            String userPwd = mEmailPswView.getText().toString().trim();
            if (userPwd.length() == 0) {
                TNUtilsUi.showToast(R.string.alert_UserInfo_UserPwdBlank);
                return;
            }
            mProgressDialog.show();
            //p
            changeUserNameOrEmail(newEmail, "email", userPwd);

        }
    }

    //============================================handler调用====================================================

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case NEXT_PROFILE:
                //更新用户信息
                getUserInfo();
                break;
        }
    }


    //============================================p层调用====================================================

    private void changePs(String oldPs, String newPs) {
        presener.pChangePs(oldPs, newPs);
    }

    private void changeUserNameOrEmail(String nameOrEmail, String type, String userPs) {
        presener.pChangeNameOrEmail(nameOrEmail, type, userPs);

    }

    private void getUserInfo() {
        presener.pProfile();
    }

    //============================================接口结果返回====================================================

    @Override
    public void onChangePsSuccess(Object obj, final String newPs) {

        TNUtilsUi.showShortToast("密码修改成功");

        //
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                TNDb.beginTransaction();
                try {
                    //
                    TNSettings settings = TNSettings.getInstance();
                    TNDb.getInstance().execSQL(TNSQLString.USER_UPDATE_PWD, newPs, settings.userId);
                    TNDb.setTransactionSuccessful();
                    settings.password = newPs;
                    settings.savePref(false);
                } finally {
                    TNDb.endTransaction();
                }
                handler.sendEmptyMessage(NEXT_PROFILE);
            }

        });

    }

    @Override
    public void onChangePsFailed(String msg, Exception e) {
        mProgressDialog.hide();
    }

    @Override
    public void onChangeNameOrEmailSuccess(Object obj, final String nameOrEmail, final String type) {
        TNUtilsUi.showShortToast("修改成功");
        final ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                TNSettings settings = TNSettings.getInstance();
                TNDb.beginTransaction();
                try {
                    if (type.equals("userName")) {
                        settings.username = nameOrEmail;
                        settings.loginname = nameOrEmail;
                        String userId = settings.userId + "";
                        TNDb.getInstance().execSQL(TNSQLString.USER_UPDATE_NAME, nameOrEmail, userId);
                    } else {
                        settings.email = nameOrEmail;
                        String userId = settings.userId + "";
                        TNDb.getInstance().execSQL(TNSQLString.USER_UPDATE_EMAIL, nameOrEmail, userId);
                    }
                    //
                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }
                settings.savePref(false);

                //
                handler.sendEmptyMessage(NEXT_PROFILE);
            }

        });
    }

    @Override
    public void onChangeNameOrEmailFailed(String msg, Exception e) {
        mProgressDialog.hide();
        TNUtilsUi.showShortToast(msg);
    }

    @Override
    public void onProfileSuccess(Object obj) {
        mProgressDialog.hide();
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
        settings.savePref(false);

        finish();
    }

    @Override
    public void onProfileFailed(String msg, Exception e) {
        mProgressDialog.hide();
        MLog.e(msg);
    }
}
