package com.thinkernote.ThinkerNote.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsTag;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.Views.CommonDialog;
import com.thinkernote.ThinkerNote._constructer.p.TextEditPresenter;
import com.thinkernote.ThinkerNote._constructer.listener.v.OnTextEditListener;
import com.thinkernote.ThinkerNote.base.TNActBase;

import org.json.JSONObject;

/**
 * 通用类
 * <p>
 * 编辑/新建 文件夹名称，标签名称等
 * <p>
 * sjy 0614
 */
public class TNTextEditAct extends TNActBase implements OnClickListener, OnKeyListener, OnTextEditListener {

    /* Bundle:
     * TextType
     * TextHint
     * OriginalText
     * ParentId
     * IsLeaf
     */

    private String mTextType;
    private String mHint;
    private String mOriginalText;
    private Long mParentId;
    private int mIsLeaf;
    private Dialog mSyncProjectDialog = null;
    private EditText textedit = null;

    //
    TextEditPresenter presener;

    // Activity methods
    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.textedit);
        setViews();

        findViewById(R.id.textedit_back).setOnClickListener(this);
        findViewById(R.id.textedit_save).setOnClickListener(this);

        //
        presener = new TextEditPresenter(this, this);

        mSyncProjectDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
        textedit = (EditText) findViewById(R.id.textedit_text);
        textedit.setOnKeyListener(this);

        //hint, original
        Bundle b = getIntent().getExtras();
        mTextType = b.getString("TextType");
        mHint = b.getString("TextHint");
        mOriginalText = b.getString("OriginalText");
        mParentId = b.getLong("ParentId");
        mIsLeaf = b.getInt("IsLeaf");
        MLog.i(TAG, "TNTextEditAct--TextType=" + mTextType + "--TextHint=" + mHint + "--OriginalText=" + mOriginalText + "--ParentId=" + mParentId + "--IsLeaf=" + mIsLeaf);

        if (mTextType.equals("cat_add")) {
            MLog.d("SJY", "新增文件夹");
            ((TextView) findViewById(R.id.textedit_back)).setText("新增文件夹");
        } else if (mTextType.equals("cat_rename")) {
            MLog.d("SJY", "修改文件夹");
            ((TextView) findViewById(R.id.textedit_back)).setText("修改文件夹");
        } else if (mTextType.equals("tag_add")) {
            MLog.d("SJY", "新增标签");
            ((TextView) findViewById(R.id.textedit_back)).setText("新增标签");
        } else if (mTextType.equals("tag_rename")) {
            MLog.d("SJY", "修改标签");
            ((TextView) findViewById(R.id.textedit_back)).setText("修改标签");
        }
    }

    @Override
    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.maincats_toolbar_layout, R.drawable.toolbg);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null, R.id.textedit_save, R.drawable.top_save);
        TNUtilsSkin.setViewBackground(this, null, R.id.textedit_page_bg, R.drawable.page_bg);
    }

    protected void configView() {

        if (createStatus == 0) {
            ((TextView) findViewById(R.id.textedit_hint)).setText(mHint);
            if (mOriginalText.length() > 50) {
                textedit.setText("");
                textedit.setSelection(0);
            } else {
                textedit.setText(mOriginalText);
                textedit.setSelection(mOriginalText.length());
            }
        }
    }

    // Implement OnClickListener
    //-------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textedit_back:
                //lp 2011-12-22
                back();
                break;

            case R.id.textedit_save:
                if (check()) {
                    save();
                }
                break;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTextType = savedInstanceState.getString("TextType");
        mHint = savedInstanceState.getString("Hint");
        mOriginalText = savedInstanceState.getString("OriginalText");
        mParentId = savedInstanceState.getLong("ParentId");
        mIsLeaf = savedInstanceState.getInt("IsLeaf");
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("TextType", mTextType);
        outState.putString("Hint", mHint);
        outState.putString("OriginalText", mOriginalText);
        outState.putLong("ParentId", mParentId);
        outState.putInt("IsLeaf", mIsLeaf);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        mSyncProjectDialog.dismiss();
        super.onDestroy();
    }

    // Private methods
    //-------------------------------------------------------------------------------
    private void save() {
        final String text = textedit.getText().toString().trim();

        if (mTextType.equals("cat_add")) {
            folderAdd(text);
        } else if (mTextType.equals("cat_rename")) {
            if (!text.equals(mOriginalText)) {
                folderRename(text);

            } else {
                finish();
            }
        } else if (mTextType.equals("tag_add")) {
            tagAdd(text);

        } else if (mTextType.equals("tag_rename")) {
            if (!text.equals(mOriginalText)) {
                tagRename(text);

            } else {
                finish();
            }
        }
    }

    //lp 2011-12-21
    private void back() {
        if (simpleCheck()) {
            finish();
            return;
        }
        if (mTextType.equals("serch_project")) {
            finish();
            return;
        }

        if (mTextType.equals("tag_add") || mTextType.equals("tag_rename")) {

            CommonDialog dialog = new CommonDialog(this, R.string.alert_TagList_BackMsg,
                    "保存",
                    "不保存",
                    new CommonDialog.DialogCallBack() {
                        @Override
                        public void sureBack() {
                            if (check()) {
                                save();
                                if (!mTextType.equals("cat_add") && !mTextType.equals("cat_rename"))
                                    finish();
                            }
                        }

                        @Override
                        public void cancelBack() {
                            finish();
                        }

                    });
            dialog.show();
        } else if (mTextType.equals("cat_add") || mTextType.equals("cat_rename")) {
            CommonDialog dialog = new CommonDialog(this, R.string.alert_CatList_BackMsg,
                    "保存",
                    "不保存",
                    new CommonDialog.DialogCallBack() {
                        @Override
                        public void sureBack() {
                            if (check()) {
                                save();
                                if (!mTextType.equals("cat_add") && !mTextType.equals("cat_rename"))
                                    finish();
                            }
                        }

                        @Override
                        public void cancelBack() {
                            finish();
                        }

                    });
            dialog.show();


        }
    }

    //验证用户是否有输入或修改
    private boolean simpleCheck() {
        final String text = textedit.getText().toString().trim();
        if (text.equals("") || text.equals(mOriginalText)) {
            return true;
        }
        return false;
    }

    private boolean check() {
        String text = textedit.getText().toString().trim();

        if (mTextType.equals("cat_add")) {
            if (text.length() <= 0 || text.length() > 50) {
                TNUtilsUi.alert(this, R.string.alert_TextEdit_CatNameWrong);
                return false;
            }

        } else if (mTextType.equals("cat_rename")) {
            if (text.length() <= 0 || text.length() > 50) {
                TNUtilsUi.alert(this, R.string.alert_TextEdit_CatNameWrong);
                return false;
            }

        } else if (mTextType.equals("tag_add")) {
            if (text.length() <= 0 || text.length() > 50
                    || !TNUtilsTag.isTagNameOk(text)) {
                TNUtilsUi.alert(this, R.string.alert_TextEdit_TagNameWrong);
                return false;
            }
            if (TNDbUtils.getTagByText(text) != null) {
                TNUtilsUi.alert(this, R.string.alert_TextEdit_TagExist);
                return false;
            }

        } else if (mTextType.equals("tag_rename")) {
            if (text.length() <= 0 || text.length() > 50
                    || !TNUtilsTag.isTagNameOk(text)) {
                TNUtilsUi.alert(this, R.string.alert_TextEdit_TagNameWrong);
                return false;
            }
            if (!text.equals(mOriginalText) && TNDbUtils.getTagByText(text) != null) {
                TNUtilsUi.alert(this, R.string.alert_TextEdit_TagExist);
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        MLog.i(TAG, "keyCode:" + keyCode + event);
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0
                && !mTextType.equals("serch_project")) {
            back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        MLog.i(TAG, "keyCode:" + keyCode + event);
        if (keyCode == KeyEvent.KEYCODE_ENTER
                && event.getRepeatCount() == 0
                && event.getAction() == 1) {

            InputMethodManager imm = (InputMethodManager) v.getContext().
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            }
            if (check()) {
                save();
                if (!mTextType.equals("serch_project")) {
                    finish();
                }
            }
            return true;
        }
        return false;
    }

    //------------------------------------p层调用-----------------------------------------

    private void folderAdd(String text) {
        presener.pFolderAdd(mParentId, text);
    }

    private void folderRename(String text) {
        presener.pFolderRename(mParentId, text);
    }

    private void tagAdd(String text) {
        presener.pTagAdd(text);
    }

    private void tagRename(String text) {
        presener.pTagRename(mParentId, text);
    }

    //-------------------------------------接口结果回调-----------------------------------------
    @Override
    public void onFolderAddSuccess(Object obj) {
        TNUtilsUi.showToast("保存成功！");
        finish();
    }

    @Override
    public void onFolderAddFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onFolderRenameSuccess(Object obj, String name, long pid) {
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().execSQL(TNSQLString.CAT_RENAME, name, pid);

            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }
        TNUtilsUi.showToast("修改成功！");
        finish();
    }

    @Override
    public void onFolderRenameFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onTagAddSuccess(Object obj) {
        TNUtilsUi.showToast("保存成功！");
        finish();
    }

    @Override
    public void onTagAddFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onTagRenameSuccess(Object obj, String name, long pid) {
        TNDb.getInstance().execSQL(TNSQLString.TAG_RENAME, name, TNUtils.getPingYinIndex(name), pid);
        TNUtilsUi.showToast("修改成功！");
        finish();
    }

    @Override
    public void onTagRenameFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }
}
