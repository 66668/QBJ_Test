package com.thinkernote.ThinkerNote.Activity.settings;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.Activity.TNBindPhoneAct;
import com.thinkernote.ThinkerNote.Activity.TNCatListAct;
import com.thinkernote.ThinkerNote.Activity.TNHtmlViewAct;
import com.thinkernote.ThinkerNote.bean.localdata.TNCat;
import com.thinkernote.ThinkerNote.bean.localdata.TNUser;
import com.thinkernote.ThinkerNote.db.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.TNUtils;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsAtt;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsDialog;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsSkin;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.utils.MLog;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.views.dialog.CommonDialog;
import com.thinkernote.ThinkerNote.views.dialog.InviteCodeDialog;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnSettingsListener;
import com.thinkernote.ThinkerNote.mvp.p.SettingsPresenter;

/**
 * 共用设置界面：用户信息/个性化设置/空间信息/关于我们
 */
public class TNCommonSettingsAct extends TNActBase implements OnClickListener, OnGroupClickListener
        , OnSettingsListener {


    private String mType;//用来区分是哪个设置页面

    private int mSyncType;
    private int pictureCompressionMode;
    private Dialog mProgressDialog = null;
    private TNSettings mSettings;
    //p
    private SettingsPresenter presener;
    //-------------------------控件相关-------------------------------

    //-------------------------用户信息-------------------------------
    LinearLayout ly_userName, ly_userPhone, ly_userPs, ly_userEmail, ly_verifyemail;
    TextView tv_group, tv_veryfyEmail, tv_userEmail, tv_userPhone, tv_userName;

    //-------------------------个性设置-------------------------------
    LinearLayout ly_defaultFile, ly_picPs, ly_picZip;
    TextView tv_defaultFile, tv_picPs, tv_picZip;

    //-------------------------空间-------------------------------
    LinearLayout ly_space, ly_inviteCode, ly_clearCache, ly_invite;
    TextView tv_space, tv_inviteCode;

    //-------------------------关于-------------------------------
    LinearLayout ly_advice, ly_comment, ly_homePage, ly_connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_settings_common);
        initMyView();
        getMyIntent();
        //设置界面显示
        presener = new SettingsPresenter(this);
    }

    private void initMyView() {
        //用户信息
        ly_userName = findViewById(R.id.ly_userName);
        ly_userPhone = findViewById(R.id.ly_userPhone);
        ly_userPs = findViewById(R.id.ly_userPs);
        ly_userEmail = findViewById(R.id.ly_userEmail);
        ly_verifyemail = findViewById(R.id.ly_verifyemail);

        tv_group = findViewById(R.id.tv_group);
        tv_veryfyEmail = findViewById(R.id.tv_veryfyEmail);
        tv_userEmail = findViewById(R.id.tv_userEmail);
        tv_userPhone = findViewById(R.id.tv_userPhone);
        tv_userName = findViewById(R.id.tv_userName);

        ly_userName.setOnClickListener(this);
        ly_userPhone.setOnClickListener(this);
        ly_userPs.setOnClickListener(this);
        ly_userEmail.setOnClickListener(this);
        ly_verifyemail.setOnClickListener(this);

        //个性设置
        ly_defaultFile = findViewById(R.id.ly_defaultFile);
        ly_picPs = findViewById(R.id.ly_picPs);
        ly_picZip = findViewById(R.id.ly_picZip);

        tv_defaultFile = findViewById(R.id.tv_defaultFile);
        tv_picPs = findViewById(R.id.tv_picPs);
        tv_picZip = findViewById(R.id.tv_picZip);

        ly_defaultFile.setOnClickListener(this);
        ly_picPs.setOnClickListener(this);
        ly_picZip.setOnClickListener(this);

        //空间
        ly_space = findViewById(R.id.ly_space);
        ly_inviteCode = findViewById(R.id.ly_inviteCode);
        ly_clearCache = findViewById(R.id.ly_clearCache);
        ly_invite = findViewById(R.id.ly_invite);

        tv_space = findViewById(R.id.tv_space);
        tv_inviteCode = findViewById(R.id.tv_picZip);

        ly_space.setOnClickListener(this);
        ly_inviteCode.setOnClickListener(this);
        ly_clearCache.setOnClickListener(this);
        ly_invite.setOnClickListener(this);
        //关于
        ly_advice = findViewById(R.id.ly_advice);
        ly_comment = findViewById(R.id.ly_comment);
        ly_homePage = findViewById(R.id.ly_homePage);
        ly_connect = findViewById(R.id.ly_connect);

        ly_advice.setOnClickListener(this);
        ly_comment.setOnClickListener(this);
        ly_homePage.setOnClickListener(this);
        ly_connect.setOnClickListener(this);
        setViews();

        mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);


    }

    private void getMyIntent() {
        //
        mType = getIntent().getStringExtra("Type");
        //title
        if (mType.equals("USER_INFO")) {
            ((TextView) findViewById(R.id.settings_home_btn)).setText("用户信息");
            tv_group.setText(R.string.userinfo_userinfo);
        } else if (mType.equals("SETTING")) {
            ((TextView) findViewById(R.id.settings_home_btn)).setText("个性化设置");
            tv_group.setText(R.string.userinfo_settings);
        } else if (mType.equals("ABOUT_US")) {
            ((TextView) findViewById(R.id.settings_home_btn)).setText("关于我们");
            tv_group.setText(R.string.about_company);
        } else if (mType.equals("SPACE_INFO")) {
            ((TextView) findViewById(R.id.settings_home_btn)).setText("空间信息");
            tv_group.setText(R.string.userinfo_spaceinfo);

        }
    }

    private void setShow() {
        mSettings = TNSettings.getInstance();
        if (mType.equals("USER_INFO")) {
            //用户可见
            ly_userName.setVisibility(View.VISIBLE);
            ly_userPhone.setVisibility(View.VISIBLE);
            ly_userPs.setVisibility(View.VISIBLE);
            ly_userEmail.setVisibility(View.VISIBLE);
            ly_verifyemail.setVisibility(View.VISIBLE);
            //填充信息
            TNUser user = TNDbUtils.getUser(mSettings.userId);
            tv_userName.setText(user.username);
            tv_userPhone.setText(user.phone);

            if (user.userEmail == null || user.userEmail.equals(""))
                tv_userEmail.setText(R.string.userinfo_email_no);
            else
                tv_userEmail.setText(user.userEmail);

            //验证邮箱
            if (user.emailVerify != 0) {
                tv_veryfyEmail.setText(R.string.userinfo_verifyemail_no);
            } else {
                tv_veryfyEmail.setText(R.string.userinfo_verifyemail_yes);
            }

        } else if (mType.equals("SETTING")) {
            //设置可见
            ly_defaultFile.setVisibility(View.VISIBLE);
            ly_picPs.setVisibility(View.VISIBLE);
            ly_picZip.setVisibility(View.VISIBLE);
            //填充信息
            // 默认文件夹
            TNCat cat = TNDbUtils.getCat(mSettings.defaultCatId);
            // 有时Cat会为空，原因不明，暂如此保护处理
            String info = "";
            if (cat != null)
                info = cat.catName;
            tv_defaultFile.setText(info);

            // 设置密码锁
            String picPs = getString(R.string.userinfo_lockpattern_no);
            if (mSettings.lockPattern.size() > 0) {
                picPs = getString(R.string.userinfo_lockpattern_yes);
            }
            tv_picPs.setText(picPs);

            {// 图片压缩
                String[] compressionmode = getResources().getStringArray(
                        R.array.compression);
                int mode = mSettings.pictureCompressionMode;
                if (mode == -1) {
                    mode = 1;
                }
                tv_picZip.setText(compressionmode[mode]);
            }

        } else if (mType.equals("ABOUT_US")) {
            //关于 可见
            ly_advice.setVisibility(View.VISIBLE);
            ly_comment.setVisibility(View.VISIBLE);
            ly_homePage.setVisibility(View.VISIBLE);
            ly_connect.setVisibility(View.VISIBLE);

        } else if (mType.equals("SPACE_INFO")) {
            //空间可见
            ly_space.setVisibility(View.VISIBLE);
            ly_inviteCode.setVisibility(View.VISIBLE);
            ly_clearCache.setVisibility(View.VISIBLE);
            ly_invite.setVisibility(View.VISIBLE);
            TNUser user = TNDbUtils.getUser(mSettings.userId);
            // 已用空间
            String info = String.format("%.2f M / %.2f M",
                    user.usedSpace / 1024f / 1024f,
                    user.totalSpace / 1024f / 1024f);
            tv_space.setText(info);
            tv_inviteCode.setText(TNUtils.toInviteCode(user.userId));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_home_btn:
                finish();
                break;
            case R.id.ly_userName://用户
                changeUserName();
                break;
            case R.id.ly_userPhone:
                changePhone();
                break;
            case R.id.ly_userPs:
                changePassword();
                break;
            case R.id.ly_userEmail:
                changeEmail();
                break;
            case R.id.ly_verifyemail:
                verifyemail();
                break;
            case R.id.ly_defaultFile://设置
                changeDefauldtFolder();
                break;
            case R.id.ly_picPs:
                ChangeLockpattern();
                break;
            case R.id.ly_picZip:
                ChangePictureCompressionmode();
                break;
            case R.id.ly_space://空间
                break;
            case R.id.ly_inviteCode:
                break;
            case R.id.ly_clearCache:
                clearCache();
                break;
            case R.id.ly_invite:
                invite();
                break;
            case R.id.ly_advice://关于
                advice();
                break;
            case R.id.ly_comment:
                comment();
                break;
            case R.id.ly_homePage:
                homepage();
                break;
            case R.id.ly_connect:
                contact();
                break;
        }
    }


    @Override
    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.settings_toolbar, R.drawable.toolbg);

        findViewById(R.id.settings_home_btn).setOnClickListener(this);
    }

    @Override
    protected void configView() {
        if (mType.equals("USER_INFO") && TNUtils.isNetWork()) {
            getprofile();
        }
        setShow();
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v,
                                int groupPosition, long id) {
        return true;
    }


    @Override
    public void onDestroy() {
        mProgressDialog.dismiss();
        super.onDestroy();
    }

    /**
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == R.string.userinfo_defaultfolder) {
            if (resultCode == RESULT_OK) {
                setDefaultFolder(data.getLongExtra("SelectedCatId", 0));

            }
        }
    }

    //------------------------------------------------用户功能-----------------------------------------
    //修改用户信息
    public void changeUserName() {
        if (!TNUtils.checkNetwork(this))
            return;
        Bundle bundle = new Bundle();
        bundle.putString("type", "username");
        startActivity(TNChangeUserInfoAct.class, bundle);
    }

    //更换手机号
    public void changePhone() {
        if (!TNUtils.checkNetwork(this))
            return;
        Bundle bundle = new Bundle();
        bundle.putString("type", "change");
        startActivity(TNBindPhoneAct.class, bundle);

    }

    //修改密码
    public void changePassword() {
        if (!TNUtils.checkNetwork(this))
            return;

        Bundle bundle = new Bundle();
        bundle.putString("type", "password");
        startActivity(TNChangeUserInfoAct.class, bundle);
    }

    //修改邮箱
    public void changeEmail() {
        if (!TNUtils.checkNetwork(this))
            return;

        Bundle bundle = new Bundle();
        bundle.putString("type", "email");
        startActivity(TNChangeUserInfoAct.class, bundle);
    }

    //邮箱验证
    public void verifyemail() {
        if (!TNUtils.checkNetwork(this))
            return;

        TNUser user = TNDbUtils.getUser(mSettings.userId);

        if (user.userEmail == null || user.userEmail.equals("")) {
            TNUtilsUi.alert(this,
                    R.string.alert_UserInfo_EmailVerify_EmailBlank);
            return;
        }
        mProgressDialog.show();
        pVerifyEmail();

    }

    //------------------------------------------------设置功能-----------------------------------------
    //设置默认文件路径
    public void changeDefauldtFolder() {
        Bundle b = new Bundle();
        b.putLong("OriginalCatId", TNSettings.getInstance().defaultCatId);
        b.putInt("Type", 2);
        startActForResult(TNCatListAct.class, b, R.string.userinfo_defaultfolder);
    }


    //修改锁
    public void ChangeLockpattern() {
        Bundle b = new Bundle();
        b.putInt("Type", 0);
        b.putString("OriginalPath", "[]");

        Intent intent = new Intent(this, TNLockAct.class);
        intent.putExtras(b);
        startActivity(intent);
    }

    /**
     * 图压缩
     */
    public void ChangePictureCompressionmode() {

        pictureCompressionMode = TNSettings.getInstance().pictureCompressionMode;
        MLog.i(TAG, pictureCompressionMode + "");
        if (pictureCompressionMode == -1) {
            pictureCompressionMode = 1;
        }

        CommonDialog dialog = new CommonDialog(this, R.string.userinfo_picture_compressionmode,
                "压缩",
                "不压缩",
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        //0,不压缩  1,压缩(默认)
                        pictureCompressionMode = 1;
                        TNSettings settings = TNSettings.getInstance();
                        settings.pictureCompressionMode = pictureCompressionMode;
                        MLog.i(TAG, pictureCompressionMode + "");
                        settings.savePref(true);
                        configView();
                    }

                    @Override
                    public void cancelBack() {
                        pictureCompressionMode = 0;
                        TNSettings settings = TNSettings.getInstance();
                        settings.pictureCompressionMode = pictureCompressionMode;
                        MLog.i(TAG, pictureCompressionMode + "");
                        settings.savePref(true);
                        configView();
                    }

                });
        dialog.show();
        dialog.getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    //------------------------------------------------空间功能-----------------------------------------
    //清除缓存
    public void clearCache() {
        CommonDialog dialog = new CommonDialog(this, R.string.alert_About_ClearCacheHint,
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        pClearCache();
                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.cancelAll();
                    }

                    @Override
                    public void cancelBack() {
                        mProgressDialog.hide();
                    }

                });
        dialog.show();
        dialog.getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    public void contribution() {
        Bundle b = new Bundle();
        b.putString("HtmlType", "contribution_rule");
        startActivity(TNHtmlViewAct.class, b);
    }

    //邀请码
    public void setInviteCode() {
        if (!TNUtils.checkNetwork(this))
            return;

        InviteCodeDialog dialog = new InviteCodeDialog(this,
                new InviteCodeDialog.DialogCallBack() {
                    @Override
                    public void sureBack(String code) {
                        if (code.length() == 0) {
                            TNUtilsUi.alert(TNCommonSettingsAct.this,
                                    R.string.alert_UserInfo_invitecode_blank);
                            return;
                        }
                        mProgressDialog.show();
                    }

                    @Override
                    public void cancelBack() {
                        mProgressDialog.hide();
                    }

                });
        dialog.show();
        dialog.getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    public void invite() {
        TNUser user = TNDbUtils.getUser(mSettings.userId);
        ;
        TNUtilsUi.inviteFriend(this, user.userId, user.username);
    }

    // ---------------------------------关于功能---------------------------------
    public void advice() {
        startActivity(TNReportAct.class);
    }

    public void comment() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + getPackageName()));
        TNUtilsDialog.startIntent(this, intent,
                R.string.alert_About_CantOpenComment);
    }

    public void homepage() {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://" + getString(R.string.about_homepage_url)));
        TNUtilsDialog.startIntent(this, intent,
                R.string.alert_About_CantOpenWeb);
    }

    public void contact() {
        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.parse("mailto:info@qingbiji.cn"));
        TNUtilsDialog.startIntent(this, intent,
                R.string.alert_About_CantSendEmail);
    }

    public void friends() {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://shouji.360.cn/partner.and.php?id=101089"));
        TNUtilsDialog.startIntent(this, intent,
                R.string.alert_About_CantOpenWeb);
    }

    //----------------------------------p层调用--------------------------------------
    //获取用户信息
    private void getprofile() {
        presener.pGetProfile();
    }

    private void setDefaultFolder(long pid) {
        presener.setDefaultFolder(pid);

    }

    private void pVerifyEmail() {
        presener.verifyEmail();
    }

    private void pClearCache() {
        TNUtilsAtt.deleteTempFiles();
        TNDbUtils.clearCache();
        TNUtilsUi.showToast("清除成功");
    }


    //----------------------------------接口回调--------------------------------------
    @Override
    public void onDefaultFolderSuccess(Object obj, long pid) {
        mProgressDialog.hide();
        TNSettings settings = TNSettings.getInstance();
        settings.defaultCatId = pid;
        settings.savePref(false);
        //
        TNUtilsUi.showShortToast("默认文件夹设置成功");
        configView();
    }

    @Override
    public void onDefaultFoldeFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onVerifyEmailSuccess(Object obj) {
        mProgressDialog.hide();
        TNUtilsUi.showToast("邮箱验证邮件已发送，请去邮箱验证");
    }

    @Override
    public void onVerifyEmailFailed(String msg, Exception e) {
        mProgressDialog.hide();
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onProfileSuccess(Object obj) {
        mProgressDialog.hide();
    }

    @Override
    public void onProfileFailed(String msg, Exception e) {

        mProgressDialog.hide();
        TNUtilsUi.showToast(msg);
    }
}
