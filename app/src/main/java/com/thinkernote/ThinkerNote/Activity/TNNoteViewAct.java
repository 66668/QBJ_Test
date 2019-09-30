package com.thinkernote.ThinkerNote.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.thinkernote.ThinkerNote.BuildConfig;
import com.thinkernote.ThinkerNote.DBHelper.NoteAttrDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.NoteDbHelper;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.General.TNUtilsDialog;
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.main.GetNoteByNoteIdBean;
import com.thinkernote.ThinkerNote.dialog.CommonDialog;
import com.thinkernote.ThinkerNote.mvp.MyRxManager;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnNoteViewListener;
import com.thinkernote.ThinkerNote.mvp.p.NoteViewDownloadPresenter;
import com.thinkernote.ThinkerNote.mvp.p.NoteViewPresenter;
import com.thinkernote.ThinkerNote.other.PoPuMenuView;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 笔记详情
 */
public class TNNoteViewAct extends TNActBase implements OnClickListener,
//        SynthesizerPlayerListener,//隐藏
        NoteViewDownloadPresenter.OnDownloadEndListener,
        NoteViewDownloadPresenter.OnDownloadStartListener,
        PoPuMenuView.OnPoPuMenuItemClickListener,
        OnNoteViewListener {

    public static final String TAG = "TNNoteViewAct";
    public static final long ATT_MAX_DOWNLOAD_SIZE = 50 * 1024;
    public static final int DIALOG_DELETE = 101;//
    public static final int WEBBVIEW_START = 102;//
    public static final int WEBBVIEW_OPEN_ATT = 103;//
    public static final int WEBBVIEW_LOADING = 104;//
    public static final int WEBBVIEW_SHOW = 105;//
    public static final int GETNOTEBYNOTEID_SUCCESS = 106;//

    // Class members
    // -------------------------------------------------------------------------------
    private Dialog mProgressDialog = null;
    private JSInterface mJSInterface;
    private TNNoteAtt mCurAtt;
    private long mCurAttId;
    private long mNoteLocalId;
    private TNNote mNote;
    private Tencent mTencent;
    private IUiListener mListener;

    //    private SynthesizerPlayer mSynthesizerPlayer;
    private String mPlainText = null;
    private int mStartPos = 0;
    private int mEndPos = 0;
    private float mScale;
    private GestureDetector mGestureDetector;

    private PoPuMenuView mPopuMenu;

    private WebView mWebView;

    private CommonDialog dialog;
    //p
    private NoteViewPresenter presenter;
    private NoteViewDownloadPresenter download;

    //讯飞语音合成
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 默认发音人
    private String voicer = "xiaoyan";

    private String[] mCloudVoicersEntries;
    private String[] mCloudVoicersValue;

    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case WEBBVIEW_OPEN_ATT://打开 文件的操作弹窗

                mCurAttId = (long) msg.obj;
                MLog.d("AAA", "TNNoteViewAct", "打开att操作弹窗--mCurAttId-" + mCurAttId);
                //弹窗
                openContextMenu(findViewById(R.id.noteview_openatt_menu));
                break;
            case WEBBVIEW_START:
                //webView显示
                TNNoteAtt att = (TNNoteAtt) msg.getData().getSerializable("att");
                String s = "<img name=\\\"loading\\\" src=\\\"file:///android_asset/download.png\\\" /><span name=\\\"abcd\\\"><br />%s(%s)</span>";
                s = String.format(s, att.attName, att.size / 1024 + "K");
                mWebView.loadUrl(String.format("javascript:wave(\"%d\", \"%s\")",
                        att.attId, s));
                MLog.d(TAG, "start javascript:loading");
                mWebView.loadUrl("javascript:loading()");
                break;
            case WEBBVIEW_LOADING:
                MLog.d("WEBBVIEW_LOADING");
                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                mWebView.loadDataWithBaseURL(""
                        , mNote.makeHtml((int) (dm.widthPixels / dm.scaledDensity))
                        , "text/html"
                        , "utf-8"
                        , null);
                break;
            case WEBBVIEW_SHOW:
                Bundle b = msg.getData();
                //javascript:wave("1", "<div id=\"1\"><a onClick=\"window.demo.openAtt(1)\"><img src=\"file://null\" /></a></div>")
                //javascript:wave("1", "<div id=\"1\"><a onClick=\"window.demo.openAtt(1)\"><img src=\"file:///storage/emulated/0/Android/data/com.thinkernote.ThinkerNote/files/Attachment/28/28499/28499260.jpeg\" /></a></div>")
                mWebView.loadUrl(String.format("javascript:wave(\"%d\", \"%s\")", b.getLong("attLocalId"), b.getString("s")));
                break;
            case DIALOG_DELETE:

                mProgressDialog.hide();
                finish();

                break;
            case GETNOTEBYNOTEID_SUCCESS:
                //
                mNote = TNDbUtils.getNoteByNoteLocalId(mNoteLocalId);
                download.setNewNote(mNote);
                startAutoDownload();

                Message msg1 = new Message();
                msg1.what = WEBBVIEW_LOADING;
                handler.sendMessage(msg1);

                break;
        }
        super.handleMessage(msg);
    }

    @SuppressLint("JavascriptInterface")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.noteview);
        myIntent();
        setViews();

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScale = metric.scaledDensity;

        presenter = new NoteViewPresenter(this, this);

        mTencent = Tencent.createInstance(TNConst.QQ_APP_ID, this.getApplicationContext());
        mListener = new IUiListener() {
            @Override
            public void onError(UiError arg0) {
                TNUtilsUi.showToast("分享失败：" + arg0.errorMessage);
            }

            @Override
            public void onComplete(JSONObject jobj) {
                TNUtilsUi.showToast("分享成功");
            }

            @Override
            public void onCancel() {
//				TNUtilsUi.showToast("分享取消");
            }
        };

        // initialize
        findViewById(R.id.noteview_home).setOnClickListener(this);
        findViewById(R.id.noteview_edit).setOnClickListener(this);
        findViewById(R.id.noteview_read_close).setOnClickListener(this);
        findViewById(R.id.noteview_read_play).setOnClickListener(this);
        findViewById(R.id.noteview_more).setOnClickListener(this);

        registerForContextMenu(findViewById(R.id.noteview_openatt_menu));
        registerForContextMenu(findViewById(R.id.noteview_read_menu));
        registerForContextMenu(findViewById(R.id.noteview_share_menu));

        download = new NoteViewDownloadPresenter(this);
        download.setOnDownloadEndListener(this);
        download.setOnDownloadStartListener(this);
        mGestureDetector = new GestureDetector(this, new TNGestureListener());

        mWebView = (WebView) findViewById(R.id.noteview_web);
        // 启用javascript
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mJSInterface = new JSInterface();
        // 添加js交互接口类，并起别名 demo，js代码中要试用 demo参数
        mWebView.addJavascriptInterface(mJSInterface, "demo");
        //自定义WebViewClient 监听卸载configView中
        //mWebView点击监听
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                view.loadUrl("javascript:loading()");
                startAutoDownload();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                MLog.e("TNNoteViewAct", "WebViewClient--shouldOverrideUrlLoading" + url);
                super.shouldOverrideUrlLoading(view, url);

                if (url.startsWith("http:") || url.startsWith("https:")) {
                    return false;
                }
                // Otherwise allow the OS to handle it
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                TNUtilsDialog.startIntent(TNNoteViewAct.this, intent,
                        R.string.alert_NoteView_CantOpenMsg);
                return true;
            }

        });

        mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
        //
        initTts();
    }

    /**
     * intent值处理 包括小部件跳转
     */
    private void myIntent() {

        Intent intent = getIntent();
        //从item获取笔记id/或者从小部件跳转获取笔记id，保持SCHEME_ITEMKEY= "NoteLocalId"
        mNoteLocalId = intent.getExtras().getLong("NoteLocalId"); //获取跳转 mNoteLocalId

    }

    //讯飞语音初始化
    private void initTts() {
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(TNNoteViewAct.this, mTtsInitListener);

        // 云端发音人名称列表
        mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
        mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                TNUtilsUi.showToast("语音合成功能不可用");
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };


    @Override
    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.noteview_toolbar_layout,
                R.drawable.toolbg);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null,
                R.id.noteview_edit, R.drawable.editnote);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null,
                R.id.noteview_more, R.drawable.more);
        TNUtilsSkin.setViewBackground(this, null, R.id.noteview_page,
                R.drawable.page_bg);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//		configView();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onDestroy() {
        mProgressDialog.dismiss();
        if (mPopuMenu != null)
            mPopuMenu.dismiss();

        super.onDestroy();

        if (presenter != null) {
            presenter = null;
        }

        if (download != null) {
            download.cancelDownload();
            download = null;
        }
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mTencent.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == R.id.noteview_binding_sina) {
            String accessToken = data.getStringExtra("AccessToken");
            String uniqueId = data.getStringExtra("UniqueId");
            if (accessToken != null) {
                Bundle b = new Bundle();
                b.putLong("NoteId", mNote.noteId);
                b.putString("AccessToken", accessToken);
                b.putString("UniqueId", uniqueId);
                // 无
//				startActivity(TNWeiboSendAct.class, b);
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outBundle) {
        outBundle.putInt("START_POS", mStartPos);
        outBundle.putInt("END_POS", mEndPos);
        outBundle.putLong("NOTE_ID", mNoteLocalId);
        super.onSaveInstanceState(outBundle);
    }


    @Override
    public void onRestoreInstanceState(Bundle outBundle) {
        super.onRestoreInstanceState(outBundle);
        mStartPos = outBundle.getInt("START_POS");
        mEndPos = outBundle.getInt("END_POS");
        mNoteLocalId = outBundle.getLong("NOTE_ID");
        configView();
    }


    protected void configView() {
        mNote = TNDbUtils.getNoteByNoteLocalId(mNoteLocalId);

        if (createStatus == 0) {
            download.setNewNote(mNote);
        } else {
            download.updateNote(mNote);
        }
        //判断是否是回收站的笔记，如果是 顶部显示还原的按钮
        if (mNote.trash == 1) {
            ((ImageButton) findViewById(R.id.noteview_more))
                    .setImageResource(R.drawable.shiftdelete);
            ((ImageButton) findViewById(R.id.noteview_edit))
                    .setImageResource(R.drawable.restorenote);
        } else {
            ((ImageButton) findViewById(R.id.noteview_more))
                    .setImageResource(R.drawable.more);
            ((ImageButton) findViewById(R.id.noteview_edit))
                    .setImageResource(R.drawable.editnote);
        }

        if (mNote.syncState == 1) {
            if (!TNUtils.isNetWork()) {
                Message msg = new Message();
                msg.what = WEBBVIEW_LOADING;
                handler.sendMessage(msg);
                TNUtilsUi.alert(this, R.string.alert_NoteView_NetNotWork);
            } else {
                MLog.e("configView--mNote.syncState == 1--正在获取笔记");
                mWebView.loadDataWithBaseURL("", getString(R.string.getingcontent), "text/html", "utf-8", null);
                //
                pGetNote(mNote.noteId);
            }
        } else {
            Message msg = new Message();
            msg.what = WEBBVIEW_LOADING;
            handler.sendMessage(msg);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //TODO 添加结束接口调用代码（结束异步调用）
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configView();
    }


    @Override
    protected void onPause() {
        super.onPause();
//        if (null != mSynthesizerPlayer) {
//            if (mSynthesizerPlayer.getState().toString().equals("PLAYING")) {
//                mSynthesizerPlayer.pause();
//                ImageButton playBtn = (ImageButton) findViewById(R.id.noteview_read_play);
//                playBtn.setImageResource(R.drawable.ic_media_play);
//            }
//        }
    }


    // ContextMenu
    // -------------------------------------------------------------------------------
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        switch (v.getId()) {
            case R.id.noteview_openatt_menu:
                getMenuInflater().inflate(R.menu.openatt_menu, menu);
                break;

            case R.id.noteview_read_menu:
                getMenuInflater().inflate(R.menu.read_menu, menu);
                break;

            case R.id.noteview_share_menu:
                getMenuInflater().inflate(R.menu.noteview_share, menu);
                break;

            default:
                MLog.d(TAG, "onCreateContextMenu default");
                break;
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //==================openatt_menu相关=====================
            case R.id.openatt_menu_view: {//查看
                //更新 mNote
                mCurAtt = mNote.getAttDataByLocalId(mCurAttId);

                MLog.e("AAA", "文件点击事件--查看--mNote:" + mNote.toString());
                MLog.e("AAA", "id=" + mCurAttId + "--mCurAtt:" + mCurAtt.toString());
                //打开文件
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri contentUri = null;
                if (mCurAtt.path == null || mCurAtt.path.isEmpty()) {
                    TNUtilsUi.showToast("文件处理，请稍后查看");
                    break;
                }
                //打开文件
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//7.0+版本安全设置
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".FileProvider", new File(mCurAtt.path));
                    } catch (Exception e) {
                        TNUtilsUi.showToast("文件处理，请稍后查看");
                    }
                } else {//7.0-正常调用
                    contentUri = Uri.fromFile(new File(mCurAtt.path));
                }

                if (contentUri != null) {
                    intent.setDataAndType(contentUri, TNUtilsAtt.getMimeType(mCurAtt.type, mCurAtt.attName));
                    MLog.d("AAA", "笔记详情", "响应--查看");
                    TNUtilsDialog.startIntent(this, intent, R.string.alert_NoteView_CantOpenAttMsg);
                } else {
                    MLog.e("AAA", "笔记详情", "响应--查看--异常");
                }

                break;
            }

            case R.id.openatt_menu_save: {//保存
                //更新 mNote
                mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);
                mCurAtt = mNote.getAttDataByLocalId(mCurAttId);
                MLog.e(TAG, createStatus + " " + TNNoteViewAct.this.isFinishing() + "id=" + mCurAttId + "--mCurAtt:" + mCurAtt.toString());
                saveAttDialog();
                break;
            }

            case R.id.openatt_menu_send: {//发送
                //更新 mNote
                mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);
                mCurAtt = mNote.getAttDataByLocalId(mCurAttId);
                MLog.e(TAG, createStatus + " " + TNNoteViewAct.this.isFinishing() + "id=" + mCurAttId + "--mCurAtt:" + mCurAtt.toString());
                try {
                    String temp = TNUtilsAtt.getTempPath(mCurAtt.path);
                    TNUtilsAtt.copyFile(mCurAtt.path, temp);
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);

                    intent.setType(TNUtilsAtt.getMimeType(mCurAtt.type, mCurAtt.attName));

                    Uri contentUri = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//7.0+版本安全设置
                        contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".FileProvider", new File(temp));
                    } else {//7.0-正常调用
                        contentUri = Uri.fromFile(new File(temp));
                    }
                    intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    TNUtilsDialog.startIntent(this, intent,
                            R.string.alert_NoteView_CantSendAttMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            //==================read_menu朗读相关=====================
            case R.id.read_menu_restart://
//                mStartPos = 0;
//                setReadBarVisible(true);
//
//                if (mSynthesizerPlayer != null)
//                    mSynthesizerPlayer.playText(getNextReadStr(), null, this);
                break;

            case R.id.read_menu_continue:
//                setReadBarVisible(true);
//                if (mSynthesizerPlayer != null)
//                    mSynthesizerPlayer.playText(getNextReadStr(), null, this);
                break;

            //==================noteview_share分享相关=====================
            case R.id.noteview_menu_shareto_WX: {//分享微信好友
                mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);
                if (mNote.syncState == 1) {
                    TNUtilsUi.showToast(R.string.alert_NoteList_NotCompleted_Share);
                    break;
                }
                TNUtilsUi.sendToWX(this, mNote, false);
                break;
            }

            case R.id.noteview_menu_shareto_WXCycle: {//分享朋友圈
                mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);
                if (mNote.syncState == 1) {
                    TNUtilsUi.showToast(R.string.alert_NoteList_NotCompleted_Share);
                    break;
                }
                TNUtilsUi.sendToWX(this, mNote, true);
                break;
            }

            case R.id.noteview_menu_shareto_QQ: {//分享qq好友
                mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);
                if (mNote.syncState == 1) {
                    TNUtilsUi.showToast(R.string.alert_NoteList_NotCompleted_Share);
                    break;
                }
                TNUtilsUi.sendToQQ(TNNoteViewAct.this, mNote, mTencent, mListener);
                break;
            }

            case R.id.noteview_menu_shareto_SMS: {//分享到短信
                mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);
                if (mNote.syncState == 1) {
                    TNUtilsUi.showToast(R.string.alert_NoteList_NotCompleted_Share);
                    break;
                }
                TNUtilsUi.sendToSMS(this, mNote);
                break;
            }
            //==================share_url_menu 链接相关=====================

            case R.id.share_url_menu_copy: {//复制链接
                mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);
                ClipboardManager c = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                c.setText("http://www.qingbiji.cn/note/" + TNUtils.Hash17(mNote.noteId));
                break;
            }

            case R.id.share_url_menu_send: {//邮件发送
                mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);
                String msg = getString(R.string.shareinfo_publicnote_url, mNote.title, TNUtils.Hash17(mNote.noteId));
                String email = String.format("mailto:?subject=%s&body=%s", mNote.title, msg);
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(email));
                TNUtilsDialog.startIntent(this, intent,
                        R.string.alert_About_CantSendEmail);
                break;
            }

            case R.id.share_url_menu_open: {//打开链接
                mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.qingbiji.cn/note/" + TNUtils.Hash17(mNote.noteId)));
                TNUtilsDialog.startIntent(this, intent,
                        R.string.alert_About_CantOpenWeb);
                break;
            }

            case R.id.share_url_menu_sms: {//短信分享
                mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);

                String msg = getString(R.string.shareinfo_publicnote_url, mNote.title, TNUtils.Hash17(mNote.noteId));
                TNUtilsUi.sendToSMS(this, msg);
                break;
            }
            case R.id.share_url_menu_other: {//其他分享
                mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);

                String msg = getString(R.string.shareinfo_publicnote_url, mNote.title, TNUtils.Hash17(mNote.noteId));
                TNUtilsUi.shareContent(this, msg, "轻笔记分享");
                break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    // Implement OnClickListener
    // -------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.noteview_home: {//
                finish();
                break;
            }

            case R.id.noteview_edit://编辑
                editNote();
                break;

            case R.id.noteview_read_close:
//                if (mSynthesizerPlayer != null)
//                    mSynthesizerPlayer.cancel();
//                setReadBarVisible(false);
                break;

            case R.id.noteview_read_play://暂停
//                if (mSynthesizerPlayer == null)
//                    break;
//
//                if (mSynthesizerPlayer.getState().toString().equals("PLAYING")) {
//                    mSynthesizerPlayer.pause();
//                    ImageButton playBtn = (ImageButton) findViewById(R.id.noteview_read_play);
//                    playBtn.setImageResource(R.drawable.ic_media_play);
//                } else if (mSynthesizerPlayer.getState().toString()
//                        .equals("PAUSED")) {
//                    ImageButton playBtn = (ImageButton) findViewById(R.id.noteview_read_play);
//                    playBtn.setImageResource(R.drawable.ic_media_pause);
//                    mSynthesizerPlayer.resume();
//                    break;
//                }
                break;

            case R.id.noteview_more: {
                if (mNote.trash == 1) {
                    showRealDeleteDialog(mNote.noteLocalId);
                } else {
                    if (!isFinishing()) {
                        setPopuMenu();
                        mPopuMenu.show(v);
                    }
                }
                break;
            }

        }
    }

    @Override
    public void onPoPuMenuItemClick(int id) {
        switch (id) {
            case R.id.noteview_actionitem_share:// 分享
                openContextMenu(findViewById(R.id.noteview_share_menu));
                break;

            case R.id.noteview_actionitem_tag: {// 更换标签
                if (mNote.syncState == 1) {
                    Toast.makeText(this,
                            R.string.alert_NoteList_NotCompleted_ChangTag,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                Bundle b = new Bundle();
                b.putString("TagStrForEdit", mNote.tagStr);
                b.putSerializable("ChangeTagForNoteList", mNote.noteLocalId);
                startActivity(TNTagListAct.class, b);
                break;
            }

            case R.id.noteview_actionitem_move: {// 移动
                if (mNote.syncState == 1) {
                    Toast.makeText(this, R.string.alert_NoteList_NotCompleted_Move,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                Bundle b = new Bundle();
                b.putLong("OriginalCatId", mNote.catId);
                b.putInt("Type", 1);
                b.putLong("ChangeFolderForNoteList", mNote.noteLocalId);
                startActivity(TNCatListAct.class, b);
                break;
            }

            case R.id.noteview_actionitem_attribute:// 属性
                Bundle b = new Bundle();
                b.putLong("NoteLocalId", mNoteLocalId);
                startActivity(TNNoteInfoAct.class, b);
                break;

            case R.id.noteview_actionitem_delete: // 删除
                deleteNote();
                break;

            case R.id.noteview_actionitem_selecttext: {// 选择文本
                try {
                    Method m = WebView.class.getMethod("emulateShiftHeld",
                            (Class[]) null);
                    m.invoke(mWebView, (Object[]) null);
                } catch (Exception e) {
                    e.printStackTrace();
                    // fallback
                    KeyEvent shiftPressEvent = new KeyEvent(0, 0,
                            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
                    shiftPressEvent.dispatch(mWebView);
                }
                break;
            }
        }

    }


    // onDownloadListener
    // -------------------------------------------------------------------------------
    @Override
    public void onStart(TNNoteAtt att) {
        Message msg = new Message();
        Bundle date = new Bundle();
        date.putSerializable("att", att);
        msg.what = WEBBVIEW_START;
        msg.arg1 = 2;
        msg.setData(date);
        handler.sendMessage(msg);
    }


    @Override
    public void onEnd(TNNoteAtt att, boolean isSucess, String msg) {
        mNote = TNDbUtils.getNoteByNoteLocalId(mNoteLocalId);

        if (isSucess) {
            downloadOver(att, isSucess, msg);
        } else {
            configView();
        }
    }


    // Action respond methods
    // -------------------------------------------------------------------------------


    /**
     * 文件下载结束后的操作
     *
     * @param att
     * @param isSucess
     * @param errorMsg
     */
    public void downloadOver(TNNoteAtt att, boolean isSucess, String errorMsg) {
        if (isInFront) {
            if (isSucess) {
                mNote = TNDbUtils.getNoteByNoteLocalId(mNoteLocalId);
                MLog.i("AAA", "downloadOver: mNote=" + mNote.toString());

                mCurAtt = mNote.getAttDataByLocalId(att.attLocalId);

                try {
                    att.makeThumbnail();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                /**
                 * 编写js代码 注入 监听事件
                 */
                String s;
                if (att.type > 10000 && att.type < 20000)
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file://%s\\\" /></a></div>",
                                    att.attLocalId, att.attLocalId, att.path);
                else if (att.type > 20000 && att.type < 30000)
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file:///android_asset/audio.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");
                else if (att.type == 40001)
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file:///android_asset/pdf.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");
                else if (att.type == 40002)
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file:///android_asset/txt.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");
                else if (att.type == 40003 || att.type == 40010)
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file:///android_asset/word.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");
                else if (att.type == 40005 || att.type == 40011)
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file:///android_asset/ppt.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");
                else if (att.type == 40009 || att.type == 40012)
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file:///android_asset/excel.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");
                else
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.openAtt(%d)\\\"><img src=\\\"file:///android_asset/unknown.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");

                MLog.e("AAA", "js代码：" + s);

                Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("s", s);
                b.putLong("attLocalId", att.attLocalId);
                msg.what = WEBBVIEW_SHOW;
                msg.setData(b);
                handler.sendMessage(msg);
            } else {
                //没测试过是否可用 sjy 0809
                MLog.d("AAA", "download", "下载结束 失败");
                String s = "";
                if (TextUtils.isEmpty(att.path) && att.syncState == 1) {
                    s = String
                            .format("<div id=\\\"%d\\\"><a onClick=\\\"window.demo.downloadAtt(%d)\\\"><img id=\\\"img%d\\\" src=\\\"file:///android_asset/needdownload.png\\\" /><br />%s(%s)</a></div>",
                                    att.attLocalId, att.attLocalId,
                                    att.attLocalId, att.attName,
                                    (att.size * 100 / 1024) / 100f + "K");
                } else {
                    s = String
                            .format("<img src=\\\"file:///android_asset/missing.png\\\" />%s<br />%s(%s)",
                                    getString(R.string.alert_NoteView_AttMissing),
                                    att.attName, (att.size * 100 / 1024) / 100f
                                            + "K");
                }

                MLog.e("AAA", "TNNoteViewAct", "js代码：" + s);
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("s", s);
                b.putLong("attLocalId", att.attLocalId);
                msg.what = WEBBVIEW_SHOW;
                msg.setData(b);
                handler.sendMessage(msg);
            }
        }
    }


    private void setPopuMenu() {
        mPopuMenu = new PoPuMenuView(this);
        mPopuMenu.addItem(R.id.noteview_actionitem_tag,
                getString(R.string.noteview_actionitem_tag), mScale);
        if (Integer.valueOf(Build.VERSION.SDK) <= 7) {
            mPopuMenu.addItem(R.id.noteview_actionitem_selecttext,
                    getString(R.string.noteview_actionitem_selecttext), mScale);
        }
        mPopuMenu.addItem(R.id.noteview_actionitem_move,
                getString(R.string.noteview_actionitem_move), mScale);
        mPopuMenu.addItem(R.id.noteview_actionitem_attribute,
                getString(R.string.noteview_actionitem_attribute), mScale);
        mPopuMenu.addItem(R.id.noteview_actionitem_delete,
                getString(R.string.noteview_actionitem_delete), mScale);
        mPopuMenu.addItem(R.id.noteview_actionitem_share,
                getString(R.string.noteview_actionitem_share), mScale);

        mPopuMenu.setOnPoPuMenuItemClickListener(this);
    }

    private void deleteNote() {
        if (mNote.trash == 1) {
            showRealDeleteDialog(mNote.noteLocalId);
        } else {
            showDeleteDialog(mNote.noteLocalId);
        }
    }

    private void editNote() {
        mNote = TNDbUtils.getNoteByNoteLocalId(mNoteLocalId);
        if (mNote.trash == 1) {
            resetNoteDialog(mNote.noteLocalId);
        } else {
            if (mNote.syncState != 1) {
                Bundle b = new Bundle();
                b.putLong("NoteForEdit", mNote.noteLocalId);
                startActivity(TNNoteEditAct.class, b);
            } else {
                TNUtilsUi.showToast(R.string.alert_NoteView_NotCompleted);
            }
        }
    }

    /**
     * 下载附件
     */
    private void startAutoDownload() {
        if (!TNUtils.isNetWork())
            return;

        if (mNote == null)
            return;
        download.start();
    }

    private void setReadBarVisible(boolean visible) {
        if (visible) {
            LinearLayout readLayout = (LinearLayout) findViewById(R.id.noteview_read_layout);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY);
            readLayout.setLayoutParams(layoutParams);
            ProgressBar pb = (ProgressBar) findViewById(R.id.noteview_read_progressbar);
            pb.setProgress(0);
            pb.setSecondaryProgress(0);
            ImageButton playBtn = (ImageButton) findViewById(R.id.noteview_read_play);
            playBtn.setImageResource(R.drawable.ic_media_pause);
        } else {
            LinearLayout readLayout = (LinearLayout) findViewById(R.id.noteview_read_layout);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0,
                    Gravity.NO_GRAVITY);
            readLayout.setLayoutParams(layoutParams);
        }
    }

    private String getNextReadStr() {
        String result = null;
        if (mStartPos + 150 > mPlainText.length()) {
            result = mPlainText.substring(mStartPos);
            mEndPos = mPlainText.length();
        } else {
            // String subStr = plainText.substring(readedPos + 100, readedPos +
            // 150);
            int index = 150;
            for (int i = 100; i < 150; i++) {
                char posChar = mPlainText.charAt(mStartPos + i);
                if (posChar == '\r' || posChar == '\n' || posChar == '.'
                        || posChar == '!' || posChar == '?' || posChar == '。'
                        || posChar == '!' || posChar == '?' || posChar == '，'
                        || posChar == ',') {
                    // Log.i(TAG, "break: " + (int)posChar + posChar);
                    index = i;
                    break;
                }
            }
            if (index == 150) {
                for (int i = 100; i < 150; i++) {
                    char posChar = mPlainText.charAt(mStartPos + i);
                    if (posChar == '，' || posChar == ',') {
                        // Log.i(TAG, "break: " + (int)posChar + posChar);
                        index = i;
                        break;
                    }
                }
            }
            result = mPlainText.substring(mStartPos, mStartPos + index);
            mEndPos = mStartPos + index;
        }
        return result;
    }

    //=================================================弹窗===========================================================

    /**
     * retNote 弹窗
     *
     * @param noteLocalId
     */
    private void resetNoteDialog(final long noteLocalId) {
        dialog = new CommonDialog(this, R.string.alert_NoteView_RestoreHint,
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        if (!MyRxManager.getInstance().isSyncing()) {
                            TNUtilsUi.showNotification(TNNoteViewAct.this, R.string.alert_NoteView_Synchronizing, false);
                            //具体执行
                            mProgressDialog.show();
                            ExecutorService service = Executors.newSingleThreadExecutor();
                            service.execute(new Runnable() {
                                @Override
                                public void run() {
                                    TNDb.beginTransaction();
                                    try {
                                        TNDb.getInstance().execSQL(TNSQLString.NOTE_SET_TRASH, 0, 7, System.currentTimeMillis() / 1000, noteLocalId);

                                        TNDb.setTransactionSuccessful();
                                    } finally {
                                        TNDb.endTransaction();
                                    }
                                    handler.sendEmptyMessage(DIALOG_DELETE);
                                }
                            });

                        }
                    }

                    @Override
                    public void cancelBack() {
                        mProgressDialog.hide();
                    }

                });
        dialog.show();
    }

    /**
     * realDeleteDialog 弹窗
     *
     * @param noteLocalId
     */
    private void showRealDeleteDialog(final long noteLocalId) {
        dialog = new CommonDialog(this, R.string.alert_NoteView_RealDeleteNoteMsg,
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        if (!MyRxManager.getInstance().isSyncing()) {
                            mProgressDialog.show();
                            TNUtilsUi.showNotification(TNNoteViewAct.this, R.string.alert_NoteView_Synchronizing, false);
                            mProgressDialog.show();
                            //具体执行
                            ExecutorService service = Executors.newSingleThreadExecutor();
                            service.execute(new Runnable() {
                                @Override
                                public void run() {
                                    TNDb.beginTransaction();
                                    try {
                                        TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, 5, noteLocalId);

                                        TNDb.setTransactionSuccessful();
                                    } finally {
                                        TNDb.endTransaction();
                                    }
                                    handler.sendEmptyMessage(DIALOG_DELETE);
                                }
                            });

                        }
                    }

                    @Override
                    public void cancelBack() {
                        mProgressDialog.hide();
                    }

                });
        dialog.show();
    }

    private void saveAttDialog() {
        if (!TNUtilsAtt.hasExternalStorage()) {
            TNUtilsUi.alert(this, R.string.alert_NoSDCard);
            return;
        }
        if (!TNUtilsAtt.hasExternalStorage()) {
            TNUtilsUi.alert(this, R.string.alert_NoSDCard);
            return;
        }

        String hint = String.format(
                getString(R.string.alert_NoteView_SaveAttHint), "/ThinkerNote/"
                        + mCurAtt.attName);

        dialog = new CommonDialog(this, hint,
                "保存",
                "取消",
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        try {
                            TNUtilsAtt.copyFile(mCurAtt.path, Environment
                                    .getExternalStorageDirectory().getPath()
                                    + "/ThinkerNote/" + mCurAtt.attName);
                            TNUtilsUi.showToast(R.string.alert_NoteView_AttSaved);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void cancelBack() {
                        mProgressDialog.hide();
                    }

                });
        dialog.show();

    }

    /**
     * 删除 弹窗
     *
     * @param noteLocalId
     */
    private void showDeleteDialog(final long noteLocalId) {
        int msg = R.string.alert_NoteView_DeleteNoteMsg;
        if (TNSettings.getInstance().isInProject()) {
            msg = R.string.alert_NoteView_DeleteNoteMsg_InGroup;
        }
        dialog = new CommonDialog(this, msg,
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        if (!MyRxManager.getInstance().isSyncing()) {
                            TNUtilsUi.showNotification(TNNoteViewAct.this, R.string.alert_NoteView_Synchronizing, false);
                            mProgressDialog.show();
                            //具体执行
                            ExecutorService service = Executors.newSingleThreadExecutor();
                            service.execute(new Runnable() {
                                @Override
                                public void run() {
                                    TNDb.beginTransaction();
                                    try {
                                        TNDb.getInstance().execSQL(TNSQLString.NOTE_SET_TRASH, 2, 6, System.currentTimeMillis() / 1000, noteLocalId);

                                        TNNote note = TNDbUtils.getNoteByNoteLocalId(noteLocalId);
                                        TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);
                                        TNDb.setTransactionSuccessful();
                                    } finally {
                                        TNDb.endTransaction();
                                    }
                                    handler.sendEmptyMessage(DIALOG_DELETE);
                                }
                            });

                        }
                    }

                    @Override
                    public void cancelBack() {
                        mProgressDialog.hide();
                    }

                });
        dialog.show();
    }


    //=================================js交互========================================


    /**
     * js调用android代码
     * android4.2以后，任何为JS暴露的接口，都需要加@JavascriptInterface
     */
    final class JSInterface {
        /**
         * This is not called on the UI thread. Post a runnable to invoke
         * loadUrl on the UI thread.
         */
        @JavascriptInterface
        public void downloadAtt(long id) {
            MLog.d("download", "JSInterface-->downloadAtt:" + id);
            download.start(id);
        }

        @JavascriptInterface
        public void openAtt(long attId) {

            Message msg = Message.obtain();
            msg.what = WEBBVIEW_OPEN_ATT;
            msg.arg1 = 1;
            msg.obj = attId;
            //等待图片下载完成
            handler.sendMessage(msg);
        }

        @JavascriptInterface
        public void showSource(String html) {
            MLog.e("HTML", html);
        }
    }

    public void showInnerHTML() {
        mWebView.loadUrl("javascript:window.demo.showSource('<head>'+"
                + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
    }

    private class TNGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            View toolbar = findViewById(R.id.noteview_toolbar_layout);
            if (e2.getY() - e1.getY() > 10) {
                if (toolbar.getVisibility() != View.VISIBLE)
                    findViewById(R.id.noteview_toolbar_layout).setVisibility(View.VISIBLE);
            } else if (e1.getY() - e2.getY() > 10) {
                if (toolbar.getVisibility() != View.GONE)
                    findViewById(R.id.noteview_toolbar_layout).setVisibility(View.GONE);
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

    }

    //2-11-2
    public static void updateNote(GetNoteByNoteIdBean bean) {
        try {
            long noteId = bean.getId();
            if (noteId >= 0) {

            }
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

            MLog.e("updateNote接口返回--tempObj:" + tempObj.toString());
            if (note == null)
                NoteDbHelper.addOrUpdateNote(tempObj);
            else
                NoteDbHelper.updateNote(tempObj);
        } catch (Exception e) {
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

    //------------------------------p层调用------------------------------
    private void pGetNote(long mNoteLocalId) {
        presenter.pGetNote(mNoteLocalId);
    }

    //-----------------------------接口结果回调-------------------------------

    @Override
    public void onGetNoteSuccess(Object obj) {
        updateNote((GetNoteByNoteIdBean) obj);

        Message msg = new Message();
        msg.what = GETNOTEBYNOTEID_SUCCESS;
        handler.sendMessage(msg);

    }

    @Override
    public void onGetNoteFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }
}
