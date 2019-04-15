package com.thinkernote.ThinkerNote.Activity.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
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
import android.widget.Toast;

import com.thinkernote.ThinkerNote.Activity.TNNoteViewAct;
import com.thinkernote.ThinkerNote.Activity.TNPagerAct;
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
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.PullToRefresh.PullToRefreshBase.OnLastItemVisibleListener;
import com.thinkernote.ThinkerNote.PullToRefresh.PullToRefreshBase.OnRefreshListener;
import com.thinkernote.ThinkerNote.PullToRefresh.PullToRefreshListView;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.Utils.SPUtil;
import com.thinkernote.ThinkerNote._constructer.presenter.SynchronizeDataPresenterImpl;
import com.thinkernote.ThinkerNote._interface.p.ISynchronizeDataPresenter;
import com.thinkernote.ThinkerNote._interface.v.OnSynchronizeDataListener;
import com.thinkernote.ThinkerNote.base.TNChildViewBase;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.GetNoteByNoteIdBean;
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
 * 我的笔记--全部笔记frag
 */
public class TNPageNotes extends TNChildViewBase implements OnItemLongClickListener, OnRefreshListener, OnItemClickListener
        , OnLastItemVisibleListener, OnSynchronizeDataListener {
    private static final String TAG = "TNNotesPage";
    //正常登录的同步常量
    public static final int DELETE_LOCALNOTE = 101;//1
    public static final int DELETE_REALNOTE = 102;//
    public static final int DELETE_REALNOTE2 = 103;//
    public static final int UPDATA_EDITNOTES = 104;//
    public static final int CHILD_HANDLER_1_4 = 105;//
    public static final int UI_HANDLER_1_4 = 106;//
    public static final int CHILD_HANDLER_2_11 = 107;//
    public static final int UI_HANDLER_2_11 = 108;//

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
    private ISynchronizeDataPresenter presenter;

    private TNSettings mSettings = TNSettings.getInstance();

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

    public TNPageNotes(TNPagerAct activity) {
        mActivity = activity;
        pageId = R.id.page_notes;

        //p
        presenter = new SynchronizeDataPresenterImpl(mActivity, this);

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

            pSynchronizeData();

        } else {
            mPullListview.onRefreshComplete();
            TNUtilsUi.showToast(R.string.alert_Net_NotWork);
        }

    }


    // -------------------------------------handler------------------------------------------

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
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
                    pUpdataNote13(position + 1, is13);
                } else {
                    //执行一个position或下一个接口
                    MLog.d("sync----2-11-2-->Success--pUpdataNote");
                    pUpdataNote(position + 1, false);
                }
                break;
        }
    }

    /**
     * 创建HandlerThread,用于异步处理数据库数据
     */
    HandlerThread handlerThread1_4 = new HandlerThread("catfrag_4");
    HandlerThread handlerThread2_11 = new HandlerThread("catfrag2-11");
    Handler chlidHanlder2_11;
    Handler chlidHanlder1_4;

    /**
     * 同步结束后的操作
     *
     * @param state 0 = 成功/1=back取消同步/2-异常触发同步终止
     */
    private void endSynchronize(int state) {
        MLog.d("frag同步--全部笔记--同步结束");
        mapList.clear();
        flagMap.clear();
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

    // ------------------------------------数据库-------------------------------------------

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
     * 调用OldNoteAdd接口，就触发更新db
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
     * 1_4 调用GetFoldersByFolderId接口，就触发插入db
     */
    private void insertDBCatsSQL(AllFolderBean allFolderBean, long pCatId) {

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
     * 2-10-1 更新日记时间
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


    /**
     * 2-11-2
     * 该处工作环境最恶劣，上千跳接口返回数据走该处执行耗时任务，有必要手动gc处理内存
     *
     * @param bean
     */
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
            MLog.e("TNNotePageNotes--updateNote:" + e.toString());
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

    // ------------------------------------p层调用-------------------------------------------
    //-------第一次登录同步的p调用-------

    /**
     * (一)同步 第一个调用的方法
     * 执行顺序：先arrayFolderName对应的所有接口，再arrayTagName对应的所有接口，
     * 接口个数=arrayFolderName.size + arrayTagName.size
     */

    private void pSynchronizeData() {
        MLog.d("frag同步--全部笔记--pSynchronizeData");
        if (mSettings.firstLaunch) {//如果第一次登录app，执行该处方法
            //需要同步的文件数据
            arrayFolderName = new String[]{TNConst.FOLDER_DEFAULT, TNConst.FOLDER_MEMO, TNConst.GROUP_FUN, TNConst.GROUP_WORK, TNConst.GROUP_LIFE};
            arrayTagName = new String[]{TNConst.TAG_IMPORTANT, TNConst.TAG_TODO, TNConst.TAG_GOODSOFT};

            //同步第一个数据（有数组，循环调用）
            pFolderAdd(0, arrayFolderName.length, arrayFolderName[0]);
        } else {//如果正常启动，执行该处
            syncOldNote();
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
     * 第一次登录同步
     * <p>
     * （一.2）更新 tag
     */
    private void pTagAdd(int position, int arraySize, String name) {
        MLog.d("frag同步--全部笔记--pTagAdd 1-2");
        presenter.tagAdd(position, arraySize, name);
    }

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
        MLog.d("sync---1-3-pGetFolder");

        if (cats.size() == 0) {
            MLog.d("frag同步--全部笔记--syncGetFolder 1-3");
            presenter.pGetFolder();
        } else {
            //执行下一个接口
            pGetTagList();
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
                pGetTagList();
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
        MLog.d("frag同步--全部笔记--syncTNCat 1-5");
        if (postion < catsSize) {
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
            pGetTagList();
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
        MLog.d("frag同步--全部笔记--pFirstFolderAdd 1-5");
        presenter.pFirstFolderAdd(workPos, workSize, catID, name, catPos, flag);
    }


    //-------正常登录同步的p调用-------


    /**
     * 0720改：先执行syncOldNote--->syncProfile()--pGetTagList()
     * （二。2+二。3）正常登录的数据同步（非第一次登录的同步）
     * 执行顺序：同步老数据(先上传图片接口，再OldNote接口)，没有老数据就同步用户信息接口
     * 接口个数 = addOldNotes.size * oldNotesAtts.size;
     */
    private void syncOldNote() {
        MLog.d("frag同步--全部笔记--syncOldNote ");
        if (!mSettings.syncOldDb) {
            //add老数据库的笔记
            addOldNotes = TNDbUtils.getOldDbNotesByUserId(TNSettings.getInstance().userId);
            if (addOldNotes.size() > 0) {
                //先 上传数组的第一个
                TNNote tnNote = addOldNotes.get(0);
                Vector<TNNoteAtt> oldNotesAtts = tnNote.atts;
                if (oldNotesAtts.size() > 0) {//有图，先上传图片
                    pUploadOldNotePic(0, oldNotesAtts.size(), 0, addOldNotes.size(), oldNotesAtts.get(0));
                } else {//如果没有图片，就执行OldNote
                    pOldNote(0, addOldNotes.size(), addOldNotes.get(0), false, addOldNotes.get(0).content);
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
    private void pUploadOldNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        MLog.d("frag同步--全部笔记--pUploadOldNotePic 2-2");
        presenter.pUploadOldNotePic(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.3)正常同步 第2个执行的接口 循环调用
     * 和（二.2组成双层for循环，该处是最外层for执行）
     */

    private void pOldNote(int position, int arraySize, TNNote tnNoteAtt, boolean isNewDb, String content) {
        MLog.d("frag同步--全部笔记--pOldNote 2-3");
        presenter.pOldNoteAdd(position, arraySize, tnNoteAtt, isNewDb, content);
    }

    /**
     * 0720改：先执行syncOldNote--->syncProfile()--pGetTagList()
     * （二.1）正常同步 第一个接口
     */
    private void syncProfile() {

        MLog.d("frag同步--全部笔记--syncProfile 2-1");
        presenter.pProfile();
    }

    /**
     * 0720改：先执行syncOldNote--->syncProfile()--pGetTagList()
     * <p>
     * (二.4)正常同步 pGetTagList
     */

    private void pGetTagList() {
        Vector<TNTag> tags = TNDbUtils.getTagList(mSettings.userId);
        if (tags == null || tags.size() == 0) {
            MLog.d("frag同步--全部笔记--pGetTagList 2-4");
            presenter.pGetTagList();
        } else {
            //执行下一个接口
            pAddNewNote();
        }
    }


    /**
     * (二.5+二.6)正常同步 pAddNewNote
     * 说明：同(二.2+二.3)的执行顺序，先处理notepos的图片，处理完就上传notepos的文本，然后再处理notepos+1的图片，如此循环
     * 接口个数：addNewNotes.size * addNewNotes.size
     */

    private void pAddNewNote() {
        MLog.d("frag同步--全部笔记--pAddNewNote");
        addNewNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 3);

        if (addNewNotes.size() > 0) {
            //先 上传数组的第一个
            TNNote tnNote = addNewNotes.get(0);
            Vector<TNNoteAtt> newNotesAtts = tnNote.atts;
            if (newNotesAtts.size() > 0) {//有图，先上传图片
                pNewNotePic(0, newNotesAtts.size(), 0, addNewNotes.size(), newNotesAtts.get(0));
            } else {//如果没有图片，就执行OldNote
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
    private void pNewNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        MLog.d("frag同步--全部笔记--pNewNotePic 2-5");
        presenter.pNewNotePic(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.6)正常同步 第2个执行的接口 循环调用
     * 和（二.5组成双层for循环，该处是最外层for执行）
     */

    private void pNewNote(int position, int arraySize, TNNote tnNoteAtt, boolean isNewDb, String content) {
        MLog.d("frag同步--全部笔记--pNewNote 2-6");
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
    private void recoveryNote(int position) {
        MLog.e("frag同步--全部笔记--recoveryNote 2-7--- recoveryNotes.size()" + recoveryNotes.size());
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
        MLog.d("frag同步--全部笔记--pRecoveryNote 2-7-1");
        presenter.pRecoveryNote(noteID, position, arrySize);
    }

    /**
     * (二.7)02
     */
    private void pRecoveryNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        MLog.d("frag同步--全部笔记--pRecoveryNotePic 2-7-2");
        presenter.pRecoveryNotePic(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.7)03
     */
    private void pRecoveryNoteAdd(int position, int arraySize, TNNote tnNoteAtt, boolean isNewDb, String content) {
        MLog.d("frag同步--全部笔记--pRecoveryNoteAdd 2-7-3");
        presenter.pRecoveryNoteAdd(position, arraySize, tnNoteAtt, isNewDb, content);
    }


    /**
     * (二.8)
     *
     * @param position
     */
    private void pDelete(int position) {
        MLog.e("frag同步--全部笔记--pDelete 2-8----eleteNotes.size()=" + deleteNotes.size());
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
        MLog.d("frag同步--全部笔记--pNoteDelete 2-8");
        presenter.pDeleteNote(noteId, postion);
    }

    /**
     * (二.8)删除本地数据 （不调接口）
     */
    private void pNoteLocalDelete(final int position, final long noteLocalId) {
        MLog.d("frag同步--全部笔记--pNoteLocalDelete 2-8");
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
        MLog.e("frag同步--全部笔记--pRealDelete 2-9---deleteRealNotes.size()" + deleteRealNotes.size());
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
        MLog.d("frag同步--全部笔记--pDeleteReadNotesSql 2-9");
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
        MLog.d("frag同步--全部笔记--pDeleteRealNotes 2-9");
        //
        presenter.pDeleteRealNotes(noteId, postion);

    }

    /**
     * (二.10)
     */
    private void pGetAllNoteIds() {
        MLog.d("frag同步--全部笔记--pGetAllNoteIds 2-10");
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
    private void pEditNotePic(int position) {

        MLog.d("frag同步--全部笔记--pEditNotePic 2-10-1");
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
                            //上传图片，之后上传文本
                            TNNote note = EditNotePicBefore(editNotes.get(j));//上传图片，处理content参数
                            pEditNotePic(position, 0, note);
                        } else {
                            updataEditNotesState(position, editNotes.get(j).noteLocalId);
                        }
                    }
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
     * @param tnNote
     */
    private void pEditNotePic(int cloudsPos, int attsPos, TNNote tnNote) {
        MLog.d("frag同步--全部笔记--pEditNotePic 2-10-1");
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
                    pEditNotePic(cloudsPos, attsPos + 1, note);

                } else {
                    //接口，上传图片
                    presenter.pEditNotePic(cloudsPos, attsPos, note);
                }
            } else {
                //图片上传完，再上传文本
                pEditNotes(cloudsPos, note);
            }
        } else {
            //执行下一个接口
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
        MLog.d("frag同步--全部笔记--pEditNotes 2-11-1");
        if (cloudIds.size() > 0 && cloudsPos < (cloudIds.size())) {
            presenter.pEditNote(cloudsPos, note);
        } else {
            //执行下一个接口
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
        MLog.d("frag同步--全部笔记--pUpdataNote 2-11-2");
        //为2-11-2接口返回，做预处理
        setChildHandler2_11(position);
        //
        if (cloudIds.size() > 0 && position < (cloudIds.size())) {
            boolean isExit = false;
            long id = cloudIds.get(position).getId();
            int lastUpdate = cloudIds.get(position).getUpdate_at();

            //本地更新
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
            //下一个接口
            //同步回收站的笔记
            trashNotes = TNDbUtils.getNoteListByTrash(mSettings.userId, TNConst.CREATETIME);
            pTrashNotes();

        }
    }

    /**
     * (二.11)-2/(二.13) 更新云端的笔记
     * <p>
     * p层
     */
    private void pUpdataNote(int position, long noteId, boolean is13) {
        MLog.d("frag同步--全部笔记--pUpdataNote 2-11-2");
        presenter.pGetNoteByNoteId(position, noteId, is13);
    }

    /**
     * (二.12) 同步回收站的笔记
     */
    private void pTrashNotes() {
        MLog.d("frag同步--全部笔记--pTrashNotes 2-12");
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
    private void pUpdataNote13(int position, boolean is13) {
        MLog.d("frag同步--全部笔记--pUpdataNote13 2-13");
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
                pUpdataNote(position, noteId, is13);
            } else {
                pUpdataNote13(position + 1, is13);
            }
        } else {
            //同步所有接口完成，结束同步
            endSynchronize(0);
        }
    }


    //=============================================接口结果回调(成对的success+failed)======================================================

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
            syncOldNote();
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
        insertDBCatsSQL(allFolderBean, -1);


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
                    insertDBCatsSQL(allFolderBean, catID);

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
            insertDBCatsSQL(allFolderBean, catID);

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
                pGetTagList();
            }
        } else {
            //执行下一个接口
            pGetTagList();
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
                pGetTagList();
            }
        } else {
            //执行下一个接口
            pGetTagList();
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
        upDataAttIdSQL(oldNotePicBean.getId(), tnNoteAtt);

        if (notePos < noteArrySize - 1) {
            if (picPos < picArrySize - 1) {
                //继续上传下张图
                Vector<TNNoteAtt> oldNotesAtts = addOldNotes.get(notePos).atts;
                pUploadOldNotePic(picPos + 1, picArrySize, notePos, noteArrySize, oldNotesAtts.get(picPos + 1));
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
                pOldNote(notePos, noteArrySize, note, false, content);
            }
        } else {

            //
            TNNote note = addOldNotes.get(notePos);
            if (note.catId == -1) {
                note.catId = TNSettings.getInstance().defaultCatId;
            }
            pOldNote(notePos, noteArrySize, note, false, content);
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
            upDataNoteLocalIdSQL(oldNoteAddBean, addOldNotes.get(position));
        }

        if (position < arraySize - 1) {
            //执行下一个 图片
            Vector<TNNoteAtt> oldNotesAtts = addOldNotes.get(position + 1).atts;
            pUploadOldNotePic(0, oldNotesAtts.size(), position + 1, arraySize, addOldNotes.get(position + 1).atts.get(0));
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
        pAddNewNote();
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
    public void onSyncRecoveryNotePicSuccess(Object obj, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
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
    public void onSyncRecoveryNotePicFailed(String msg, Exception e, int picPos, int picArry, int notePos, int noteArry) {
        MLog.e(msg);
    }

    //2-7-3
    @Override
    public void onSyncRecoveryNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {

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
    public void onSyncEditNotePicFailed(String msg, Exception e, int cloudsPos, int attsPos, TNNote tnNote) {
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
        handleNote((GetNoteByNoteIdBean) obj, position, is13);
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
        pUpdataNote13(0, true);

    }

    @Override
    public void onSyncpGetAllTrashNoteIdsFailed(String msg, Exception e) {

    }
}
