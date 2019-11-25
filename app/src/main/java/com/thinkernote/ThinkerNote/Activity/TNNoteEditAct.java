package com.thinkernote.ThinkerNote.Activity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.BuildConfig;
import com.thinkernote.ThinkerNote.db.NoteAttrDbHelper;
import com.thinkernote.ThinkerNote.bean.localdata.TNNote;
import com.thinkernote.ThinkerNote.bean.localdata.TNNoteAtt;
import com.thinkernote.ThinkerNote.bean.localdata.TNUser;
import com.thinkernote.ThinkerNote.db.Database.TNDb;
import com.thinkernote.ThinkerNote.db.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.db.Database.TNSQLString;
import com.thinkernote.ThinkerNote.base.TNConst;
import com.thinkernote.ThinkerNote.utils.actfun.TNRecord;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.actfun.TNSpeek;
import com.thinkernote.ThinkerNote.utils.TNUtils;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsAtt;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsDialog;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsHtml;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsSkin;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.service.LocationService;
import com.thinkernote.ThinkerNote.utils.MLog;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.views.dialog.CommonDialog;
import com.thinkernote.ThinkerNote.mvp.MyRxManager;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnSyncListener;
import com.thinkernote.ThinkerNote.mvp.p.SyncPresenter;
import com.thinkernote.ThinkerNote.views.PoPuMenuView;
import com.thinkernote.ThinkerNote.views.PoPuMenuView.OnPoPuMenuItemClickListener;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 主页--写笔记界面
 * sjy 0626
 */
public class TNNoteEditAct extends TNActBase implements OnClickListener,
        OnFocusChangeListener, TextWatcher,
        OnPoPuMenuItemClickListener, OnSyncListener {

    private static final String TAG = "NOTE";
    //正常登录的同步常量
    private static final int AUTO_SAVE = 1;//2min本地自动保存
    private static final int SAVE_LOCAL = 102;//自动保存
    private static final int START_SYNC = 103;//保存时同时上传（暂不用）
    private static final int SAVE_EXIT = 104;//保存本地退出
    private static final int MAX_CONTENT_LEN = 4 * 100 * 1024;

    private TNNote mNote = null;//全局笔记，最终操作都是这一个笔记
    private TNNoteAtt mCurrentAtt;
    private Uri mCameraUri = null;
    private boolean mIsStartOtherAct = false;
    private int mSelection = -1;
    private float mScale;

    //功能封装
    private TNRecord mRecord;//录音
    private TNSpeek mSpeek;//语音转文字
    // 保存倒计时
    private Timer mTimer;
    private TimerTask mTimerTask;
    //p层
    private SyncPresenter syncPresenter;

    //================布局控件=================
    //toolbar控制布局
    private RelativeLayout ly_speek;
    private LinearLayout ly_record;
    private LinearLayout ly_note;
    private TextView speek_start;
    private TextView tv_back;//返回按钮，适配9.0+无返回按钮的情况
    private Button speek_stop;
    private ImageView speek_img;
    private TextView mRecordTime;
    private ProgressBar mRecordAmplitudeProgress;
    private EditText mTitleView;
    private EditText mContentView;
    private PoPuMenuView mPopuMenu;
    private LinearLayout mAttsLayout;
    private ProgressDialog mProgressDialog = null;


    //================================初始化+复写=================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit);

        initAct();
        syncPresenter = new SyncPresenter(this);
        //开启百度定位
        if (savedInstanceState == null) {
//            TNLBSService.getInstance().startLocation();
            LocationService.getInstance().start();
        }
        if (savedInstanceState == null) {
            MLog.d("SJY", "TNNoteEditAct--onCreate--01");
            initNote();
        } else {//获取onRestoreInstance笔记
            MLog.d("SJY", "TNNoteEditAct--onCreate--02");
            Serializable obj = (TNNote) savedInstanceState
                    .getSerializable("NOTE");
            Uri uri = savedInstanceState.getParcelable("CAMERA_URI");
            boolean tag = savedInstanceState.getBoolean("IS_OTHER_ACT");
            if (obj != null) {
                mNote = (TNNote) obj;
                mIsStartOtherAct = tag;
            }
            if (uri != null)
                mCameraUri = uri;
        }
        startTimer();
        mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
        showToolbar("note");
    }

    private void initAct() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScale = metric.scaledDensity;

        mContentView = (EditText) findViewById(R.id.noteedit_input_content);
        mTitleView = (EditText) findViewById(R.id.noteedit_input_title);
        mAttsLayout = (LinearLayout) findViewById(R.id.noteedit_atts_linearlayout);
        mRecordTime = (TextView) findViewById(R.id.record_time);
        mRecordAmplitudeProgress = (ProgressBar) findViewById(R.id.record_progressbar);
        ly_speek = findViewById(R.id.noteedit_speek_layout);
        ly_record = findViewById(R.id.noteedit_record_layout);
        ly_note = findViewById(R.id.noteedit_toolbar_layout);
        speek_start = findViewById(R.id.speek_start);
        speek_stop = findViewById(R.id.speek_stop);
        speek_img = findViewById(R.id.speek_img);
        tv_back = findViewById(R.id.tv_back);

        findViewById(R.id.noteedit_save).setOnClickListener(this);
        findViewById(R.id.noteedit_camera).setOnClickListener(this);
        findViewById(R.id.noteedit_doodle).setOnClickListener(this);
        findViewById(R.id.noteedit_other).setOnClickListener(this);
        findViewById(R.id.noteedit_record).setOnClickListener(this);
        findViewById(R.id.noteedit_speakinput).setOnClickListener(this);
        findViewById(R.id.record_start).setOnClickListener(this);
        findViewById(R.id.record_stop).setOnClickListener(this);
        findViewById(R.id.record_stop).setOnClickListener(this);
        speek_start.setOnClickListener(this);
        speek_stop.setOnClickListener(this);
        tv_back.setOnClickListener(this);

        mTitleView.setOnFocusChangeListener(this);
        //有的手机不支持软键盘
        mContentView.requestFocus();
        mContentView.setTextIsSelectable(true);
        //打开软键盘，补充
        mContentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputManager = (InputMethodManager) mContentView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mContentView, 0);


//                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                manager.showSoftInputFromInputMethod(mContentView.getWindowToken(), InputMethodManager.SHOW_FORCED);
            }
        });

        mContentView.setOnFocusChangeListener(this);
        mContentView.addTextChangedListener(this);
    }

    /**
     * 处理appwidget的判断
     */
    private void initNote() {

        if (getIntent().hasExtra("NoteForEdit")) {
            long id = getIntent().getLongExtra("NoteForEdit", -1);
            // edit note
            if (id < 0) {// new note
                mNote = (TNNote) getIntent().getSerializableExtra("NOTE");
            } else
                // edit note
                mNote = TNDbUtils.getNoteByNoteLocalId(id);
        } else {
            mNote = TNNote.newNote();

            Intent it = getIntent();
            if (it != null && it.getAction() != null) {
                if (it.getAction().equals("com.thinkernote.ThinkerNote.appwidget.action.ADD")) { //优先处理appwidget
                    //判断用户是否登陆 (判断userId)
                    TNSettings settings = TNSettings.getInstance();
                    TNUser user = TNDbUtils.getUser(settings.userId);
                    if (user == null ||
                            !settings.isLogin() ||
                            (settings.expertTime != 0 && (settings.expertTime * 1000 - System.currentTimeMillis() < 0))
                    ) {
                        TNUtilsUi.showToast("请先登录！");
                        startActivity(this, TNLoginAct.class);
                        this.finish();
                        return;
                    } else {

                    }
                } else {
                    Bundle extras = it.getExtras();

                    if (extras.containsKey(Intent.EXTRA_STREAM)) {
                        Object extraStream = extras.get(Intent.EXTRA_STREAM);
                        if (Uri.class.isInstance(extraStream)) {
                            Uri uri = (Uri) extraStream;
                            String path = getPath(uri);
                            if (path != null) {
                                File file = new File(getPath(uri));
                                mNote.atts.add(TNNoteAtt.newAtt(file, this));
                            }
                        } else if (ArrayList.class.isInstance(extraStream)) {
                            @SuppressWarnings("unchecked")
                            ArrayList<Uri> uris = (ArrayList<Uri>) extraStream;
                            for (Uri uri : uris) {
                                File file = new File(getPath(uri));
                                mNote.atts.add(TNNoteAtt.newAtt(file, this));
                            }
                        }
                    }
                    if (extras.containsKey(Intent.EXTRA_SUBJECT)) {
                        Object subject = extras.get(Intent.EXTRA_SUBJECT);
                        if (subject == null) {
                            mNote.title = "";
                        } else
                            mNote.title = subject.toString();
                    }
                    if (extras.containsKey(Intent.EXTRA_TEXT)) {
                        Object text = extras.get(Intent.EXTRA_TEXT);
                        if (text == null)
                            mNote.content = "";
                        else
                            mNote.content = text.toString();
                    }
                }
            }

        }
        if (mNote == null) {
            MLog.d("编辑笔记--关闭");
            finish();
            return;
        }

        //
        if (mNote.originalNote == null && mNote.noteLocalId > 0) {
            TNNote newnote = TNNote.newNote();
            newnote.originalNote = mNote;
            newnote.noteLocalId = mNote.noteLocalId;
            newnote.noteId = mNote.noteId;
            newnote.content = mNote.content;
            newnote.title = mNote.title;
            newnote.tagStr = mNote.tagStr;
            newnote.catId = mNote.catId;
            newnote.atts.addAll(mNote.atts);
            newnote.createTime = mNote.createTime;
            if (mNote.isEditable()) {
                newnote.content = mNote.getPlainText();
            } else {
                newnote.setMappingAndPlainText();
            }
            mNote = newnote;
        }
    }

    @Override
    protected void configView() {
        if (createStatus == 0) {
            startTargetAct(getIntent().getStringExtra("Target"));
        }
        refreshAttsView();
        initContentView();
        mTitleView.setText(mTitleView.getHint().equals(mNote.title) ? "" : mNote.title);
        mContentView.setText(mNote.content);
        mContentView.setSelection(mNote.content.length());
    }

    private void refreshAttsView() {
        mAttsLayout.removeAllViews();
        boolean needRefresh = false;
        for (TNNoteAtt att : mNote.atts) {
            //此判断是为了解决特殊用户在查看附件时把附件删除引起的问题
            if (new File(att.path).length() <= 0) {
                mNote.atts.remove(att);
                String temp = String.format("<tn-media hash=\"%s\"></tn-media>", att.digest);
                mNote.content = mNote.content.replaceAll(temp, "");
                needRefresh = true;
                break;
            }
            ImageView attView = new ImageView(this);
            setAttView(attView, att);
            mAttsLayout.addView(attView);
        }
        if (needRefresh) {
            configView();
        }
        mAttsLayout.setGravity(Gravity.CENTER);
    }

    private void initContentView() {
        int attViewHeight = 0;
        if (mNote.atts != null && mNote.atts.size() > 0) {
            attViewHeight = 85 + 38;
        }
        mContentView.setMinLines((getWindowManager().getDefaultDisplay()
                .getHeight() - TNUtils.dipToPx(this, 90) - attViewHeight) / mContentView.getLineHeight());
    }

    @Override
    public void onDestroy() {
        //关闭倒计时
        try {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
//            TNLBSService.getInstance().stopLocation();
            LocationService.getInstance().stop();
        } catch (Exception e) {
        }
        handleProgressDialog("dismiss");

        super.onDestroy();
    }


    /**
     * 缓存机制
     *
     * @param outBundle
     */
    @Override
    public void onSaveInstanceState(Bundle outBundle) {
        saveInput();
        outBundle.putSerializable("NOTE", mNote);
        outBundle.putParcelable("CAMERA_URI", mCameraUri);
        outBundle.putBoolean("IS_OTHER_ACT", mIsStartOtherAct);

        super.onSaveInstanceState(outBundle);
    }

    @Override
    public void onRestoreInstanceState(Bundle outBundle) {
        super.onRestoreInstanceState(outBundle);

        mNote = (TNNote) outBundle.getSerializable("NOTE");
        mCameraUri = outBundle.getParcelable("CAMERA_URI");
        mIsStartOtherAct = outBundle.getBoolean("IS_OTHER_ACT");

    }

    @Override
    protected void onResume() {
        super.onResume();
        setCursorLocation();
        ((ScrollView) findViewById(R.id.noteedit_scrollview)).scrollTo(0, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getCursorLocation();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.noteedit_save: {//保存并退出
                TNUtilsUi.hideKeyboard(this, R.id.noteedit_save);
                saveInput();
                if (mRecord != null && !mRecord.isStop()) {
                    handleProgressDialog("show");
                    mRecord.asynStop(8);//关闭录音
                    break;
                }
                saveNote();
                break;
            }

            case R.id.tv_back://返回
                MLog.d("返回");
                backDialog();
                break;
            case R.id.noteedit_other://其他功能
                setOtherBtnPopuMenu();
                mPopuMenu.show(v);
                break;
            case R.id.noteedit_record: {//录音
                startRecord();
                break;
            }
            case R.id.noteedit_doodle: {//涂鸦
                startActForResult(TNNoteDrawAct.class, null, R.id.noteedit_doodle);
                break;
            }
            case R.id.noteedit_camera://相机
                startCamera();
                break;

            case R.id.record_start://录音的开始/暂停
                if (mRecord == null)
                    mRecord = new TNRecord(handler);

                if (mRecord.isRecording()) {
                    mRecord.pause();
                    ((Button) v).setText(R.string.noteedit_record_start);
                } else {
                    mRecord.start();
                    ((Button) v).setText(R.string.noteedit_record_pause);
                }
                break;

            case R.id.record_stop://录音的结束
//			stopRecord();
                handleProgressDialog("show");
                mRecord.asynStop(7);
                break;
            case R.id.noteedit_speakinput://语音
                if (TNUtils.checkNetwork(this)) {
                    startSpeek();
                } else {
                    TNUtilsUi.showToast(R.string.alert_Net_NotWork);
                }

                break;
            case R.id.speek_start://语音重新开始
                if (speek_start.getText().toString().contains("正在听")) {
                    return;
                }
                if (mSpeek == null) {
                    mSpeek = new TNSpeek(this);
                    //重新连接回调
                    mSpeek.setCallBack(speekCallBack);
                }
                mSpeek.speekStart();
                break;
            case R.id.speek_stop://语音 关闭
                showToolbar("note");
                endSpeek();
                break;


        }
    }

    /**
     * 工具栏切换
     *
     * @param type
     */
    private void showToolbar(String type) {
        if (type.equals("record")) {
            ly_note.setVisibility(View.GONE);
            ly_record.setVisibility(View.VISIBLE);
            ly_speek.setVisibility(View.GONE);
        } else if (type.equals("speek")) {
            ly_note.setVisibility(View.GONE);
            ly_speek.setVisibility(View.VISIBLE);
            ly_record.setVisibility(View.GONE);
        } else if (type.equals("note")) {
            ly_record.setVisibility(View.GONE);
            ly_note.setVisibility(View.VISIBLE);
            ly_speek.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            backDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //================================回调+监听=================================

    @Override
    public void onPoPuMenuItemClick(int id) {
        switch (id) {
            case R.id.noteedit_picture: {//拍照
                String action;
                action = Intent.ACTION_PICK;
                Intent intent = new Intent(action);
                intent.setType("image/*");
                intent.putExtra("return-data", true);
                TNUtilsDialog.startIntentForResult(this, intent,
                        R.string.alert_NoteEdit_NoImage, R.id.noteedit_picture);
                break;
            }
            case R.id.noteedit_tag: {//输入标签
                saveInput();
                Bundle b = new Bundle();
                b.putString("TagStrForEdit", mNote.tagStr);
                startActForResult(TNTagListAct.class, b, R.id.noteedit_tag);
                break;
            }

            case R.id.noteedit_addatt://添加附件
                saveInput();
                startActForResult(TNFileListAct.class, null, R.id.noteedit_addatt);
                break;

            case R.id.noteedit_insertcurrenttime: {//插入当前时间
                if (mTitleView.isFocused()) {
                    if (mTitleView.getText().toString().length() > 75) {
                        TNUtilsUi.showToast("标题太长了，无法继续插入");
                        break;
                    }
                    insertCurrentTime(mTitleView);
                } else if (mContentView.isFocused()) {
                    insertCurrentTime(mContentView);
                } else {
                    mContentView.requestFocus();
                    insertCurrentTime(mContentView);
                }
                break;
            }

            case R.id.noteedit_folders: {//请选择文件夹 按钮
                saveInput();
                Bundle b = new Bundle();
                b.putLong("OriginalCatId", mNote.catId);
                b.putInt("Type", 2);
                startActForResult(TNCatListAct.class, b, R.id.noteedit_folders);
                break;
            }

            case R.id.noteedit_att_look://查看

                if (mCurrentAtt != null) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri contentUri = null;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//7.0+版本安全设置
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".FileProvider", new File(mCurrentAtt.path));
                    } else {//7.0-正常调用
                        contentUri = Uri.fromFile(new File(mCurrentAtt.path));
                    }

//                  intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                    intent.setDataAndType(contentUri, TNUtilsAtt.getMimeType(mCurrentAtt.type, mCurrentAtt.attName));

                    TNUtilsDialog.startIntent(this, intent,
                            R.string.alert_NoteView_CantOpenAttMsg);
                }
                break;

            case R.id.noteedit_att_delete://删除
                mNote.atts.remove(mCurrentAtt);
                String temp = String.format("<tn-media hash=\"%s\"></tn-media>", mCurrentAtt.digest);
                mNote.content = mNote.content.replaceAll(temp, "");
                mCurrentAtt = null;
                configView();
                break;

        }
    }

    /**
     * 笔记输入监听
     *
     * @param v
     * @param hasFocus
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == R.id.noteedit_input_title && !hasFocus) {
            String title = ((EditText) v).getText().toString();
            String trimTitle = title.trim();
            if (!title.equals(trimTitle)) {
                if (((EditText) v).getSelectionStart() > trimTitle.length()) {
                    ((EditText) v).setSelection(trimTitle.length());
                }
                ((EditText) v).setText(trimTitle);
            }
        } else if (v.getId() == R.id.noteedit_input_content && !hasFocus) {
            String content = ((EditText) v).getText().toString();
            String trimTitle = content.trim();
            if (!content.equals(trimTitle)) {
                if (((EditText) v).getSelectionStart() > trimTitle.length()) {
                    ((EditText) v).setSelection(trimTitle.length());
                }
                ((EditText) v).setText(trimTitle);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        String content = mContentView.getText().toString();
        ((TextView) findViewById(R.id.noteedit_wordcount)).setText(String
                .format(getString(R.string.noteedit_wordcount),
                        content.length()));

        if (mNote.noteLocalId > 0 && mNote.originalNote != null
                && !mNote.originalNote.isEditable()) {
            TNUtilsHtml.WhileTextViewChangeText(mNote, content);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        MLog.e(TAG, "str:" + s + " start:" + start + " count:" + count
                + " after:" + after);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        MLog.e(TAG, "str:" + s + " start:" + start + " before:" + before
                + " count:" + count);
    }

    /**
     * 界面返回处理
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK
                || (data == null && requestCode != R.id.noteedit_camera)) {
            if (mIsStartOtherAct) {
                toFinish();
            }
            if (getIntent().hasExtra("Target")) {
                toFinish();
            }
            return;
        }
        getIntent().removeExtra("Target");

        mIsStartOtherAct = false;
        if (requestCode == R.id.noteedit_camera) {
            addAtt(getPath(mCameraUri), false);
        } else if (requestCode == R.id.noteedit_picture) {
            addAtt(getPath(data.getData()), false);
        } else if (requestCode == R.id.noteedit_record) {
            addAtt(getPath(data.getData()), false);
        } else if (requestCode == R.id.noteedit_folders) {
            long catId = data.getLongExtra("SelectedCatId", 0);
            mNote.catId = catId;
        } else if (requestCode == R.id.noteedit_addatt) {
            addAtt(data.getStringExtra("SelectedFile"), false);
        } else if (requestCode == R.id.noteedit_tag) {
            mNote.tagStr = data.getStringExtra("EditedTagStr");
        } else if (requestCode == R.id.noteedit_doodle) {
            addAtt(data.getStringExtra("TuYa"), false);
        }
    }

    //==========================================handler======================================

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case AUTO_SAVE://2min自动保存
                //自动保存
                saveInput();
                if (mNote.isModified() && checkNote()) {
                    mNote.prepareToSave();
                    pNoteSave(mNote, false, false);
                }
                break;
            case 3:// 音量振幅
                mRecordAmplitudeProgress.setProgress(msg.arg1);
                break;
            case 4:// 计时
                mRecordTime.setText(formatTime(msg.arg1, msg.arg2));
                break;
            case 5://达到了设置的录音长度限制(20M)
                getCursorLocation();
                saveInput();
                addAtt(mRecord.getRecordTmpPath(), true);
                configView();
                showToolbar("note");
                mRecord = null;
                mRecordTime.setText(formatTime(0, 0));
                setCursorLocation();
                TNUtilsUi.alert(TNNoteEditAct.this, R.string.alert_NoteEdit_Record_Interrupt);
                break;
            case 6://空间不够(TNRecord发送的处理)
                TNUtilsUi.alert(TNNoteEditAct.this, R.string.alert_NoSDCard);
                showToolbar("note");
                break;
            case 7://异步停止录音
                handleProgressDialog("hide");
                getCursorLocation();
                saveInput();
                addAtt(mRecord.getRecordTmpPath(), true);
                configView();
                showToolbar("note");
                mRecord = null;
                mRecordTime.setText(formatTime(0, 0));
                setCursorLocation();
                break;
            case 8://保存笔记，异步停止录音
                handleProgressDialog("hide");
                addAtt(mRecord.getRecordTmpPath(), true);
                mRecord = null;
                saveNote();
                break;
            case 9://录音出错
                if (mRecord.getRecordTmpPath() == null) {
                    showToolbar("note");
                    mRecord = null;
                    mRecordTime.setText(formatTime(0, 0));
                    TNUtilsUi.showShortToast(R.string.alert_NoteEdit_Record_Error);
                    return;
                }
                getCursorLocation();
                saveInput();
                addAtt(mRecord.getRecordTmpPath(), true);
                configView();
                showToolbar("note");
                mRecord = null;
                mRecordTime.setText(formatTime(0, 0));
                setCursorLocation();
                TNUtilsUi.showShortToast(R.string.alert_NoteEdit_Record_Error);
                break;
            case SAVE_EXIT://保存并退出
                handleProgressDialog("hide");
                if (msg.obj == null) {
                    TNUtilsUi.showToast("存储空间不足");
                } else {
//                    TNUtilsUi.showShortToast(R.string.alert_NoteSave_SaveOK);//
                    mNote = (TNNote) msg.obj;
                    //更新已保存的笔记
                    getIntent().putExtra("NoteForEdit", mNote.noteLocalId);
                    initNote();

                    if (!mTitleView.hasFocus()) {
                        mTitleView.setText(mNote.title);
                    }
                }
                finish();
                break;
            case SAVE_LOCAL://自动保存
                handleProgressDialog("hide");
                if (msg.obj == null) {
                    TNUtilsUi.showToast("存储空间不足");
                } else {
//                    TNUtilsUi.showShortToast(R.string.alert_NoteSave_SaveOK);//
                    mNote = (TNNote) msg.obj;
                    //更新已保存的笔记
                    getIntent().putExtra("NoteForEdit", mNote.noteLocalId);
                    initNote();

                    if (!mTitleView.hasFocus()) {
                        mTitleView.setText(mNote.title);
                    }
                }
                break;
            case START_SYNC://保存并同步到后台
                handleProgressDialog("hide");
                TNUtilsUi.showShortToast(R.string.alert_NoteSave_SaveOK);
                if (msg.obj == null) {
                    TNUtilsUi.showToast("存储空间不足");
                } else {
                    //获取note
                    mNote = (TNNote) msg.obj;
                    if (MyRxManager.getInstance().isSyncing()) {
                        finish();
                        return;
                    }
                    TNUtilsUi.showNotification(this, R.string.alert_NoteView_Synchronizing, false);
                    syncEdit();
                    break;

                }
        }
        super.handleMessage(msg);
    }


    /**
     * ==================功能选择==================
     */
    private void startTargetAct(String target) {
        if (target == null) {
            return;
        }
        if (target.equals("camera")) {
            startCamera();
        } else if (target.equals("doodle")) {
            startActForResult(TNNoteDrawAct.class, null, R.id.noteedit_doodle);
        } else if (target.equals("record")) {
            getIntent().removeExtra("Target");
            startRecord();
        }
    }

    /**
     * ==================其他==================
     */
    private void setOtherBtnPopuMenu() {
        if (mPopuMenu != null) {
            mPopuMenu.dismiss();
        }
        mPopuMenu = new PoPuMenuView(this);
        mPopuMenu.addItem(R.id.noteedit_picture,
                getString(R.string.noteedit_popomenu_picture), mScale);
        if (!TNSettings.getInstance().isInProject()) {
            mPopuMenu.addItem(R.id.noteedit_tag,
                    getString(R.string.noteedit_popomenu_tag), mScale);
        }
        mPopuMenu.addItem(R.id.noteedit_addatt,
                getString(R.string.noteedit_popomenu_addatt), mScale);
        mPopuMenu.addItem(R.id.noteedit_insertcurrenttime,
                getString(R.string.noteedit_popomenu_insertcurrenttime), mScale);
        mPopuMenu.addItem(R.id.noteedit_folders,
                getString(R.string.noteedit_popomenu_folders), mScale);
        mPopuMenu.setOnPoPuMenuItemClickListener(this);
    }

    /**
     * ==============系统相机==============
     */
    private void startCamera() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ContentValues values = new ContentValues();
            values.put(Media.TITLE, "image");
            mCameraUri = getContentResolver().insert(
                    Media.EXTERNAL_CONTENT_URI, values);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);
            TNUtilsDialog.startIntentForResult(this, intent,
                    R.string.alert_NoteEdit_NoCamera, R.id.noteedit_camera);
        } catch (IllegalArgumentException e) {
            // 目前仅在1.6，HTC Magic发生过
            // getContentResolver().insert可能产生异常
            // java.lang.IllegalArgumentException: Unknown URL
            // content://media/external/images/media
        } catch (Exception e) {
        }
    }

    /**
     * ==============附件处理==============
     */
    private void setAttBtnPopuMenu() {
        if (mPopuMenu != null) {
            mPopuMenu.dismiss();
        }
        mPopuMenu = new PoPuMenuView(this);
        mPopuMenu.addItem(R.id.noteedit_att_look,
                getString(R.string.noteedit_popomenu_lookatt), mScale);
        mPopuMenu.addItem(R.id.noteedit_att_delete,
                getString(R.string.noteedit_popomenu_deleteatt), mScale);
        mPopuMenu.setOnPoPuMenuItemClickListener(this);
    }


    /**
     * ==============开始录音==============
     */
    private void startRecord() {
        if (mRecord == null)
            mRecord = new TNRecord(handler);

        showToolbar("record");
        mRecord.start();
        ((Button) findViewById(R.id.record_start)).setText(R.string.noteedit_record_pause);
    }

    /**
     * ==============开始语音==============
     */
    private void startSpeek() {
        if (mSpeek == null)
            mSpeek = new TNSpeek(this);
        //重新连接回调
        mSpeek.setCallBack(speekCallBack);
        showToolbar("speek");
        mSpeek.speekStart();
    }

    private void endSpeek() {
        mSpeek.setCallBack(null);
    }


    /**
     * 自定义语音转文字 回调
     */
    TNSpeek.SpeekCallBack speekCallBack = new TNSpeek.SpeekCallBack() {

        @Override
        public void onShowError() {
            speek_start.setText(R.string.noteedit_speek_error);
        }

        @Override
        public void onShowSpeeking() {
            speek_start.setText(R.string.noteedit_speeking);
        }

        @Override
        public void onShowRestart() {
            speek_start.setText(R.string.noteedit_speek_start);
        }

        @Override
        public void onShowImgChanged(int i) {
            if (i == 0 || i == 1 || i == 2) {
                speek_img.setBackground(ContextCompat.getDrawable(TNNoteEditAct.this, R.mipmap.recorder_player2));//1
            } else if (i == 3 || i == 4) {
                speek_img.setBackground(ContextCompat.getDrawable(TNNoteEditAct.this, R.mipmap.recorder_player3));//2
            } else if (i == 5 || i == 6) {
                speek_img.setBackground(ContextCompat.getDrawable(TNNoteEditAct.this, R.mipmap.recorder_player4));//3
            } else if (i == 7 || i == 8) {
                speek_img.setBackground(ContextCompat.getDrawable(TNNoteEditAct.this, R.mipmap.recorder_player5));//4
            } else if (i == 9 || i == 10) {
                speek_img.setBackground(ContextCompat.getDrawable(TNNoteEditAct.this, R.mipmap.recorder_player6));//5
            } else if (i == 9 || i == 10) {
                speek_img.setBackground(ContextCompat.getDrawable(TNNoteEditAct.this, R.mipmap.recorder_player7));//6
            } else if (i > 11) {
                speek_img.setBackground(ContextCompat.getDrawable(TNNoteEditAct.this, R.mipmap.recorder_player8));//7
            } else {
                speek_img.setBackground(ContextCompat.getDrawable(TNNoteEditAct.this, R.mipmap.recorder_player2)); //1
            }
        }

        @Override
        public void onResultBack(StringBuffer builder) {
            EditText currentText = null;
            if (mTitleView.isFocused()) {
                currentText = mTitleView;
            } else if (mContentView.isFocused()) {
                currentText = mContentView;
            }

            if (currentText != null) {
                int start = currentText.getSelectionStart();
                int end = currentText.getSelectionEnd();
                currentText.getText().replace(Math.min(start, end),
                        Math.max(start, end), builder);
                currentText.setSelection(Math.min(start, end) + builder.length(),
                        Math.min(start, end) + builder.length());
            }
        }
    };


    /**
     * 保存按钮/back/
     */
    private void saveNote() {
        if (checkNote()) {
            handleProgressDialog("show");
            mNote.prepareToSave();
            pNoteSave(mNote, false, true);//false保存到后台/true同时上传后台
        }
    }

    /**
     * 保存笔记
     */
    private void backDialog() {
        saveInput();
        if (!mNote.isModified() && (mRecord == null || mRecord.isStop())) {
            MLog.d("SJY", "保存到本地退出");
            toFinish();
            return;
        }
        CommonDialog dialog = new CommonDialog(this, R.string.alert_NoteEdit_SaveMsg,
                "保存",
                "不保存",
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        saveNote();
                    }

                    @Override
                    public void cancelBack() {
                        toFinish();
                    }

                });
        dialog.show();
    }


    /**
     * 保存笔记
     */
    private void saveInput() {
        String title = mTitleView.getText().toString().trim();
        if (!title.equals(mNote.title)) {
            mNote.title = title;
        }

        String content = mContentView.getText().toString();
        if (!content.equals(mNote.content)) {
            mNote.content = content;
        }

        if (mNote.title.length() == 0) {
            mNote.title = this.getString(R.string.noteedit_title);
        }

    }


    //================================方法处理=================================


    private void toFinish() {
        if (mRecord != null && !mRecord.isStop()) {
            mRecord.cancle();
        }

        if (mSpeek != null) {
            mSpeek.speekEnd();
            mSpeek = null;
        }
        finish();
    }


    private void setCursorLocation() {
        if (mTitleView.hasFocus()) {
            setSelectTion(mTitleView);
        } else if (mContentView.hasFocus()) {
            setSelectTion(mContentView);
        } else {
            if (mTitleView.getText().length() == 0) {
                mTitleView.requestFocus();
                mSelection = 0;
                setSelectTion(mTitleView);
            } else {
                mContentView.requestFocus();
                mSelection = mContentView.getText().length();
                setSelectTion(mContentView);
            }
        }
    }

    private void getCursorLocation() {
        if (mTitleView.hasFocus()) {
            mSelection = mTitleView.getSelectionStart();
        } else if (mContentView.hasFocus()) {
            mSelection = mContentView.getSelectionStart();
        }
    }

    private void setSelectTion(EditText editText) {
        try {
            if (mSelection < 0) {
                mSelection = editText.getText().length();
            }
            editText.setSelection(mSelection);
        } catch (Exception e) {
            mSelection = editText.getText().length();
            editText.setSelection(mSelection);

        }
    }

    /**
     * 插入当前时间
     *
     * @param et
     */
    private void insertCurrentTime(EditText et) {
        int index = et.getSelectionStart();
        String date = "【"
                + TNUtilsUi.formatHighPrecisionDate(this,
                System.currentTimeMillis()) + "】";
        StringBuffer sb = new StringBuffer(et.getText().toString());
        sb.insert(index, date);
        et.setText(sb.toString());
        Selection.setSelection(et.getText(), index + date.length());
    }

    /**
     * 添加附件
     *
     * @param path
     * @param delete
     */
    private void addAtt(final String path, boolean delete) {
        if (path == null) {
            return;
        }

        if (mNote.atts.size() > 200) {
            TNUtilsUi.alert(this, R.string.alert_Att_too_Much);
            return;
        }

        File file = new File(path);
        if (file.getName().indexOf(" ") > -1) {
            TNUtilsUi.alert(this, R.string.alert_Att_Name_FormatWrong);
            return;
        }
        if (file.length() <= 0) {
            TNUtilsUi.alert(this, R.string.alert_NoteEdit_AttSizeWrong);
        } else if (file.length() > TNConst.ATT_MAX_LENTH) {
            TNUtilsUi.alert(this, R.string.alert_NoteEdit_AttTooLong);
        } else {
            mNote.atts.add(TNNoteAtt.newAtt(file, this));
            if (delete)
                file.delete();
        }
    }

    /**
     * 附件
     *
     * @param attView
     * @param att
     */
    private void setAttView(ImageView attView, final TNNoteAtt att) {
        LayoutParams layoutParams = new LayoutParams(
                100, LayoutParams.WRAP_CONTENT);

        if (att.type > 10000 && att.type < 20000) {
            Bitmap thumbnail = TNUtilsAtt.makeThumbnailBitmap(att.path,
                    100, 73);
            if (thumbnail != null) {
                attView.setImageBitmap(thumbnail);
            } else {
                attView.setImageURI(Uri.parse(att.path));
            }
            layoutParams.setMargins((int) (2 * mScale), (int) (4 * mScale), (int) (2 * mScale), (int) (4 * mScale));
        } else if (att.type > 20000 && att.type < 30000)
            TNUtilsSkin.setImageViewDrawable(this, attView, R.drawable.ic_audio);
        else if (att.type == 40001)
            TNUtilsSkin.setImageViewDrawable(this, attView, R.drawable.ic_pdf);
        else if (att.type == 40002)
            TNUtilsSkin.setImageViewDrawable(this, attView, R.drawable.ic_txt);
        else if (att.type == 40003 || att.type == 40010)
            TNUtilsSkin.setImageViewDrawable(this, attView, R.drawable.ic_word);
        else if (att.type == 40005 || att.type == 40011)
            TNUtilsSkin.setImageViewDrawable(this, attView, R.drawable.ic_ppt);
        else if (att.type == 40009 || att.type == 40012)
            TNUtilsSkin.setImageViewDrawable(this, attView, R.drawable.ic_excel);
        else
            TNUtilsSkin.setImageViewDrawable(this, attView, R.drawable.ic_unknown);

        attView.setLayoutParams(layoutParams);

        attView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setAttBtnPopuMenu();
                mPopuMenu.show(v);
                mCurrentAtt = att;
            }
        });
    }

    private String formatTime(int minute, int secend) {
        String time = "";
        if (minute < 10) {
            time += "0";
        }
        time += String.valueOf(minute);
        time += ":";
        if (secend < 10) {
            time += "0";
        }
        time += String.valueOf(secend);
        return time;
    }


    private boolean checkNote() {
        int length = mNote.content.length();
        if (length > MAX_CONTENT_LEN) {
            TNUtilsUi.alert(this, R.string.alert_NoteEdit_ContentTooLong);
            return false;
        } else {
            return true;
        }
    }

    private String getPath(Uri uri) {
        try {
            String[] projection = {Media.DATA};

            Cursor cursor = managedQuery(uri, projection, null, null, null);
            if (cursor != null) {
                // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
                // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE
                // MEDIA
                int column_index = cursor
                        .getColumnIndexOrThrow(Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } else {
                return uri.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 2min自动保存笔记,不退出
     */
    private void startTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mTimerTask = null;
        }
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            public void run() {
                Message message = new Message();
                message.what = AUTO_SAVE;
                handler.sendMessage(message);
            }
        };
        mTimer.schedule(mTimerTask,
//                1000,
//                2000
                60 * 1000, //60 * 1000
                2 * 60 * 1000
        );//60 * 1000
    }

    private void handleProgressDialog(String type) {
        try {
            if (type.equals("show")) {
                if (mProgressDialog == null) {
                    mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
                }
                mProgressDialog.show();
            } else if (type.equals("hide")) {
                if (mProgressDialog != null) {
                    mProgressDialog.hide();
                }
            } else if (type.equals("dismiss")) {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //    同步结束
    private void endSynchronize() {
        try {
            handleProgressDialog("hide");
            TNNoteEditAct.this.finish();
        } catch (Exception e) {
            TNNoteEditAct.this.finish();
        }

    }

    //==========================================数据库操作======================================

    /**
     * 数据库操作 线程操作
     * 自动保存，不上传后台，完成按钮保存，上传后台
     *
     * @param note
     * @param isNeedSync
     * @param isNeedFinish isNeedSync为false情况下才可用
     */
    private void pNoteSave(final TNNote note, final boolean isNeedSync, final boolean isNeedFinish) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();

                if (note.title.length() <= 0) {
                    note.resetTitle();
                }

                if (note.catId == -1) {
                    note.catId = TNSettings.getInstance().defaultCatId;
                }

                note.lastUpdate = (int) (System.currentTimeMillis() / 1000);

                TNDb.beginTransaction();
                try {
                    if (note.noteLocalId < 0) {
                        // insert
                        note.createTime = (int) (System.currentTimeMillis() / 1000);
                        long id = TNDb.getInstance().insertSQL(TNSQLString.NOTE_INSERT//19个参数
                                , new Object[]{note.title,
                                        TNSettings.getInstance().userId,
                                        note.catId,
                                        note.trash,
                                        note.content,
                                        note.source,
                                        note.createTime,
                                        note.lastUpdate,
                                        3,
                                        -1,
                                        note.shortContent,
                                        note.tagStr,
                                        note.lbsLongitude,
                                        note.lbsLatitude,
                                        note.lbsRadius,
                                        note.lbsAddress,
                                        TNSettings.getInstance().username,
                                        note.thumbnail,
                                        note.contentDigest});//19
                        note.noteLocalId = id;

                        MLog.d("保存新笔记id=" + id);
                    } else {
                        MLog.d("更新笔记");
                        // update
                        note.syncState = note.noteId != -1 ? 4 : 3;
                        TNDb.getInstance().execSQL(TNSQLString.NOTE_LOCAL_UPDATE,
                                note.title,
                                note.catId,
                                note.content,
                                note.createTime,
                                note.lastUpdate,
                                note.shortContent,
                                note.tagStr,
                                note.contentDigest,
                                note.syncState,
                                note.noteLocalId);
                    }

                    // save att/(文件 图片)的保存
                    TNNote note2 = attLocalSave(note);

                    if (note2 == null) {
                        //如果null,则存储空间不足
                        msg.obj = note2;
                    } else {
                        msg.obj = note2;
                        TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note2.catId);
                        MLog.d(TAG, "保存的内容：" + note2.toString());

                    }
                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }

                //执行完异步，移除what=AUTO_SAVE的消息队列
                handler.removeMessages(AUTO_SAVE);
                //通知更新UI
                if (isNeedSync) {//结束编辑退出，同步到后台
                    msg.what = START_SYNC;
                    handler.sendMessage(msg);
                } else {//编辑下，自动保存到本地
                    if (isNeedFinish) {
                        msg.what = SAVE_EXIT;
                        handler.sendMessage(msg);
                    } else {
                        msg.what = SAVE_LOCAL;
                        handler.sendMessage(msg);
                    }
                }
            }
        });
    }

    /**
     * 附件保存到本地
     * save attr
     *
     * @param tnNote
     * @return
     */
    private TNNote attLocalSave(TNNote tnNote) {
        TNNote note = tnNote;
        Vector<TNNoteAtt> newAtts = note.atts;
        Vector<TNNoteAtt> exsitAtts = TNDbUtils.getAttrsByNoteLocalId(note.noteLocalId);
        try {

            if (exsitAtts.size() != 0) {//有图
                MLog.d(TAG, "save attr", "exsitAtts.size()=" + exsitAtts.size() + "--有图");

                //循环判断是否与本地同步，新增没有就删除本地
                for (int k = 0; k < exsitAtts.size(); k++) {
                    boolean exsit = false;
                    TNNoteAtt tempLocalAtt = exsitAtts.get(k);
                    for (int i = 0; i < newAtts.size(); i++) {
                        long attLocalId = newAtts.get(i).attLocalId;
                        if (tempLocalAtt.attLocalId == attLocalId) {
                            exsit = true;
                        }
                    }
                    if (!exsit) {
                        NoteAttrDbHelper.deleteAttByAttLocalId(tempLocalAtt.attLocalId);
                    }
                }
                //循环判断是否与新增同步，本地没有就插入数据
                for (int k = 0; k < newAtts.size(); k++) {
                    TNNoteAtt att = newAtts.get(k);
                    if (att.attLocalId == -1) {
                        //保存图片
                        long attLocalId = TNDb.getInstance().insertSQL(TNSQLString.ATT_INSERT, new Object[]{att.attName,
                                att.type,
                                att.path,
                                note.noteLocalId,
                                att.size,
                                0,
                                TNUtilsAtt.fileToMd5(att.path),
                                att.attId,
                                att.width,
                                att.height});
                        MLog.d(TAG, "attLocalSave--attLocalId=" + attLocalId);
                        // copy file to path
                        String tPath = TNUtilsAtt.getAttPath(attLocalId, att.type);
                        //结束 save attr 直接返回
                        if (tPath == null) {
                            return null;//"存储空间不足"
                        }

                        //tPath = tPath + TNUtilsAtt.getAttSuffix(att.type);
                        TNUtilsAtt.copyFile(att.path, tPath);
                        TNUtilsAtt.recursionDeleteDir(new File(att.path));
                        MLog.d(TAG, "save attr", att.path + " >> " + tPath + "(" + att.digest + ")");

                        //本地笔记保存文件路径
                        TNDb.getInstance().execSQL(TNSQLString.ATT_PATH, tPath, attLocalId);
                        note.atts.get(k).attLocalId = attLocalId;
                    }
                }
            } else {//无图

                MLog.d(TAG, "save attr", "exsitAtts.size()=0--无图");
                for (int i = 0; i < note.atts.size(); i++) {
                    TNNoteAtt att = note.atts.get(i);
                    // insert
                    long attLocalId = TNDb.getInstance().insertSQL(TNSQLString.ATT_INSERT
                            , new Object[]{att.attName,
                                    att.type,
                                    att.path,
                                    note.noteLocalId,
                                    att.size,
                                    3,
                                    TNUtilsAtt.fileToMd5(att.path),
                                    att.attId,
                                    att.width,
                                    att.height});

                    note.atts.get(i).attLocalId = attLocalId;
                }
            }

            //如果笔记的第一个附件是图片，则设置笔记的缩略图
            Vector<TNNoteAtt> noteAttrs = TNDbUtils.getAttrsByNoteLocalId(note.noteLocalId);
            if (noteAttrs.size() > 0) {//有图
                MLog.d(TAG, "save attr 第一个附件是图片");
                TNNoteAtt temp = noteAttrs.get(0);
                if (temp.type > 10000 && temp.type < 20000) {
                    TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, temp.path, note.noteLocalId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return note;
    }

    //========================================p层调用========================================

    private void syncEdit() {
        handleProgressDialog("show");
        syncPresenter.synchronizeData("EDIT");
    }
    //========================================回调========================================

    @Override
    public void onSyncEditSuccess() {
        endSynchronize();
    }

    @Override
    public void onSyncFailed(Exception e, String msg) {
        endSynchronize();
    }

    //接口回调不用
    @Override
    public void onSyncSuccess(String obj) {

    }


}
