package com.thinkernote.ThinkerNote.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNActionResult;
import com.thinkernote.ThinkerNote.Action.TNAction.TNRunner;
import com.thinkernote.ThinkerNote.Adapter.TNNotesAdapter;
import com.thinkernote.ThinkerNote.DBHelper.CatDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.NoteAttrDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.NoteDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.TagDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.UserDbHelper;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Data.TNTag;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNHandleError;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsDialog;
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.PullToRefresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.thinkernote.ThinkerNote.PullToRefresh.PullToRefreshBase.OnRefreshListener;
import com.thinkernote.ThinkerNote.PullToRefresh.PullToRefreshListView;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.presenter.NoteListPresenterImpl;
import com.thinkernote.ThinkerNote._interface.p.INoteListPresenter;
import com.thinkernote.ThinkerNote._interface.v.OnNoteListListener;
import com.thinkernote.ThinkerNote._interface.v.OnSynchronizeDataListener;
import com.thinkernote.ThinkerNote._interface.v.OnSynchronizeEditListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.GetNoteByNoteIdBean;
import com.thinkernote.ThinkerNote.bean.main.NoteListBean;
import com.thinkernote.ThinkerNote.bean.main.OldNoteAddBean;
import com.thinkernote.ThinkerNote.bean.main.OldNotePicBean;
import com.thinkernote.ThinkerNote.bean.main.TagItemBean;
import com.thinkernote.ThinkerNote.bean.main.TagListBean;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 重要的类
 * <p>
 * //1 allNote, 2 cat, 3 recycle, 4 tag, 5 serch, 7 个人公开, 8  他人公开
 * 5--搜索结果 展示界面
 * 说明：
 */
public class TNNoteListAct extends TNActBase implements OnClickListener, OnItemLongClickListener,
        OnLastItemVisibleListener, OnRefreshListener, OnItemClickListener, OnNoteListListener {

    //syncData
    public static final String TAG = "TAG";//1
    public static final int DELETE_LOCALNOTE = 101;//1
    public static final int DELETE_REALNOTE = 102;//
    public static final int DELETE_REALNOTE2 = 103;//
    public static final int UPDATA_EDITNOTES = 104;//
    private static final int SEARCH = 105;

    //syncEdit
    public static final int DELETE_LOCALNOTE_2 = 106;//1
    public static final int DELETE_REALNOTE_2 = 107;//
    public static final int DELETE_REALNOTE2_2 = 108;//
    public static final int UPDATA_EDITNOTES_2 = 109;//
    public static final int SYNC_DATA_BY_NOTEID = 110;//
    public static final int DIALOG_DELETE = 111;//

    public static final int CHILD_HANDLER_1_4 = 112;//
    public static final int UI_HANDLER_1_4 = 113;//
    public static final int CHILD_HANDLER_2_11 = 114;//
    public static final int UI_HANDLER_2_11 = 115;//
    public static final int CHILD_HANDLER2_2_11 = 116;//
    public static final int UI_HANDLER2_2_11 = 117;//
    /*
     * Bundle: ListType ListDetail
     */

    private PullToRefreshListView mPullListview;
    private ListView mListView;
    private Vector<TNNote> mNotes;
    private long mCurNoteId;
    private TNNote mCurNote;
    private float mScale;
    private ProgressDialog mProgressDialog;
    private LinearLayout mLoadingView;

    private int mListType; //1 allNote, 2 cat, 3 recycle, 4 tag, 5 serch, 7 个人公开, 8  他人公开
    private long mListDetail;
    private String mKeyWord;

    private TNTag mTag;
    private TNCat mCat;

    private TNSettings mSettings = TNSettings.getInstance();

    private TNNotesAdapter mNotesAdapter = null;
    private int mCount;
    private int mPageNum = 1;

    private AlertDialog dialog;//GetDataByNoteId的弹窗；
    /**
     * 如下数据，当最后一个接口调用完成后，一定好清空数据
     */

    private String[] arrayFolderName;//第一次登录，要同步的数据，（1-1）
    private String[] arrayTagName;//第一次登录，要同步的数据，（1-2）
    //
    private Vector<TNCat> cats;//第一次登录，要同步的数据，（1-3）
    private String[] groupWorks;//（3）下第一个数组数据
    private String[] groupLife;//（3）下第2个数组数据
    private String[] groupFun;//（3）下第3个数组数据
    //
    private Vector<TNNote> addOldNotes;//（2-2）正常同步，第一个调用数据
    //    private Vector<TNNoteAtt> oldNotesAtts;//（2-3）正常同步，第一个调用数据中第一调用的数据 不可使用全局，易错
    //
    private Vector<TNNote> addNewNotes;//（2-5）正常同步，第5个调用数据
//    private Vector<TNNoteAtt> newNotesAtts;//（2-5）正常同步，第5个调用数据中第一调用的数据 不可使用全局，易错

    private Vector<TNNote> recoveryNotes;//(2-7)正常同步，第7个调用数据
//    Vector<TNNoteAtt> recoveryNotesAtts;//(2-7)正常同步，第7个调用数据中第一调用的数据 不可使用全局，易错

    Vector<TNNote> deleteNotes;//(2-8)正常同步，第8个调用数据
    Vector<TNNote> deleteRealNotes;//(2-9)正常同步，第9个调用数据
    Vector<TNNote> allNotes;//(2-10)正常同步，第10个调用数据
    Vector<TNNote> editNotes;//(2-11)正常同步，第11个调用数据
    Vector<TNNote> trashNotes;//(2-12)正常同步，第12个调用数据
    //接口返回数据
    private List<List<AllFolderItemBean>> mapList = new ArrayList<>();//递归调用使用的数据集合，size最大是5；//后台需求
    private List<AllNotesIdsBean.NoteIdItemBean> cloudIds;//2-10接口返回
    List<AllNotesIdsBean.NoteIdItemBean> trashNoteArr;//(2-12)接口返回，，第13个调用数据
    //p
    private INoteListPresenter presenter;

    // Activity methods
    // -------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notelist);
        MLog.e("TNNoteListAct" + "跳转  TNNoteListAct");
        setViews();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScale = metric.scaledDensity;

        mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);

        // TODO 未发现调用
        TNAction.regResponder(TNActionType.GetAllData, this, "respondGetAllData");
        // register action
        TNAction.regResponder(TNActionType.GetNoteListByTagId, this, "respondGetNoteList");
        TNAction.regResponder(TNActionType.GetNoteListByFolderId, this, "respondGetNoteList");
        TNAction.regResponder(TNActionType.GetAllDataByNoteId, this, "respondGetAllDataByNoteId");
        TNAction.regResponder(TNActionType.SynchronizeEdit, this, "respondSynchronizeEdit");
        TNAction.regResponder(TNActionType.Synchronize, this, "respondSynchronize");


        TNAction.regResponder(TNActionType.GetNoteListBySearch, this, "respondGetNoteListBySearch");
        TNAction.regResponder(TNActionType.GetAllData, this, "respondGetAllData");
        //
        presenter = new NoteListPresenterImpl(this, this, dataListener, editListener);
        //获取跳转值
        getIntentData();


        mNotes = new Vector<TNNote>();
        //设置list布局及适配器
        mPullListview = (PullToRefreshListView) findViewById(R.id.notelist_list);
        mListView = mPullListview.getRefreshableView();
        mLoadingView = (LinearLayout) TNUtilsUi.addListHelpInfoFootView(this, mListView, TNUtilsUi.getFootViewTitle(this, mListType), TNUtilsUi.getFootViewInfo(this, mListType));
        mNotesAdapter = new TNNotesAdapter(this, mNotes, mScale);
        mListView.setAdapter(mNotesAdapter);

        mListView.setOnItemLongClickListener(this);
        mListView.setOnItemClickListener(this);
        mPullListview.setOnRefreshListener(this);
        mPullListview.setOnLastItemVisibleListener(this);
    }

    //获取跳转值
    private void getIntentData() {
        Bundle b = getIntent().getExtras();
        mListType = b.getInt("ListType", 0);
        mCount = b.getInt("count", 0);
        if (mListType == 5) {
            mKeyWord = b.getString("ListDetail");
            findViewById(R.id.notelist_search).setVisibility(View.GONE);
        } else {
            mListDetail = b.getLong("ListDetail", -1);
        }

        if (mListType == 3) {
            findViewById(R.id.ll_clearrecycler).setVisibility(View.VISIBLE);
            findViewById(R.id.maincats_menu_clearrecycler).setOnClickListener(this);
        }
        MLog.e(TAG, "跳转后--" + "ListType=" + mListType + "tag.tagId=" + mListDetail + "--tag.noteCounts=" + mCount);
    }

    @Override
    public void onDestroy() {
        mProgressDialog.dismiss();
        super.onDestroy();
    }

    @Override
    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.maincats_toolbar_layout, R.drawable.toolbg);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null, R.id.notelist_newnote, R.drawable.newnote);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null, R.id.notelist_search, R.drawable.search);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null, R.id.notelist_sort, R.drawable.sort);
        TNUtilsSkin.setViewBackground(this, null, R.id.notelist_page_bg, R.drawable.page_bg);

        findViewById(R.id.notelist_home).setOnClickListener(this);
        findViewById(R.id.notelist_folder).setOnClickListener(this);
        findViewById(R.id.notelist_newnote).setOnClickListener(this);
        findViewById(R.id.notelist_search).setOnClickListener(this);
        findViewById(R.id.notelist_sort).setOnClickListener(this);

        registerForContextMenu(findViewById(R.id.notelist_menu));
        registerForContextMenu(findViewById(R.id.notelist_recyclermenu));
        registerForContextMenu(findViewById(R.id.notelist_itemmenu));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Bundle b = getIntent().getExtras();
        mListType = b.getInt("ListType", 0);
        mListDetail = b.getLong("ListDetail", -1);
    }

    @Override
    protected void configView() {
        if (createStatus == 0 && TNUtils.isNetWork()) {
            mPullListview.setRefreshing();
            requestData();
        } else {
            getNativeData();
        }

    }

    // Implement OnClickListener
    // -------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //=======================布局控件=======================
            case R.id.notelist_search: {
                Bundle b = new Bundle();
                b.putInt("SearchType", 1);
                startActivity(TNSearchAct.class, b);
                break;
            }
            case R.id.notelist_sort: {
                //排序
                if (mSettings.sort == TNConst.CREATETIME) {
                    mSettings.sort = TNConst.UPDATETIME;
                    TNUtilsUi.showShortToast("按更新时间排序");
                } else {
                    mSettings.sort = TNConst.CREATETIME;
                    TNUtilsUi.showShortToast("按创建时间排序");
                }
                mSettings.savePref(false);
                if (TNUtils.isNetWork()) {
                    mPullListview.setRefreshing();
                    mPageNum = 1;
                    requestData();
                } else {
                    getNativeData();
                }
                break;
            }

            case R.id.notelist_home: {
                MLog.d(TAG, "want to go home...");
                finish();
                break;
            }

            case R.id.notelist_folder: {
                if (mListType == 2) {
                    Bundle b = new Bundle();
                    b.putLong("CatId", Long.valueOf(mListDetail));
                    startActivity(TNCatInfoAct.class, b);
                } else if (mListType == 4) {
                    Bundle b = new Bundle();
                    b.putLong("TagId", Long.valueOf(mListDetail));
                    startActivity(TNTagInfoAct.class, b);
                }
                break;
            }

            case R.id.notelist_newnote: {
                TNNote note = TNNote.newNote();
                if (mListType == 1) {
                    note.catId = mSettings.defaultCatId;
                } else if (mListType == 2) {
                    note.catId = mCat.catId;
                } else if (mListType == 4) {
                    note.tagStr = mTag.tagName;
                }
                Bundle b = new Bundle();
                b.putLong("NoteForEdit", note.noteLocalId);
                b.putSerializable("NOTE", note);
                startActivity(TNNoteEditAct.class, b);
                break;
            }

            case R.id.maincats_menu_clearrecycler: {
                clearrecyclerDialog();

                break;
            }
            //=======================menu_recycler=======================
            case R.id.recycler_menu_restore: {//还原
                mMenuBuilder.destroy();
                resetNoteDialog(mCurNoteId);
                break;
            }

            case R.id.recycler_menu_delete: {//彻底删除
                mMenuBuilder.destroy();
                showRealDeleteDialog(mCurNoteId);
                break;
            }

            case R.id.recycler_menu_view: {//查看
                mMenuBuilder.destroy();
                Bundle b = new Bundle();
                b.putLong("NoteLocalId", mCurNoteId);
                startActivity(TNNoteViewAct.class, b);
                break;
            }

            case R.id.recycler_menu_cancel: {//取消
                mMenuBuilder.destroy();
                break;
            }

            //=======================notelistitem_menu=======================
            case R.id.notelistitem_menu_view: {//查看
                mMenuBuilder.destroy();
                Bundle b = new Bundle();
                b.putLong("NoteLocalId", mCurNoteId);
                startActivity(TNNoteViewAct.class, b);
                break;
            }

            case R.id.notelistitem_menu_edit: {//编辑
                mMenuBuilder.destroy();
                TNNote note = TNDbUtils.getNoteByNoteLocalId(mCurNoteId);
                if (note.trash == 1) {
                    resetNoteDialog(note.noteLocalId);
                } else {
                    if (note.syncState == 2) {
                        Bundle b = new Bundle();
                        b.putLong("NoteForEdit", note.noteLocalId);
                        b.putLong("NoteLocalId", note.noteLocalId);
                        startActivity(TNNoteEditAct.class, b);
                    } else {
                        TNHandleError.handleErrorCode(this,
                                this.getResources().getString(R.string.alert_NoteView_NotCompleted));
                    }
                }
                break;
            }

            case R.id.notelistitem_menu_changetag: {//更换标签
                mMenuBuilder.destroy();
                TNNote note = TNDbUtils.getNoteByNoteLocalId(mCurNoteId);
                if (note.syncState != 2) {
                    TNHandleError.handleErrorCode(this,
                            this.getResources().getString(R.string.alert_NoteList_NotCompleted_ChangTag));
                    break;
                }
                Bundle b = new Bundle();
                b.putString("TagStrForEdit", note.tagStr);
                b.putLong("ChangeTagForNoteList", note.noteLocalId);
                startActivity(TNTagListAct.class, b);
                break;
            }

            case R.id.notelistitem_menu_moveto: {//移动到文件夹
                mMenuBuilder.destroy();
                TNNote note = TNDbUtils.getNoteByNoteLocalId(mCurNoteId);
                if (note.syncState != 2) {
                    TNUtilsUi.showToast(R.string.alert_NoteList_NotCompleted_Move);
                    break;
                }
                Bundle b = new Bundle();
                b.putLong("OriginalCatId", note.catId);
                b.putInt("Type", 1);
                b.putLong("ChangeFolderForNoteList", note.noteLocalId);
                startActivity(TNCatListAct.class, b);
                break;
            }

            case R.id.notelistitem_menu_sync: {//完全同步
                MLog.d("TNNotelsitAct--notelistitem_menu--完全同步");
                mMenuBuilder.destroy();
                TNNote note = TNDbUtils.getNoteByNoteLocalId(mCurNoteId);
                if (note.noteId == -1) {
                    break;
                }
                showSyncDialog(note.noteId);

                break;
            }

            case R.id.notelistitem_menu_info: {//属性
                mMenuBuilder.destroy();
                Bundle b = new Bundle();
                b.putLong("NoteLocalId", mCurNoteId);
                startActivity(TNNoteInfoAct.class, b);
                break;
            }

            case R.id.notelistitem_menu_delete: {//删除
                mMenuBuilder.destroy();
                //
                showDeleteDialog(mCurNoteId);

                break;
            }

            case R.id.notelist_menu_cancel://取消
                mMenuBuilder.destroy();
                break;
        }
    }

    private void setButtonsAndNoteList() {
        MLog.i(TAG, "setButtons " + mNotes);
        String title = null;
        switch (mListType) {
            case 1:
                title = getString(R.string.notelist_allnote);
                findViewById(R.id.notelist_newnote).setVisibility(View.INVISIBLE);
                break;
            case 2:
                mCat = TNDbUtils.getCat(mListDetail);
                title = mCat.catName;
                break;
            case 3:
                title = getString(R.string.notelist_recycler);
                findViewById(R.id.notelist_newnote).setVisibility(View.INVISIBLE);
                findViewById(R.id.notelist_search).setVisibility(View.INVISIBLE);
                break;
            case 4:
                mTag = TNDbUtils.getTag(mListDetail);
                title = mTag.tagName;
                break;
            case 5:
                title = getString(R.string.notelist_search_result);
                findViewById(R.id.notelist_newnote).setVisibility(View.INVISIBLE);
                break;
        }

        Button folderBtn = (Button) findViewById(R.id.notelist_folder);
        ((TextView) findViewById(R.id.notelist_home)).setText(title);
        folderBtn.setText(String.format("%s(%d)", title, mNotes.size()));
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
        if (position > 0) {
            mCurNote = mNotes.get(position - 1);
            mCurNoteId = mCurNote.noteLocalId;
            if (mListType == 3) {
                addRecycleMenu();
            } else {
                Bundle b = new Bundle();
                b.putLong("NoteLocalId", mCurNoteId);
                startActivity(TNNoteViewAct.class, b);
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
                                   long id) {
        if (position > 0) {
            mCurNote = mNotes.get(position - 1);

            mCurNoteId = mCurNote.noteLocalId;
            if (mListType == 3) {
                addRecycleMenu();
            } else {
                addItemMenu();
            }
        }
        return true;
    }

    private void addItemMenu() {
        View view = addMenu(R.layout.menu_notelistitem);
        view.findViewById(R.id.notelistitem_menu_view).setOnClickListener(this);
        view.findViewById(R.id.notelistitem_menu_edit).setOnClickListener(this);
        view.findViewById(R.id.notelistitem_menu_sync).setOnClickListener(this);
        view.findViewById(R.id.notelistitem_menu_delete).setOnClickListener(this);
        view.findViewById(R.id.notelistitem_menu_moveto).setOnClickListener(this);
        view.findViewById(R.id.notelistitem_menu_changetag).setOnClickListener(this);
        view.findViewById(R.id.notelistitem_menu_info).setOnClickListener(this);
        view.findViewById(R.id.notelistitem_menu_cancel).setOnClickListener(this);
    }

    private void addRecycleMenu() {
        View view = addMenu(R.layout.menu_recycler);
        view.findViewById(R.id.recycler_menu_restore).setOnClickListener(this);
        view.findViewById(R.id.recycler_menu_delete).setOnClickListener(this);
        view.findViewById(R.id.recycler_menu_view).setOnClickListener(this);
        view.findViewById(R.id.recycler_menu_cancel).setOnClickListener(this);
    }

    @Override
    public void onRefresh() {
        if (TNActionUtils.isSynchronizing()) {
            TNUtilsUi.showNotification(this, R.string.alert_Synchronize_TooMuch, false);
            return;
        }
        mPageNum = 1;
        requestData();
    }

    @Override
    public void onLastItemVisible() {
        if (mPageNum != 1) {
            mLoadingView.setVisibility(View.VISIBLE);
            requestData();
        }
    }

    private void getNativeData() {
        if (mListType == 5) {
            pNoteListSearch(mKeyWord);

        } else {
            switch (mListType) {
                case 2:
                    mNotes = TNDbUtils.getNoteListByCatId(mSettings.userId, mListDetail, mSettings.sort, TNConst.MAX_PAGE_SIZE);
                    break;
                case 3:
                    mNotes = TNDbUtils.getNoteListByTrash(mSettings.userId, mSettings.sort);
                    break;
                case 4:
                    mTag = TNDbUtils.getTag(mListDetail);
                    mNotes = TNDbUtils.getNoteListByTagName(mSettings.userId, mTag.tagName, mSettings.sort, TNConst.MAX_PAGE_SIZE);
                    break;
            }

            mNotesAdapter.updateNotes(mNotes);
            mNotesAdapter.notifyDataSetChanged();
        }
        setButtonsAndNoteList();
    }

    private void requestData() {
        switch (mListType) {
            case 2://folder
                getNoteListByFolderId(mListDetail, mPageNum, TNConst.PAGE_SIZE, mSettings.sort);

                break;
            case 3:
                //CatFrag跳转值
                if (mPageNum == 1)
                    syncData();
                break;
            case 4://tag
                //
                MLog.e("获取标签数据");
                getNoteListByTagId(mListDetail, mPageNum, TNConst.PAGE_SIZE, mSettings.sort);

                break;
            case 5:
                pNoteListSearch(mKeyWord);
                break;
        }
    }

    public void dialogCallBackSyncCancell() {
        mPullListview.onRefreshComplete();
    }


    private void notifyData(NoteListBean bean) {
        mPageNum = bean.getPagenum();
        mCount = bean.getCount();

        if (mCount > mPageNum * TNConst.PAGE_SIZE) {
            mPageNum++;
        }

        switch (mListType) {
            case 2:
                mNotes = TNDbUtils.getNoteListByCatId(mSettings.userId, mListDetail, mSettings.sort, TNConst.PAGE_SIZE * mPageNum);
                break;
            case 4:
                mTag = TNDbUtils.getTag(mListDetail);
                mNotes = TNDbUtils.getNoteListByTagName(mSettings.userId, mTag.tagName, mSettings.sort, TNConst.PAGE_SIZE * mPageNum);
                break;
        }

        mNotesAdapter.updateNotes(mNotes);
        mNotesAdapter.notifyDataSetChanged();

        setButtonsAndNoteList();
    }


    public void respondGetAllData(TNAction aAction) {
        if (aAction.result == TNActionResult.Cancelled) {
            TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell, true);
        } else if (!TNHandleError.handleResult(this, aAction, false)) {
            TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
            if (TNActionUtils.isSynchroniz(aAction)) {
                TNSettings settings = TNSettings.getInstance();
                settings.originalSyncTime = System.currentTimeMillis();
                settings.savePref(false);
            }
        } else {
            TNUtilsUi.showNotification(this,
                    R.string.alert_Synchronize_Stoped, true);
        }
    }

    /**
     * 同步Data结束后的操作
     *
     * @param state 0 = 成功/1=back取消同步/2-异常触发同步终止
     */
    private void endSynchronize1(int state) {
        MLog.d("NoteList同步---结束");
        mapList.clear();
        flagMap.clear();

        mLoadingView.setVisibility(View.GONE);
        mPullListview.onRefreshComplete();
        mNotes = TNDbUtils.getNoteListByTrash(mSettings.userId, mSettings.sort);
        mNotesAdapter.updateNotes(mNotes);
        mNotesAdapter.notifyDataSetChanged();
        setButtonsAndNoteList();
        if (state == 0) {
            // 正常流程完成
        } else if (state == 1) {
            TNUtilsUi.showNotification(this, R.string.alert_Synchronize_Stoped, true);
        } else {
            TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell, true);
        }
    }

    /**
     * 同步Edit结束后的操作
     *
     * @param state 0 = 成功/1=back取消同步/2-异常触发同步终止
     */
    private void endSynchronize2(int state) {
        MLog.d("SynchronizeEdit结束");
//        mLoadingView.setVisibility(View.GONE);
//        mPullListview.onRefreshComplete();
//        mNotes = TNDbUtils.getNoteListByTrash(mSettings.userId, mSettings.sort);
//        mNotesAdapter.updateNotes(mNotes);
//        mNotesAdapter.notifyDataSetChanged();
//        setButtonsAndNoteList();

        if (state == 0) {
            // 正常流程完成
            TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
        } else if (state == 1) {
            TNUtilsUi.showNotification(this, R.string.alert_Synchronize_Stoped, true);
        } else {
            TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell, true);
        }
    }
    // ---------------------------------------弹窗----------------------------------------

    /**
     * clearrecycler 弹窗
     */
    private void clearrecyclerDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //title
        LayoutInflater lf1 = LayoutInflater.from(this);
        View title = lf1.inflate(R.layout.dialog, null);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_layout, R.drawable.page_color);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_top_bar, R.drawable.dialog_top_bg);
        TNUtilsSkin.setImageViewDrawable(this, title, R.id.dialog_icon, R.drawable.dialog_icon);
        builder.setCustomTitle(title);

        ((TextView) title.findViewById(R.id.dialog_title)).setText(R.string.alert_Title);//title

        ((TextView) title.findViewById(R.id.dialog_msg)).setText((Integer) R.string.alert_NoteList_ClearRecycle);//content

        //
        final DialogInterface.OnClickListener posListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TNActionUtils.isSynchronizing()) {
                    //具体执行
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    service.execute(new Runnable() {
                        @Override
                        public void run() {

                            Vector<TNNote> notes = TNDbUtils.getNoteListByTrash(TNSettings.getInstance().userId, TNConst.CREATETIME);
                            TNDb.beginTransaction();
                            try {
                                for (int i = 0; i < notes.size(); i++) {
                                    TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, 5, notes.get(i).noteLocalId);
                                }
                                TNDb.setTransactionSuccessful();
                            } finally {
                                TNDb.endTransaction();
                            }

                            handler.sendEmptyMessage(DIALOG_DELETE);
                        }
                    });

                }
            }
        };
        builder.setPositiveButton(R.string.alert_OK, posListener);

        //
        DialogInterface.OnClickListener negListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                dialog.dismiss();
            }
        };
        builder.setNegativeButton(R.string.alert_Cancel, negListener);
        dialog = builder.create();
        dialog.show();
    }

    /**
     * retNote 弹窗
     *
     * @param noteLocalId
     */
    private void resetNoteDialog(final long noteLocalId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //title
        LayoutInflater lf1 = LayoutInflater.from(this);
        View title = lf1.inflate(R.layout.dialog, null);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_layout, R.drawable.page_color);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_top_bar, R.drawable.dialog_top_bg);
        TNUtilsSkin.setImageViewDrawable(this, title, R.id.dialog_icon, R.drawable.dialog_icon);
        builder.setCustomTitle(title);

        ((TextView) title.findViewById(R.id.dialog_title)).setText(R.string.alert_Title);//title

        ((TextView) title.findViewById(R.id.dialog_msg)).setText((Integer) R.string.alert_NoteView_RestoreHint);//content

        //
        final DialogInterface.OnClickListener posListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TNActionUtils.isSynchronizing()) {
                    TNUtilsUi.showNotification(TNNoteListAct.this, R.string.alert_NoteView_Synchronizing, false);
                    //具体执行
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
        };
        builder.setPositiveButton(R.string.alert_OK, posListener);

        //
        DialogInterface.OnClickListener negListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                dialog.dismiss();
            }
        };
        builder.setNegativeButton(R.string.alert_Cancel, negListener);
        dialog = builder.create();
        dialog.show();
    }

    /**
     * realDeleteDialog 弹窗
     *
     * @param noteLocalId
     */
    private void showRealDeleteDialog(final long noteLocalId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //title
        LayoutInflater lf1 = LayoutInflater.from(this);
        View title = lf1.inflate(R.layout.dialog, null);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_layout, R.drawable.page_color);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_top_bar, R.drawable.dialog_top_bg);
        TNUtilsSkin.setImageViewDrawable(this, title, R.id.dialog_icon, R.drawable.dialog_icon);
        builder.setCustomTitle(title);

        ((TextView) title.findViewById(R.id.dialog_title)).setText(R.string.alert_Title);//title

        ((TextView) title.findViewById(R.id.dialog_msg)).setText((Integer) R.string.alert_NoteView_RealDeleteNoteMsg);//content

        //
        final DialogInterface.OnClickListener posListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TNActionUtils.isSynchronizing()) {
                    TNUtilsUi.showNotification(TNNoteListAct.this, R.string.alert_NoteView_Synchronizing, false);
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
        };
        builder.setPositiveButton(R.string.alert_OK, posListener);

        //
        DialogInterface.OnClickListener negListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                dialog.dismiss();
            }
        };
        builder.setNegativeButton(R.string.alert_Cancel, negListener);
        dialog = builder.create();
        dialog.show();
    }

    /**
     * 删除 弹窗
     *
     * @param noteLocalId
     */
    private void showDeleteDialog(final long noteLocalId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //title
        LayoutInflater lf1 = LayoutInflater.from(this);
        View title = lf1.inflate(R.layout.dialog, null);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_layout, R.drawable.page_color);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_top_bar, R.drawable.dialog_top_bg);
        TNUtilsSkin.setImageViewDrawable(this, title, R.id.dialog_icon, R.drawable.dialog_icon);
        builder.setCustomTitle(title);

        ((TextView) title.findViewById(R.id.dialog_title)).setText(R.string.alert_Title);//title
        //content
        int msg = R.string.alert_NoteView_DeleteNoteMsg;
        if (TNSettings.getInstance().isInProject()) {
            msg = R.string.alert_NoteView_DeleteNoteMsg_InGroup;
        }
        ((TextView) title.findViewById(R.id.dialog_msg)).setText((Integer) msg);//content

        //
        final DialogInterface.OnClickListener posListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TNActionUtils.isSynchronizing()) {
                    TNUtilsUi.showNotification(TNNoteListAct.this, R.string.alert_NoteView_Synchronizing, false);
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
        };
        builder.setPositiveButton(R.string.alert_OK, posListener);

        //
        DialogInterface.OnClickListener negListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                dialog.dismiss();
            }
        };
        builder.setNegativeButton(R.string.alert_Cancel, negListener);
        dialog = builder.create();
        dialog.show();
    }

    /**
     * 完全同步
     */
    private void showSyncDialog(final long noteId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //title
        LayoutInflater lf1 = LayoutInflater.from(this);
        View title = lf1.inflate(R.layout.dialog, null);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_layout, R.drawable.page_color);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_top_bar, R.drawable.dialog_top_bg);
        TNUtilsSkin.setImageViewDrawable(this, title, R.id.dialog_icon, R.drawable.dialog_icon);
        builder.setCustomTitle(title);
        ((TextView) title.findViewById(R.id.dialog_title)).setText(R.string.alert_Title);//title
        ((TextView) title.findViewById(R.id.dialog_msg)).setText((Integer) R.string.alert_MainCats_SynchronizeNoteAll);//content

        //
        final DialogInterface.OnClickListener posListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TNActionUtils.isSynchronizing()) {
                    TNUtilsUi.showNotification(TNNoteListAct.this, R.string.alert_NoteView_Synchronizing, false);
                    //监听
                    MLog.d("同步GetDataByNoteId");
                    pSynceDataByNoteId(noteId);
                }
            }
        };
        builder.setPositiveButton(R.string.maincats_menu_syncall, posListener);

        //
        DialogInterface.OnClickListener negListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                dialog.dismiss();
            }
        };
        builder.setNegativeButton(R.string.alert_Cancel, negListener);
        dialog = builder.create();
        dialog.show();
    }

    // ---------------------------------------handler----------------------------------------

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case SEARCH:
                mPullListview.onRefreshComplete();
                mLoadingView.setVisibility(View.GONE);
                mNotes = (Vector<TNNote>) msg.obj;
                mNotesAdapter.updateNotes(mNotes);
                mNotesAdapter.notifyDataSetChanged();
                setButtonsAndNoteList();
                break;
            case DELETE_LOCALNOTE://2-8-2的调用
                //执行下一个position/执行下一个接口
                pDelete1(((int) msg.obj + 1));
                break;

            case DELETE_REALNOTE://2-9 deleteRealNotes
                //执行下一个position/执行下一个接口
                pRealDelete1(((int) msg.obj + 1));
                break;
            case DELETE_REALNOTE2://2-9 deleteRealNotes
                //执行下一个position/执行下一个接口

                if (isRealDelete1 && isRealDelete2) {
                    //执行下一个
                    pRealDelete1((int) msg.obj + 1);

                    //复原 false,供下次循环使用
                    isRealDelete1 = false;
                    isRealDelete2 = false;
                }

                break;
            case UPDATA_EDITNOTES://2-11 更新日记时间返回
                //执行下一个position/执行下一个接口
                pEditNotePic1((int) msg.obj + 1);
                break;
            case DELETE_LOCALNOTE_2://2-8-2的调用
                //执行下一个position/执行下一个接口
                pDelete2(((int) msg.obj + 1));
                break;

            case DELETE_REALNOTE_2://2-9 deleteRealNotes
                //执行下一个position/执行下一个接口
                pRealDelete2(((int) msg.obj + 1));
                break;
            case DELETE_REALNOTE2_2://2-9 deleteRealNotes
                //执行下一个position/执行下一个接口
                if (isRealDelete1_2 && isRealDelete2_2) {
                    //执行下一个
                    pRealDelete2((int) msg.obj + 1);

                    //复原 false,供下次循环使用
                    isRealDelete1_2 = false;
                    isRealDelete2_2 = false;
                }

                break;
            case UPDATA_EDITNOTES_2://2-11 更新日记时间返回
                //执行下一个position/执行下一个接口
                pEditNotePic2((int) msg.obj + 1);
                break;
            case DIALOG_DELETE:

                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
                configView();
                if (TNUtils.isNetWork()) {
                    if (TNActionUtils.isSynchronizing()) {
                        return;
                    }
                    TNUtilsUi.showNotification(this, R.string.alert_NoteView_Synchronizing, false);
                    syncEdit();
                }
                break;
            case SYNC_DATA_BY_NOTEID:
                //关闭弹窗
                MLog.d("同步完成-->GetDataByNoteId");
                TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
                dialog.dismiss();
                dialog = null;
                TNUtilsUi.showToast("同步完成");
                break;

            case UI_HANDLER_1_4:
                //执行新循环
                syncGetFoldersByFolderId(0, true);
                break;
            case UI_HANDLER_2_11:
                Bundle bundle = msg.getData();
                int position = bundle.getInt("int");
                boolean is13 = bundle.getBoolean("boolean");

                if (is13) {
                    MLog.d("sync----2-11-2-->Success--pUpdataNote13");
                    pUpdataNote131(position + 1, is13);
                } else {
                    //执行一个position或下一个接口
                    MLog.d("sync----2-11-2-->Success--pUpdataNote");
                    pUpdataNote1(position + 1, false);
                }
                break;
            case UI_HANDLER2_2_11:
                Bundle bundle2 = msg.getData();
                int position2 = bundle2.getInt("int");
                boolean is132 = bundle2.getBoolean("boolean");

                if (is132) {
                    MLog.d("sync----2-11-2-->Success--pUpdataNote13");
                    pUpdataNote132(position2 + 1, is132);
                } else {
                    //执行一个position或下一个接口
                    MLog.d("sync----2-11-2-->Success--pUpdataNote");
                    pUpdataNote2(position2 + 1, false);
                }
                break;
        }
    }

    /**
     * 创建HandlerThread,用于异步处理数据库数据
     */
    HandlerThread handlerThread1_4 = new HandlerThread("notelist1_4");
    HandlerThread handlerThread2_11 = new HandlerThread("notelist2-11");
    Handler chlidHanlder2_11, chlidHanlder2_2_11, chlidHanlder1_4;

    // ---------------------------------------数据库操作----------------------------------------
    // 本地搜索
    private void pNoteListSearch(final String mKeyWord) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                TNSettings settings = TNSettings.getInstance();
                Vector<TNNote> notes = TNDbUtils.getNoteListBySearch(settings.userId, mKeyWord, settings.sort);

                Message msg = Message.obtain();
                msg.what = SEARCH;
                msg.obj = notes;
                handler.sendMessage(msg);
            }
        });

    }

    //异步
    public static void insertDbNotes(final NoteListBean bean, final boolean isTrash) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                List<NoteListBean.NoteItemBean> notesObj = bean.getNotes();
                if (notesObj == null || notesObj.size() <= 0) {
                    return;
                }
                int trash = isTrash ? 2 : 0;
                for (int i = 0; i < notesObj.size(); i++) {
                    NoteListBean.NoteItemBean obj = notesObj.get(i);
                    long noteId = obj.getId();
                    long lastUpdate = TNUtils.formatStringToTime((obj.getUpdate_at()) + "") / 1000;

                    List<NoteListBean.NoteItemBean.TagItemBean> tags = obj.getTags();
                    String tagStr = "";
                    for (int k = 0; k < tags.size(); k++) {
                        NoteListBean.NoteItemBean.TagItemBean tempTag = tags.get(k);
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

                    int catId = -1;
                    if (obj.getFolder_id() > 0) {
                        catId = obj.getFolder_id();
                    } else {
                        catId = -1;
                    }

                    int syncState = 1;
                    TNNote note = TNDbUtils.getNoteByNoteId(noteId);
                    if (note != null) {
                        if (note.lastUpdate > lastUpdate) {
                            continue;
                        } else {
                            syncState = note.syncState;
                        }
                    }
                    JSONObject tempObj = TNUtils.makeJSON(
                            "title", obj.getTitle(),
                            "userId", TNSettings.getInstance().userId,
                            "trash", trash,
                            "source", "android",
                            "catId", catId,
                            "content", obj.getSummary(),
                            "createTime", com.thinkernote.ThinkerNote.Utils.TimeUtils.getMillsOfDate(obj.getCreate_at()) / 1000,
                            "lastUpdate", lastUpdate,
                            "syncState", syncState,
                            "noteId", noteId,
                            "shortContent", obj.getSummary(),
                            "tagStr", tagStr,
                            "lbsLongitude", 0,
                            "lbsLatitude", 0,
                            "lbsRadius", 0,
                            "lbsAddress", "",
                            "nickName", TNSettings.getInstance().username,
                            "thumbnail", "",
                            "contentDigest", obj.getContent_digest()
                    );
                    NoteDbHelper.addOrUpdateNote(tempObj);
                }
            }
        });

    }

    /**
     * 调用图片上传，就触发更新db
     *
     * @param attrId
     */
    private void upDataAttIdSQL1(final long attrId, final TNNoteAtt tnNoteAtt) {
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
     * 调用OldNoteAdd接口，就触发更新db
     */
    private void upDataNoteLocalIdSQL1(OldNoteAddBean oldNoteAddBean, TNNote note) {
        long id = oldNoteAddBean.getId();
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_NOTEID_BY_NOTELOCALID, id, note.noteLocalId);
            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }
    }

    /**
     *
     */
    private void setChildHandler1_4() {
        if (!handlerThread1_4.isAlive()) {
            //开启handlerThread线程
            handlerThread1_4.start();
            //构建异步handler
            chlidHanlder1_4 = new Handler(handlerThread1_4.getLooper(), new Handler.Callback() {

                @Override
                public boolean handleMessage(Message msg) {
                    switch (msg.what) {
                        case CHILD_HANDLER_1_4://处理1-4接口数据
                            //获取数据
                            Bundle bundle = msg.getData();
                            long pCatId = bundle.getLong("long");
                            AllFolderBean allFolderBean = (AllFolderBean) bundle.getSerializable("bean");

                            //耗时操作
                            CatDbHelper.clearCatsByParentId(pCatId);
                            List<AllFolderItemBean> beans = allFolderBean.getFolders();

                            for (int i = 0; i < beans.size(); i++) {
                                AllFolderItemBean bean = beans.get(i);

                                JSONObject tempObj = TNUtils.makeJSON(
                                        "catName", bean.getName(),
                                        "userId", mSettings.userId,
                                        "trash", 0,
                                        "catId", bean.getId(),
                                        "noteCounts", bean.getCount(),
                                        "catCounts", bean.getFolder_count(),
                                        "deep", bean.getFolder_count() > 0 ? 1 : 0,
                                        "pCatId", pCatId,
                                        "isNew", -1,
                                        "createTime", TNUtils.formatStringToTime(bean.getCreate_at()),
                                        "lastUpdateTime", TNUtils.formatStringToTime(bean.getUpdate_at()),
                                        "strIndex", TNUtils.getPingYinIndex(bean.getName())
                                );
                                CatDbHelper.addOrUpdateCat(tempObj);
                            }

                            //操作完成，返回UI
                            handler.sendEmptyMessage(UI_HANDLER_1_4);
                            break;
                    }

                    return false;
                }
            });
        }

    }

    /**
     * 调用GetFoldersByFolderId接口，就触发插入db
     */
    private void insertDBCatsSQL1(AllFolderBean allFolderBean, long pCatId) {

        //触发 异步handler,执行耗时操作
        Bundle bundle = new Bundle();
        bundle.putLong("long", pCatId);
        bundle.putSerializable("bean", allFolderBean);
        //
        Message msg = new Message();
        msg.setData(bundle);
        msg.what = CHILD_HANDLER_1_4;//传递给异步handler耗时处理
        chlidHanlder1_4.sendMessage(msg);

    }

    /**
     * 调用recovery接口(2-7-1)，就触发更新db
     */
    private void recoveryNoteSQL1(long noteId) {

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

    private void updataDeleteNoteSQL1(long noteId) {

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
    private void deleteRealSQL1(final long nonteLocalID, final int position) {
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
     * 2-10-1 更新日记
     *
     * @param noteId
     */
    private void updataEditNotesState1(final int position, final long noteId) {
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
    private void updataEditNotes1(final int position, final TNNote note) {
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
    private void handleNote1(GetNoteByNoteIdBean bean, final int position, final boolean is13) {


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

    private void setChildHandler1_2_11(int position) {
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
                            updateNote1(myBean);

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

    /**
     * 2-11-2
     * 该处工作环境最恶劣，上千跳接口返回数据走该处执行耗时任务，有必要手动gc处理内存
     *
     * @param bean
     */
    public static void updateNote1(GetNoteByNoteIdBean bean) {
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
                            insertAttr1(tempAtt, note.noteLocalId);
                        }
                    }
                } else {
                    for (int i = 0; i < atts.size(); i++) {
                        GetNoteByNoteIdBean.Attachments tempAtt = atts.get(i);
                        syncState = 1;
                        insertAttr1(tempAtt, note.noteLocalId);
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
            MLog.e("TNNoteList--updateNote:" + e.toString());
        }
    }

    public static void insertAttr1(GetNoteByNoteIdBean.Attachments tempAtt, long noteLocalId) {
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

    //===============================syncEdit数据库更新=====================================

    /**
     * 调用图片上传，就触发更新db
     *
     * @param attrId
     */
    private void upDataAttIdSQL2(final long attrId, final TNNoteAtt tnNoteAtt) {
        upDataAttIdSQL1(attrId, tnNoteAtt);
    }

    /**
     * 调用OldNoteAdd接口，就触发更新db
     */
    private void upDataNoteLocalIdSQL2(OldNoteAddBean oldNoteAddBean, TNNote note) {
        upDataNoteLocalIdSQL2(oldNoteAddBean, note);
    }


    /**
     * 调用recovery接口(2-7-1)，就触发更新db
     */
    private void recoveryNoteSQL2(long noteId) {
        recoveryNoteSQL1(noteId);
    }

    private void updataDeleteNoteSQL2(long noteId) {
        updataDeleteNoteSQL1(noteId);
    }


    /**
     * 2-9-2接口
     */
    private void deleteRealSQL2(final long nonteLocalID, final int position) {

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
                msg.what = DELETE_REALNOTE2_2;
                handler.sendMessage(msg);
            }
        });
    }

    /**
     * 2-10-1 更新日记
     *
     * @param noteId
     */
    private void updataEditNotesState2(final int position, final long noteId) {
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
                msg.what = UPDATA_EDITNOTES_2;
                handler.sendMessage(msg);
            }
        });

    }

    /**
     * 2-11-1 更新日记时间 （接口返回处理）
     */
    private void updataEditNotes2(final int position, final TNNote note) {
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
                msg.what = UPDATA_EDITNOTES_2;
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
    private void handleNote2(GetNoteByNoteIdBean bean, final int position, final boolean is13) {


        //触发 异步handler,执行耗时操作
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", bean);
        bundle.putBoolean("boolean", is13);
        bundle.putInt("int", position);
        //向chlidHanlder2_11发送msg
        Message msg = new Message();
        msg.setData(bundle);
        msg.what = CHILD_HANDLER2_2_11;//传递给异步handler耗时处理
        chlidHanlder2_2_11.sendMessage(msg);

    }

    private void setChildHandler2_2_11(int position) {
        if (position == 0 && !handlerThread2_11.isAlive()) {
            //开启handlerThread线程
            handlerThread2_11.start();
            //构建异步handler
            chlidHanlder2_2_11 = new Handler(handlerThread2_11.getLooper(), new Handler.Callback() {

                @Override
                public boolean handleMessage(Message msg) {
                    switch (msg.what) {
                        case CHILD_HANDLER2_2_11://处理2-11-2接口数据
                            //获取数据
                            Bundle bundle = msg.getData();
                            int position = bundle.getInt("int");
                            boolean is13 = bundle.getBoolean("boolean");
                            GetNoteByNoteIdBean myBean = (GetNoteByNoteIdBean) bundle.getSerializable("bean");
                            //耗时操作
                            updateNote2(myBean);

                            //操作完成，返回UI
                            Bundle UIBundle = new Bundle();
                            UIBundle.putInt("int", position);
                            UIBundle.putBoolean("boolean", is13);
                            Message message = Message.obtain();
                            message.setData(bundle);
                            message.what = UI_HANDLER2_2_11;
                            handler.sendMessage(message);
                            break;
                    }

                    return false;
                }
            });
        }
    }


    //2-11-2
    public static void updateNote2(GetNoteByNoteIdBean bean) {
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
                            insertAttr2(tempAtt, note.noteLocalId);
                        }
                    }
                } else {
                    for (int i = 0; i < atts.size(); i++) {
                        GetNoteByNoteIdBean.Attachments tempAtt = atts.get(i);
                        syncState = 1;
                        insertAttr2(tempAtt, note.noteLocalId);
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
            MLog.e("TNNoteListAct--updateNote:" + e.toString());
        }
    }

    public static void insertAttr2(GetNoteByNoteIdBean.Attachments tempAtt, long noteLocalId) {
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


    //-------------------------------------GetDataByNoteId数据库操作----------------------------------------

    private void syncGetDataByNoteIdSQL(final long noteId) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                TNNote note = TNDbUtils.getNoteByNoteId(noteId);
                note.syncState = 2;
                if (note.attCounts > 0) {
                    for (int i = 0; i < note.atts.size(); i++) {
                        TNNoteAtt tempAtt = note.atts.get(i);
                        if (i == 0 && tempAtt.type > 10000 && tempAtt.type < 20000) {
                            TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, tempAtt.path, note.noteLocalId);
                        }
                        if (TextUtils.isEmpty(tempAtt.path) || "null".equals(tempAtt.path)) {
                            note.syncState = 1;
                        }
                    }
                }
                TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, note.syncState, note.noteLocalId);

                handler.sendEmptyMessage(SYNC_DATA_BY_NOTEID);
            }
        });

    }

    //-------------------------------------p层调用----------------------------------------
    //1-1 GetNoteListByFolderId
    private void getNoteListByFolderId(long mListDetail, int mPageNum, int size, String sort) {
        presenter.pGetNoteListByFolderID(mListDetail, mPageNum, size, sort);
    }

    //1-2 GetNoteListByTagId
    private void getNoteListByTagId(long mListDetail, int mPageNum, int size, String sort) {

        presenter.pGetNoteListByTagID(mListDetail, mPageNum, size, sort);
    }


    /**
     * GetAllDataByNoteId
     * TNActionType.GetAllDataByNoteId的顺序是：
     * GetNoteById-->SyncNoteAtt的循环下载--->数据库
     *
     * @param noteId
     */

    private void pSynceDataByNoteId(long noteId) {
        MLog.d("GetDataByNoteId-->pSynceDataByNoteId");
        presenter.pGetDataByNoteId(noteId);

    }

    private void pSynceNoteAttr(int position, Vector<TNNoteAtt> atts, long noteId) {
        if (position < atts.size()) {
            MLog.d("GetDataByNoteId-->pSynceNoteAttr");
            presenter.pSynceNoteAttr(position, atts.get(position), atts, noteId);
        } else {
            //下一个接口
            syncGetDataByNoteIdSQL(noteId);
        }

    }

    //-------------------------------------p层调用 同步Edit数据----------------------------------------

    /**
     * 同步edit
     * (二.5+二.6)正常同步 pAddNewNote
     * 说明：同(二.2+二.3)的执行顺序，先处理notepos的图片，处理完就上传notepos的文本，然后再处理notepos+1的图片，如此循环
     * 接口个数：addNewNotes.size * addNewNotes.size
     */

    private void syncEdit() {

        addNewNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 3);

        if (addNewNotes.size() > 0) {
            //先 上传数组的第一个
            TNNote tnNote = addNewNotes.get(0);
            Vector<TNNoteAtt> newNotesAtts = tnNote.atts;
            if (newNotesAtts.size() > 0) {//有图，先上传图片
                pNewNotePic2(0, newNotesAtts.size(), 0, addNewNotes.size(), newNotesAtts.get(0));
            } else {//如果没有图片，就执行OldNote
                pNewNote2(0, addNewNotes.size(), addNewNotes.get(0), false, addNewNotes.get(0).content);
            }
        } else {
            //下个执行接口
            recoveryNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 7);
            recoveryNote2(0);
        }
    }

    /**
     * (二.5)正常同步 第一个执行的接口 上传图片OldNotePic 循环调用
     * 和（二.6组成双层for循环，该处是最内层for执行）
     */
    private void pNewNotePic2(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        presenter.pNewNotePic2(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.6)正常同步 第2个执行的接口 循环调用
     * 和（二.5组成双层for循环，该处是最外层for执行）
     */

    private void pNewNote2(int position, int arraySize, TNNote tnNoteAtt, boolean isNewDb, String content) {

        presenter.pNewNote2(position, arraySize, tnNoteAtt, isNewDb, content);
    }


    /**
     * (二.7)正常同步 recoveryNote
     * 从0开始执行
     * 接口个数：如果走NoteRecovery:recoveryNotes.size /如果走NoteAdd：recoveryNotes.size * recoveryNotesattrs.size
     * 说明：同(二.7-2+二.7-3)的执行顺序，先处理recoveryNotes的图片，处理完就上传recoveryNotes的文本，然后再处理position+1的图片，如此循环
     *
     * @param position 标记，表示recoveryNotes的开始位置，非recoveryNotesAtts位置
     */
    private void recoveryNote2(int position) {
        if (position < recoveryNotes.size() && position >= 0) {
            if (recoveryNotes.get(position).noteId != -1) {
                //循环执行
                pRecoveryNote2(recoveryNotes.get(position).noteId, position, recoveryNotes.size());
            } else {
                Vector<TNNoteAtt> recoveryNotesAtts = recoveryNotes.get(position).atts;
                if (recoveryNotesAtts.size() > 0) {//有图，先上传图片
                    pRecoveryNotePic2(0, recoveryNotesAtts.size(), position, recoveryNotes.size(), recoveryNotesAtts.get(0));
                } else {//如果没有图片，就执行RecoveryNoteAdd
                    pRecoveryNoteAdd2(0, recoveryNotes.size(), recoveryNotes.get(position), true, recoveryNotes.get(position).content);
                }
            }
        } else {

            //执行下一个接口
            deleteNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 6);
            pDelete2(0);
        }

    }

    /**
     * (二.7)01
     */
    private void pRecoveryNote2(long noteID, int position, int arrySize) {
        presenter.pRecoveryNote2(noteID, position, arrySize);
    }

    /**
     * (二.7)02
     */
    private void pRecoveryNotePic2(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        presenter.pRecoveryNotePic2(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.7)03
     */
    private void pRecoveryNoteAdd2(int position, int arraySize, TNNote tnNoteAtt, boolean isNewDb, String content) {
        presenter.pRecoveryNoteAdd2(position, arraySize, tnNoteAtt, isNewDb, content);
    }


    /**
     * (二.8)
     *
     * @param position
     */
    private void pDelete2(int position) {

        if (deleteNotes.size() > 0 && position < deleteNotes.size()) {
            if (deleteNotes.get(position).noteId != -1) {
                pNoteDelete2(deleteNotes.get(position).noteId, position);
            } else {
                //不调接口
                pNoteLocalDelete2(position, deleteNotes.get(position).noteLocalId);
            }
        } else {
            //下一个接口
            deleteRealNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 5);
            pRealDelete2(0);
        }
    }

    /**
     * (二.8)
     */
    private void pNoteDelete2(long noteId, int postion) {
        presenter.pDeleteNote2(noteId, postion);
    }

    /**
     * (二.8)删除本地数据 （不调接口）
     */
    private void pNoteLocalDelete2(final int position, final long noteLocalId) {

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
                msg.what = DELETE_LOCALNOTE_2;
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
    private boolean isRealDelete1_2 = false;
    private boolean isRealDelete2_2 = false;

    private void pRealDelete2(int position) {

        if (deleteRealNotes.size() > 0 && position < deleteRealNotes.size()) {
            if (deleteRealNotes.get(position).noteId == -1) {
                //
                pDeleteReadNotesSql2(deleteRealNotes.get(position).noteLocalId, position);
            } else {
                //2个接口
                pDeleteRealNotes2(deleteRealNotes.get(position).noteId, position);
            }
        } else {
            //下一个接口
            pGetAllNoteIds2();
        }
    }


    /**
     * (二.9) deleteReadNotes
     * 数据库
     * 接口个数：2
     *
     * @param nonteLocalID
     */
    private void pDeleteReadNotesSql2(final long nonteLocalID, final int position) {
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
                msg.what = DELETE_REALNOTE_2;
                handler.sendMessage(msg);
            }
        });
    }

    /**
     * (二.9)
     */
    private void pDeleteRealNotes2(long noteId, int postion) {
        //
        presenter.pDeleteRealNotes2(noteId, postion);

    }

    /**
     * (二.10)
     */
    private void pGetAllNoteIds2() {
        //
        presenter.pGetAllNotesId2();
    }

    /**
     * (二.10)-1 editNote上传图片
     * 说明：
     * 2-10-1和2-11-1图片上传和2-5/2-6相同
     *
     * @param position cloudIds数据的其实操作位置
     */
    private void pEditNotePic2(int position) {
        if (cloudIds.size() > 0 && position < (cloudIds.size() - 1)) {
            long id = cloudIds.get(position).getId();
            int lastUpdate = cloudIds.get(position).getUpdate_at();

            if (editNotes == null || editNotes.size() <= 0) {
                //执行下一个接口
                pUpdataNote2(0, false);
            }
            //找出该日记，比较时间
            for (int j = 0; j < editNotes.size(); j++) {

                if (id == editNotes.get(j).noteId) {
                    if (editNotes.get(j).lastUpdate > lastUpdate) {
                        //上传图片，之后上传文本
                        TNNote note = EditNotePicBefore(editNotes.get(j));//上传图片，处理content参数
                        pEditNotePic2(position, 0, note);
                    } else {
                        updataEditNotesState2(position, editNotes.get(j).noteLocalId);
                    }
                }
                if ((j == (editNotes.size() - 1)) && id != editNotes.get(j).noteId) {
                    //执行下一个position
                    pEditNotePic2(position + 1);
                }
            }

        } else {
            pUpdataNote2(0, false);
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
     * @param tnNote
     */
    private void pEditNotePic2(int cloudsPos, int attsPos, TNNote tnNote) {
        TNNote note = tnNote;
        if (cloudIds.size() > 0 && cloudsPos < (cloudIds.size() - 1)) {

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
                    pEditNotePic2(cloudsPos, attsPos + 1, note);

                } else {
                    //接口，上传图片
                    presenter.pEditNotePic2(cloudsPos, attsPos, note);
                }
            } else {
                //图片上传完，再上传文本
                pEditNotes2(cloudsPos, note);
            }
        } else {
            //执行下一个接口
            pUpdataNote2(0, false);

        }

    }


    /**
     * (二.11)-1 edit  通过最后更新时间来与云端比较，是否该上传本地编辑的笔记
     * 上传文本
     *
     * @param cloudsPos cloudIds数据的其实操作位置
     */
    private void pEditNotes2(int cloudsPos, TNNote note) {
        if (cloudIds.size() > 0 && cloudsPos < (cloudIds.size() - 1)) {
            presenter.pEditNote2(cloudsPos, note);
        } else {
            //执行下一个接口
            pUpdataNote2(0, false);
        }
    }

    /**
     * (二.11)-2 更新云端的笔记
     *
     * @param position 执行的位置
     * @param is13     (二.11)-2和(二.13)调用同一个接口，用于区分
     */
    private void pUpdataNote2(int position, boolean is13) {
        setChildHandler2_2_11(position);
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
                        pUpdataNote2(position, id, is13);
                    }
                    break;
                }
            }
            if (!isExit) {
                pUpdataNote2(position, id, is13);
            } else {
                //
                //下一个position
                pUpdataNote2(position + 1, is13);
            }
        } else {
            //下一个接口
            //同步回收站的笔记
            trashNotes = TNDbUtils.getNoteListByTrash(mSettings.userId, TNConst.CREATETIME);
            pTrashNotes2();

        }
    }

    /**
     * (二.11)-2/(二.13) 更新云端的笔记
     * <p>
     * p层
     */
    private void pUpdataNote2(int position, long noteId, boolean is13) {
        presenter.pGetNoteByNoteId2(position, noteId, is13);
    }

    /**
     * (二.12) 同步回收站的笔记
     */
    private void pTrashNotes2() {
        presenter.pGetAllTrashNoteIds2();
    }

    /**
     * (二.13) 更新云端的笔记
     * <p>
     * 该接口同(二.11)-2
     *
     * @param position
     * @param is13
     */
    private void pUpdataNote132(int position, boolean is13) {
        if (trashNoteArr.size() > 0 && (position < trashNoteArr.size()) && position >= 0) {
            AllNotesIdsBean.NoteIdItemBean bean = trashNoteArr.get(position);
            long noteId = bean.getId();
            boolean trashNoteExit = false;
            for (TNNote trashNote : trashNotes) {
                if (trashNote.noteId == noteId) {
                    trashNoteExit = true;
                    break;
                }
            }
            if (!trashNoteExit) {
                pUpdataNote2(position, noteId, is13);
            } else {
                pUpdataNote132(position + 1, is13);
            }
        } else {
            //同步所有接口完成，结束同步
            endSynchronize2(0);
        }
    }

    //-------------------------------------p层调用 同步所有数据----------------------------------------

    //-------第一次登录同步的p调用-------

    /**
     * (一)同步 第一个调用的方法
     * 执行顺序：先arrayFolderName对应的所有接口，再arrayTagName对应的所有接口，
     * 接口个数=arrayFolderName.size + arrayTagName.size
     */

    private void syncData() {
        MLog.d("NoteList同步---syncData");
        //TODO
        //        TNAction.runActionAsync(TNActionType.Synchronize, "Trash");
        if (mSettings.firstLaunch) {//如果第一次登录app，执行该处方法
            //需要同步的文件数据
            arrayFolderName = new String[]{TNConst.FOLDER_DEFAULT, TNConst.FOLDER_MEMO, TNConst.GROUP_FUN, TNConst.GROUP_WORK, TNConst.GROUP_LIFE};
            arrayTagName = new String[]{TNConst.TAG_IMPORTANT, TNConst.TAG_TODO, TNConst.TAG_GOODSOFT};

            //同步第一个数据（有数组，循环调用）
            pFolderAdd(0, arrayFolderName.length, arrayFolderName[0]);
        } else {//如果正常启动，执行该处
            syncOldNote1();
        }
    }

    /**
     * 第一次登录同步(按如下执行顺序调用接口)
     * <p>
     * （一.1）更新 文件
     */
    private void pFolderAdd(int position, int arraySize, String name) {
        MLog.d("sync---1-1-synchronizeData-pFolderAdd");
        if (position < arraySize) {
            presenter.folderAdd(position, arraySize, name);
        } else {//同步完成后，再同步其他接口列表数据
            //（有数组，循环调用）
            pTagAdd(0, arrayTagName.length, arrayTagName[0]);
        }
    }

    /**
     * 0720改：先执行syncOldNote--->syncProfile()--syncGetFolder()--pGetTagList()
     * 第一次登录同步
     * <p>
     * （一.2）更新 tag
     */
    private void pTagAdd(int position, int arraySize, String name) {
        MLog.d("NoteList同步---pTagAdd 1-2");
        presenter.tagAdd(position, arraySize, name);
    }

    //-------正常登录同步的p调用-------

    /**
     * 0720改：先执行syncOldNote--->syncProfile()--syncGetFolder()--pGetTagList()
     * <p>
     * 1.3---1.5是GetAllFolders所有步骤
     * <p>
     * （一.3）更新 GetFolder
     */
    private void syncGetFolder() {
        //cats.size()==0||main|catsFrag必执行，其他界面不执行
        setChildHandler1_4();
        Vector<TNCat> cats = TNDbUtils.getAllCatList(mSettings.userId);
        if (cats.size() == 0) {
            MLog.d("NoteList同步---syncGetFolder 1-3");
            presenter.pGetFolder();
        } else {
            //执行下一个接口
            pGetTagList1();
        }
    }

    /**
     * 第一次登录同步 每一次层的调用
     * <p>
     * （一.4） GetFoldersByFolderId
     * list中都要调用接口，串行调用
     * 接口个数= allFolderItemBeans.size的n个连乘（n最大5）
     * <p>
     * 说明：此处有个bug，如果mapList的第一层执行到有getFolder_count>=1时，就会调用接口，使mapList增加一层，这没有问题。
     * 但是，当新增的一层执行完后，还会返回执行mapList的第一层，还是从0开始的，又会触碰getFolder_count>=1这个已经执行完的位置，所以需要标记跳过他,避免重复执行
     *
     * @param isAdd 如果mapList.add之后立即执行该方法，为true
     */
    Map<String, Integer> flagMap = new HashMap<>();//key值用mapList.size+"A"+position 标记

    private void syncGetFoldersByFolderId(int startPos, boolean isAdd) {
        MLog.d("1-4--syncGetFoldersByFolderId--mapList.size()=" + mapList.size());
        if (mapList.size() > 0 && mapList.size() <= 5) {
            //有1---5，for循环层层内嵌,从最内层（mapList.size最大处）开始执行
            List<AllFolderItemBean> allFolderItemBeans = mapList.get(mapList.size() - 1);
            //
            MLog.d("1-4--syncGetFoldersByFolderId--allFolderItemBeans.size()=" + allFolderItemBeans.size());
            if (allFolderItemBeans.size() > 0) {
                if (startPos < allFolderItemBeans.size() - 1) {
                    //从1层的第一个数据开始
                    if (isAdd) {
                        syncGetFoldersByFolderId(0, allFolderItemBeans);
                    } else {
                        syncGetFoldersByFolderId(startPos, allFolderItemBeans);
                    }
                } else {
                    //执行上一层的循环
                    if (mapList.size() == 1) {
                        //执行下一个接口
                        syncTNCat();
                    } else {
                        //执行上一层的循环
                        MLog.d("执行上一层的循环");
                        mapList.remove(mapList.size() - 1);//移除最后一个item

                        //移除最后一层后，暴露上一层，所以需要获取上一层的执行过的位置，从该位置继续执行
                        List<AllFolderItemBean> allFolderItemBeans2 = mapList.get(mapList.size() - 1);
                        for (int i = 0; i < allFolderItemBeans2.size(); i++) {

                            if (flagMap.get(mapList.size() + "A" + i) != null) {//查找出该存储的值
                                int newPos = flagMap.get(mapList.size() + "A" + i);//key获取value
                                //移除
                                flagMap.remove(mapList.size() + "A" + i);
                                syncGetFoldersByFolderId(newPos + 1, false);//
                                break;
                            }

                        }

                    }
                }
            } else {
                //执行上一层的循环
                if (mapList.size() == 1) {
                    //执行下一个接口
                    syncTNCat();
                } else {
                    //执行上一层的新循环
                    MLog.d("执行上一层的循环");
                    mapList.remove(mapList.size() - 1);//移除最后一个item
                    //移除最后一层后，暴露上一层，所以需要获取上一层的执行过的位置，从该位置继续执行
                    List<AllFolderItemBean> allFolderItemBeans2 = mapList.get(mapList.size() - 1);
                    for (int i = 0; i < allFolderItemBeans2.size(); i++) {
                        if (flagMap.get(mapList.size() + "A" + i) != null) {//查找出该存储的值
                            int newPos = flagMap.get(mapList.size() + "A" + i);
                            //移除
                            flagMap.remove(mapList.size() + "A" + i);
                            syncGetFoldersByFolderId(newPos + 1, false);//
                            break;
                        } else {
                            MLog.e("卡死在这里了");
                        }
                    }
                }
            }

        } else {
            TNUtilsUi.showToast("递归调用数据出错了！");
        }
    }

    /**
     * 执行GetFoldersByFolderId的具体步骤 p层调用
     */

    private void syncGetFoldersByFolderId(int startPos, List<AllFolderItemBean> beans) {
        MLog.e("sync---1-4-syncGetFoldersByFolderId--startPos=" + startPos + "--Folder_count=" + beans.get(startPos).getFolder_count());
        if (beans.get(startPos).getFolder_count() == 0) {//没有数据就跳过
            MLog.d("sync---1-4-syncGetFoldersByFolderId--下一个position");
            syncGetFoldersByFolderId(startPos + 1, false);
        } else {
            MLog.d("sync---1-4-syncGetFoldersByFolderId--调用当前position接口：");
            presenter.pGetFoldersByFolderId(beans.get(startPos).getId(), startPos, beans);
        }
    }
    /**
     * （一.5）更新TNCat
     * 双层for循环的样式,串行执行接口
     * 接口个数 = 3*cats.size*groupXXX.size;
     */
    private void syncTNCat() {
        MLog.d("sync---1-5-syncTNCat");
        MLog.d("sync---1-5-syncTNCat");
        if (mSettings.firstLaunch) {
            //同步TNCat
            cats = TNDbUtils.getAllCatList(mSettings.userId);
            if (cats == null || cats.size() <= 0) {
                //执行下一个接口
                pGetTagList1();
            } else if (cats.size() > 0) {
                //先执行最外层的数据
                syncTNCat(0, cats.size());
            }
        }

    }

    /**
     * （一.5）更新 postion的TNCat数据
     *
     * @param postion
     */
    private void syncTNCat(int postion, int catsSize) {
        MLog.d("NoteList同步---syncTNCat 1-5");
        if (postion < catsSize ) {
            //获取postion条数据
            TNCat tempCat = cats.get(postion);

            if (TNConst.GROUP_WORK.equals(tempCat.catName)) {
                groupWorks = new String[]{TNConst.FOLDER_WORK_NOTE, TNConst.FOLDER_WORK_UNFINISHED, TNConst.FOLDER_WORK_FINISHED};
            }
            if (TNConst.GROUP_LIFE.equals(tempCat.catName)) {
                groupLife = new String[]{TNConst.FOLDER_LIFE_DIARY, TNConst.FOLDER_LIFE_KNOWLEDGE, TNConst.FOLDER_LIFE_PHOTO};

            }
            if (TNConst.GROUP_FUN.equals(tempCat.catName)) {
                groupFun = new String[]{TNConst.FOLDER_FUN_TRAVEL, TNConst.FOLDER_FUN_MOVIE, TNConst.FOLDER_FUN_GAME};
            }

            //执行顺序:groupWorks-->groupLife-->groupFun
            if (groupWorks == null && groupLife == null && groupFun == null) {
                //postion下没有数据，执行下个position
                syncTNCat(postion + 1, catsSize);
            } else {
                if (groupWorks != null) {
                    //执行顺序:groupWorks-->groupLife-->groupFun
                    pFirstFolderAdd(0, groupWorks.length, tempCat.catId, tempCat.catName, postion, 1);//执行第一个
                } else {
                    if (groupLife != null) {
                        //执行顺序:groupWorks-->groupLife-->groupFun
                        pFirstFolderAdd(0, groupLife.length, tempCat.catId, tempCat.catName, postion, 2);//执行第2个
                    } else {
                        //保险一点，我对这个数据不甚了解 sjy 0622
                        if (groupFun != null) {
                            //执行顺序:groupWorks-->groupLife-->groupFun
                            pFirstFolderAdd(0, groupFun.length, tempCat.catId, tempCat.catName, postion, 2);//执行第3个
                        } else {
                            //postion下没有数据，执行下个position
                            syncTNCat(postion + 1, catsSize);
                        }

                    }
                }
            }
        } else {
            //执行下一个接口
            pGetTagList1();
        }
    }

    /**
     * （一.5）具体执行TNCat的步骤 p层调用
     *
     * @param workPos
     * @param workSize
     * @param catID
     * @param catPos
     * @param flag     TNCat下有三条数据数组，flag决定执行哪一条数据的标记
     */
    private void pFirstFolderAdd(int workPos, int workSize, long catID, String name, int catPos, int flag) {
        MLog.d("NoteList同步---pFirstFolderAdd 1-5");
        presenter.pFirstFolderAdd(workPos, workSize, catID, name, catPos, flag);
    }


    //-------正常登录同步的p调用-------


    /**
     * 0720改：先执行syncOldNote--->syncProfile()--syncGetFolder()--pGetTagList()
     * <p>
     * （二。2+二。3）正常登录的数据同步（非第一次登录的同步）
     * 执行顺序：同步老数据(先上传图片接口，再OldNote接口)，没有老数据就同步用户信息接口
     * 接口个数 = addOldNotes.size * oldNotesAtts.size;
     */
    private void syncOldNote1() {
        MLog.d("NoteList同步---syncOldNote1 2-2");
        if (!mSettings.syncOldDb) {
            //add老数据库的笔记
            addOldNotes = TNDbUtils.getOldDbNotesByUserId(TNSettings.getInstance().userId);
            if (addOldNotes.size() > 0) {
                //先 上传数组的第一个
                TNNote tnNote = addOldNotes.get(0);
                Vector<TNNoteAtt> oldNotesAtts = tnNote.atts;
                if (oldNotesAtts.size() > 0) {//有图，先上传图片
                    pUploadOldNotePic1(0, oldNotesAtts.size(), 0, addOldNotes.size(), oldNotesAtts.get(0));
                } else {//如果没有图片，就执行OldNote
                    pOldNote1(0, addOldNotes.size(), addOldNotes.get(0), false, addOldNotes.get(0).content);
                }
            } else {
                //下个执行接口
                syncProfile();
            }
        } else {
            //下个执行接口
            syncProfile();
        }
    }

    /**
     * (二.2)正常同步 第一个执行的接口 上传图片OldNotePic 循环调用
     * 说明：先处理notepos的图片，处理完就上传notepos的文本，然后再处理notepos+1的图片...,如此循环
     * 和（二.3组成双层for循环，该处是最内层for执行）
     */
    private void pUploadOldNotePic1(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        MLog.d("NoteList同步---pUploadOldNotePic1 2-2");
        presenter.pUploadOldNotePic(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.3)正常同步 第2个执行的接口 循环调用
     * 和（二.2组成双层for循环，该处是最外层for执行）
     */

    private void pOldNote1(int position, int arraySize, TNNote tnNoteAtt, boolean isNewDb, String content) {
        MLog.d("NoteList同步---pOldNote1 2-3");
        presenter.pOldNoteAdd(position, arraySize, tnNoteAtt, isNewDb, content);
    }

    /**
     * 0720改：先执行syncOldNote--->syncProfile()--syncGetFolder()--pGetTagList()
     * <p>
     * （二.1）正常同步 第一个接口
     */
    private void syncProfile() {
        mSettings.syncOldDb = true;
        mSettings.savePref(false);

        MLog.d("NoteList同步---syncProfile 2-1");
        presenter.pProfile();
    }

    /**
     * 0720改：先执行syncOldNote--->syncProfile()--syncGetFolder()--pGetTagList()
     * (二.4)正常同步 pGetTagList
     */

    private void pGetTagList1() {
        Vector<TNTag> tags = TNDbUtils.getTagList(mSettings.userId);
        if (tags.size() == 0) {
            MLog.d("NoteList同步---pGetTagList1 2-4");
            presenter.pGetTagList();
        } else {
            //执行下一个接口
            pAddNewNote1();
        }

    }


    /**
     * (二.5+二.6)正常同步 pAddNewNote
     * 说明：同(二.2+二.3)的执行顺序，先处理notepos的图片，处理完就上传notepos的文本，然后再处理notepos+1的图片，如此循环
     * 接口个数：addNewNotes.size * addNewNotes.size
     */

    private void pAddNewNote1() {
        MLog.d("NoteList同步---pAddNewNote1 2-5");
        addNewNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 3);

        if (addNewNotes.size() > 0) {
            //先 上传数组的第一个
            TNNote tnNote = addNewNotes.get(0);
            Vector<TNNoteAtt> newNotesAtts = tnNote.atts;
            if (newNotesAtts.size() > 0) {//有图，先上传图片
                pNewNotePic1(0, newNotesAtts.size(), 0, addNewNotes.size(), newNotesAtts.get(0));
            } else {//如果没有图片，就执行OldNote
                pNewNote1(0, addNewNotes.size(), addNewNotes.get(0), false, addNewNotes.get(0).content);
            }
        } else {
            //下个执行接口
            recoveryNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 7);
            recoveryNote1(0);
        }
    }

    /**
     * (二.5)正常同步 第一个执行的接口 上传图片OldNotePic 循环调用
     * 和（二.6组成双层for循环，该处是最内层for执行）
     */
    private void pNewNotePic1(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        MLog.d("NoteList同步---pNewNotePic1 2-5");
        presenter.pNewNotePic(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.6)正常同步 第2个执行的接口 循环调用
     * 和（二.5组成双层for循环，该处是最外层for执行）
     */

    private void pNewNote1(int position, int arraySize, TNNote tnNoteAtt, boolean isNewDb, String content) {
        MLog.d("NoteList同步---pNewNote1 2-6");
        presenter.pNewNote(position, arraySize, tnNoteAtt, isNewDb, content);
    }


    /**
     * (二.7)正常同步 recoveryNote
     * 从0开始执行
     * 接口个数：如果走NoteRecovery:recoveryNotes.size /如果走NoteAdd：recoveryNotes.size * recoveryNotesattrs.size
     * 说明：同(二.7-2+二.7-3)的执行顺序，先处理recoveryNotes的图片，处理完就上传recoveryNotes的文本，然后再处理position+1的图片，如此循环
     *
     * @param position 标记，表示recoveryNotes的开始位置，非recoveryNotesAtts位置
     */
    private void recoveryNote1(int position) {
        MLog.d("NoteList同步---recoveryNote1 2-7");
        if (position < recoveryNotes.size() && position >= 0) {
            if (recoveryNotes.get(position).noteId != -1) {
                //循环执行
                pRecoveryNote1(recoveryNotes.get(position).noteId, position, recoveryNotes.size());
            } else {
                Vector<TNNoteAtt> recoveryNotesAtts = recoveryNotes.get(position).atts;
                if (recoveryNotesAtts.size() > 0) {//有图，先上传图片
                    pRecoveryNotePic1(0, recoveryNotesAtts.size(), position, recoveryNotes.size(), recoveryNotesAtts.get(0));
                } else {//如果没有图片，就执行RecoveryNoteAdd
                    pRecoveryNoteAdd1(0, recoveryNotes.size(), recoveryNotes.get(position), true, recoveryNotes.get(position).content);
                }
            }
        } else {

            //执行下一个接口
            deleteNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 6);
            pDelete1(0);
        }

    }

    /**
     * (二.7)01
     */
    private void pRecoveryNote1(long noteID, int position, int arrySize) {
        MLog.d("NoteList同步---pRecoveryNote1 2-7-1");
        presenter.pRecoveryNote(noteID, position, arrySize);
    }

    /**
     * (二.7)02
     */
    private void pRecoveryNotePic1(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        MLog.d("NoteList同步---pRecoveryNotePic1 2-7-2");
        presenter.pRecoveryNotePic(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.7)03
     */
    private void pRecoveryNoteAdd1(int position, int arraySize, TNNote tnNoteAtt, boolean isNewDb, String content) {
        MLog.d("NoteList同步---pRecoveryNoteAdd1 2-7-3");
        presenter.pRecoveryNoteAdd(position, arraySize, tnNoteAtt, isNewDb, content);
    }


    /**
     * (二.8)
     *
     * @param position
     */
    private void pDelete1(int position) {
        MLog.d("NoteList同步---pDelete1 2-8");
        if (deleteNotes.size() > 0 && position < deleteNotes.size()) {
            if (deleteNotes.get(position).noteId != -1) {
                pNoteDelete1(deleteNotes.get(position).noteId, position);
            } else {
                //不调接口
                pNoteLocalDelete1(position, deleteNotes.get(position).noteLocalId);
            }
        } else {
            //下一个接口
            deleteRealNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 5);
            pRealDelete1(0);
        }
    }

    /**
     * (二.8)
     */
    private void pNoteDelete1(long noteId, int postion) {
        MLog.d("NoteList同步---pNoteDelete1 2-8");
        presenter.pDeleteNote(noteId, postion);
    }

    /**
     * (二.8)删除本地数据 （不调接口）
     */
    private void pNoteLocalDelete1(final int position, final long noteLocalId) {

        MLog.d("NoteList同步---pNoteLocalDelete1 2-8");
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

    private void pRealDelete1(int position) {

        MLog.d("NoteList同步---pRealDelete1 2-9");
        if (deleteRealNotes.size() > 0 && position < deleteRealNotes.size()) {
            if (deleteRealNotes.get(position).noteId == -1) {
                //
                pDeleteReadNotesSql1(deleteRealNotes.get(position).noteLocalId, position);
            } else {
                //2个接口
                pDeleteRealNotes1(deleteRealNotes.get(position).noteId, position);
            }
        } else {
            //下一个接口
            pGetAllNoteIds1();
        }
    }


    /**
     * (二.9) deleteReadNotes
     * 数据库
     * 接口个数：2
     *
     * @param nonteLocalID
     */
    private void pDeleteReadNotesSql1(final long nonteLocalID, final int position) {
        MLog.d("NoteList同步---pDeleteReadNotesSql1 2-9");
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
    private void pDeleteRealNotes1(long noteId, int postion) {
        //
        MLog.d("NoteList同步---pDeleteRealNotes1 2-9");
        presenter.pDeleteRealNotes(noteId, postion);

    }

    /**
     * (二.10)
     */
    private void pGetAllNoteIds1() {
        MLog.d("NoteList同步---pGetAllNoteIds1 2-10");
        //
        presenter.pGetAllNotesId();
    }

    /**
     * (二.10)-1 editNote上传图片
     * 说明：
     * 2-10-1和2-11-1图片上传和2-5/2-6相同
     *
     * @param position cloudIds数据的其实操作位置
     */
    private void pEditNotePic1(int position) {
        MLog.d("NoteList同步---pEditNotePic1 2-10-1");
        if (cloudIds.size() > 0 && position < (cloudIds.size())) {
            long id = cloudIds.get(position).getId();
            int lastUpdate = cloudIds.get(position).getUpdate_at();
            if (editNotes == null || editNotes.size() <= 0) {
                //执行下一个接口
                pUpdataNote1(0, false);
            }
            //找出该日记，比较时间
            for (int j = 0; j < editNotes.size(); j++) {

                if (id == editNotes.get(j).noteId) {
                    if (editNotes.get(j).lastUpdate > lastUpdate) {
                        //上传图片，之后上传文本
                        TNNote note = EditNotePicBefore(editNotes.get(j));//上传图片，处理content参数
                        pEditNotePic1(position, 0, note);
                    } else {
                        updataEditNotesState1(position, editNotes.get(j).noteLocalId);

                    }
                }
                if ((j == (editNotes.size() - 1)) && id != editNotes.get(j).noteId) {
                    //执行下一个position
                    pEditNotePic1(position + 1);
                }
            }

        } else {
            //执行下一个接口
            pUpdataNote1(0, false);
        }
    }

    /**
     * (二.10)-1
     * 图片上传
     *
     * @param cloudsPos cloudIds数据的其实操作位置
     * @param tnNote
     */
    private void pEditNotePic1(int cloudsPos, int attsPos, TNNote tnNote) {
        MLog.d("NoteList同步---pEditNotePic1 2-10-1");
        TNNote note = tnNote;
        if (cloudIds.size() > 0 && cloudsPos < (cloudIds.size())) {
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
                    pEditNotePic1(cloudsPos, attsPos + 1, note);

                } else {
                    //接口，上传图片
                    presenter.pEditNotePic(cloudsPos, attsPos, note);
                }
            } else {
                //图片上传完，再上传文本
                pEditNotes1(cloudsPos, note);
            }
        } else {
            //执行下一个接口
            pUpdataNote1(0, false);

        }

    }


    /**
     * (二.11)-1 edit  通过最后更新时间来与云端比较，是否该上传本地编辑的笔记
     * 上传文本
     *
     * @param cloudsPos cloudIds数据的其实操作位置
     */
    private void pEditNotes1(int cloudsPos, TNNote note) {
        MLog.d("NoteList同步---pEditNotes1 2-11-1");
        if (cloudIds.size() > 0 && cloudsPos < (cloudIds.size() - 1)) {
            presenter.pEditNote(cloudsPos, note);
        } else {
            //执行下一个接口
            pUpdataNote1(0, false);
        }
    }

    /**
     * (二.11)-2 更新云端的笔记
     *
     * @param position 执行的位置
     * @param is13     (二.11)-2和(二.13)调用同一个接口，用于区分
     */
    private void pUpdataNote1(int position, boolean is13) {
        MLog.d("NoteList同步---pUpdataNote1 2-11-2");
        //
        setChildHandler1_2_11(position);

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
                        pUpdataNote1(position, id, is13);
                    }
                    break;
                }
            }
            if (!isExit) {
                pUpdataNote1(position, id, is13);
            } else {
                //
                //下一个position
                pUpdataNote1(position + 1, is13);
            }
        } else {
            //下一个接口
            //同步回收站的笔记
            trashNotes = TNDbUtils.getNoteListByTrash(mSettings.userId, TNConst.CREATETIME);
            pTrashNotes1();

        }
    }

    /**
     * (二.11)-2/(二.13) 更新云端的笔记
     * <p>
     * p层
     */
    private void pUpdataNote1(int position, long noteId, boolean is13) {
        MLog.d("NoteList同步---pUpdataNote1 2-11-2");
        presenter.pGetNoteByNoteId(position, noteId, is13);
    }

    /**
     * (二.12) 同步回收站的笔记
     */
    private void pTrashNotes1() {
        MLog.d("NoteList同步---pTrashNotes1 2-12");
        presenter.pGetAllTrashNoteIds();
    }

    /**
     * (二.13) 更新云端的笔记
     * <p>
     * 该接口同(二.11)-2
     *
     * @param position
     * @param is13
     */
    private void pUpdataNote131(int position, boolean is13) {
        MLog.d("NoteList同步---pUpdataNote131 2-13");
        if (trashNoteArr.size() > 0 && (position < trashNoteArr.size()) && position >= 0) {
            AllNotesIdsBean.NoteIdItemBean bean = trashNoteArr.get(position);
            long noteId = bean.getId();
            boolean trashNoteExit = false;
            for (TNNote trashNote : trashNotes) {
                if (trashNote.noteId == noteId) {
                    trashNoteExit = true;
                    break;
                }
            }
            if (!trashNoteExit) {
                pUpdataNote1(position, noteId, is13);
            } else {
                pUpdataNote131(position + 1, is13);
            }
        } else {
            //同步所有接口完成，结束同步
            endSynchronize1(0);
        }
    }


    //==================================接口结果返回=======================================

    //1-1/同1-2
    @Override
    public void onListByFolderIdSuccess(Object obj, long tagId, int mPageNum, int pageSize, String sort) {
        NoteListBean bean = (NoteListBean) obj;
        insertDbNotes(bean, false);//异步

        mLoadingView.setVisibility(View.GONE);
        mPullListview.onRefreshComplete();

        if (pageSize == TNConst.MAX_PAGE_SIZE) {
            int currentCount = mPageNum * TNConst.PAGE_SIZE;
            int count = bean.getCount();
            if (count > currentCount) {
                mPageNum++;
                getNoteListByFolderId(tagId, mPageNum, TNConst.MAX_PAGE_SIZE, sort);
            }
            notifyData(bean);
        } else {
            notifyData(bean);
        }
    }

    @Override
    public void onListByFolderIdFailed(String msg, Exception e) {
        mLoadingView.setVisibility(View.GONE);
        mPullListview.onRefreshComplete();
    }

    //1-2/同1-1
    @Override
    public void onNoteListByTagIdSuccess(Object obj, long tagId, int mPageNum, int pageSize, String sort) {

        NoteListBean bean = (NoteListBean) obj;
        insertDbNotes(bean, false);//异步

        mLoadingView.setVisibility(View.GONE);
        mPullListview.onRefreshComplete();

        if (pageSize == TNConst.MAX_PAGE_SIZE) {
            int currentCount = mPageNum * TNConst.PAGE_SIZE;
            int count = bean.getCount();
            if (count > currentCount) {
                mPageNum++;
                getNoteListByTagId(tagId, mPageNum, TNConst.MAX_PAGE_SIZE, sort);
            }
            notifyData(bean);
        } else {
            notifyData(bean);
        }
    }


    @Override
    public void onNoteListByTagIdFailed(String msg, Exception e) {
        mLoadingView.setVisibility(View.GONE);
        mPullListview.onRefreshComplete();
    }


    //==================================GetDataByNoteId接口返回=======================================

    //GetDataByNoteId
    @Override
    public void onGetDataByNoteIdSuccess(Object obj, long noteId) {
        //更新数据库
        updateNote1((GetNoteByNoteIdBean) obj);
        //GetDataByNoteId的下一个接口
        TNNote note = TNDbUtils.getNoteByNoteId(noteId);
        Vector<TNNoteAtt> atts = note.atts;
        if (atts.size() > 0) {
            pSynceNoteAttr(0, atts, noteId);
        } else {
            //下一个接口/
            syncGetDataByNoteIdSQL(noteId);
        }
    }

    @Override
    public void onGetDataByNoteIdFailed(String msg, Exception e) {
        MLog.d(msg);
    }

    @Override
    public void onSyncNoteAttrSuccess(Object obj, int position, Vector<TNNoteAtt> atts, long noteId) {
        //执行一个循环
        pSynceNoteAttr(position + 1, atts, noteId);
    }

    @Override
    public void onSyncNoteAttrFailed(String msg, Exception e) {
        MLog.d(msg);
        TNUtilsUi.showToast(msg);
    }

    //==================================接口结果返回 syncData=======================================

    DataListener dataListener = new DataListener();

    class DataListener implements OnSynchronizeDataListener {

        //---接口结果回调  第一次登录的同步---

        //1-1
        @Override
        public void onSyncFolderAddSuccess(Object obj, int position, int arraySize) {
            MLog.d("sync----1-1-->Success");
            //下一个position
            pFolderAdd(position + 1, arraySize, arrayFolderName[position + 1]);

        }

        @Override
        public void onSyncFolderAddFailed(String msg, Exception e, int position, int arraySize) {
            MLog.e(msg);

        }

        //1-2
        @Override
        public void onSyncTagAddSuccess(Object obj, int position, int arraySize) {
            if (position < arraySize - 1) {//同步该接口的列表数据，
                //（有数组，循环调用）
                pTagAdd(position + 1, arraySize, arrayTagName[position + 1]);
            } else {
                //
                mSettings.firstLaunch = false;
                mSettings.savePref(false);
                //执行下个接口
                syncOldNote1();
            }
        }

        @Override
        public void onSyncTagAddFailed(String msg, Exception e, int position, int arraySize) {
            MLog.e(msg);

            //
//        if (position + 1 < arraySize) {//同步该接口的列表数据
//            //（有数组，循环调用）
//            pTagAdd(position + 1, arraySize, arrayTagName[position + 1]);
//        } else {
//            mSettings.savePref(false);
//            //再调用正常的同步接口
//            syncNormalLogData();
//        }
        }

        //1-3
        @Override
        public void onSyncGetFolderSuccess(Object obj) {

            AllFolderBean allFolderBean = (AllFolderBean) obj;
            List<AllFolderItemBean> allFolderItemBeans = allFolderBean.getFolders();
            mapList.add(allFolderItemBeans);
            //更新数据库
            insertDBCatsSQL1(allFolderBean, -1);
        }

        @Override
        public void onSyncGetFolderFailed(String msg, Exception e) {
            MLog.e(msg);
        }

        //1-4
        @Override
        public void onSyncGetFoldersByFolderIdSuccess(Object obj, long catID, int startPos, List<AllFolderItemBean> beans) {

            AllFolderBean allFolderBean = (AllFolderBean) obj;
            List<AllFolderItemBean> allFolderItemBeans = allFolderBean.getFolders();
            //
            MLog.d("sync----1-4-->Success--接口返回数据：" + allFolderItemBeans.size());
            //判断是否有返回值
            if (allFolderBean == null || allFolderItemBeans == null || allFolderItemBeans.size() <= 1) {
                if (allFolderItemBeans.size() == 1) {
                    //需要这么写 勿改
                    AllFolderItemBean itemBean = allFolderItemBeans.get(0);
                    if (itemBean.getCount() == 0) {
                        //执行下个position循环
                        syncGetFoldersByFolderId(startPos + 1, false);
                    } else {
                        //有多个数据
                        //新增循环层前添加标记，标记已经执行完的上一层位置
                        flagMap.put(mapList.size() + "A" + startPos, startPos);
                        //1-4新增循环
                        mapList.add(allFolderItemBeans);
                        //更新数据库
                        insertDBCatsSQL1(allFolderBean, catID);

                    }
                } else {//没有数据

                    //执行下个position循环
                    syncGetFoldersByFolderId(startPos + 1, false);
                }
            } else {
                //有多个数据
                //新增循环层前添加标记，标记已经执行完的上一层位置
                flagMap.put(mapList.size() + "A" + startPos, startPos);
                //1-4新增循环
                mapList.add(allFolderItemBeans);
                //更新数据库
                insertDBCatsSQL1(allFolderBean, catID);

            }
        }

        @Override
        public void onSyncGetFoldersByFolderIdFailed(String msg, Exception e, long catID, int startPos, List<AllFolderItemBean> beans) {
            MLog.e(msg);
            //执行下个position循环
            syncGetFoldersByFolderId(startPos + 1, false);
        }

        //1-5
        @Override
        public void onSyncFirstFolderAddSuccess(Object obj, int workPos, int workSize, long catID, String name, int catPos, int flag) {
            MLog.d("sync----1-5-->Success");
            if (catPos < cats.size() - 1) {
                if (flag == 1) {//groupWorks
                    if (workPos < workSize - 1) {
                        pFirstFolderAdd(workPos + 1, groupWorks.length, catID, name, catPos, 1);//继续执行第1个
                    } else {//执行下个TNCat
                        syncTNCat(catPos + 1, cats.size());//执行for的外层TNCat的下一个
                    }
                } else if (flag == 2) {//groupLife
                    if (workPos < workSize - 1) {
                        pFirstFolderAdd(workPos + 1, groupLife.length, catID, name, catPos, 2);//继续执行第2个
                    } else {//执行下个TNCat
                        syncTNCat(catPos + 1, cats.size());//执行for的外层TNCat的下一个
                    }
                } else if (flag == 3) {//groupFun
                    if (workPos < workSize - 1) {
                        pFirstFolderAdd(workPos + 1, groupFun.length, catID, name, catPos, 3);//继续执行第3个
                    } else {//执行下个TNCat
                        syncTNCat(catPos + 1, cats.size());//执行for的外层TNCat的下一个
                    }
                } else {
                    //执行下一个接口
                    pGetTagList1();
                }
            } else {
                //执行下一个接口
                pGetTagList1();
            }
        }

        @Override
        public void onSyncFirstFolderAddFailed(String msg, Exception e, int workPos, int workSize, long catID, String name, int catPos, int flag) {
            MLog.e(msg);
            if (catPos < cats.size() - 1) {
                if (flag == 1) {//groupWorks
                    if (workPos < workSize - 1) {
                        pFirstFolderAdd(workPos + 1, groupWorks.length, catID, name, catPos, 1);//继续执行第1个
                    } else {//执行下个TNCat
                        syncTNCat(catPos + 1, cats.size());//执行for的外层TNCat的下一个
                    }
                } else if (flag == 2) {//groupLife
                    if (workPos < workSize - 1) {
                        pFirstFolderAdd(workPos + 1, groupLife.length, catID, name, catPos, 2);//继续执行第2个
                    } else {//执行下个TNCat
                        syncTNCat(catPos + 1, cats.size());//执行for的外层TNCat的下一个
                    }
                } else if (flag == 3) {//groupFun
                    if (workPos < workSize - 1) {
                        pFirstFolderAdd(workPos + 1, groupFun.length, catID, name, catPos, 3);//继续执行第3个
                    } else {//执行下个TNCat
                        syncTNCat(catPos + 1, cats.size());//执行for的外层TNCat的下一个
                    }
                } else {
                    //执行下一个接口
                    pGetTagList1();
                }
            } else {
                //执行下一个接口
                pGetTagList1();
            }
        }


        //----接口结果回调  正常同步---
        //2-1
        @Override
        public void onSyncProfileSuccess(Object obj) {
            ProfileBean profileBean = (ProfileBean) obj;
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
            settings.firstLaunch = false;//在此处设置 false
            settings.savePref(false);
            //执行下个接口（该处是 第一次登录的最后一个同步接口，下一个正常登录的同步接口）
            syncGetFolder();
        }

        @Override
        public void onSyncProfileAddFailed(String msg, Exception e) {
            //
            MLog.e(msg);
        }

        //2-2 OldNotePic
        @Override
        public void onSyncOldNotePicSuccess(Object obj, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
            String content = addOldNotes.get(notePos).content;
            OldNotePicBean oldNotePicBean = (OldNotePicBean) obj;
            //更新图片 数据库
            upDataAttIdSQL1(oldNotePicBean.getId(), tnNoteAtt);

            if (notePos < noteArrySize - 1) {
                if (picPos < picArrySize - 1) {
                    //继续上传下张图
                    Vector<TNNoteAtt> oldNotesAtts = addOldNotes.get(notePos).atts;
                    pUploadOldNotePic1(picPos + 1, picArrySize, notePos, noteArrySize, oldNotesAtts.get(picPos + 1));
                } else {//所有图片上传完成，就处理
                    String digest = oldNotePicBean.getMd5();
                    long attId = oldNotePicBean.getId();
                    //更新 content
                    String s1 = String.format("<tn-media hash=\"%s\" />", digest);
                    String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", digest, attId);
                    content = content.replaceAll(s1, s2);

                    //
                    TNNote note = addOldNotes.get(notePos);
                    if (note.catId == -1) {
                        note.catId = TNSettings.getInstance().defaultCatId;
                    }
                    pOldNote1(notePos, noteArrySize, note, false, content);
                }
            } else {

                //
                TNNote note = addOldNotes.get(notePos);
                if (note.catId == -1) {
                    note.catId = TNSettings.getInstance().defaultCatId;
                }
                pOldNote1(notePos, noteArrySize, note, false, content);
            }
        }

        @Override
        public void onSyncOldNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
            MLog.e(msg);
        }

        //2-3OldNoteAdd
        @Override
        public void onSyncOldNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {
            OldNoteAddBean oldNoteAddBean = (OldNoteAddBean) obj;

            if (isNewDb) {//false时表示老数据库的数据上传，不用在修改本地的数据
                upDataNoteLocalIdSQL1(oldNoteAddBean, addOldNotes.get(position));
            }

            if (position < arraySize - 1) {
                //执行下一个 图片
                Vector<TNNoteAtt> oldNotesAtts = addOldNotes.get(position + 1).atts;
                pUploadOldNotePic1(0, oldNotesAtts.size(), position + 1, arraySize, addOldNotes.get(position + 1).atts.get(0));
            } else {
                //执行下个接口
                syncProfile();
            }
        }

        @Override
        public void onSyncOldNoteAddFailed(String msg, Exception e, int position, int arraySize) {
            MLog.e(msg);

            //
//        if (position < arraySize - 1) {
//            pUploadOldNotePic(0, oldNotesAtts.size(), position + 1, arraySize, addOldNotes.get(position + 1).atts.get(0));
//        } else {
//            mSettings.syncOldDb = false;
//            mSettings.savePref(false);
//            //执行下个接口
//        }
        }

        //2-4
        @Override
        public void onSyncTagListSuccess(Object obj) {

            TagListBean tagListBean = (TagListBean) obj;
            List<TagItemBean> beans = tagListBean.getTags();
            //
            TNSettings settings = TNSettings.getInstance();
            TagDbHelper.clearTags();

            for (int i = 0; i < beans.size(); i++) {
                TagItemBean itemBean = beans.get(i);

                String tagName = itemBean.getName();
                if (TextUtils.isEmpty(tagName)) {
                    tagName = "无";
                }
                JSONObject tempObj = TNUtils.makeJSON(
                        "tagName", tagName,
                        "userId", settings.userId,
                        "trash", 0,
                        "tagId", itemBean.getId(),
                        "strIndex", TNUtils.getPingYinIndex(tagName),
                        "count", itemBean.getCount()
                );
                TagDbHelper.addOrUpdateTag(tempObj);
            }

            //执行下个接口
            pAddNewNote1();
        }

        @Override
        public void onSyncTagListAddFailed(String msg, Exception e) {
            MLog.e(msg);
        }

        //2-5
        @Override
        public void onSyncNewNotePicSuccess(Object obj, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
            String content = addNewNotes.get(notePos).content;
            OldNotePicBean newPicbean = (OldNotePicBean) obj;
            //更新图片 数据库
            upDataAttIdSQL1(newPicbean.getId(), tnNoteAtt);
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
                    pNewNotePic1(picPos + 1, picArrySize, notePos, noteArrySize, newNotesAtts.get(picPos + 1));
                } else {
                    //所有图片上传完成，就开始上传文本
                    TNNote note = addNewNotes.get(notePos);
                    if (note.catId == -1) {
                        note.catId = TNSettings.getInstance().defaultCatId;
                    }
                    pNewNote1(notePos, noteArrySize, note, false, content);
                }
            } else {
                //所有图片上传完成，就开始上传newPos的文本
                TNNote note = addNewNotes.get(notePos);
                if (note.catId == -1) {
                    note.catId = TNSettings.getInstance().defaultCatId;
                }
                pNewNote1(notePos, noteArrySize, note, false, content);
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
                upDataNoteLocalIdSQL1(newNoteBean, addNewNotes.get(position));
            }


            if (position < arraySize - 1) {
                //处理position + 1下的图片上传
                Vector<TNNoteAtt> newNotesAtts = addNewNotes.get(position + 1).atts;
                pNewNotePic1(0, newNotesAtts.size(), position + 1, arraySize, addNewNotes.get(position + 1).atts.get(0));
            } else {

                //执行下个接口
                recoveryNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 7);
                recoveryNote1(0);
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
            recoveryNoteSQL1(noteId);

            //执行循环的下一个position+1数据/下一个接口
            recoveryNote1(position + 1);
        }

        @Override
        public void onSyncRecoveryFailed(String msg, Exception e) {
            MLog.e(msg);
        }

        //2-7-2
        @Override
        public void onSyncRecoveryNotePicSuccess(Object obj, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
            String content = recoveryNotes.get(notePos).content;
            OldNotePicBean recoveryPicbean = (OldNotePicBean) obj;

            //更新图片 数据库
            upDataAttIdSQL1(recoveryPicbean.getId(), tnNoteAtt);

            if (notePos < noteArrySize - 1) {
                if (picPos < picArrySize - 1) {
                    //继续上传下张图
                    Vector<TNNoteAtt> newNotesAtts = recoveryNotes.get(notePos).atts;
                    pRecoveryNotePic1(picPos + 1, picArrySize, notePos, noteArrySize, newNotesAtts.get(picPos + 1));
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
                    pRecoveryNoteAdd1(notePos, noteArrySize, note, true, content);
                }
            } else {

                //所有图片上传完成，就开始上传newPos的文本
                TNNote note = recoveryNotes.get(notePos);
                if (note.catId == -1) {
                    note.catId = TNSettings.getInstance().defaultCatId;
                }
                pRecoveryNoteAdd1(notePos, noteArrySize, note, true, content);
            }
        }

        @Override
        public void onSyncRecoveryNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
            MLog.e(msg);
        }

        //2-7-3
        @Override
        public void onSyncRecoveryNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {

            OldNoteAddBean recoveryNoteBean = (OldNoteAddBean) obj;
            //更新数据库
            if (isNewDb) {//false时表示老数据库的数据上传，不用在修改本地的数据
                upDataNoteLocalIdSQL1(recoveryNoteBean, recoveryNotes.get(position));
            }

            //处理position + 1下的TNNote/下一个接口
            recoveryNote1(position + 1);

        }

        @Override
        public void onSyncRecoveryNoteAddFailed(String msg, Exception e, int position, int arraySize) {
            MLog.e(msg);
        }

        //2-8
        @Override
        public void onSyncDeleteNoteSuccess(Object obj, long noteId, int position) {

            //更新数据
            updataDeleteNoteSQL1(noteId);

            //执行下一个
            pDelete1(position + 1);
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
            updataDeleteNoteSQL1(noteId);

            if (isRealDelete1 && isRealDelete2) {
                //执行下一个
                pRealDelete1(position + 1);

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
            deleteRealSQL1(noteId, position);
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
            pEditNotePic1(0);
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
            upDataAttIdSQL1(editPicbean.getId(), note.atts.get(attsPos));
            //执行下一个attsPos的图片上传
            pEditNotePic1(cloudsPos, attsPos + 1, note);
        }

        @Override
        public void onSyncEditNotePicFailed(String msg, Exception e, int cloudsPos, int attsPos, TNNote tnNote) {
            MLog.e(msg);
        }


        //2-11-1
        @Override
        public void onSyncEditNoteSuccess(Object obj, int cloudsPos, TNNote note) {

            //更新下一个cloudsPos位置的数据
            updataEditNotes1(cloudsPos, note);
        }

        @Override
        public void onSyncEditNoteAddFailed(String msg, Exception e) {
            MLog.e(msg);
        }

        //2-11-2
        @Override
        public void onSyncpGetNoteByNoteIdSuccess(Object obj, int position, boolean is13) {
            handleNote1((GetNoteByNoteIdBean) obj, position, is13);
        }

        @Override
        public void onSyncpGetNoteByNoteIdFailed(String msg, Exception e) {
            MLog.e(msg);
        }

        //2-12
        @Override
        public void onSyncpGetAllTrashNoteIdsSuccess(Object obj) {
            trashNoteArr = (List<AllNotesIdsBean.NoteIdItemBean>) obj;
            ExecutorService executorService = Executors.newCachedThreadPool();//开启线程池
            for (final TNNote trashNote : trashNotes) {
                boolean trashNoteExit = false;
                for (int i = 0; i < trashNoteArr.size(); i++) {
                    AllNotesIdsBean.NoteIdItemBean bean = trashNoteArr.get(i);
                    long noteId = bean.getId();
                    if (trashNote.noteId == noteId) {
                        trashNoteExit = true;
                        break;
                    }
                }
                if (!trashNoteExit) {

                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            TNDb.beginTransaction();
                            try {
                                //
                                TNDb.getInstance().deleteSQL(TNSQLString.NOTE_DELETE_BY_NOTEID, new String[]{trashNote.noteId + ""});
                                TNDb.setTransactionSuccessful();
                            } finally {
                                TNDb.endTransaction();
                            }
                        }
                    });
                }
            }
            //执行下一个接口
            pUpdataNote131(0, true);

        }

        @Override
        public void onSyncpGetAllTrashNoteIdsFailed(String msg, Exception e) {

        }
    }


    //==========================syncEdit==============================
    EditListener editListener = new EditListener();

    class EditListener implements OnSynchronizeEditListener {
        //2-5
        @Override
        public void onSyncNewNotePicSuccess2(Object obj, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {

            String content = addNewNotes.get(notePos).content;
            OldNotePicBean newPicbean = (OldNotePicBean) obj;
            //更新图片 数据库
            upDataAttIdSQL2(newPicbean.getId(), tnNoteAtt);
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
                    pNewNotePic2(picPos + 1, picArrySize, notePos, noteArrySize, newNotesAtts.get(picPos + 1));
                } else {
                    //所有图片上传完成，就开始上传文本
                    TNNote note = addNewNotes.get(notePos);
                    if (note.catId == -1) {
                        note.catId = TNSettings.getInstance().defaultCatId;
                    }
                    pNewNote2(notePos, noteArrySize, note, false, content);
                }
            } else {
                //所有图片上传完成，就开始上传newPos的文本
                TNNote note = addNewNotes.get(notePos);
                if (note.catId == -1) {
                    note.catId = TNSettings.getInstance().defaultCatId;
                }
                pNewNote2(notePos, noteArrySize, note, false, content);
            }
        }

        @Override
        public void onSyncNewNotePicFailed2(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
            MLog.e(msg);
        }

        //2-6
        @Override
        public void onSyncNewNoteAddSuccess2(Object obj, int position, int arraySize, boolean isNewDb) {

            OldNoteAddBean newNoteBean = (OldNoteAddBean) obj;
            //更新数据库
            if (isNewDb) {//false时表示老数据库的数据上传，不用在修改本地的数据
                upDataNoteLocalIdSQL2(newNoteBean, addNewNotes.get(position));
            }


            if (position < arraySize - 1) {
                //处理position + 1下的图片上传
                Vector<TNNoteAtt> newNotesAtts = addNewNotes.get(position + 1).atts;
                pNewNotePic2(0, newNotesAtts.size(), position + 1, arraySize, addNewNotes.get(position + 1).atts.get(0));
            } else {

                //执行下个接口
                recoveryNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 7);
                recoveryNote2(0);
            }
        }

        @Override
        public void onSyncNewNoteAddFailed2(String msg, Exception e, int position, int arraySize) {
            MLog.e(msg);
        }

        //2-7-1
        @Override
        public void onSyncRecoverySuccess2(Object obj, long noteId, int position) {
            //更新数据库
            recoveryNoteSQL2(noteId);

            //执行循环的下一个position+1数据/下一个接口
            recoveryNote2(position + 1);
        }

        @Override
        public void onSyncRecoveryFailed2(String msg, Exception e) {
            MLog.e(msg);
        }

        //2-7-2
        @Override
        public void onSyncRecoveryNotePicSuccess2(Object obj, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
            String content = recoveryNotes.get(notePos).content;
            OldNotePicBean recoveryPicbean = (OldNotePicBean) obj;

            //更新图片 数据库
            upDataAttIdSQL2(recoveryPicbean.getId(), tnNoteAtt);

            if (notePos < noteArrySize - 1) {
                if (picPos < picArrySize - 1) {
                    //继续上传下张图
                    Vector<TNNoteAtt> newNotesAtts = recoveryNotes.get(notePos).atts;
                    pRecoveryNotePic2(picPos + 1, picArrySize, notePos, noteArrySize, newNotesAtts.get(picPos + 1));
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
                    pRecoveryNoteAdd2(notePos, noteArrySize, note, true, content);
                }
            } else {

                //所有图片上传完成，就开始上传newPos的文本
                TNNote note = recoveryNotes.get(notePos);
                if (note.catId == -1) {
                    note.catId = TNSettings.getInstance().defaultCatId;
                }
                pRecoveryNoteAdd2(notePos, noteArrySize, note, true, content);
            }
        }

        @Override
        public void onSyncRecoveryNotePicFailed2(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
            MLog.e(msg);
        }

        //2-7-3
        @Override
        public void onSyncRecoveryNoteAddSuccess2(Object obj, int position, int arraySize, boolean isNewDb) {

            OldNoteAddBean recoveryNoteBean = (OldNoteAddBean) obj;
            //更新数据库
            if (isNewDb) {//false时表示老数据库的数据上传，不用在修改本地的数据
                upDataNoteLocalIdSQL2(recoveryNoteBean, recoveryNotes.get(position));
            }

            //处理position + 1下的TNNote/下一个接口
            recoveryNote2(position + 1);

        }

        @Override
        public void onSyncRecoveryNoteAddFailed2(String msg, Exception e, int position, int arraySize) {
            MLog.e(msg);
        }

        //2-8
        @Override
        public void onSyncDeleteNoteSuccess2(Object obj, long noteId, int position) {

            //更新数据
            updataDeleteNoteSQL2(noteId);

            //执行下一个
            pDelete2(position + 1);
        }

        @Override
        public void onSyncDeleteNoteFailed2(String msg, Exception e) {
            MLog.e(msg);
        }

        //2-9-1
        @Override
        public void onSyncpDeleteRealNotes1Success2(Object obj, long noteId, int position) {
            isRealDelete1_2 = true;
            //更新数据
            updataDeleteNoteSQL2(noteId);

            if (isRealDelete1_2 && isRealDelete2_2) {
                //执行下一个
                pRealDelete2(position + 1);

                //复原 false,供下次循环使用
                isRealDelete1_2 = false;
                isRealDelete2_2 = false;
            }

        }

        @Override
        public void onSyncDeleteRealNotes1Failed2(String msg, Exception e, int position) {
            isRealDelete1_2 = true;
            MLog.e(msg);
        }

        //2-9-2
        @Override
        public void onSyncDeleteRealNotes2Success2(Object obj, long noteId, int position) {
            isRealDelete2_2 = true;
            //更新数据库
            deleteRealSQL2(noteId, position);
        }

        @Override
        public void onSyncDeleteRealNotes2Failed2(String msg, Exception e, int position) {
            isRealDelete2_2 = true;
            MLog.e(msg);
        }

        //2-10
        @Override
        public void onSyncAllNotesIdSuccess2(Object obj) {
            cloudIds = (List<AllNotesIdsBean.NoteIdItemBean>) obj;

            //与云端同步数据 sjy-0623
            allNotes = TNDbUtils.getAllNoteList(TNSettings.getInstance().userId);
            for (int i = 0; i < allNotes.size(); i++) {
                boolean isExit = false;
                final TNNote note = allNotes.get(i);
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
            pEditNotePic2(0);
        }

        @Override
        public void onSyncAllNotesIdAddFailed2(String msg, Exception e) {
            MLog.e(msg);
        }

        //2-10-1
        @Override
        public void onSyncEditNotePicSuccess2(Object obj, int cloudsPos, int attsPos, TNNote tnNote) {
            TNNote note = tnNote;
            OldNotePicBean editPicbean = (OldNotePicBean) obj;
            note.atts.get(attsPos).digest = editPicbean.getMd5();
            note.atts.get(attsPos).attId = editPicbean.getId();
            String s1 = String.format("<tn-media hash=\"%s\" />", note.atts.get(attsPos).digest);
            String s2 = String.format("<tn-media hash=\"%s\" att-id=\"%s\" />", note.atts.get(attsPos).digest, note.atts.get(attsPos).attId);
            note.content = note.content.replaceAll(s1, s2);
            //更新图片 数据库
            upDataAttIdSQL2(editPicbean.getId(), note.atts.get(attsPos));
            //执行下一个attsPos的图片上传
            pEditNotePic2(cloudsPos, attsPos + 1, note);
        }

        @Override
        public void onSyncEditNotePicFailed2(String msg, Exception e, int cloudsPos, int attsPos, TNNote tnNote) {
            MLog.e(msg);
        }


        //2-11-1
        @Override
        public void onSyncEditNoteSuccess2(Object obj, int cloudsPos, TNNote note) {

            //更新下一个cloudsPos位置的数据
            updataEditNotes2(cloudsPos, note);
        }

        @Override
        public void onSyncEditNoteAddFailed2(String msg, Exception e) {
            MLog.e(msg);
        }

        //2-11-2
        @Override
        public void onSyncpGetNoteByNoteIdSuccess2(Object obj, int position, boolean is13) {
            handleNote2((GetNoteByNoteIdBean) obj, position, is13);

        }

        @Override
        public void onSyncpGetNoteByNoteIdFailed2(String msg, Exception e) {
            MLog.e(msg);
        }

        //2-12
        @Override
        public void onSyncpGetAllTrashNoteIdsSuccess2(Object obj) {
            trashNoteArr = (List<AllNotesIdsBean.NoteIdItemBean>) obj;
            ExecutorService executorService = Executors.newCachedThreadPool();//开启线程池
            for (final TNNote trashNote : trashNotes) {
                boolean trashNoteExit = false;
                for (int i = 0; i < trashNoteArr.size(); i++) {
                    AllNotesIdsBean.NoteIdItemBean bean = trashNoteArr.get(i);
                    long noteId = bean.getId();
                    if (trashNote.noteId == noteId) {
                        trashNoteExit = true;
                        break;
                    }
                }
                if (!trashNoteExit) {

                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            TNDb.beginTransaction();
                            try {
                                //
                                TNDb.getInstance().deleteSQL(TNSQLString.NOTE_DELETE_BY_NOTEID, new String[]{trashNote.noteId + ""});
                                TNDb.setTransactionSuccessful();
                            } finally {
                                TNDb.endTransaction();
                            }
                        }
                    });
                }
            }
            //执行下一个接口
            pUpdataNote132(0, true);

        }

        @Override
        public void onSyncpGetAllTrashNoteIdsFailed2(String msg, Exception e) {

        }
    }

}
