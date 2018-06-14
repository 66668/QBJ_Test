package com.thinkernote.ThinkerNote.Activity;

import java.util.Vector;

import org.json.JSONObject;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNRunner;
import com.thinkernote.ThinkerNote.Adapter.TNPreferenceAdapter;
import com.thinkernote.ThinkerNote.Data.TNPreferenceChild;
import com.thinkernote.ThinkerNote.Data.TNPreferenceGroup;
import com.thinkernote.ThinkerNote.Data.TNTag;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNHandleError;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote._constructer.presenter.TagInfoPresenterImpl;
import com.thinkernote.ThinkerNote._interface.m.ITagInfoModule;
import com.thinkernote.ThinkerNote._interface.p.ITagInfoPresener;
import com.thinkernote.ThinkerNote._interface.v.OnCommonListener;
import com.thinkernote.ThinkerNote._interface.v.OnTagInfoListener;
import com.thinkernote.ThinkerNote.base.TNActBase;

/**
 * 标签属性
 */
public class TNTagInfoAct extends TNActBase
        implements OnClickListener, OnChildClickListener, OnGroupClickListener, OnTagInfoListener {

    /* Bundle:
     * TagLocalId
     */
    private ExpandableListView mListView;
    private Vector<TNPreferenceGroup> mGroups;
    private TNPreferenceChild mCurrChild;
    private long mTagId;
    private TNTag mTag;

    //p
    private ITagInfoPresener presener;

    // Activity methods
    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taginfo);

        presener = new TagInfoPresenterImpl(this, this);
        setViews();
        mTagId = getIntent().getLongExtra("TagId", -1);
        // initialize
        findViewById(R.id.taginfo_back).setOnClickListener(this);

        mGroups = new Vector<TNPreferenceGroup>();

        mListView = (ExpandableListView) findViewById(R.id.taginfo_expandablelistview);
        mListView.setAdapter(new TNPreferenceAdapter(this, mGroups));

        mListView.setOnGroupClickListener(this);
        mListView.setOnChildClickListener(this);
    }

    @Override
    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.taginfo_toolbar_layout, R.drawable.toolbg);
        TNUtilsSkin.setViewBackground(this, null, R.id.taginfo_page, R.drawable.page_bg);
    }

    protected void configView() {
        mTag = TNDbUtils.getTag(mTagId);
        getTagInfos();
        ((BaseExpandableListAdapter) mListView.getExpandableListAdapter()).notifyDataSetChanged();
        for (int i = 0; i < mGroups.size(); i++) {
            mListView.expandGroup(i);
        }
    }

    private void getTagInfos() {
        mGroups.clear();
        TNPreferenceGroup group = null;

        //标签
        group = new TNPreferenceGroup(getString(R.string.taginfo_tag));
        {
            {//名称
                group.addChild(new TNPreferenceChild(getString(R.string.taginfo_name), mTag.tagName, true, new TNRunner(this, "changeName")));
            }
            {//笔记数量
                String info = String.valueOf(mTag.noteCounts) + getString(R.string.taginfo_noteunit);
                group.addChild(new TNPreferenceChild(getString(R.string.taginfo_notecount), info, false, null));
            }
            {//删除
                group.addChild(new TNPreferenceChild(getString(R.string.taginfo_delete), null, true, new TNRunner(this, "deleteTag")));
            }
        }
        mGroups.add(group);
    }

    // Implement OnClickListener
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

    // Implement OnClickListener
    //-------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.taginfo_back:
                finish();
                break;
        }
    }

    public void changeName() {
        Bundle b = new Bundle();
        b.putString("TextType", "tag_rename");
        b.putString("TextHint", getString(R.string.textedit_tag));
        b.putString("OriginalText", mTag.tagName);
        b.putLong("ParentId", mTagId);
        startActivity(TNTextEditAct.class, b);
    }

    public void deleteTag() {
        DialogInterface.OnClickListener pbtn_Click =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTag(mTagId);
                    }
                };

        JSONObject jsonData = TNUtils.makeJSON(
                "CONTEXT", this,
                "TITLE", R.string.alert_Title,
                "MESSAGE", R.string.alert_TagInfo_DeleteMsg,
                "POS_BTN", R.string.alert_OK,
                "POS_BTN_CLICK", pbtn_Click,
                "NEG_BTN", R.string.alert_Cancel
        );
        TNUtilsUi.alertDialogBuilder(jsonData).show();
    }

    //--------------------------------------p层调用------------------------------------------
    private void deleteTag(long mTagId) {
        presener.pTagDelete(mTagId);
    }

    //---------------------------------------接口回调------------------------------------------


    @Override
    public void onSuccess(Object obj, long pid) {
        TNDb.getInstance().execSQL(TNSQLString.TAG_REAL_DELETE, pid);
        finish();
    }

    @Override
    public void onFailed(String msg, Exception e) {

    }
}
