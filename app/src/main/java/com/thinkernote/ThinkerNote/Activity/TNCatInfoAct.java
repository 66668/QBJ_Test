package com.thinkernote.ThinkerNote.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.thinkernote.ThinkerNote.Adapter.TNPreferenceAdapter;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Data.TNPreferenceChild;
import com.thinkernote.ThinkerNote.Data.TNPreferenceGroup;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNRunner;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.dialog.CommonDialog;
import com.thinkernote.ThinkerNote.mvp.MyRxManager;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnCatInfoListener;
import com.thinkernote.ThinkerNote.mvp.p.CatInfoPresenter;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  有反射方法执行，禁止混淆
 * 文件夹信息
 */
public class TNCatInfoAct extends TNActBase
        implements OnClickListener, OnChildClickListener, OnGroupClickListener, OnCatInfoListener {
    public static final int CAT_DELETE = 107;//

    /* Bundle:
     * CatLocalId
     */

    private ExpandableListView mListView;
    private Vector<TNPreferenceGroup> mGroups;
    private TNPreferenceChild mCurrChild;
    private long mCatId;
    private TNCat mCurrentCat;
    private CommonDialog dialog;//GetDataByNoteId的弹窗；

    //p
    CatInfoPresenter presenter;

    // Activity methods
    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catinfo);
        setViews();
        presenter = new CatInfoPresenter(this, this);
        mCatId = getIntent().getLongExtra("CatId", -1);

        mGroups = new Vector<TNPreferenceGroup>();

        mListView = (ExpandableListView) findViewById(R.id.catinfo_expandablelistview);
        mListView.setAdapter(new TNPreferenceAdapter(this, mGroups));

        mListView.setOnGroupClickListener(this);
        mListView.setOnChildClickListener(this);
    }

    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.catinfo_toolbar_layout, R.drawable.toolbg);
        TNUtilsSkin.setViewBackground(this, null, R.id.catinfo_page, R.drawable.page_bg);
        findViewById(R.id.catinfo_back).setOnClickListener(this);
    }

    // configView
    //-------------------------------------------------------------------------------
    protected void configView() {
        getCatInfos();
        ((BaseExpandableListAdapter) mListView.getExpandableListAdapter()).notifyDataSetChanged();
        for (int i = 0; i < mGroups.size(); i++) {
            mListView.expandGroup(i);
        }
    }

    private void getCatInfos() {
        TNSettings setting = TNSettings.getInstance();
        mCurrentCat = TNDbUtils.getCat(mCatId);

        mGroups.clear();
        TNPreferenceGroup group = null;

        //文件夹
        group = new TNPreferenceGroup(getString(R.string.catinfo_folder));
        {    //名称
            {
                boolean visibleMoreBtn = true;
                group.addChild(new TNPreferenceChild(getString(R.string.catinfo_name), mCurrentCat.catName, visibleMoreBtn, new TNRunner(this, "changeFolderName")));
            }
            {//所属文件夹
                String info = mCurrentCat.catName;
                if (mCurrentCat.pCatId > 0)
                    info = TNDbUtils.getCat(mCurrentCat.pCatId).catName;
                else
                    info = getString(R.string.catinfo_nogroup);
                boolean visibleMoreBtn = true;
                group.addChild(new TNPreferenceChild(getString(R.string.catinfo_group), info, visibleMoreBtn, new TNRunner(this, "changeFolderParent")));
            }
            {//笔记数量
                group.addChild(new TNPreferenceChild(getString(R.string.catinfo_notecount), String.valueOf(mCurrentCat.noteCounts), false, null));
            }
            {//默认文件夹
                String info = getString(R.string.catinfo_no);
                if (mCurrentCat.catId == setting.defaultCatId) {
                    info = getString(R.string.catinfo_yes);
                }
                group.addChild(new TNPreferenceChild(getString(R.string.catinfo_isdefault), info, false, null));
            }
            {//删除
                if (mCurrentCat.catId != setting.defaultCatId) {
                    boolean visibleMoreBtn = true;
                    group.addChild(new TNPreferenceChild(getString(R.string.catinfo_delete), null, visibleMoreBtn, new TNRunner(this, "deleteFolder")));
                }
            }
            //设为默认文件夹
            if (mCurrentCat.catId != setting.defaultCatId) {
                group.addChild(new TNPreferenceChild(getString(R.string.catinfo_setdefault), null, true, new TNRunner(this, "setdefault")));
            }
        }
        mGroups.add(group);
    }

    //Child click methods
    public void changeFolderName() {
        Bundle b = new Bundle();
        b.putString("TextType", "cat_rename");
        b.putString("TextHint", getString(R.string.textedit_folder));
        b.putString("OriginalText", mCurrentCat.catName);
        b.putLong("ParentId", mCurrentCat.catId);
        startActivity(TNTextEditAct.class, b);
    }

    public void changeFolderParent() {
        Bundle b = new Bundle();
        b.putLong("OriginalCatId", mCurrentCat.pCatId);
        b.putLong("ChangeFolderForFolderList", mCurrentCat.catId);
        b.putInt("Type", 0);

        startActForResult(TNCatListAct.class, b, R.string.catinfo_group);
    }

    public void deleteFolder() {
        showCatDeleteDialog(mCurrentCat);
    }

    public void setdefault() {
        setDefaultCatDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == R.string.catinfo_group) {
            if (resultCode == RESULT_OK) {
                MLog.i(TAG, "selectedId = " + data.getLongExtra("SelectedCatId", 0));
                long selectId = data.getLongExtra("SelectedCatId", 0);
                mCurrentCat.pCatId = selectId;
                configView();
            }
        }
    }

    // implements OnClickListener
    //-------------------------------------------------------------------------------
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

    // implements OnClickListener
    //-------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.catinfo_back:
                finish();
                break;
        }
    }

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case CAT_DELETE:
                configView();
                break;
        }
    }

    /**
     * 文件删除 弹窗
     *
     * @param cat
     */
    private void showCatDeleteDialog(final TNCat cat) {
        dialog = new CommonDialog(this, R.string.alert_CatInfo_Delete_HasChild,
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        if (!MyRxManager.getInstance().isSyncing()) {
                            TNUtilsUi.showNotification(TNCatInfoAct.this, R.string.alert_NoteView_Synchronizing, false);
                            //具体执行
                            pCatDelete(cat);
                        }
                    }

                    @Override
                    public void cancelBack() {
                    }

                });
        dialog.show();
    }

    private void setDefaultCatDialog() {
        dialog = new CommonDialog(this, R.string.alert_CatInfo_SetDefaultMsg,
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        setDefaultFoldler();
                    }

                    @Override
                    public void cancelBack() {
                    }

                });
        dialog.show();
    }

    //--------------------------------------p层调用-----------------------------------------
    private void setDefaultFoldler() {
        presenter.pSetDefaultFolder(mCurrentCat.catId);
    }

    private void pCatDelete(TNCat cat) {
        presenter.pDeleteCat(cat.catId);
    }

    //--------------------------------------接口回调-----------------------------------------
    @Override
    public void onSuccess(Object obj) {
        finish();
    }

    @Override
    public void onFailed(String msg, Exception e) {
        MLog.e(msg);
    }

    @Override
    public void onDeleteFolderSuccess(Object obj, final long catId) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                TNDb.beginTransaction();
                try {
                    TNDb.getInstance().execSQL(TNSQLString.CAT_DELETE_CAT, catId);
                    TNDb.getInstance().execSQL(TNSQLString.NOTE_TRASH_CATID, 2, System.currentTimeMillis() / 1000, TNSettings.getInstance().defaultCatId, catId);
                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }
                handler.sendEmptyMessage(CAT_DELETE);
            }
        });
    }

    @Override
    public void onDeleteFolderFailed(String msg, Exception e) {
        MLog.e(msg);
    }
}
