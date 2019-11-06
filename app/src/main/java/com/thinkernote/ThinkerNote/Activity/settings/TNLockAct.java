package com.thinkernote.ThinkerNote.Activity.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.Activity.TNLoginAct;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.TNUtils;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsUi;
import com.thinkernote.ThinkerNote.views.LockPatternView;
import com.thinkernote.ThinkerNote.views.LockPatternView.OnLockPatternListener;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.utils.MLog;
import com.thinkernote.ThinkerNote.base.TNActBase;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 九宫格锁 界面
 */
public class TNLockAct extends TNActBase implements OnLockPatternListener, OnClickListener {

    /* Bundle:
     * Type 0 设置图案， 1 确认图案， 2 输入图案
     * OriginalPath
     */

    private LockPatternView mPatternView;
    private TextView mHint;
    private Button mLeftBtn;
    private Button mRightBtn;

    private int mType;
    private Queue<Integer> mOriginalPath;
    private Queue<Integer> mPath;
    int count;


    // Activity methods
    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lock);

        count = 0;

        mHint = (TextView) findViewById(R.id.lock_hint);
        mPatternView = (LockPatternView) findViewById(R.id.lock_patternView);
        mPatternView.setOnLockPatternListener(this);
        mLeftBtn = (Button) findViewById(R.id.lock_left);
        mLeftBtn.setOnClickListener(this);
        mRightBtn = (Button) findViewById(R.id.lock_right);
        mRightBtn.setOnClickListener(this);

        mType = getIntent().getIntExtra("Type", 2);
        mOriginalPath = TNUtils.getPath(getIntent().getStringExtra("OriginalPath"));
        MLog.d("Type=" + mType + "--mOriginalPath=" + mOriginalPath.toString());
        mPath = new LinkedList<Integer>();
    }

    @Override
    protected void configView() {
        if (createStatus == 1)
            return;

        if (mType == 0) {
            mPatternView.setPracticeMode(true);
            mHint.setText(getString(R.string.lock_drawhint));
            //mLeftBtn.setVisibility(View.INVISIBLE);
            mLeftBtn.setText(getString(R.string.lock_cancel));
            mRightBtn.setText(getString(R.string.lock_next));
        } else if (mType == 1) {
            mPatternView.setPracticeMode(true);
            mPatternView.setPath(mOriginalPath);
            mHint.setText(getString(R.string.lock_redrawhint));
            //mLeftBtn.setText(getString(R.string.lock_cancel));
            mLeftBtn.setVisibility(View.GONE);
            mRightBtn.setText(getString(R.string.lock_ok));
        } else if (mType == 2) {
            setTitle("lock");
            mPatternView.setPracticeMode(true);
            mPatternView.setAutoRefresh(true);
            mPatternView.setPath(mOriginalPath);
            mHint.setText(getString(R.string.lock_unlockhint));

            if (TNUtilsUi.isCallFromOutside(this)) {
                mLeftBtn.setText(getString(R.string.lock_cancel));
                mRightBtn.setVisibility(View.GONE);
            } else {
                mLeftBtn.setVisibility(View.GONE);
                mRightBtn.setText(getString(R.string.lock_relogin));
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onInputFilished(Queue<Integer> path, boolean match) {
        mPath = path;
        if (mType == 2) {
            if (mPath.equals(mOriginalPath)) {
                TNSettings settings = TNSettings.getInstance();
                MLog.i(TAG, "set needShowLock = false");
                settings.needShowLock = false;
                settings.savePref(false);
                finish();
            } else {
                count++;
                if (count == 3) {
                    TNUtilsUi.showToast(R.string.alert_Lock_PasswordWrong_Third);
                    TNSettings settings = TNSettings.getInstance();
                    settings.isLogout = true;
                    settings.lockPattern = new LinkedList<Integer>();
                    settings.userId = -1;
                    settings.phone = "";
                    settings.email = "";
                    settings.password = "";
                    settings.savePref(true);
                    settings.savePref(true);
                    startActivity(TNLoginAct.class);
                    finish();
                } else {
                    if (count == 1) {
                        TNUtilsUi.showToast(R.string.alert_Lock_PasswordWrong_First);
                    } else if (count == 2) {
                        TNUtilsUi.showToast(R.string.alert_Lock_PasswordWrong_Second);
                    } else {
                        count = 0;
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lock_left:
                if (mType == 0) {
                    TNSettings.getInstance().lockPattern = new LinkedList<Integer>();
                    TNSettings.getInstance().savePref(true);
                    finish();
                    TNUtilsUi.showToast(R.string.lock_lockcancelled);
                } else if (mType == 2) {
                    TNSettings.getInstance().isLogout = true;
                    finish();
                }
                break;

            case R.id.lock_right:
                if (mType == 0) {
                    if (mPath.size() == 0) {
                        TNUtilsUi.showToast(R.string.lock_nopattern);
                    } else {
                        Bundle b = new Bundle();
                        b.putInt("Type", 1);
                        b.putString("OriginalPath", mPath.toString());
                        //
                        Intent intent = new Intent(this,TNLockAct.class);
                        intent.putExtras(b);
                        startActivity(intent);
                        finish();
                    }
                } else if (mType == 1) {
                    if (mPath.equals(mOriginalPath)) {
                        TNSettings.getInstance().lockPattern = mPath;
                        TNSettings.getInstance().needShowLock = false;
                        TNSettings.getInstance().savePref(true);
                        finish();
                        TNUtilsUi.showToast(R.string.lock_setted);
                    } else {
                        finish();
                        TNUtilsUi.showToast(R.string.lock_unmatch);
                    }
                } else if (mType == 2) {
                    TNSettings.getInstance().isLogout = true;
                    finish();
                }
                break;
        }
    }


}
