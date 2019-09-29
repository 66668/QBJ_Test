package com.thinkernote.ThinkerNote.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件夹信息(属性)
 */
public class TNCatInfoAct extends TNActBase
        implements OnClickListener, OnGroupClickListener, OnCatInfoListener {
    public static final int CAT_DELETE = 107;//

    private long mCatId;
    private TNCat mCurrentCat;
    private CommonDialog dialog;//GetDataByNoteId的弹窗；

    //p
    CatInfoPresenter presenter;

    //
    LinearLayout ly_cat, ly_catGroup, ly_note, ly_default, ly_delete, ly_setDefault;
    TextView tv_cat, tv_catGroup, tv_note, tv_default;

    // Activity methods
    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_catinfo);
        initMyView();
        setViews();
        mCatId = getIntent().getLongExtra("CatId", -1);
        showView();
        presenter = new CatInfoPresenter(this, this);
    }

    /**
     *
     */
    private void initMyView() {
        ly_cat = findViewById(R.id.ly_cat);
        ly_catGroup = findViewById(R.id.ly_catGroup);
        ly_note = findViewById(R.id.ly_note);
        ly_default = findViewById(R.id.ly_default);
        ly_setDefault = findViewById(R.id.ly_setDefault);
        ly_delete = findViewById(R.id.ly_delete);


        tv_cat = findViewById(R.id.tv_cat);
        tv_catGroup = findViewById(R.id.tv_catGroup);
        tv_note = findViewById(R.id.tv_note);
        tv_default = findViewById(R.id.tv_default);

        ly_cat.setOnClickListener(this);
        ly_catGroup.setOnClickListener(this);
        ly_delete.setOnClickListener(this);
        ly_setDefault.setOnClickListener(this);

    }

    private void showView() {
        //填充信息
        TNSettings setting = TNSettings.getInstance();
        mCurrentCat = TNDbUtils.getCat(mCatId);
        tv_cat.setText(mCurrentCat.catName);
        //
        String info = mCurrentCat.catName;
        if (mCurrentCat.pCatId > 0)
            info = TNDbUtils.getCat(mCurrentCat.pCatId).catName;
        else
            info = getString(R.string.catinfo_nogroup);
        tv_catGroup.setText(info);
        //
        tv_note.setText(String.valueOf(mCurrentCat.noteCounts));

        //
        if (mCurrentCat.catId == setting.defaultCatId) {
            tv_default.setText(R.string.catinfo_yes);
        } else {
            tv_default.setText(R.string.catinfo_no);
        }

        //
        if (mCurrentCat.catId != setting.defaultCatId) {
            ly_delete.setVisibility(View.VISIBLE);
        } else {
            ly_delete.setVisibility(View.GONE);
        }

        //设为默认文件夹
        if (mCurrentCat.catId != setting.defaultCatId) {
            ly_setDefault.setVisibility(View.VISIBLE);
        } else {
            ly_setDefault.setVisibility(View.GONE);
        }
    }

    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.catinfo_toolbar_layout, R.drawable.toolbg);
        TNUtilsSkin.setViewBackground(this, null, R.id.catinfo_page, R.drawable.page_bg);
        findViewById(R.id.catinfo_back).setOnClickListener(this);
    }

    // configView
    //-------------------------------------------------------------------------------
    protected void configView() {
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

    // implements OnClickListener
    //-------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.catinfo_back:
                finish();
                break;
            case R.id.ly_cat:
                changeFolderName();
                break;
            case R.id.ly_catGroup:
                changeFolderParent();
                break;
            case R.id.ly_delete:
                deleteFolder();
                break;
            case R.id.ly_setDefault:
                setdefault();
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
