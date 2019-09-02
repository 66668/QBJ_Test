package com.thinkernote.ThinkerNote.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.thinkernote.ThinkerNote.DBHelper.UserDbHelper;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.Utils.SPUtil;
import com.thinkernote.ThinkerNote.Utils.TNActivityManager;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.login.QQBean;
import com.thinkernote.ThinkerNote.mvp.http.rxbus.RxBus;
import com.thinkernote.ThinkerNote.mvp.http.rxbus.RxBusBaseMessage;
import com.thinkernote.ThinkerNote.mvp.http.rxbus.RxCodeConstants;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnLogListener;
import com.thinkernote.ThinkerNote.mvp.p.LogPresenter;
import com.thinkernote.ThinkerNote.other.TNLinearLayout;
import com.thinkernote.ThinkerNote.other.TNLinearLayout.TNLinearLayoutListener;

import org.json.JSONObject;

import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * 登陆 界面
 * sjy 0607
 */
public class TNLoginAct extends TNActBase implements OnClickListener, OnLogListener {

    // Class members
    //-------------------------------------------------------------------------------
    private String mUserName, mPassword;
    private Dialog mLoginingDialog = null;
    private AlphaAnimation mAlphaAnimation;
    private static IWXAPI WXapi;

    private Boolean isClickQQ = false;

    //=========sina相关==============
    /**
     * 显示认证后的信息，如 AccessToken
     */
    private TextView mTokenText;
    /**
     * 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能
     */
    private Oauth2AccessToken mAccessToken;
    /**
     * 注意：SsoHandler 仅当 SDK 支持 SSO 时有效
     */
    private SsoHandler mSsoHandler;
    //=======================

    private Tencent mTencent;//qq
    private String mLoginId;

    //p层相关
    private LogPresenter logPresener;
    private LoginBean loginBean;
    private ProfileBean profileBean;//登录更新

    // Activity methodsƒ
    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        TNActivityManager.getInstance().finishOtherActivity(this);
        initView();
        //初始化 p
        logPresener = new LogPresenter(this, this);
        // 设置主界面不调用锁屏
        TNSettings.getInstance().needShowLock2 = false;
    }

    private void initView() {
        // 根据屏幕大小改变布局
        ((TNLinearLayout) findViewById(R.id.login_layout)).setListener(new TNLinearLayoutListener() {
            @Override
            public void onTNLinearLayoutMeasure(int widthMeasureSpec, int heightMeasureSpec) {

            }
        });

        // initialize event
        findViewById(R.id.login_trial_btn).setOnClickListener(this);
        findViewById(R.id.login_login).setOnClickListener(this);
        findViewById(R.id.login_forget_password).setOnClickListener(this);
        findViewById(R.id.login_weixin).setOnClickListener(this);
        findViewById(R.id.login_qq).setOnClickListener(this);
        findViewById(R.id.login_sina).setOnClickListener(this);

        //加载动画
        mLoginingDialog = TNUtilsUi.progressDialog(TNLoginAct.this, R.string.logging_in);
        mAlphaAnimation = new AlphaAnimation(0, 1);
        mAlphaAnimation.setDuration(1500);

        initRxBus();
    }

    /**
     * 该处是onResume的重写
     */
    protected void configView() {
        if (createStatus == 0) {
            TNSettings settings = TNSettings.getInstance();
            EditText name = (EditText) findViewById(R.id.login_username);
            if (settings.syncOldDb) {
                if (settings.loginname.length() > 0) {
                    name.setText(settings.loginname);
                    name.setSelection(settings.loginname.length());
                }
            } else {
                if (settings.username.length() > 0) {
                    name.setText(settings.username);
                    name.setSelection(settings.username.length());
                } else if (settings.userId != 0) {
                    String userName = UserDbHelper.getOldDbUserName(settings.userId);
                    name.setText(userName);
                    name.setSelection(userName.length());
                }
            }
        } else {
            if (!isClickQQ)
                mLoginingDialog.hide();//获取第三方信息后直接点返回
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        MLog.e("onResume-->getWechatIntent");

    }

    @Override
    public void onDestroy() {
        mLoginingDialog.dismiss();
        super.onDestroy();
        //去除Rxbus的监听
        if (null != compositeDisposable && !compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_login://正常登录
                login();
                break;
            case R.id.login_qq://qq登录
//			TNUtilsUi.alert(this, "即将上线，\n有问题请联系QQ客服:1657975956");
                mLoginingDialog.show();
                isClickQQ = true;
                loginQQ();
                break;
            case R.id.login_sina://新浪登录
                mLoginingDialog.show();
                loginSina();
                break;
            case R.id.login_weixin://微信登录
                mLoginingDialog.show();
                loginWechat();

                break;
            case R.id.login_forget_password://忘记密码
                startActivity(TNFindPasswordAct.class);
                break;

            case R.id.login_trial_btn: {//注册

                Bundle b = new Bundle();
                b.putString("type", "register");
                startActivity(TNRegistAct.class, b);
                break;
            }


        }
    }
    //-------------------------------------正常登录------------------------------------------

    private void login() {
        mUserName = ((EditText) findViewById(R.id.login_username))
                .getText().toString().trim().toLowerCase();
        if (mUserName.length() == 0) {
            TNUtilsUi.alert(this, R.string.alert_Login_UsernameBlank);
            ((EditText) findViewById(R.id.login_username)).requestFocus();
            return;
        }

        mPassword = ((EditText) findViewById(R.id.login_password))
                .getText().toString();
        if (mPassword.length() == 0) {
            TNUtilsUi.alert(this, R.string.alert_Login_PasswordBlank);
            ((EditText) findViewById(R.id.login_password)).requestFocus();
            return;
        }
        //
        TNSettings settings = TNSettings.getInstance();
        settings.loginname = mUserName;
        settings.password = mPassword;
        settings.userType = 0;
        settings.savePref(false);

        TNUtilsUi.hideKeyboard(this, R.id.login_password);
        mLoginingDialog.show();

        //调用p层代码
        if (TNUtils.isNetWork()) {
            pLoginNormal(mUserName, mPassword);
        } else {
            TNUtilsUi.showToast(R.string.alert_Net_NotWork);
            mLoginingDialog.hide();
        }

    }
//-------------------------------------QQ登录------------------------------------------

    private void loginQQ() {
        mTencent = Tencent.createInstance(TNConst.QQ_APP_ID, this.getApplicationContext());
        IUiListener listener = new IUiListener() {
            @Override
            public void onError(UiError arg0) {
                mLoginingDialog.hide();
                isClickQQ = false;
                Toast.makeText(getApplicationContext(), "Auth error : " + arg0.errorDetail, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onComplete(JSONObject jobj) {
                // openid 用于唯一标识用户身份（每一个openid与QQ号码对应）。
                // access_token 用户进行应用邀请、分享、支付等基本业务请求的凭据。
                // expires_in access_token的有效时间，在有效期内可以发起业务请求，过期失效。
//				isClickQQ = false;
                mLoginId = jobj.optString("openid");
                String accessToken = jobj.optString("access_token");
                String refreshToken = jobj.optString("pay_token");
                getQQUnionId(accessToken, refreshToken);
            }

            @Override
            public void onCancel() {
                isClickQQ = false;
                mLoginingDialog.hide();
                Toast.makeText(getApplicationContext(), "QQ login cancel",
                        Toast.LENGTH_LONG).show();
            }
        };
        mSsoHandler = null;
        mTencent.login(this, TNConst.QQ_SCOPE, listener);
    }

    private void getQQUnionId(final String accessToken, final String refreshToken) {
        String url = "https://graph.qq.com/oauth2.0/me?access_token=" + accessToken + "&unionid=1";
        pGetQQUnionId(url, accessToken, refreshToken);

//        AsyncHttpClient client = new AsyncHttpClient();
//        client.get(url, new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] data) {
//                super.onSuccess(statusCode, headers, data);
//                String s = new String(data);
//                String[] split = s.split(":");
//                s = split[split.length - 1];
//                split = s.split("\"");
//                s = split[1];
//                String unionId = s;
//                //
//
//                pLoginThird(3, unionId, System.currentTimeMillis(), accessToken, refreshToken, "QQ" + System.currentTimeMillis());
//
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] data, Throwable e) {
//                super.onFailure(statusCode, headers, data, e);
//                Toast.makeText(getApplicationContext(), "Auth Fail", Toast.LENGTH_LONG).show();
//            }
//        });

    }


    //-------------------------------------wechat登录------------------------------------------
    private void loginWechat() {
        if (TNUtils.isNetWork()) {
            WXapi = WXAPIFactory.createWXAPI(this, TNConst.WX_APP_ID, true);
            WXapi.registerApp(TNConst.WX_APP_ID);
            SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = "wechat_sdk_demo";
            WXapi.sendReq(req);
        } else {
            TNUtilsUi.showToast(R.string.alert_Net_NotWork);
            mLoginingDialog.hide();
        }

    }


    //-------------------------------------sina登录------------------------------------------

    private void loginSina() {
        //初始化
        WbSdk.install(this, new AuthInfo(this, TNConst.SINA_APP_KEY, TNConst.SINA_REDIRECT_URL, TNConst.SINA_SCOPE));
        //调用
        mSsoHandler = new SsoHandler(TNLoginAct.this);
        mSsoHandler.authorizeClientSso(new SelfWbAuthListener());
    }


    /**
     * 微博认证授权回调类。 1. SSO登陆时，需要在{@link #onActivityResult}
     * 中调用mSsoHandler.authorizeCallBack后， 该回调才会被执行。 2. 非SSO登陆时，当授权后，就会被执行。
     * 当授权成功后，请保存该access_token、expires_in等信息到SharedPreferences中。
     */
    private class SelfWbAuthListener implements com.sina.weibo.sdk.auth.WbAuthListener {
        @Override
        public void onSuccess(final Oauth2AccessToken token) {
            TNLoginAct.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAccessToken = token;
                    String access_token = mAccessToken.KEY_ACCESS_TOKEN;
                    String refresh_token = mAccessToken.KEY_REFRESH_TOKEN;
                    String expires_in = mAccessToken.KEY_EXPIRES_IN;
                    String uid = mAccessToken.KEY_UID;
                    if (mAccessToken.isSessionValid()) {
                        pLoginThird(2, uid, System.currentTimeMillis(), access_token, refresh_token, "sina" + System.currentTimeMillis());
                    } else {
                        mLoginingDialog.hide();
                        Toast.makeText(getApplicationContext(), "Auth Fail", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        @Override
        public void cancel() {
            mLoginingDialog.hide();
            Toast.makeText(getApplicationContext(), "sina login cancel",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailure(WbConnectErrorMessage errorMessage) {
            mLoginingDialog.hide();
            Toast.makeText(TNLoginAct.this, errorMessage.getErrorMessage(), Toast.LENGTH_LONG).show();
        }
    }


    //--------------------------------------其他重写-----------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MLog.d("登录返回结果处理" + requestCode + "--" + resultCode);

        //qq
        if (mTencent != null) {
            mTencent.onActivityResult(requestCode, resultCode, data);
        }

        // sina
        // SSO 授权回调 重要：发起 SSO 登陆的Activity必须重写onActivityResult
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }


    //-----------------------------------p层调用接口--------------------------------------------

    //正常登录
    private void pLoginNormal(String name, String ps) {
        logPresener.loginNormal(name, ps);
    }

    //第三方登录  qq登录/sina 登录/wechat 登录
    private void pLoginThird(int btype, String type, long currentTime, String accessToken, String refreshToken, String name) {
        logPresener.loginThird(btype, type, currentTime, accessToken, refreshToken, name);
    }

    private void updateProfile() {
        logPresener.pUpdataProfile();
    }

    private void pGetQQUnionId(String url, String accessToken, String refreshToken) {
        logPresener.getQQUnionId(url, accessToken, refreshToken);
    }

    //-----------------------------------接口返回的回调--------------------------------------------

    /**
     * 正常登录 返回
     * 根据以前接口要求，调用登录接口成功后，还需再调用一个更新接口
     *
     * @param obj
     */
    @Override
    public void onLoginNormalSuccess(Object obj) {
        loginBean = (LoginBean) obj;
        TNSettings settings = TNSettings.getInstance();
        settings.isLogout = false;

        //
        settings.password = mPassword;
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
    public void onLoginNormalFailed(String msg, Exception e) {
        loginBean = null;
        mLoginingDialog.hide();
        if (e == null) {
            TNUtilsUi.alert(this, msg);
        } else {
            TNUtilsUi.alert(this, "error!");
        }
    }

    @Override
    public void onQQUnionIdSuccess(Object obj, String accessToken, String refreshToken) {
        QQBean bean = (QQBean) obj;
        pLoginThird(3, bean.getUnioid(), System.currentTimeMillis(), accessToken, refreshToken, "QQ" + System.currentTimeMillis());

    }

    @Override
    public void onQQUnionIdFailed(String msg, Exception e) {
        MLog.e("获取qq unionId失败");
    }

    @Override
    public void onLoginThirdSuccess(Object obj) {
        loginBean = (LoginBean) obj;

        TNSettings settings = TNSettings.getInstance();
        settings.isLogout = false;
        settings.firstLaunch = false;//该数据，只有第三方登录成功才设置
        //
        settings.password = mPassword;
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
    public void onLoginThirdFailed(String msg, Exception e, String bid, int btype, long currentTime, String accessToken, String refreshToken, String name) {
        mLoginingDialog.hide();
        if (msg.equals("用户未绑定轻笔记")) {//登录的参数还需要使用，所以在回调中再返回
            Bundle b = new Bundle();
            b.putString("type", "third");
            b.putInt("bType", btype);
            b.putString("bid", bid);
            b.putLong("stamp", currentTime);
            b.putString("accessToken", accessToken);
            b.putString("refreshToken", refreshToken);
            b.putString("name", name);
            b.putString("password", mPassword);
            startActivity(TNBindAccountAct.class, b);//绑定手机号
        } else {
            TNUtilsUi.alert(this, msg);
        }
    }

    //正常登录的更新
    @Override
    public void onLogProfileSuccess(Object obj) {
        profileBean = (ProfileBean) obj;
        mLoginingDialog.hide();

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
        MLog.d("TNLoginAct--TNMainAct");

        startActivity(TNMainAct.class);
        finish();
    }

    @Override
    public void onLogProfileFailed(String msg, Exception e) {
        mLoginingDialog.hide();
        profileBean = null;
    }

    //
    /**
     * 使用RxBus 实现微信登录信息返回
     * home页跳转到new页
     */
    /**
     * 使用RxBus 实现fragment之间的跳转
     * home页跳转到new页
     */
    CompositeDisposable compositeDisposable;

    private void initRxBus() {
        compositeDisposable = new CompositeDisposable();
        RxBus.getInstance().toObservable(RxCodeConstants.WEChat_BACK_LOG, RxBusBaseMessage.class)
                .subscribe(new Observer<RxBusBaseMessage>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(RxBusBaseMessage rxBusBaseMessage) {
                        final String unionid = SPUtil.getString("unionid", "");
                        final String access_token = SPUtil.getString("access_token", "");
                        final String refresh_token = SPUtil.getString("refresh_token", "");
                        final String nickName = SPUtil.getString("nickName", "");
                        MLog.d("TNLoginAct-->RxBus", unionid, access_token, refresh_token, nickName);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pLoginThird(9, unionid, System.currentTimeMillis(), access_token, refresh_token, nickName);
                            }
                        });

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}
