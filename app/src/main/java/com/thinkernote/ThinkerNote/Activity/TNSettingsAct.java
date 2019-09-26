package com.thinkernote.ThinkerNote.Activity;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.General.TNRunner;
import com.thinkernote.ThinkerNote.Adapter.TNPreferenceAdapter;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Data.TNPreferenceChild;
import com.thinkernote.ThinkerNote.Data.TNPreferenceGroup;
import com.thinkernote.ThinkerNote.Data.TNUser;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.dialog.CommonDialog;
import com.thinkernote.ThinkerNote.dialog.InviteCodeDialog;
import com.thinkernote.ThinkerNote.dialog.VolumeDialog;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnSettingsListener;
import com.thinkernote.ThinkerNote.mvp.p.SettingsPresenter;

import java.util.Vector;

/**
 *  有反射方法执行，禁止混淆
 * 主页--设置--多个功能共用界面：用户信息/个性化设置/空间信息
 */
public class TNSettingsAct extends TNActBase implements OnClickListener, OnChildClickListener, OnGroupClickListener
        , OnSettingsListener {

    /* bundle:
     * type String
     */

    private String mType;//用来区分是哪个设置页面
    private Vector<TNPreferenceGroup> mGroups;
    private ExpandableListView settingExpandableListView;

    private TNPreferenceChild mCurrChild;
    private int mSyncType;
    private int pictureCompressionMode;
    private Dialog mProgressDialog = null;
    private TNSettings mSettings;
    //p
    private SettingsPresenter presener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        initMyView();

        presener = new SettingsPresenter(this, this);
    }

    private void initMyView() {
        //
        mSettings = TNSettings.getInstance();
        mType = getIntent().getStringExtra("Type");
        mGroups = new Vector<TNPreferenceGroup>();
        settingExpandableListView = (ExpandableListView) findViewById(R.id.settings_expandable_listview);
        settingExpandableListView.setAdapter(new TNPreferenceAdapter(this, mGroups));
        settingExpandableListView.setOnGroupClickListener(this);
        settingExpandableListView.setOnChildClickListener(this);

        mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);

        //title
        if (mType.equals("USER_INFO")) {
            ((TextView) findViewById(R.id.settings_home_btn)).setText("用户信息");
        } else if (mType.equals("SETTING")) {
            ((TextView) findViewById(R.id.settings_home_btn)).setText("个性化设置");
        } else if (mType.equals("AUDIO_SETTING")) {
            ((TextView) findViewById(R.id.settings_home_btn)).setText("语音朗读设置");
        } else if (mType.equals("SPACE_INFO")) {
            ((TextView) findViewById(R.id.settings_home_btn)).setText("空间信息");
        }
        setViews();
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

        } else {
            setGroupList();
            ((BaseExpandableListAdapter) settingExpandableListView.getExpandableListAdapter()).notifyDataSetChanged();
            for (int i = 0; i < mGroups.size(); i++) {
                settingExpandableListView.expandGroup(i);
            }
        }
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v,
                                int groupPosition, long id) {
        return true;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
                                int groupPosition, int childPosition, long id) {
        mCurrChild = mGroups.get(groupPosition).getChilds().get(childPosition);
        if (mCurrChild.getTargetMethod() != null) {
            mCurrChild.getTargetMethod().run();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_home_btn:
                finish();
                break;
        }
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

    //-----------------------------------------------------------------------------------------

    /**
     * 填充 用户信息要展示的数据
     */
    private void setGroupList() {
        if (mGroups == null) {
            mGroups = new Vector<TNPreferenceGroup>();
        } else {
            mGroups.clear();
        }

        TNPreferenceGroup group = null;
        TNUser user = TNDbUtils.getUser(mSettings.userId);

        if (mType.equals("USER_INFO")) {// 用户信息展示内容
            group = new TNPreferenceGroup(getString(R.string.userinfo_userinfo));

            //用户名
            group.addChild(new TNPreferenceChild(getString(R.string.userinfo_username), user.username, true, new TNRunner(this, "changeUserName")));
            //手机号
            group.addChild(new TNPreferenceChild(getString(R.string.userinfo_phone), user.phone, true, new TNRunner(this, "changePhone")));
            {//密码
                String info = "******";
//				if(user.preCode != null && user.preCode.length() > 0)
//					info = user.preCode;
                group.addChild(new TNPreferenceChild(getString(R.string.userinfo_password), info, true, new TNRunner(this, "changePassword")));
            }
            {//邮箱
                String info = null;
                if (user.userEmail == null || user.userEmail.equals(""))
                    info = getString(R.string.userinfo_email_no);
                else
                    info = user.userEmail;
                group.addChild(new TNPreferenceChild(getString(R.string.userinfo_email), info, true, new TNRunner(this, "changeEmail")));
            }
            //验证邮箱
            if (user.emailVerify != 0) {
                group.addChild(new TNPreferenceChild(getString(R.string.userinfo_verifyemail), getString(R.string.userinfo_verifyemail_no), true, new TNRunner(this, "verifyemail")));
            } else {
                group.addChild(new TNPreferenceChild(getString(R.string.userinfo_verifyemail), getString(R.string.userinfo_verifyemail_yes), true, new TNRunner(this, "verifyemail")));
            }

            mGroups.add(group);
        } else if (mType.equals("SETTING")) {// 个性化 展示内容
            group = new TNPreferenceGroup(getString(R.string.userinfo_settings));

            {// 默认文件夹
                TNCat cat = TNDbUtils.getCat(mSettings.defaultCatId);
                // 有时Cat会为空，原因不明，暂如此保护处理
                String info = "";
                if (cat != null)
                    info = cat.catName;
                group.addChild(new TNPreferenceChild(getString(R.string.userinfo_defaultfolder), info, true, new TNRunner(this, "changeDefauldtFolder")));
            }
//			{// 同步
//				String[] syncStatus = getResources().getStringArray(R.array.sync);
//				group.addChild(new TNPreferenceChild(getString(R.string.userinfo_sync), syncStatus[mSettings.sync], true, new TNRunner(this, "changeSyncMode")));
//			}
            {// 设置密码锁
                String info = getString(R.string.userinfo_lockpattern_no);
                if (mSettings.lockPattern.size() > 0) {
                    info = getString(R.string.userinfo_lockpattern_yes);
                }
                group.addChild(new TNPreferenceChild(getString(R.string.userinfo_lockpattern), info, true, new TNRunner(this, "ChangeLockpattern")));
            }
            {// 图片压缩
                String[] compressionmode = getResources().getStringArray(
                        R.array.compression);
                int mode = mSettings.pictureCompressionMode;
                if (mode == -1) {
                    mode = 1;
                }
                group.addChild(new TNPreferenceChild(getString(R.string.userinfo_picture_compressionmode), compressionmode[mode], true, new TNRunner(this, "ChangePictureCompressionmode")));
            }

            mGroups.add(group);
        } else if (mType.equals("AUDIO_SETTING")) {// 语音朗读设置 展示内容
            group = new TNPreferenceGroup(getString(R.string.userinfo_audio_settings));

            {// 声音类型
                String info = getString(R.string.userinfo_voice_female);
                if (mSettings.voice.equals("xiaoyu")) {
                    info = getString(R.string.userinfo_voice_male);
                }
                group.addChild(new TNPreferenceChild(getString(R.string.userinfo_voice), info, true, new TNRunner(this, "ChangeVoice")));
            }
            {// 语速
                group.addChild(new TNPreferenceChild(getString(R.string.userinfo_speed), String.valueOf(mSettings.speed), true, new TNRunner(this, "ChangeSpeed")));
            }
            {// 音量
                group.addChild(new TNPreferenceChild(getString(R.string.userinfo_volume), String.valueOf(mSettings.volume), true, new TNRunner(this, "ChangeVolume")));
            }

            mGroups.add(group);
        } else if (mType.equals("SPACE_INFO")) {// 存储空间 展示内容

            group = new TNPreferenceGroup(getString(R.string.userinfo_spaceinfo));

            {// 已用空间
                String info = String.format("%.2f M / %.2f M",
                        user.usedSpace / 1024f / 1024f,
                        user.totalSpace / 1024f / 1024f);
                group.addChild(new TNPreferenceChild(getString(R.string.userinfo_usedspace), info, false, null));
            }
            {// 我的邀请码
                group.addChild(new TNPreferenceChild(getString(R.string.userinfo_invite_code), TNUtils.toInviteCode(user.userId), false, null));
            }
            {// 清除缓存
                group.addChild(new TNPreferenceChild(getString(R.string.userinfo_clear_cache), null, true, new TNRunner(this, "clearCache")));
            }
            {// 邀请朋友使用
                group.addChild(new TNPreferenceChild(getString(R.string.userinfo_invite), null, true, new TNRunner(this, "invite")));
            }

            mGroups.add(group);
        }
    }

    //------------------------------------------------触发功能-----------------------------------------
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

    //
    public void ChangeRemindLockGroup() {
        TNSettings settings = TNSettings.getInstance();
        if (settings.remindLockGroup) {
            settings.remindLockGroup = false;
            TNUtilsUi
                    .showShortToast(R.string.alert_UserInfo_remindLockGroup_closed);
        } else {
            settings.remindLockGroup = true;
            TNUtilsUi
                    .showShortToast(R.string.alert_UserInfo_remindLockGroup_opened);
        }
        settings.savePref(true);
        configView();
    }

    //
    public void ChangeRemindLockNote() {
        TNSettings settings = TNSettings.getInstance();
        if (settings.remindLockNote) {
            settings.remindLockNote = false;
            TNUtilsUi
                    .showShortToast(R.string.alert_UserInfo_remindLockNote_closed);
        } else {
            settings.remindLockNote = true;
            TNUtilsUi
                    .showShortToast(R.string.alert_UserInfo_remindLockNote_opened);
        }
        settings.savePref(true);
        configView();
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


    //修改声音
    public void ChangeVoice() {
        TNSettings settings = TNSettings.getInstance();
        if (settings.voice.equals("xiaoyan")) {
            settings.voice = "xiaoyu";
        } else {
            settings.voice = "xiaoyan";
        }
        settings.savePref(true);
        configView();
    }

    //修改语音
    public void ChangeSpeed() {
        VolumeDialog dialog = new VolumeDialog(this, new VolumeDialog.DialogCallBack() {
            @Override
            public void sureBack(int str) {
                TNSettings.getInstance().speed = str;
                TNSettings.getInstance().savePref(true);
                configView();
            }

            @Override
            public void cancelBack() {

            }
        });
        dialog.show();
    }

    //设置音量
    public void ChangeVolume() {
        //
        VolumeDialog dialog = new VolumeDialog(this, new VolumeDialog.DialogCallBack() {
            @Override
            public void sureBack(int str) {
                TNSettings.getInstance().volume = str;
                TNSettings.getInstance().savePref(true);
                configView();
            }

            @Override
            public void cancelBack() {

            }
        });
        dialog.show();
    }

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
                            TNUtilsUi.alert(TNSettingsAct.this,
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

    // ------------------------------------------------------------------


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
        setGroupList();
        ((BaseExpandableListAdapter) settingExpandableListView.getExpandableListAdapter()).notifyDataSetChanged();
        for (int i = 0; i < mGroups.size(); i++) {
            settingExpandableListView.expandGroup(i);
        }
    }

    @Override
    public void onProfileFailed(String msg, Exception e) {

        mProgressDialog.hide();
        TNUtilsUi.showToast(msg);
    }
}
