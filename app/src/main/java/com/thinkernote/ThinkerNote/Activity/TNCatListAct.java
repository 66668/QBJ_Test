package com.thinkernote.ThinkerNote.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.DBHelper.CatDbHelper;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.p.CatListPresenter;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnCatListListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.main.AllFolderBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;

import org.json.JSONObject;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 请选择文件夹list
 * sjy 0626
 */
public class TNCatListAct extends TNActBase
        implements OnClickListener, OnItemClickListener, OnCatListListener {

    private static final int MOVE_NOTE = 101;
    /* Bundle:
     * OriginalCatId
     * Type: int  0 move cat, 1 move note, 2 表示startActivityForResult带回的
     */

    private ListView mListView;
    private long mSelectCatId = -1;
    private long mOriginalCatId = -1;
    private int mType; //
    private Vector<TNCat> mCats;
    private long mNoteLocalId;
    private long mCatId;
    private TNCat mPCat = null;
    private TNCatBaseAdapter mAdapter;
    private ProgressDialog mProgressDialog;

    //p
    CatListPresenter presener;

    // Activity methods
    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catlist);
        setViews();
        //p
        presener = new CatListPresenter(this, this);

        // initialize
        findViewById(R.id.catlist_back).setOnClickListener(this);
        findViewById(R.id.catlist_save).setOnClickListener(this);
        findViewById(R.id.catlist_new).setOnClickListener(this);

        mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);

        mCats = new Vector<TNCat>();
        mOriginalCatId = getIntent().getLongExtra("OriginalCatId", 0);
        mType = getIntent().getIntExtra("Type", 0);
        mNoteLocalId = (Long) getIntent().getLongExtra("ChangeFolderForNoteList", 0);
        mCatId = (Long) getIntent().getLongExtra("ChangeFolderForFolderList", 0);
        mSelectCatId = mOriginalCatId;

        mListView = (ListView) findViewById(R.id.catlist_list);
        mListView.setOnItemClickListener(this);
        mAdapter = new TNCatBaseAdapter();
        mListView.setAdapter(mAdapter);

        findViewById(R.id.catlist_new).setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectCatId = savedInstanceState.getLong("SelectCatId");
        mOriginalCatId = savedInstanceState.getLong("OriginalCatId");
        mPCat = (TNCat) savedInstanceState.getSerializable("ParentCat");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("SelectCatId", mSelectCatId);
        outState.putLong("OriginalCatId", mOriginalCatId);
        outState.putSerializable("ParentCat", mPCat);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        MLog.i(TAG, "keyCode:" + keyCode + event);
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mOriginalCatId != mSelectCatId) {
                confirmSaveDialog();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.taglist_toolbar_layout, R.drawable.toolbg);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null, R.id.catlist_new, R.drawable.newfolder1);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null, R.id.catlist_save, R.drawable.ok);
        TNUtilsSkin.setViewBackground(this, null, R.id.catlist_page_bg, R.drawable.page_bg);
    }

    // ConfigView
    //-------------------------------------------------------------------------------
    protected void configView() {
        if (mPCat == null || mPCat.catId == -1) {
            getParentFodler();
        } else {
            getFoldersByFolderId();
        }
    }

    private void initParentCat() {
        if (mOriginalCatId > 0) {
            TNCat cat = TNDbUtils.getCat(mOriginalCatId);
            if (cat == null)
                return;
            mPCat = TNDbUtils.getCat(cat.pCatId);
            if (mPCat != null) {
                return;
            }
        } else {

        }
        mPCat = new TNCat();
        mPCat.catId = -1;
    }

    // implements OnClickListener
    //-------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.catlist_back:
                if (mPCat.catId == -1) {
                    setResult(Activity.RESULT_CANCELED, null);
                    finish();
                    break;
                }
                mPCat = TNDbUtils.getCat(mPCat.pCatId);
                configView();
                break;

            case R.id.catlist_save:
                if (mOriginalCatId != mSelectCatId || mCatId != mSelectCatId) {
                    save();
                } else {
                    setResult(Activity.RESULT_CANCELED, null);
                }
                finish();
                break;

            case R.id.catlist_new:
                Bundle b = new Bundle();
                b.putString("TextType", "cat_add");
                b.putString("TextHint", getString(R.string.textedit_folder));
                b.putString("OriginalText", "");
                b.putLong("ParentId", mPCat.catId);
                startActivity(TNTextEditAct.class, b);
                break;
        }
    }

    // private methods
    //-------------------------------------------------------------------------------
    private void confirmSaveDialog() {
        DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                save();
                finish();
            }
        };

        DialogInterface.OnClickListener nbtn_Click = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(Activity.RESULT_CANCELED, null);
                finish();
            }
        };

        JSONObject jsonData = TNUtils.makeJSON(
                "CONTEXT", this,
                "TITLE", R.string.alert_Title,
                "MESSAGE", R.string.alert_CatList_BackMsg,
                "POS_BTN", R.string.alert_Save,
                "POS_BTN_CLICK", pbtn_Click,
                "NEU_BTN", R.string.alert_NoSave,
                "NEU_BTN_CLICK", nbtn_Click,
                "NEG_BTN", R.string.alert_Cancel
        );
        TNUtilsUi.alertDialogBuilder(jsonData).show();
    }

    private void save() {
        mProgressDialog.show();
        if (mType == 1) {
            moveNote();
        } else if (mType == 0) {
            moveFolder();
        } else if (mType == 2) {
            Intent it = new Intent();
            it.putExtra("SelectedCatId", mSelectCatId);
            setResult(Activity.RESULT_OK, it);
        }
    }

    @Override
    public void onDestroy() {
        mProgressDialog.dismiss();
        super.onDestroy();
    }

    // Class TNCatBaseAdapter
    //-------------------------------------------------------------------------------
    private class TNCatBaseAdapter extends BaseAdapter implements OnCheckedChangeListener {

        @Override
        public int getCount() {
            return mCats.size();
        }

        @Override
        public Object getItem(int position) {
            return mCats.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mCats.get(position).catId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(TNCatListAct.this);
                convertView = inflater.inflate(R.layout.catlistitem, null);
                holder.catNameTV = (TextView) convertView.findViewById(R.id.catlistitem_title);
                holder.catSelectRB = (RadioButton) convertView.findViewById(R.id.catlistitem_select);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            TNUtilsSkin.setImageViewDrawable(TNCatListAct.this, convertView, R.id.catlistitem_image, R.drawable.foldericon);
            TNCat cat = mCats.get(position);
            holder.catNameTV.setText(cat.catName);
            holder.catSelectRB.setOnCheckedChangeListener(this);
            holder.catSelectRB.setTag(cat.catId);
            if (cat.catId == mOriginalCatId) {
                holder.catSelectRB.setChecked(true);
            } else {
                holder.catSelectRB.setChecked(false);
            }
            return convertView;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            Long catLocalId = (Long) buttonView.getTag();
            if (!isChecked && catLocalId == mSelectCatId) {
                buttonView.setChecked(true);
                return;
            }
            if (isChecked && catLocalId != mSelectCatId) {
                Long oldCatId = mSelectCatId;
                mSelectCatId = catLocalId;

                //Log.d(TAG, "selectPos:" + selectPos);
                if (oldCatId >= 0) {
                    ListView lv = (ListView) findViewById(R.id.catlist_list);
                    RadioButton rb = (RadioButton) lv.findViewWithTag(
                            (Object) oldCatId);
                    if (rb != null)
                        rb.setChecked(false);
                }
            }
        }
    }

    private class ViewHolder {
        TextView catNameTV;
        RadioButton catSelectRB;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (mCats.get(position).catCounts == 0) {
            return;
        }
        mPCat = mCats.get(position);
        configView();
    }

    /**
     * 调用GetFoldersByFolderId接口，就触发插入db
     */
    private void insertDBCatsSQL(AllFolderBean allFolderBean, long pCatId) {
        TNSettings settings = TNSettings.getInstance();
        CatDbHelper.clearCatsByParentId(pCatId);
        List<AllFolderItemBean> beans = allFolderBean.getFolders();
        for (int i = 0; i < beans.size(); i++) {
            AllFolderItemBean bean = beans.get(i);

            JSONObject tempObj = TNUtils.makeJSON(
                    "catName", bean.getName(),
                    "userId", settings.userId,
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
    }

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case MOVE_NOTE:
                finish();
                break;
        }
    }

    //------------------------------------p层调用-------------------------------------------
    private void getParentFodler() {
        presener.pParentFodler();
    }

    private void getFoldersByFolderId() {
        presener.pGetFolderByFolderId(mPCat.catId);

    }

    //移动本地note
    private void moveNote() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                long noteLocalId = mNoteLocalId;
                long catId = mSelectCatId;
                int lastUpdate = (int) (System.currentTimeMillis() / 1000);
                TNNote note = TNDbUtils.getNoteByNoteLocalId(noteLocalId);
                int syncState = note.noteId == -1 ? 3 : 4;
                TNDb.beginTransaction();
                try {
                    TNDb.getInstance().execSQL(TNSQLString.NOTE_MOVE_CAT, catId , syncState , lastUpdate, noteLocalId );
                    TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId );

                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }

                handler.sendEmptyMessage(MOVE_NOTE);
            }
        });


    }

    //文件夹移动文件夹
    private void moveFolder() {
        presener.pFolderMove(mCatId, mSelectCatId);
    }


    //------------------------------------接口结果返回-------------------------------------------
    @Override
    public void onParentFolderSuccess(Object obj) {
        insertDBCatsSQL((AllFolderBean) obj, -1);
        initParentCat();
        mCats = TNDbUtils.getCatsByCatId(TNSettings.getInstance().userId, -1);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onParentFolderFailed(String msg, Exception e) {
        MLog.e(msg);
    }

    @Override
    public void onGetFoldersByFolderIdSuccess(Object obj, long catId) {
        AllFolderBean bean = (AllFolderBean) obj;
        insertDBCatsSQL(bean, catId);
        mCats = TNDbUtils.getCatsByCatId(TNSettings.getInstance().userId, mPCat.catId);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetFoldersByFolderIdFailed(String msg, Exception e) {
        MLog.e(msg);
    }

    @Override
    public void onFolderMoveSuccess(Object obj) {
        mProgressDialog.hide();
        finish();
    }

    @Override
    public void onFolderMoveFailed(String msg, Exception e) {
        MLog.e(msg);
    }

}
