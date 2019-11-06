package com.thinkernote.ThinkerNote.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.bean.localdata.TNTag;
import com.thinkernote.ThinkerNote.db.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsSkin;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.views.dialog.CommonDialog;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnTagInfoListener;
import com.thinkernote.ThinkerNote.mvp.p.TagInfoPresenter;

/**
 * 标签属性
 * sjy 0625
 */
public class TNTagInfoAct extends TNActBase
        implements OnClickListener, OnGroupClickListener, OnTagInfoListener {

    /* Bundle:
     * TagLocalId
     */
    private long mTagId;
    private TNTag mTag;
    //p
    private TagInfoPresenter presener;

    //
    LinearLayout ly_tagName, ly_del;
    TextView tv_tagName, tv_noteCount;

    // Activity methods
    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taginfo);
        mTagId = getIntent().getLongExtra("TagId", -1);
        setViews();
        initMyView();

        presener = new TagInfoPresenter(this, this);

    }

    private void initMyView() {
        findViewById(R.id.taginfo_back).setOnClickListener(this);
        ly_tagName = findViewById(R.id.ly_tagName);
        ly_del = findViewById(R.id.ly_del);

        ly_del.setOnClickListener(this);
        ly_tagName.setOnClickListener(this);

        tv_tagName = findViewById(R.id.tv_tagName);
        tv_noteCount = findViewById(R.id.tv_noteCount);
    }

    @Override
    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.taginfo_toolbar_layout, R.drawable.toolbg);
        TNUtilsSkin.setViewBackground(this, null, R.id.taginfo_page, R.drawable.page_bg);
    }

    protected void configView() {
        showView();

    }

    private void showView() {
        mTag = TNDbUtils.getTag(mTagId);
        tv_tagName.setText(mTag.tagName);
        //
        String count = String.valueOf(mTag.noteCounts) + getString(R.string.taginfo_noteunit);
        tv_noteCount.setText(count);

        //
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.taginfo_back:
                finish();
                break;
            case R.id.ly_tagName:
                changeName();
                break;
            case R.id.ly_del:
                deleteTag();
                break;
        }
    }


    // Implement OnClickListener
    //-------------------------------------------------------------------------------
    @Override
    public boolean onGroupClick(ExpandableListView parent, View v,
                                int groupPosition, long id) {
        return true;
    }

    // Implement OnClickListener
    //-------------------------------------------------------------------------------


    public void changeName() {
        Bundle b = new Bundle();
        b.putString("TextType", "tag_rename");
        b.putString("TextHint", getString(R.string.textedit_tag));
        b.putString("OriginalText", mTag.tagName);
        b.putLong("ParentId", mTagId);
        startActivity(TNTextEditAct.class, b);
    }

    public void deleteTag() {
        CommonDialog dialog = new CommonDialog(this, R.string.alert_TagInfo_DeleteMsg,
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        deleteTag(mTagId);
                    }

                    @Override
                    public void cancelBack() {
                    }

                });
        dialog.show();

    }

    //--------------------------------------p层调用------------------------------------------
    private void deleteTag(long mTagId) {
        presener.pTagDelete(mTagId);
    }

    //---------------------------------------接口回调------------------------------------------


    @Override
    public void onSuccess() {
        finish();
    }

    @Override
    public void onFailed(String msg, Exception e) {

    }
}
