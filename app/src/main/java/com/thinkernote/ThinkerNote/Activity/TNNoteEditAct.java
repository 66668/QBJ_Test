package com.thinkernote.ThinkerNote.Activity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.iflytek.speech.RecognizerResult;
import com.iflytek.speech.SpeechConfig.RATE;
import com.iflytek.speech.SpeechError;
import com.iflytek.ui.RecognizerDialog;
import com.iflytek.ui.RecognizerDialogListener;
import com.thinkernote.ThinkerNote.BuildConfig;
import com.thinkernote.ThinkerNote.DBHelper.NoteAttrDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.NoteDbHelper;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNRecord;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.General.TNUtilsDialog;
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.Other.PoPuMenuView;
import com.thinkernote.ThinkerNote.Other.PoPuMenuView.OnPoPuMenuItemClickListener;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Service.LocationService;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.presenter.NoteEditPresenterImpl;
import com.thinkernote.ThinkerNote._interface.p.INoteEditPresenter;
import com.thinkernote.ThinkerNote._interface.v.OnNoteEditListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.GetNoteByNoteIdBean;
import com.thinkernote.ThinkerNote.bean.main.OldNoteAddBean;
import com.thinkernote.ThinkerNote.bean.main.OldNotePicBean;

import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 主页--写笔记界面 sjy 0626
 */
public class TNNoteEditAct extends TNActBase implements OnClickListener,
        OnFocusChangeListener, TextWatcher, RecognizerDialogListener,
        OnPoPuMenuItemClickListener, OnNoteEditListener {
    //正常登录的同步常量
    public static final int DELETE_LOCALNOTE = 101;//1
    public static final int DELETE_REALNOTE = 104;//
    public static final int DELETE_REALNOTE2 = 106;//
    public static final int UPDATA_EDITNOTES = 107;//

    public static final int UI_HANDLER_2_11 = 108;//
    public static final int CHILD_HANDLER_2_11 = 109;//

    private static final int SAVE_OVER = 102;
    private static final int START_SYNC = 103;
    private static final int MAX_CONTENT_LEN = 4 * 100 * 1024;

    private TNNote mNote = null;
    private Uri mCameraUri = null;
    private boolean mIsStartOtherAct = false;
    private RecognizerDialog mIatDialog;
    private int mSelection = -1;
    private EditText mTitleView;
    private EditText mContentView;
    private PoPuMenuView mPopuMenu;
    private LinearLayout mAttsLayout;
    private ProgressDialog mProgressDialog = null;
    private TNNoteAtt mCurrentAtt;
    private float mScale;

    private TNRecord mRecord;
    private TextView mRecordTime;
    private ProgressBar mRecordAmplitudeProgress;

    private Timer mTimer;
    private TimerTask mTimerTask;

    private TNSettings mSettings = TNSettings.getInstance();
    //p
    INoteEditPresenter presener;

    //
    private Vector<TNNote> addNewNotes;//（2-5）正常同步，第5个调用数据
//    private Vector<TNNoteAtt> newNotesAtts;//（2-5）正常同步，第5个调用数据中第一调用的数据 不可使用全局，易错

    private Vector<TNNote> recoveryNotes;//(2-7)正常同步，第7个调用数据
//    Vector<TNNoteAtt> recoveryNotesAtts;//(2-7)正常同步，第7个调用数据中第一调用的数据 不可使用全局，易错

    Vector<TNNote> deleteNotes;//(2-8)正常同步，第8个调用数据
    Vector<TNNote> deleteRealNotes;//(2-9)正常同步，第9个调用数据
    Vector<TNNote> allNotes;//(2-10)正常同步，第10个调用数据
    Vector<TNNote> editNotes;//(2-11)正常同步，第11个调用数据
    //接口返回数据
    private List<List<AllFolderItemBean>> mapList = new ArrayList<>();//递归调用使用的数据集合，size最大是5；//后台需求
    private List<AllNotesIdsBean.NoteIdItemBean> cloudIds;//2-10接口返回

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit);
        initAct();
        presener = new NoteEditPresenterImpl(this, this);
        //开启百度定位
        if (savedInstanceState == null) {
//            TNLBSService.getInstance().startLocation();
            LocationService.getInstance().start();
        }
        if (savedInstanceState == null) {
            initNote();
        } else {
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
        mContentView.requestFocus();

        findViewById(R.id.noteedit_save).setOnClickListener(this);
        findViewById(R.id.noteedit_camera).setOnClickListener(this);
        findViewById(R.id.noteedit_doodle).setOnClickListener(this);
        findViewById(R.id.noteedit_other).setOnClickListener(this);
        findViewById(R.id.noteedit_record).setOnClickListener(this);
        findViewById(R.id.noteedit_speakinput).setOnClickListener(this);
        findViewById(R.id.record_start).setOnClickListener(this);
        findViewById(R.id.record_stop).setOnClickListener(this);

        mTitleView.setOnFocusChangeListener(this);
        mContentView.addTextChangedListener(this);

    }

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
        if (mNote == null) {
            finish();
            return;
        }

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
        mTitleView.setText(mTitleView.getHint().equals(mNote.title) ? ""
                : mNote.title);
        mContentView.setText(mNote.content);
        mContentView.setSelection(mNote.content.length());
    }

    private void startTargetAct(String target) {
        if (target == null) {
            return;
        }
        if (target.equals("camera")) {
            startCamera();
        } else if (target.equals("doodle")) {
            startActForResult(TNTuYaAct.class, null, R.id.noteedit_doodle);// TODO
        } else if (target.equals("record")) {
            getIntent().removeExtra("Target");
            startRecord();
        }
    }

    @Override
    public void onDestroy() {
        try {
            mTimer.cancel();
//            TNLBSService.getInstance().stopLocation();
            LocationService.getInstance().stop();
        } catch (Exception e) {
        }
        handleProgressDialog("dismiss");

        if (mIatDialog != null)
            mIatDialog.dismiss();

        super.onDestroy();
    }

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.noteedit_save: {//保存
                TNUtilsUi.hideKeyboard(this, R.id.noteedit_save);
                saveInput();
                if (mRecord != null && !mRecord.isStop()) {
                    handleProgressDialog("show");
                    mRecord.asynStop(8);
                    break;
                }
                saveNote();
                break;
            }

            case R.id.noteedit_other://其他功能
                setOtherBtnPopuMenu();
                mPopuMenu.show(v);
                break;
            case R.id.noteedit_record: {//录音
                startRecord();
                break;
            }
            case R.id.noteedit_doodle: {//涂鸦
                startActForResult(TNTuYaAct.class, null, R.id.noteedit_doodle);
                break;
            }
            case R.id.noteedit_camera://相机
                startCamera();
                break;
            case R.id.noteedit_speakinput://语音
                showIatDialog();
                break;

            case R.id.record_start://录音的开始/暂停
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
        }
    }

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

    private void startRecord() {
        if (mRecord == null)
            mRecord = new TNRecord(handler);

        showToolbar("record");
        mRecord.start();
        ((Button) findViewById(R.id.record_start)).setText(R.string.noteedit_record_pause);
    }

    private void showToolbar(String type) {
        if (type.equals("record")) {
            findViewById(R.id.noteedit_toolbar_layout).setVisibility(View.GONE);
            findViewById(R.id.noteedit_record_layout).setVisibility(
                    View.VISIBLE);
        } else if (type.equals("play")) {

        } else {
            findViewById(R.id.noteedit_toolbar_layout).setVisibility(
                    View.VISIBLE);
            findViewById(R.id.noteedit_record_layout).setVisibility(View.GONE);
        }
    }

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
     * 其他
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

    private void initContentView() {
        int attViewHeight = 0;
        if (mNote.atts != null && mNote.atts.size() > 0) {
            attViewHeight = 85 + 38;
        }
        mContentView.setMinLines((getWindowManager().getDefaultDisplay()
                .getHeight() - TNUtils.dipToPx(this, 90) - attViewHeight) / mContentView.getLineHeight());
    }

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

    private void setAttView(ImageView attView, final TNNoteAtt att) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
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

    public void toFinish() {
        finish();
    }

    private void showIatDialog() {
        if (TNUtils.checkNetwork(this)) {
            if (mIatDialog == null) {
                mIatDialog = new RecognizerDialog(this, "appid=4ea04eee");
                mIatDialog.setEngine("sms", null, null);
                mIatDialog.setSampleRate(RATE.rate16k);
                mIatDialog.setListener(this);
            }
            mIatDialog.show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

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

    @Override
    public void onEnd(SpeechError error) {
        MLog.i(TAG, "iat onEnd:" + error);
        if (error != null) {
            TNUtilsUi.showToast(error.toString());
        }
    }

    @Override
    public void onResults(ArrayList<RecognizerResult> results, boolean islast) {
        StringBuilder builder = new StringBuilder();
        for (RecognizerResult recognizerResult : results) {
            builder.append(recognizerResult.text);
        }

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

    private void back() {
        saveInput();
        if (!mNote.isModified() && (mRecord == null || mRecord.isStop())) {
            toFinish();
            return;
        }
        DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveNote();
            }
        };
        DialogInterface.OnClickListener nbtn_Click = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mRecord != null && !mRecord.isStop()) {
                    mRecord.cancle();
                }
                toFinish();
            }
        };
        JSONObject jsonData = TNUtils.makeJSON("CONTEXT", this, "TITLE",
                R.string.alert_Title, "MESSAGE",
                R.string.alert_NoteEdit_SaveMsg, "POS_BTN",
                R.string.alert_Save, "POS_BTN_CLICK", pbtn_Click, "NEU_BTN",
                R.string.alert_NoSave, "NEU_BTN_CLICK", nbtn_Click, "NEG_BTN",
                R.string.alert_Cancel);
        TNUtilsUi.alertDialogBuilder(jsonData).show();
    }

    /**
     * 保存按钮/back/
     */
    private void saveNote() {
        if (checkNote()) {
            handleProgressDialog("show");
            mNote.prepareToSave();
            pNoteSave(mNote, false);//true false
        }
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
            String[] projection = {MediaStore.Images.Media.DATA};

            Cursor cursor = managedQuery(uri, projection, null, null, null);
            if (cursor != null) {
                // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
                // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE
                // MEDIA
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
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
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        mTimer.schedule(mTimerTask, 60 * 1000, 60 * 1000);
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


    //==========================================handler======================================

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                saveInput();
                if (mNote.isModified() && checkNote()) {
                    mNote.prepareToSave();
                    //异步执行
                    pNoteSave(mNote, false);
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
            case 6://空间不够
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
            case SAVE_OVER://只保存，不同步
                MLog.d("saveNote", "保存但不同步");
                handleProgressDialog("hide");
                if (msg.obj == null) {
                    TNUtilsUi.showToast("存储空间不足");
                } else {
                    TNUtilsUi.showShortToast(R.string.alert_NoteSave_SaveOK);
                    mNote = (TNNote) msg.obj;
                    getIntent().putExtra("NoteForEdit", mNote.noteLocalId);
                    initNote();
                    if (!mTitleView.hasFocus()) {
                        mTitleView.setText(mNote.title);
                    }
                }
                finish();
                break;
            case START_SYNC://保存并同步
                MLog.d("saveNote", "保存并同步");

                handleProgressDialog("hide");
                TNUtilsUi.showShortToast(R.string.alert_NoteSave_SaveOK);
                if (msg.obj == null) {
                    TNUtilsUi.showToast("存储空间不足");
                } else {
                    //获取note
                    mNote = (TNNote) msg.obj;

                    if (TNActionUtils.isSynchronizing()) {
                        finish();
                        return;
                    }
                    TNUtilsUi.showNotification(this, R.string.alert_NoteView_Synchronizing, false);
                    //
                    MLog.d("saveNote", "打印保存内容：" + mNote.toString());
                    for (TNNoteAtt att : mNote.atts) {
                        MLog.e("遍历打印存储信息TNNoteAtt:" + att.toString());
                    }

                    syncEdit();
                    break;

                }
            case DELETE_LOCALNOTE://2-8-2的调用
                //执行下一个position/执行下一个接口
                pDelete(((int) msg.obj + 1));
                break;

            case DELETE_REALNOTE://2-9 deleteRealNotes
                //执行下一个position/执行下一个接口
                pRealDelete(((int) msg.obj + 1));
                break;
            case DELETE_REALNOTE2://2-9 deleteRealNotes
                //执行下一个position/执行下一个接口

                if (isRealDelete1 && isRealDelete2) {
                    //执行下一个
                    pRealDelete((int) msg.obj + 1);

                    //复原 false,供下次循环使用
                    isRealDelete1 = false;
                    isRealDelete2 = false;
                }

                break;
            case UPDATA_EDITNOTES://2-11 更新日记时间返回
                //执行下一个position/执行下一个接口
                pEditNotePic((int) msg.obj + 1);
                break;
            case UI_HANDLER_2_11:
                Bundle bundle = msg.getData();
                int position = bundle.getInt("int");
                boolean is13 = bundle.getBoolean("boolean");

                //执行一个position或下一个接口
                MLog.d("sync----2-11-2-->Success--pUpdataNote");
                pUpdataNote(position + 1, false);

                break;
        }
        super.handleMessage(msg);
    }

    HandlerThread handlerThread2_11 = new HandlerThread("main_sjy2-11");
    Handler chlidHanlder2_11;

    private void endSynchronize() {
        mProgressDialog.hide();
        MLog.d("同步edit--同步结束");
        finish();
    }

    //==========================================数据库操作======================================

    /**
     * 数据库操作 线程操作
     *
     * @param note
     */
    private void pNoteSave(final TNNote note, final boolean isNeedSync) {
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
                        MLog.e("saveNote:", "insert", "note.noteLocalId < 0", "具体内容：" + note.toString());
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

                        MLog.d("saveNote:", "insert", "note.noteLocalId < 0", "note.noteLocalId=" + id);
                    } else {
                        MLog.d("saveNote:", " update", "note.noteLocalId >=0");
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
                        MLog.d("saveNote", "保存的内容：" + note2.toString());

                    }
                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }

                //执行完异步，移除what=1的消息队列
                handler.removeMessages(1);
                //通知更新UI
                if (isNeedSync) {
                    msg.what = START_SYNC;
                    handler.sendMessage(msg);
                } else {
                    msg.what = SAVE_OVER;
                    handler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * 子线程的方法
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
                MLog.d("saveNote", "save attr", "exsitAtts.size()=" + exsitAtts.size() + "--有图");

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
                        MLog.d("saveNote", "attLocalSave--attLocalId=" + attLocalId);
                        // copy file to path
                        String tPath = TNUtilsAtt.getAttPath(attLocalId, att.type);
                        //结束 save attr 直接返回
                        if (tPath == null) {
                            return null;//"存储空间不足"
                        }

                        //tPath = tPath + TNUtilsAtt.getAttSuffix(att.type);
                        TNUtilsAtt.copyFile(att.path, tPath);
                        TNUtilsAtt.recursionDeleteDir(new File(att.path));
                        MLog.d("saveNote", "save attr", att.path + " >> " + tPath + "(" + att.digest + ")");

                        //本地笔记保存文件路径
                        TNDb.getInstance().execSQL(TNSQLString.ATT_PATH, tPath, attLocalId);
                        note.atts.get(k).attLocalId = attLocalId;
                    }
                }
            } else {//无图

                MLog.d("saveNote", "save attr", "exsitAtts.size()=0--无图");
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
                MLog.d("saveNote", "save attr 第一个附件是图片");
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

    /**
     * 调用recovery接口(2-7-1)，就触发更新db
     */
    private void recoveryNoteSQL(long noteId) {

        TNNote note = TNDbUtils.getNoteByNoteId(noteId);
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().execSQL(TNSQLString.NOTE_SET_TRASH, 0, 2, System.currentTimeMillis() / 1000, note.noteLocalId);
            TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);

            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }
    }

    private void updataDeleteNoteSQL(long noteId) {

        TNNote note = TNDbUtils.getNoteByNoteId(noteId);
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().execSQL(TNSQLString.NOTE_SET_TRASH, 2, 1, System.currentTimeMillis() / 1000, note.noteLocalId);
            TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);

            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }
    }

    /**
     * 2-9-2接口
     */
    private void deleteRealSQL(final long nonteLocalID, final int position) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                TNDb.beginTransaction();
                try {
                    TNNote note = TNDbUtils.getNoteByNoteId(nonteLocalID);
                    //
                    TNDb.getInstance().deleteSQL(TNSQLString.NOTE_DELETE_BY_NOTEID, new Object[]{nonteLocalID});

                    TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);

                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }

                //
                Message msg = Message.obtain();
                msg.obj = position;
                msg.what = DELETE_REALNOTE2;
                handler.sendMessage(msg);
            }
        });
    }

    /**
     * 2-10-1 更新日记状态-->1
     *
     * @param noteId
     */
    private void updataEditNotesState(final int position, final long noteId) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                TNDb.beginTransaction();
                try {
                    //
                    TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE,
                            1,
                            noteId);
                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }
                //
                Message msg = Message.obtain();
                msg.obj = position;
                msg.what = UPDATA_EDITNOTES;
                handler.sendMessage(msg);
            }
        });

    }

    /**
     * 2-11-1 更新日记时间 （接口返回处理）
     */
    private void updataEditNotes(final int position, final TNNote note) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String shortContent = TNUtils.getBriefContent(note.content);
                TNDb.beginTransaction();
                try {
                    //
                    TNDb.getInstance().execSQL(TNSQLString.NOTE_SHORT_CONTENT, shortContent, note.noteId);
                    //
                    TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);

                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }
                //下一个position
                //
                Message msg = Message.obtain();
                msg.obj = position;
                msg.what = UPDATA_EDITNOTES;
                handler.sendMessage(msg);
            }
        });

    }

    /**
     * 处理2-11-2接口数据 使用HandlerThread处理耗时操作
     *
     * @param bean
     * @param position
     * @param is13
     */
    private void handleNote(GetNoteByNoteIdBean bean, final int position, final boolean is13) {

        //触发 异步handler,执行耗时操作
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", bean);
        bundle.putBoolean("boolean", is13);
        bundle.putInt("int", position);
        //向chlidHanlder2_11发送msg
        Message msg = new Message();
        msg.setData(bundle);
        msg.what = CHILD_HANDLER_2_11;//传递给异步handler耗时处理
        chlidHanlder2_11.sendMessage(msg);

    }

    private void setChildHandler2_11(int position) {
        if (position == 0 && !handlerThread2_11.isAlive()) {
            //开启handlerThread线程
            handlerThread2_11.start();
            //构建异步handler
            chlidHanlder2_11 = new Handler(handlerThread2_11.getLooper(), new Handler.Callback() {

                @Override
                public boolean handleMessage(Message msg) {
                    switch (msg.what) {
                        case CHILD_HANDLER_2_11://处理1-4接口数据
                            //获取数据
                            Bundle bundle = msg.getData();
                            int position = bundle.getInt("int");
                            boolean is13 = bundle.getBoolean("boolean");
                            GetNoteByNoteIdBean myBean = (GetNoteByNoteIdBean) bundle.getSerializable("bean");
                            //耗时操作
                            updateNote(myBean);

                            //操作完成，返回UI
                            Bundle UIBundle = new Bundle();
                            UIBundle.putInt("int", position);
                            UIBundle.putBoolean("boolean", is13);
                            Message message = Message.obtain();
                            message.setData(bundle);
                            message.what = UI_HANDLER_2_11;
                            handler.sendMessage(message);
                            break;
                    }

                    return false;
                }
            });
        }
    }

    //2-11-2
    public static void updateNote(GetNoteByNoteIdBean bean) {
        //
        System.gc();
        //
        try {
            long noteId = bean.getId();
            String contentDigest = bean.getContent_digest();
            TNNote note = TNDbUtils.getNoteByNoteId(noteId);//在全部笔记页同步，会走这里，没在首页同步过的返回为null

            int syncState = note == null ? 1 : note.syncState;
            List<GetNoteByNoteIdBean.TagBean> tags = bean.getTags();

            String tagStr = "";
            for (int k = 0; k < tags.size(); k++) {
                GetNoteByNoteIdBean.TagBean tempTag = tags.get(k);
                String tag = tempTag.getName();
                if ("".equals(tag)) {
                    continue;
                }
                if (tags.size() == 1) {
                    tagStr = tag;
                } else {
                    if (k == (tags.size() - 1)) {
                        tagStr = tagStr + tag;
                    } else {
                        tagStr = tagStr + tag + ",";
                    }
                }
            }

            String thumbnail = "";
            if (note != null) {
                thumbnail = note.thumbnail;
                Vector<TNNoteAtt> localAtts = TNDbUtils.getAttrsByNoteLocalId(note.noteLocalId);
                List<GetNoteByNoteIdBean.Attachments> atts = bean.getAttachments();
                if (localAtts.size() != 0) {
                    //循环判断是否与线上同步，线上没有就删除本地
                    for (int k = 0; k < localAtts.size(); k++) {
                        boolean exit = false;
                        TNNoteAtt tempLocalAtt = localAtts.get(k);
                        for (int i = 0; i < atts.size(); i++) {
                            GetNoteByNoteIdBean.Attachments tempAtt = atts.get(i);
                            long attId = tempAtt.getId();
                            if (tempLocalAtt.attId == attId) {
                                exit = true;
                            }
                        }
                        if (!exit) {
                            if (thumbnail.indexOf(String.valueOf(tempLocalAtt.attId)) != 0) {
                                thumbnail = "";
                            }
                            NoteAttrDbHelper.deleteAttById(tempLocalAtt.attId);
                        }
                    }
                    //循环判断是否与线上同步，本地没有就插入数据
                    for (int k = 0; k < atts.size(); k++) {
                        GetNoteByNoteIdBean.Attachments tempAtt = atts.get(k);
                        long attId = tempAtt.getId();
                        boolean exit = false;
                        for (int i = 0; i < localAtts.size(); i++) {
                            TNNoteAtt tempLocalAtt = localAtts.get(i);
                            if (tempLocalAtt.attId == attId) {
                                exit = true;
                            }
                        }
                        if (!exit) {
                            syncState = 1;
                            insertAttr(tempAtt, note.noteLocalId);
                        }
                    }
                } else {
                    for (int i = 0; i < atts.size(); i++) {
                        GetNoteByNoteIdBean.Attachments tempAtt = atts.get(i);
                        syncState = 1;
                        insertAttr(tempAtt, note.noteLocalId);
                    }
                }

                //如果本地的更新时间晚就以本地的为准
                if (note.lastUpdate > (com.thinkernote.ThinkerNote.Utils.TimeUtils.getMillsOfDate(bean.getUpdate_at()) / 1000)) {
                    return;
                }

                if (atts.size() == 0) {
                    syncState = 2;
                }
            }

            int catId = -1;
            if (bean.getFolder_id() > 0) {
                catId = bean.getFolder_id();
            }

            JSONObject tempObj = TNUtils.makeJSON(
                    "title", bean.getTitle(),
                    "userId", TNSettings.getInstance().userId,
                    "trash", bean.getTrash(),
                    "source", "android",
                    "catId", catId,
                    "content", TNUtilsHtml.codeHtmlContent(bean.getContent(), true),
                    "createTime", com.thinkernote.ThinkerNote.Utils.TimeUtils.getMillsOfDate(bean.getCreate_at()) / 1000,
                    "lastUpdate", com.thinkernote.ThinkerNote.Utils.TimeUtils.getMillsOfDate(bean.getUpdate_at()) / 1000,
                    "syncState", syncState,
                    "noteId", noteId,
                    "shortContent", TNUtils.getBriefContent(bean.getContent()),
                    "tagStr", tagStr,
                    "lbsLongitude", bean.getLongitude() <= 0 ? 0 : bean.getLongitude(),
                    "lbsLatitude", bean.getLatitude() <= 0 ? 0 : bean.getLatitude(),
                    "lbsRadius", bean.getRadius() <= 0 ? 0 : bean.getRadius(),
                    "lbsAddress", bean.getAddress(),
                    "nickName", TNSettings.getInstance().username,
                    "thumbnail", thumbnail,
                    "contentDigest", contentDigest
            );
            if (note == null)
                NoteDbHelper.addOrUpdateNote(tempObj);
            else
                NoteDbHelper.updateNote(tempObj);
        } catch (Exception e) {
            MLog.e("操作有异常：" + e.toString());
            //该异常只在TNMainAct的2-11-2的数据处理函数使用，这里只使用try catch 不处理即可 sjy
            MLog.e("TNNoteEditAct--updateNote:" + e.toString());
        }
    }

    public static void insertAttr(GetNoteByNoteIdBean.Attachments tempAtt, long noteLocalId) {
        long attId = tempAtt.getId();
        String digest = tempAtt.getDigest();
        //
        TNNoteAtt noteAtt = TNDbUtils.getAttrById(attId);
        noteAtt.attName = tempAtt.getName();
        noteAtt.type = tempAtt.getType();
        noteAtt.size = tempAtt.getSize();
        noteAtt.syncState = 1;

        JSONObject tempObj = TNUtils.makeJSON(
                "attName", noteAtt.attName,
                "type", noteAtt.type,
                "path", noteAtt.path,
                "noteLocalId", noteLocalId,
                "size", noteAtt.size,
                "syncState", noteAtt.syncState,
                "digest", digest,
                "attId", attId,
                "width", noteAtt.width,
                "height", noteAtt.height
        );
        NoteAttrDbHelper.addOrUpdateAttr(tempObj);
    }

    /**
     * 调用图片上传，就触发更新db
     *
     * @param attrId
     */
    private void upDataAttIdSQL(final long attrId, final TNNoteAtt tnNoteAtt) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                TNDb.beginTransaction();
                try {
                    TNDb.getInstance().execSQL(TNSQLString.ATT_UPDATE_SYNCSTATE_ATTID, 2, attrId, (int) tnNoteAtt.noteLocalId);
                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }
            }
        });
    }

    /**
     * 调用NoteAdd接口，就触发更新db
     */
    private void upDataNoteLocalIdSQL(OldNoteAddBean oldNoteAddBean, TNNote note) {
        long id = oldNoteAddBean.getId();
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_NOTEID_BY_NOTELOCALID, id, note.noteLocalId);
            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }
    }

    //========================================p层调用 ========================================

    private void syncEdit() {
        mProgressDialog.show();
        pAddNewNote();
    }

    /**
     * (二.5+二.6)正常同步 pAddNewNote
     * 说明：同(二.2+二.3)的执行顺序，先处理notepos的图片，处理完就上传notepos的文本，然后再处理notepos+1的图片，如此循环
     * 接口个数：addNewNotes.size * addNewNotes.size
     */

    private void pAddNewNote() {
        addNewNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 3);
        MLog.e("同步edit--pAddNewNote---addNewNotes.size()=" + addNewNotes.size());
        if (addNewNotes.size() > 0) {
            //先 上传数组的第一个
            TNNote tnNote = addNewNotes.get(0);
            Vector<TNNoteAtt> newNotesAtts = tnNote.atts;
            if (newNotesAtts.size() > 0) {//有图，先上传图片
                MLog.d("同步edit--pAddNewNote--有图，先上传图片");
                pNewNotePic(0, newNotesAtts.size(), 0, addNewNotes.size(), newNotesAtts.get(0));
            } else {//没有图片，就执行Note
                MLog.d("同步edit--pAddNewNote--无图，就上传Note");
                pNewNote(0, addNewNotes.size(), addNewNotes.get(0), false, addNewNotes.get(0).content);
            }
        } else {
            //下个执行接口
            recoveryNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 7);
            recoveryNote(0);
        }
    }

    /**
     * (二.5)正常同步 第一个执行的接口 上传图片OldNotePic 循环调用
     * 和（二.6组成双层for循环，该处是最内层for执行）
     */
    private void pNewNotePic(int picPos, int picArrySize, int notePos,
                             int noteArrySize, TNNoteAtt tnNoteAtt) {
        MLog.e("同步edit--pNewNotePic 2-5--上传图片");
        presener.pNewNotePic(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.6)正常同步 第2个执行的接口 循环调用
     * 和（二.5组成双层for循环，该处是最外层for执行）
     */
    private void pNewNote(int position, int arraySize, TNNote tnNoteAtt, boolean isNewDb, String content) {
        MLog.d("同步edit--pNewNote 2-6--content=" + content);
        presener.pNewNote(position, arraySize, tnNoteAtt, isNewDb, content);
    }


    /**
     * (二.7)正常同步 recoveryNote
     * 从0开始执行
     * 接口个数：如果走NoteRecovery:recoveryNotes.size /如果走NoteAdd：recoveryNotes.size * recoveryNotesattrs.size
     * 说明：同(二.7-2+二.7-3)的执行顺序，先处理recoveryNotes的图片，处理完就上传recoveryNotes的文本，然后再处理position+1的图片，如此循环
     *
     * @param position 标记，表示recoveryNotes的开始位置，非recoveryNotesAtts位置
     */
    private void recoveryNote(int position) {
        MLog.e("同步edit--recoveryNote 2-7--recoveryNotes.size()=" + recoveryNotes.size());
        if (position < recoveryNotes.size() && position >= 0) {
            if (recoveryNotes.get(position).noteId != -1) {
                //循环执行
                pRecoveryNote(recoveryNotes.get(position).noteId, position, recoveryNotes.size());
            } else {
                Vector<TNNoteAtt> recoveryNotesAtts = recoveryNotes.get(position).atts;
                if (recoveryNotesAtts.size() > 0) {//有图，先上传图片
                    pRecoveryNotePic(0, recoveryNotesAtts.size(), position, recoveryNotes.size(), recoveryNotesAtts.get(0));
                } else {//如果没有图片，就执行RecoveryNoteAdd
                    pRecoveryNoteAdd(0, recoveryNotes.size(), recoveryNotes.get(position), true, recoveryNotes.get(position).content);
                }
            }
        } else {

            //执行下一个接口
            deleteNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 6);
            pDelete(0);
        }

    }

    /**
     * (二.7)01
     */
    private void pRecoveryNote(long noteID, int position, int arrySize) {
        MLog.d("同步edit--pRecoveryNote 2-7-1");

        presener.pRecoveryNote(noteID, position, arrySize);
    }

    /**
     * (二.7)02
     */
    private void pRecoveryNotePic(int picPos, int picArrySize, int notePos,
                                  int noteArrySize, TNNoteAtt tnNoteAtt) {
        MLog.d("同步edit--pRecoveryNotePic 2-7-2");
        presener.pRecoveryNotePic(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.7)03
     */
    private void pRecoveryNoteAdd(int position, int arraySize, TNNote tnNoteAtt,
                                  boolean isNewDb, String content) {
        MLog.d("同步edit--pRecoveryNoteAdd 2-7-3");
        presener.pRecoveryNoteAdd(position, arraySize, tnNoteAtt, isNewDb, content);
    }


    /**
     * (二.8)
     *
     * @param position
     */
    private void pDelete(int position) {
        MLog.e("同步edit--pDelete 2-8---deleteNotes.size()=" + deleteNotes.size());
        if (deleteNotes.size() > 0 && position < deleteNotes.size()) {
            if (deleteNotes.get(position).noteId != -1) {
                pNoteDelete(deleteNotes.get(position).noteId, position);
            } else {
                //不调接口
                pNoteLocalDelete(position, deleteNotes.get(position).noteLocalId);
            }
        } else {
            //下一个接口
            deleteRealNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 5);
            pRealDelete(0);
        }
    }

    /**
     * (二.8)
     */
    private void pNoteDelete(long noteId, int postion) {
        MLog.d("同步edit--pNoteDelete 2-8");
        presener.pDeleteNote(noteId, postion);
    }

    /**
     * (二.8)删除本地数据 （不调接口）
     */
    private void pNoteLocalDelete(final int position, final long noteLocalId) {
        MLog.d("同步edit--pNoteLocalDelete 2-8");
        //使用异步操作，完成后，执行下一个 position或接口
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                TNDb.beginTransaction();
                try {
                    //
                    TNDb.getInstance().execSQL(TNSQLString.NOTE_SET_TRASH, 2, 6, System.currentTimeMillis() / 1000, noteLocalId);
                    //
                    TNNote note = TNDbUtils.getNoteByNoteLocalId(noteLocalId);
                    TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);

                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }

                //
                Message msg = Message.obtain();
                msg.obj = position;
                msg.what = DELETE_LOCALNOTE;
                handler.sendMessage(msg);
            }
        });

    }

    /**
     * (二.9)deleteRealNotes
     *
     * @param position deleteRealNotes执行位置
     */
    //添加标记，两个线程异步执行，都执行完，isRealDelete都设置为true,再执行下一poistion，
    private boolean isRealDelete1 = false;
    private boolean isRealDelete2 = false;

    private void pRealDelete(int position) {
        MLog.e("同步edit--pRealDelete 2-9--deleteRealNotes.size()=" + deleteRealNotes.size());
        if (deleteRealNotes.size() > 0 && position < deleteRealNotes.size()) {
            if (deleteRealNotes.get(position).noteId == -1) {
                //
                pDeleteReadNotesSql(deleteRealNotes.get(position).noteLocalId, position);
            } else {
                //2个接口
                pDeleteRealNotes(deleteRealNotes.get(position).noteId, position);
            }
        } else {
            //下一个接口
            pGetAllNoteIds();
        }
    }


    /**
     * (二.9) deleteReadNotes
     * 数据库
     * 接口个数：2
     *
     * @param nonteLocalID
     */
    private void pDeleteReadNotesSql(final long nonteLocalID, final int position) {
        MLog.d("同步edit--pDeleteReadNotesSql 2-9");
        //使用异步操作，完成后，执行下一个 position或接口
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                TNDb.beginTransaction();
                try {
                    //
                    TNDb.getInstance().deleteSQL(TNSQLString.NOTE_DELETE_BY_NOTELOCALID, new Object[]{nonteLocalID});
                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }

                //
                Message msg = Message.obtain();
                msg.obj = position;
                msg.what = DELETE_REALNOTE;
                handler.sendMessage(msg);
            }
        });
    }

    /**
     * (二.9)
     */
    private void pDeleteRealNotes(long noteId, int postion) {
        MLog.d("同步edit--pDeleteRealNotes 2-9");
        //
        presener.pDeleteRealNotes(noteId, postion);

    }

    /**
     * (二.10)
     */
    private void pGetAllNoteIds() {
        //
        MLog.d("同步edit--pGetAllNoteIds 2-10");
        presener.pGetAllNotesId();
    }

    /**
     * (二.10)-1 editNote上传图片
     * 说明：
     * 2-10-1和2-11-1图片上传和2-5/2-6相同
     *
     * @param position cloudIds数据的其实操作位置
     */

    private void pEditNotePic(int position) {
        MLog.e("同步edit--pEditNotePic 2-10-1----editNotes：" + editNotes.size());
        if (cloudIds.size() > 0 && position < (cloudIds.size())) {
            long id = cloudIds.get(position).getId();
            int lastUpdate = cloudIds.get(position).getUpdate_at();
            if (editNotes != null && editNotes.size() > 0) {
                if (editNotes == null || editNotes.size() <= 0) {
                    //执行下一个接口
                    pUpdataNote(0, false);
                }
                //找出该日记，比较时间
                for (int j = 0; j < editNotes.size(); j++) {
                    if (id == editNotes.get(j).noteId) {
                        if (editNotes.get(j).lastUpdate > lastUpdate) {
                            MLog.e("同步edit--pEditNotePic 2-10-1----发现要上传的编辑笔记：" + editNotes.get(j).toString());
                            //上传图片，之后上传文本
                            TNNote note = EditNotePicBefore(editNotes.get(j));//上传图片，处理content参数
                            pEditNotePic(position, 0, note);
                        } else {
                            MLog.e("同步edit--pEditNotePic 2-10-1----另一个手机编辑本笔记---更改状态-->1");
                            updataEditNotesState(position, editNotes.get(j).noteLocalId);
                        }
                    }

                    //方便跳出循环，执行下一个接口
                    if ((j == (editNotes.size() - 1)) && id != editNotes.get(j).noteId) {
                        //执行下一个position
                        pEditNotePic(position + 1);
                    }
                }
            } else {
                //执行下一个循环
                pEditNotePic(position + 1);
            }

        } else {
            //执行下一个接口
            pUpdataNote(0, false);
        }
    }

    /**
     * 对note的content进行处理,供(二.10)-1的pEditNotePic()使用
     *
     * @param tnNote
     * @return
     */
    private TNNote EditNotePicBefore(TNNote tnNote) {
        TNNote note = tnNote;
        String shortContent = TNUtils.getBriefContent(note.content);
        String content = note.content;
        ArrayList list = new ArrayList();
        int index1 = content.indexOf("<tn-media");
        int index2 = content.indexOf("</tn-media>");
        while (index1 >= 0 && index2 > 0) {
            String temp = content.substring(index1, index2 + 11);
            list.add(temp);
            content = content.replaceAll(temp, "");
            index1 = content.indexOf("<tn-media");
            index2 = content.indexOf("</tn-media>");
        }
        for (int i = 0; i < list.size(); i++) {
            String temp = (String) list.get(i);
            boolean isExit = false;
            for (TNNoteAtt att : note.atts) {
                String temp2 = String.format("<tn-media hash=\"%s\"></tn-media>", att.digest);
                if (temp.equals(temp2)) {
                    isExit = true;
                }
            }
            if (!isExit) {
                note.content = note.content.replaceAll(temp, "");
            }
        }
        return note;
    }

    /**
     * (二.10)-1
     * 图片上传
     *
     * @param cloudsPos cloudIds数据的其实操作位置
     * @param tnnote    EditNotePicBefore(note)方法已经处理了content参数，可以直接用
     */
    private void pEditNotePic(int cloudsPos, int attsPos, TNNote tnnote) {
        TNNote note = tnnote;
        MLog.d("同步edit--pEditNotePic 2-10-1---note:" + note.toString());
        if (cloudIds.size() > 0 && cloudsPos < (cloudIds.size())) {
            /**
             * 上传attsPos的图片：
             * 有本地图片，则content直接拼接，没有就调用接口
             *
             */
            if (note.atts.size() > 0 && attsPos < note.atts.size()) {
                //上传attsPos的图片
                TNNoteAtt att = note.atts.get(attsPos);
                if (!TextUtils.isEmpty(att.path) && att.attId != -1) {
                    String s1 = String.format("<tn-media hash=\"%s\" />", att.digest);
                    String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", att.digest, att.attId);
                    note.content = note.content.replaceAll(s1, s2);
                    String s3 = String.format("<tn-media hash=\"%s\"></tn-media>", att.digest);
                    String s4 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", att.digest, att.attId);
                    note.content = note.content.replaceAll(s3, s4);

                    //执行下一个attsPos位置的数据
                    MLog.e("saveAtt", "pEditNotePic--执行下一个attsPos位置的数据--pEditNotePic" + "cloudsPos=" + cloudsPos + "--attsPos=" + attsPos + "--note" + note.toString());
                    pEditNotePic(cloudsPos, attsPos + 1, note);
                } else {
                    MLog.e("saveAtt", "pEditNotePic--同步上传att图片" + "cloudsPos=" + cloudsPos + "--attsPos=" + attsPos + "--note" + note.toString());
                    //接口，上传图片
                    presener.pEditNotePic(cloudsPos, attsPos, note);
                }
            } else {
                MLog.e("saveAtt", "pEditNotePic--同步上传att图片" + "--图片上传完，再上传文本" + "cloudsPos=" + cloudsPos);
                //图片上传完，再上传文本
                pEditNotes(cloudsPos, note);
            }
        } else {
            //执行下一个接口
            MLog.e("saveAtt", "pEditNotePic--执行下一个接口pUpdataNote");
            pUpdataNote(0, false);

        }

    }


    /**
     * (二.11)-1 edit  通过最后更新时间来与云端比较，是否该上传本地编辑的笔记
     * 上传文本
     *
     * @param cloudsPos cloudIds数据的其实操作位置
     */
    private void pEditNotes(int cloudsPos, TNNote note) {
        MLog.d("同步edit--pEditNotes 2-11-1");
        if (cloudIds.size() > 0 && cloudsPos < (cloudIds.size() - 1)) {
            presener.pEditNote(cloudsPos, note);
        } else {
            //执行下一个接口
            MLog.e("saveAtt", "pEditNotes--执行下一个接口pUpdataNote");
            pUpdataNote(0, false);
        }
    }

    /**
     * (二.11)-2 更新云端的笔记
     *
     * @param position 执行的位置
     * @param is13     (二.11)-2和(二.13)调用同一个接口，用于区分
     */
    private void pUpdataNote(int position, boolean is13) {
        MLog.d("同步edit--pUpdataNote 2-11-2");
        //为2-11-2接口返回，做预处理
        setChildHandler2_11(position);

        if (cloudIds.size() > 0 && position < (cloudIds.size() - 1)) {
            boolean isExit = false;
            long id = cloudIds.get(position).getId();
            int lastUpdate = cloudIds.get(position).getUpdate_at();

            //本地更新
            for (int j = 0; j < allNotes.size(); j++) {
                TNNote note = allNotes.get(j);
                if (id == note.noteId) {
                    isExit = true;

                    if (lastUpdate > note.lastUpdate) {
                        pUpdataNote(position, id, is13);
                    }
                    break;
                }
            }
            if (!isExit) {
                pUpdataNote(position, id, is13);
            } else {
                //
                //下一个position
                pUpdataNote(position + 1, is13);
            }

        } else {
            MLog.d("同步edit2-13--endSynchronize");
            //同步所有接口完成，结束同步
            endSynchronize();

        }
    }

    /**
     * (二.11)-2/ 更新云端的笔记
     * <p>
     * p层
     */
    private void pUpdataNote(int position, long noteId, boolean is13) {
        MLog.d("同步edit--pUpdataNote ");
        presener.pGetNoteByNoteId(position, noteId, is13);
    }

    //=============================================接口结果回调(成对的success+failed)======================================================


    //2-5
    @Override
    public void onSyncNewNotePicSuccess(Object obj, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {

        String content = addNewNotes.get(notePos).content;
        OldNotePicBean newPicbean = (OldNotePicBean) obj;
        //更新图片 数据库
        upDataAttIdSQL(newPicbean.getId(), tnNoteAtt);
        //
        String digest = newPicbean.getMd5();
        long attId = newPicbean.getId();
        //更新 content
        String s1 = String.format("<tn-media hash=\"%s\" />", digest);
        String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", digest, attId);
        content = content.replaceAll(s1, s2);
        //暂时保存content
        addNewNotes.get(notePos).content = content;

        if (notePos < noteArrySize) {
            if (picPos < picArrySize - 1) {
                //继续上传下张图
                Vector<TNNoteAtt> newNotesAtts = addNewNotes.get(notePos).atts;
                pNewNotePic(picPos + 1, picArrySize, notePos, noteArrySize, newNotesAtts.get(picPos + 1));
            } else {
                //所有图片上传完成，就开始上传文本
                TNNote note = addNewNotes.get(notePos);
                if (note.catId == -1) {
                    note.catId = TNSettings.getInstance().defaultCatId;
                }
                pNewNote(notePos, noteArrySize, note, false, content);
            }
        } else {
            //所有图片上传完成，就开始上传newPos的文本
            TNNote note = addNewNotes.get(notePos);
            if (note.catId == -1) {
                note.catId = TNSettings.getInstance().defaultCatId;
            }
            pNewNote(notePos, noteArrySize, note, false, content);
        }
    }

    @Override
    public void onSyncNewNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
        MLog.e(msg);
    }

    //2-6
    @Override
    public void onSyncNewNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {

        OldNoteAddBean newNoteBean = (OldNoteAddBean) obj;
        //更新数据库
        if (isNewDb) {//false时表示老数据库的数据上传，不用在修改本地的数据
            upDataNoteLocalIdSQL(newNoteBean, addNewNotes.get(position));
        }


        if (position < arraySize - 1) {
            //处理position + 1下的图片上传
            Vector<TNNoteAtt> newNotesAtts = addNewNotes.get(position + 1).atts;
            pNewNotePic(0, newNotesAtts.size(), position + 1, arraySize, addNewNotes.get(position + 1).atts.get(0));
        } else {

            //执行下个接口
            recoveryNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 7);
            recoveryNote(0);
        }
    }

    @Override
    public void onSyncNewNoteAddFailed(String msg, Exception e, int position, int arraySize) {
        MLog.e(msg);
    }

    //2-7-1
    @Override
    public void onSyncRecoverySuccess(Object obj, long noteId, int position) {
        //更新数据库
        recoveryNoteSQL(noteId);

        //执行循环的下一个position+1数据/下一个接口
        recoveryNote(position + 1);
    }

    @Override
    public void onSyncRecoveryFailed(String msg, Exception e) {
        MLog.e(msg);
    }

    //2-7-2
    @Override
    public void onSyncRecoveryNotePicSuccess(Object obj, int picPos, int picArrySize,
                                             int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        String content = recoveryNotes.get(notePos).content;
        OldNotePicBean recoveryPicbean = (OldNotePicBean) obj;

        //更新图片 数据库
        upDataAttIdSQL(recoveryPicbean.getId(), tnNoteAtt);

        if (notePos < noteArrySize - 1) {
            if (picPos < picArrySize - 1) {
                //继续上传下张图
                Vector<TNNoteAtt> newNotesAtts = recoveryNotes.get(notePos).atts;
                pRecoveryNotePic(picPos + 1, picArrySize, notePos, noteArrySize, newNotesAtts.get(picPos + 1));
            } else {//所有图片上传完成，就开始上传文本
                String digest = recoveryPicbean.getMd5();
                long attId = recoveryPicbean.getId();
                //更新 content
                String s1 = String.format("<tn-media hash=\"%s\" />", digest);
                String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", digest, attId);
                content = content.replaceAll(s1, s2);

                //所有图片上传完成，就开始上传newPos的文本
                TNNote note = recoveryNotes.get(notePos);
                if (note.catId == -1) {
                    note.catId = TNSettings.getInstance().defaultCatId;
                }
                pRecoveryNoteAdd(notePos, noteArrySize, note, true, content);
            }
        } else {

            //所有图片上传完成，就开始上传newPos的文本
            TNNote note = recoveryNotes.get(notePos);
            if (note.catId == -1) {
                note.catId = TNSettings.getInstance().defaultCatId;
            }
            pRecoveryNoteAdd(notePos, noteArrySize, note, true, content);
        }
    }

    @Override
    public void onSyncRecoveryNotePicFailed(String msg, Exception e, int picPos, int picArry,
                                            int notePos, int noteArry) {
        MLog.e(msg);
    }

    //2-7-3
    @Override
    public void onSyncRecoveryNoteAddSuccess(Object obj, int position, int arraySize,
                                             boolean isNewDb) {

        OldNoteAddBean recoveryNoteBean = (OldNoteAddBean) obj;
        //更新数据库
        if (isNewDb) {//false时表示老数据库的数据上传，不用在修改本地的数据
            upDataNoteLocalIdSQL(recoveryNoteBean, recoveryNotes.get(position));
        }

        //处理position + 1下的TNNote/下一个接口
        recoveryNote(position + 1);

    }

    @Override
    public void onSyncRecoveryNoteAddFailed(String msg, Exception e, int position, int arraySize) {
        MLog.e(msg);
    }

    //2-8
    @Override
    public void onSyncDeleteNoteSuccess(Object obj, long noteId, int position) {

        //更新数据
        updataDeleteNoteSQL(noteId);

        //执行下一个
        pDelete(position + 1);
    }

    @Override
    public void onSyncDeleteNoteFailed(String msg, Exception e) {
        MLog.e(msg);
    }

    //2-9-1
    @Override
    public void onSyncpDeleteRealNotes1Success(Object obj, long noteId, int position) {
        isRealDelete1 = true;
        //更新数据
        updataDeleteNoteSQL(noteId);

        if (isRealDelete1 && isRealDelete2) {
            //执行下一个
            pRealDelete(position + 1);

            //复原 false,供下次循环使用
            isRealDelete1 = false;
            isRealDelete2 = false;
        }

    }

    @Override
    public void onSyncDeleteRealNotes1Failed(String msg, Exception e, int position) {
        isRealDelete1 = true;
        MLog.e(msg);
    }

    //2-9-2
    @Override
    public void onSyncDeleteRealNotes2Success(Object obj, long noteId, int position) {
        isRealDelete2 = true;
        //更新数据库
        deleteRealSQL(noteId, position);
    }

    @Override
    public void onSyncDeleteRealNotes2Failed(String msg, Exception e, int position) {
        isRealDelete2 = true;
        MLog.e(msg);
    }

    //2-10
    @Override
    public void onSyncAllNotesIdSuccess(Object obj) {
        cloudIds = (List<AllNotesIdsBean.NoteIdItemBean>) obj;


        //与云端同步数据 sjy-0623
        allNotes = TNDbUtils.getAllNoteList(TNSettings.getInstance().userId);
        for (int i = 0; i < allNotes.size(); i++) {
            boolean isExit = false;
            final TNNote note = allNotes.get(i);
            MLog.d("saveNote", "onSyncAllNotesIdSuccess--cloudIds数据=" + cloudIds.size() + "个---本地笔记allNotes：" + allNotes.size() + "个---TNNote内容：" + note.toString());
            //查询本地是否存在
            for (int j = 0; j < cloudIds.size(); j++) {
                if (note.noteId == cloudIds.get(j).getId()) {
                    isExit = true;
                    break;
                }
            }

            //不存在就删除  /使用异步
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            if (!isExit && note.syncState != 7) {

                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        TNDb.beginTransaction();
                        try {
                            //
                            TNDb.getInstance().deleteSQL(TNSQLString.NOTE_DELETE_BY_NOTEID, new Object[]{note.noteId});
                            TNDb.setTransactionSuccessful();
                        } finally {
                            TNDb.endTransaction();
                        }
                    }
                });
            }
        }

        //edit  通过最后更新时间来与云端比较是否该上传本地编辑的笔记
        editNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 4);
        //执行下一个接口
        pEditNotePic(0);
    }

    @Override
    public void onSyncAllNotesIdAddFailed(String msg, Exception e) {
        MLog.e(msg);
    }

    //2-10-1
    @Override
    public void onSyncEditNotePicSuccess(Object obj, int cloudsPos, int attsPos, TNNote tnNote) {
        TNNote note = tnNote;
        OldNotePicBean editPicbean = (OldNotePicBean) obj;
        note.atts.get(attsPos).digest = editPicbean.getMd5();
        note.atts.get(attsPos).attId = editPicbean.getId();
        String s1 = String.format("<tn-media hash=\"%s\" />", note.atts.get(attsPos).digest);
        String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", note.atts.get(attsPos).digest, note.atts.get(attsPos).attId);
        note.content = note.content.replaceAll(s1, s2);
        //更新图片 数据库
        upDataAttIdSQL(editPicbean.getId(), note.atts.get(attsPos));
        //执行下一个attsPos的图片上传
        pEditNotePic(cloudsPos, attsPos + 1, note);
    }

    @Override
    public void onSyncEditNotePicFailed(String msg, Exception e, int cloudsPos,
                                        int attsPos, TNNote tnNote) {
        MLog.e(msg);
    }


    //2-11-1
    @Override
    public void onSyncEditNoteSuccess(Object obj, int cloudsPos, TNNote note) {

        //更新下一个cloudsPos位置的数据
        updataEditNotes(cloudsPos, note);
    }

    @Override
    public void onSyncEditNoteAddFailed(String msg, Exception e) {
        MLog.e(msg);
    }

    //2-11-2
    @Override
    public void onSyncpGetNoteByNoteIdSuccess(Object obj, int position, boolean is13) {
        handleNote((GetNoteByNoteIdBean) obj, position, false);
    }

    @Override
    public void onSyncpGetNoteByNoteIdFailed(String msg, Exception e) {
        MLog.e(msg);
    }

}
