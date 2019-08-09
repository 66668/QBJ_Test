package com.thinkernote.ThinkerNote.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.DBHelper.TagDbHelper;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNTag;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsTag;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.Views.CommonDialog;
import com.thinkernote.ThinkerNote._constructer.p.TagListPresenter;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnTagListListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.main.TagItemBean;

import org.json.JSONObject;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 选择标签/更换标签
 * sjy 0625
 */
public class TNTagListAct extends TNActBase implements OnClickListener, OnItemClickListener, OnItemLongClickListener, OnTagListListener {

    public static final int BACK_CHECKDB = 101;//1

    /* Bundle:
     * TagStrForEdit
     */
    private String mOriginal = null;
    private String mTagStr = null;
    private TNTagAdapter mAdapter;
    private long mNoteLocalId;
    private TNNote mNote = null;
    private Vector<TNTag> mTags;
    private ProgressDialog mProgressDialog = null;

    // p
    private TagListPresenter presener;

    // Activity methods
    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taglist);
        setViews();
        mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
        mTags = new Vector<TNTag>();

        presener = new TagListPresenter(this, this);

        findViewById(R.id.taglist_back).setOnClickListener(this);
        findViewById(R.id.taglist_new).setOnClickListener(this);
        findViewById(R.id.taglist_save).setOnClickListener(this);

        mOriginal = mTagStr = getIntent().getStringExtra("TagStrForEdit");
        mNoteLocalId = getIntent().getLongExtra("ChangeTagForNoteList", -1);
        MLog.d("TNNOteViewAct","获取跳转值=" + mNoteLocalId);
        if (mNoteLocalId != -1) {
            mNote = TNDbUtils.getNoteByNoteLocalId(mNoteLocalId);
            MLog.d("TNNOteViewAct","获取mNote值=" + mNote.toString());
        }

        ListView lv = (ListView) findViewById(R.id.taglist_list);
        lv.setOnItemClickListener(this);
        lv.setOnItemLongClickListener(this);
        mAdapter = new TNTagAdapter();
        lv.setAdapter(mAdapter);
    }

    @Override
    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.taglist_toolbar_layout, R.drawable.toolbg);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null, R.id.taglist_new, R.drawable.newnote);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null, R.id.taglist_save, R.drawable.ok);
        TNUtilsSkin.setViewBackground(this, null, R.id.taglist_page_bg, R.drawable.page_bg);
    }

    @Override
    public void onSaveInstanceState(Bundle outBundle) {
        outBundle.putString("TAG_STR", mTagStr);
        super.onSaveInstanceState(outBundle);
    }

    @Override
    public void onRestoreInstanceState(Bundle outBundle) {
        super.onRestoreInstanceState(outBundle);

        mTagStr = outBundle.getString("TAG_STR");
    }

    protected void configView() {
        ((TextView) findViewById(R.id.taglist_tagstr)).setText(mTagStr);
        //
        getTagList();

    }

    @Override
    public void onDestroy() {
        mProgressDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            back();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    // Implement OnClickListener
    //-------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.taglist_back:
                back();
                break;

            case R.id.taglist_new:
                Bundle b = new Bundle();
                b.putString("TextType", "tag_add");
                b.putString("TextHint", getString(R.string.textedit_tag));
                b.putString("OriginalText", "");
                startActivity(TNTextEditAct.class, b);
                break;

            case R.id.taglist_save:
                if (!mTagStr.equals(mOriginal)) {
                    if (mNote != null) {
                        noteLocalChangeTag();
                    } else {
                        Intent it = new Intent();
                        it.putExtra("EditedTagStr", mTagStr);
                        setResult(Activity.RESULT_OK, it);
                        finish();
                    }
                } else {
                    setResult(Activity.RESULT_CANCELED, null);
                    finish();
                }
                break;
        }
    }

    /**
     *
     */
    private void noteLocalChangeTag() {
        final String tags = mTagStr;
        final int lastUpdate = (int) (System.currentTimeMillis() / 1000);
        final int syncState = mNote.noteId == -1 ? 3 : 4;

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                TNDb.beginTransaction();
                try {
                    //
                    TNDb.getInstance().execSQL(TNSQLString.NOTE_CHANGE_TAG, tags, syncState, lastUpdate, mNote.noteLocalId);
                    TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, mNote.catId);

                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }
                handler.sendEmptyMessage(BACK_CHECKDB);
            }
        });

    }

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case BACK_CHECKDB://2-8-2的调用
                mNote = TNDbUtils.getNoteByNoteLocalId(mNoteLocalId);
                finish();
                break;
        }
    }

    // Implement OnItemClickListener
    //-------------------------------------------------------------------------------
    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
        MLog.d(TAG, parent.toString() + view.toString() + position + id);
        ListView lv = (ListView) findViewById(R.id.taglist_list);
        CheckBox cb = (CheckBox) lv.findViewWithTag((Object) position);
        cb.setChecked(!cb.isChecked());
    }

    // Implement OnItemLongClickListener
    //-------------------------------------------------------------------------------
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        MLog.i(TAG, "onItemLongClick");
        return false;
    }

    // Private methods
    //-------------------------------------------------------------------------------
    private void back() {
        if (!mTagStr.equals(mOriginal)) {
            CommonDialog dialog = new CommonDialog(this, R.string.alert_TagList_BackMsg,
                    "保存",
                    "不保存",
                    new CommonDialog.DialogCallBack() {
                        @Override
                        public void sureBack() {
                            if (mNote != null) {
                                noteLocalChangeTag();
                            } else {
                                Intent it = new Intent();
                                it.putExtra("EditedTagStr", mTagStr);
                                setResult(Activity.RESULT_OK, it);
                                finish();
                            }
                        }

                        @Override
                        public void cancelBack() {
                            finish();
                        }

                    });
            dialog.show();
        } else {
            setResult(Activity.RESULT_CANCELED, null);
            finish();
        }
    }


    // Class TNTagAdapter
    //-------------------------------------------------------------------------------
    private class TNTagAdapter extends BaseAdapter
            implements OnCheckedChangeListener {

        @Override
        public int getCount() {
            return mTags.size();
        }

        @Override
        public Object getItem(int position) {
            return mTags.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mTags.get(position).tagId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View layout = null;
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                layout = layoutInflater.inflate(R.layout.taglistitem, null);
            } else {
                layout = convertView;
            }
            setView(layout, position);
            return layout;
        }

        private void setView(View layout, int position) {
            TNTag tag = (TNTag) getItem(position);
            ((TextView) layout.findViewById(R.id.taglistitem_title)).setText(tag.tagName);
            CheckBox cb = ((CheckBox) layout.findViewById(R.id.taglistitem_select));
            cb.setTag(position);
            cb.setOnCheckedChangeListener(this);

            Vector<String> goodTag = TNUtilsTag.splitTagStr(mTagStr);
            cb.setChecked(goodTag.contains(tag.tagName));
        }

        @Override
        public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            TNTag tag = (TNTag) getItem((Integer) arg0.getTag());

            Vector<String> goodTag = TNUtilsTag.splitTagStr(TNTagListAct.this.mTagStr);
            if (goodTag.contains(tag.tagName) != arg1) {
                if (arg1) {
                    goodTag.add(tag.tagName);
                } else {
                    goodTag.remove(tag.tagName);
                }
                TNTagListAct.this.mTagStr = TNUtilsTag.makeTagStr(goodTag);
                ((TextView) findViewById(R.id.taglist_tagstr)).setText(
                        TNTagListAct.this.mTagStr);
            }
        }

    }

    //---------------------------------------------------p层调用-------------------------------------------------
    private void getTagList() {
        presener.pTagList();
    }

    //---------------------------------------------------接口结果回调-------------------------------------------------

    @Override
    public void onTagListSuccess() {
        mTags = TNDbUtils.getTagList(TNSettings.getInstance().userId);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTagListFailed(String msg, Exception e) {

    }
}

