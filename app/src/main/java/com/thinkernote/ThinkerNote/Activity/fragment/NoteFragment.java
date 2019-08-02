package com.thinkernote.ThinkerNote.Activity.fragment;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.Activity.TNNoteViewAct;
import com.thinkernote.ThinkerNote.Activity.TNPagerAct;
import com.thinkernote.ThinkerNote.Adapter.TNNotesAdapter;
import com.thinkernote.ThinkerNote.DBHelper.NoteDbHelper;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.PullToRefresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.thinkernote.ThinkerNote.PullToRefresh.PullToRefreshBase.OnRefreshListener;
import com.thinkernote.ThinkerNote.PullToRefresh.PullToRefreshListView;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.Utils.SPUtil;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnSyncListener;
import com.thinkernote.ThinkerNote._constructer.p.SyncPresenter;
import com.thinkernote.ThinkerNote.base.TNChildViewBase;

import java.util.Vector;

/**
 * 我的笔记--全部笔记frag
 */
public class NoteFragment extends TNChildViewBase implements OnItemLongClickListener, OnRefreshListener, OnItemClickListener
        , OnLastItemVisibleListener, OnSyncListener {
    private static final String TAG = "TNNotesPage";

    private Vector<TNNote> mNotes;
    public TNNote mCurNote;
    public boolean isNewSortord;//排序使用

    private TextView mTopDateText;
    private TextView mTopCountText;
    private LinearLayout mLoadingView;
    private Button mFolderBtn;
    private float mScale;
    public int mPageNum = 1;

    private PullToRefreshListView mPullListview;
    private ListView mListView;
    private TNNotesAdapter mAdapter = null;

    //p
    private SyncPresenter syncPresenter;

    private TNSettings mSettings = TNSettings.getInstance();

    public NoteFragment(TNPagerAct activity) {
        mActivity = activity;
        pageId = R.id.page_notes;

        //p
        syncPresenter = new SyncPresenter(mActivity, this);
        init();
    }

    public void init() {
        mChildView = LayoutInflater.from(mActivity).inflate(
                R.layout.pagerchild_notelist, null);

        DisplayMetrics metric = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScale = metric.scaledDensity;

        mNotes = new Vector<TNNote>();
        mPullListview = (PullToRefreshListView) mChildView
                .findViewById(R.id.notelist_list);
        mListView = mPullListview.getRefreshableView();
        mLoadingView = (LinearLayout) TNUtilsUi.addListHelpInfoFootView(mActivity, mListView,
                TNUtilsUi.getFootViewTitle(mActivity, 1),
                TNUtilsUi.getFootViewInfo(mActivity, 1));
        mAdapter = new TNNotesAdapter(mActivity, mNotes, mScale);
        mListView.setAdapter(mAdapter);

        mChildView.findViewById(R.id.top_group_info).setVisibility(View.GONE);
        mTopDateText = (TextView) mChildView.findViewById(R.id.notelist_top_date);
        mTopCountText = (TextView) mChildView.findViewById(R.id.notelist_top_count);
        mTopDateText.setText("全部笔记");
        mFolderBtn = (Button) mChildView.findViewById(R.id.notelist_folder);

        mListView.setOnItemLongClickListener(this);
        mListView.setOnItemClickListener(this);
        mPullListview.setOnRefreshListener(this);
        mPullListview.setOnLastItemVisibleListener(this);
    }

    @Override
    public void configView(int createStatus) {
        if (TNSettings.getInstance().originalSyncTime == 0) {
            mPullListview.setRefreshing();
            onRefresh();
        } else {
            getNativeData();
        }
    }

    private void getNativeData() {
        mNotes = TNDbUtils.getNoteListByCount(TNSettings.getInstance().userId, mPageNum * TNConst.PAGE_SIZE, TNSettings.getInstance().sort);
        mAdapter.updateNotes(mNotes);
        mAdapter.notifyDataSetChanged();
        if (mNotes.size() == mPageNum * TNConst.PAGE_SIZE)
            mPageNum++;

        if (mNotes != null) {
            int count = Integer.valueOf(NoteDbHelper.getNotesCountByAll());
            mFolderBtn.setText(String.format("%s(%d)", mActivity.getString(R.string.notelist_allnote), count));
        }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        if (position > 0) {
            mCurNote = mNotes.get(position - 1);
            mBundle.putSerializable("currentNote", mCurNote);
            mActivity.addNoteMenu(R.layout.menu_notelistitem);
        }
        return true;
    }

    @Override
    public void onLastItemVisible() {
        if (mPageNum != 1) {
            getNativeData();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Bundle b = new Bundle();
        b.putLong("NoteLocalId", id);
        mActivity.startActivity(TNNoteViewAct.class, b);
    }

    @Override
    public void onRefresh() {
        if (TNUtils.isNetWork()) {
            //主线同步
            if (SPUtil.getBoolean("MainSync", false)) {
                TNUtilsUi.showNotification(mActivity, R.string.alert_Synchronize_TooMuch, false);
                mPullListview.onRefreshComplete();
                return;
            }
            //
            if (TNActionUtils.isSynchronizing()) {
                TNUtilsUi.showNotification(mActivity, R.string.alert_Synchronize_TooMuch, false);
                mPullListview.onRefreshComplete();
                return;
            }
            mPageNum = 1;
            TNUtilsUi.showNotification(mActivity, R.string.alert_NoteView_Synchronizing, false);
            syncPresenter.synchronizeData("NOTE");
        } else {
            mPullListview.onRefreshComplete();
            TNUtilsUi.showToast(R.string.alert_Net_NotWork);
        }

    }

    @Override
    public void onSyncSuccess(String obj) {
        endSynchronize(0);
    }

    @Override
    public void onSyncFailed(Exception e, String msg) {
        endSynchronize(2);
    }

    // -------------------------------------handler------------------------------------------

    /**
     * 同步结束后的操作
     *
     * @param state 0 = 成功/1=back取消同步/2-异常触发同步终止
     */
    private void endSynchronize(int state) {
        //TODO 主线程更新
        MLog.d("frag同步--全部笔记--同步结束");
        mPullListview.onRefreshComplete();
        if (state == 0) {
            //正常结束
            TNUtilsUi.showNotification(mActivity, R.string.alert_MainCats_Synchronized, true);
            //
            TNSettings settings = TNSettings.getInstance();
            settings.originalSyncTime = System.currentTimeMillis();
            settings.savePref(false);
        } else if (state == 1) {
            TNUtilsUi.showNotification(mActivity, R.string.alert_Synchronize_Stoped, true);
        } else {
            TNUtilsUi.showNotification(mActivity, R.string.alert_SynchronizeCancell, true);
        }
        getNativeData();
    }

    //如下回调不使用
    @Override
    public void onSyncEditSuccess(String obj) {

    }

    @Override
    public void onSyncEditFailed(Exception e, String msg) {

    }

}
