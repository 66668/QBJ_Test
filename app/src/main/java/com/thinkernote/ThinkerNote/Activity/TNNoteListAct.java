package com.thinkernote.ThinkerNote.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.DisplayMetrics;
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
import com.thinkernote.ThinkerNote.Adapter.TNNotesAdapter;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Data.TNNote;
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
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.PullToRefresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.thinkernote.ThinkerNote.PullToRefresh.PullToRefreshBase.OnRefreshListener;
import com.thinkernote.ThinkerNote.PullToRefresh.PullToRefreshListView;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.Views.CommonDialog;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnNoteListListener;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnSyncListener;
import com.thinkernote.ThinkerNote._constructer.p.NoteListPresenter;
import com.thinkernote.ThinkerNote._constructer.p.SyncPresenter;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.main.NoteListBean;
import com.thinkernote.ThinkerNote.http.MyRxManager;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 列表展示类（各种列表如笔记列表、文件夹下笔记列表、回收站笔记列表、标签笔记列表、搜索笔记列表、个人公开笔记列表、他人公开列表）
 * <p>
 * //1 allNote, 2 cat, 3 recycle, 4 tag, 5 serch, 7 个人公开, 8  他人公开
 * 5--搜索结果 展示界面
 * 说明：
 */
public class TNNoteListAct extends TNActBase implements OnClickListener, OnItemLongClickListener,
        OnLastItemVisibleListener, OnRefreshListener, OnItemClickListener, OnNoteListListener, OnSyncListener {
    private static final int SEARCH = 105;
    public static final int DIALOG_DELETE = 111;//
    public static final String TAG = "NoteList";//1
    private PullToRefreshListView mPullListview;
    private ListView mListView;
    private Vector<TNNote> mNotes;
    private long mCurNoteId;
    private TNNote mCurNote;
    private float mScale;
    private ProgressDialog mProgressDialog;
    private LinearLayout mLoadingView;

    private int mListType; //1 全部笔记, 2 某个文件夹下所有笔记, 3 回收站笔记, 4 标签下所有笔记, 5 serch, 7 个人公开, 8  他人公开
    private long mListDetail; //mListType决定id类型
    private String mKeyWord;

    private TNTag mTag;
    private TNCat mCat;

    private TNSettings mSettings = TNSettings.getInstance();

    private TNNotesAdapter mNotesAdapter = null;
    private int mCount;
    private int mPageNum = 1;
    boolean isAllSync = false;  //有重叠的同步块，在notelist界面中，isAllSync =true,同步所有，isAllSync =false,从本地新笔记开始同步，

    private CommonDialog dialog;//GetDataByNoteId的弹窗；
    //p
    private NoteListPresenter presenter;
    private SyncPresenter syncPresenter;
    ExecutorService service = Executors.newSingleThreadExecutor();

    // Activity methods
    // -------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notelist);
        MLog.e("TNNoteListAct" + "跳转  TNNoteListAct");
        setViews();
        MyRxManager.getInstance().syncOver();//初始化值
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
        presenter = new NoteListPresenter(this, this);
        syncPresenter = new SyncPresenter(this, this);
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
        MyRxManager.getInstance().cancelAll();//取消同步
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
            getNoteListBySearchSQL(mKeyWord);
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
            case 2://文件夹id下所有笔记
                //获取网络数据
                getNoteListByFolderId(mListDetail, mPageNum, TNConst.PAGE_SIZE, mSettings.sort);

                break;
            case 3://同步回收站
                //
                if (mPageNum == 1) {
                    syncTrash();
                }
                break;
            case 4://tag下笔记
                getNoteListByTagId(mListDetail, mPageNum, TNConst.PAGE_SIZE, mSettings.sort);
                break;
            case 5:
                getNoteListBySearchSQL(mKeyWord);
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
    private void endSynchronize1(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MLog.d("NoteList同步---结束");

                mLoadingView.setVisibility(View.GONE);
                mPullListview.onRefreshComplete();
                mNotes = TNDbUtils.getNoteListByTrash(mSettings.userId, mSettings.sort);
                mNotesAdapter.updateNotes(mNotes);
                mNotesAdapter.notifyDataSetChanged();
                setButtonsAndNoteList();
                if (state == 0) {
                    // 正常流程完成
                } else if (state == 1) {
                    TNUtilsUi.showNotification(TNNoteListAct.this, R.string.alert_Synchronize_Stoped, true);
                } else {
                    TNUtilsUi.showNotification(TNNoteListAct.this, R.string.alert_SynchronizeCancell, true);
                }
            }
        });

    }

    /**
     * 同步Edit结束后的操作
     *
     * @param state 0 = 成功/1=back取消同步/2-异常触发同步终止
     */
    private void endSynchronize2(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MLog.d("SynchronizeEdit结束");
//        mLoadingView.setVisibility(View.GONE);
//        mPullListview.onRefreshComplete();
//        mNotes = TNDbUtils.getNoteListByTrash(mSettings.userId, mSettings.sort);
//        mNotesAdapter.updateNotes(mNotes);
//        mNotesAdapter.notifyDataSetChanged();
//        setButtonsAndNoteList();

                if (state == 0) {
                    // 正常流程完成
                    TNUtilsUi.showNotification(TNNoteListAct.this, R.string.alert_MainCats_Synchronized, true);
                } else if (state == 1) {
                    TNUtilsUi.showNotification(TNNoteListAct.this, R.string.alert_Synchronize_Stoped, true);
                } else {
                    TNUtilsUi.showNotification(TNNoteListAct.this, R.string.alert_SynchronizeCancell, true);
                }
            }
        });

    }
    // ---------------------------------------弹窗----------------------------------------

    /**
     * 清空
     */
    private void clearrecyclerDialog() {
        dialog = new CommonDialog(this, R.string.alert_NoteList_ClearRecycle,
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        if (!TNActionUtils.isSynchronizing()) {
                            //具体执行
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

                    @Override
                    public void cancelBack() {
                        mProgressDialog.hide();
                    }

                });
        dialog.show();
    }

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
                        if (!TNActionUtils.isSynchronizing()) {
                            TNUtilsUi.showNotification(TNNoteListAct.this, R.string.alert_NoteView_Synchronizing, false);
                            //具体执行
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

                        if (!TNActionUtils.isSynchronizing()) {
                            TNUtilsUi.showNotification(TNNoteListAct.this, R.string.alert_NoteView_Synchronizing, false);
                            //具体执行
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
                        if (!TNActionUtils.isSynchronizing()) {
                            TNUtilsUi.showNotification(TNNoteListAct.this, R.string.alert_NoteView_Synchronizing, false);
                            //具体执行
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

    /**
     * 完全同步
     */
    private void showSyncDialog(final long noteId) {
        dialog = new CommonDialog(this, R.string.alert_MainCats_SynchronizeNoteAll,
                "完全同步",
                "取消",
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        if (!TNActionUtils.isSynchronizing()) {
                            TNUtilsUi.showNotification(TNNoteListAct.this, R.string.alert_NoteView_Synchronizing, false);
                            //监听
                            getDetailByNoteId(noteId);
                        }
                    }

                    @Override
                    public void cancelBack() {
                        mProgressDialog.hide();
                    }

                });
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
            case DIALOG_DELETE://弹窗提示同步
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
                configView();
                if (TNUtils.isNetWork()) {
                    TNUtilsUi.showNotification(this, R.string.alert_NoteView_Synchronizing, false);
                    syncEdit();
                }
                break;
        }
    }

    // ---------------------------------------数据库操作----------------------------------------


    // 本地搜索
    private void getNoteListBySearchSQL(final String mKeyWord) {

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

    //-------------------------------------p层调用----------------------------------------
    private void getNoteListByFolderId(long mListDetail, int mPageNum, int size, String sort) {

        presenter.getNoteListByFolderid(mListDetail, mPageNum, size, sort);
    }

    // 单独调用，获取tag下的笔记列表
    private void getNoteListByTagId(long tagId, int mPageNum, int size, String sort) {
        presenter.getNoteListByTagId(tagId, mPageNum, size, sort);
    }

    /**
     * 同步edit
     */
    private void syncEdit() {
        syncPresenter.synchronizeEdit();
        isAllSync = syncPresenter.isAllSync();
    }

    /**
     * 同步edit
     */
    private void syncTrash() {
        syncPresenter.synchronizeData("TRASH");
        isAllSync = syncPresenter.isAllSync();
    }

    /**
     * 笔记详情同步（完全同步）
     *
     * @param noteId
     */
    private void getDetailByNoteId(long noteId) {
        presenter.getDetailByNoteId(noteId);
    }

    //==================================接口结果返回 syncData=======================================

    @Override
    public void onSyncSuccess(String obj) {
        endSynchronize1(0);
    }

    @Override
    public void onSyncFailed(Exception e, String msg) {
        endSynchronize1(2);
    }

    //
    @Override
    public void onSyncEditSuccess() {
        endSynchronize2(0);
    }


    //获取tag下笔记/获取文件夹下笔记（走相同回调返回）
    @Override
    public void onNoteListByIdSuccess() {
        mLoadingView.setVisibility(View.GONE);
        mPullListview.onRefreshComplete();
    }

    @Override
    public void onNoteListByIdNext(NoteListBean bean) {
        //更新tag下笔记列表
        notifyData(bean);
    }

    @Override
    public void onNoteListByIdFailed(Exception e, String msg) {
        mLoadingView.setVisibility(View.GONE);
        mPullListview.onRefreshComplete();
    }

    //完全同步一条笔记详情
    @Override
    public void onDownloadNoteSuccess() {
        //关闭弹窗
        MLog.d("同步完成-->GetDataByNoteId");
        TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
        dialog.dismiss();
        dialog = null;
        TNUtilsUi.showToast("同步完成");
    }

    @Override
    public void onDownloadNoteFailed(Exception e, String msg) {
        //关闭弹窗
        MLog.d("同步完成-->GetDataByNoteId");
        TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
        dialog.dismiss();
        dialog = null;
        TNUtilsUi.showToast("同步失败");
    }

}
