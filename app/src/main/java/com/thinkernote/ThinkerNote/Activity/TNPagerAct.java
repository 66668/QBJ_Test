package com.thinkernote.ThinkerNote.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Activity.fragment.TNPageCats;
import com.thinkernote.ThinkerNote.Activity.fragment.TNPageNotes;
import com.thinkernote.ThinkerNote.Activity.fragment.TNPageTags;
import com.thinkernote.ThinkerNote.DBHelper.NoteAttrDbHelper;
import com.thinkernote.ThinkerNote.DBHelper.NoteDbHelper;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
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
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.Other.HorizontalPager;
import com.thinkernote.ThinkerNote.Other.HorizontalPager.OnScreenSwitchListener;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.presenter.PagerPresenterImpl;
import com.thinkernote.ThinkerNote._interface.p.IPagerPresenter;
import com.thinkernote.ThinkerNote._interface.v.OnPagerListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.base.TNChildViewBase;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.GetNoteByNoteIdBean;
import com.thinkernote.ThinkerNote.bean.main.OldNoteAddBean;
import com.thinkernote.ThinkerNote.bean.main.OldNotePicBean;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.thinkernote.ThinkerNote.Utils.MLog.i;

/**
 * 主页--我的笔记
 * 样式：act+3个frag
 * 说明：syncCats的2-10和2-11-2，与main不同
 * <p>
 * sjy 0704
 */

public class TNPagerAct extends TNActBase implements OnScreenSwitchListener, OnClickListener,
        OnPagerListener {
    //正常登录的同步常量
    public static final int DELETE_LOCALNOTE = 101;//1
    public static final int DELETE_REALNOTE = 102;//
    public static final int DELETE_REALNOTE2 = 103;//
    public static final int UPDATA_EDITNOTES = 104;//
    public static final int CAT = 105;//
    public static final int DIALOG_DELETE = 106;//
    public static final int CAT_DELETE = 107;//
    public static final int CC_DELETE = 108;//清空回收站
    public static final int SYNC_DATA_BY_NOTEID = 110;//
    public static final int CHILD_HANDLER_2_11 = 111;//
    public static final int UI_HANDLER_2_11 = 112;//
    //
    private HorizontalPager mPager;
    private Vector<TNChildViewBase> mChildPages;
    private TNChildViewBase mCurrChild;
    private ProgressDialog mProgressDialog;
    private TNSettings mSettings = TNSettings.getInstance();
    private TNNote mCurrNote;
    private TNCat mCurrCat;
    private TNTag mCurTag;
    private AlertDialog dialog;//GetDataByNoteId的弹窗；
    private long catId;//syncCats使用的全局数据
    //p
    IPagerPresenter presenter;

    private Vector<TNNote> addNewNotes;//（2-5）正常同步，第5个调用数据
//    private Vector<TNNoteAtt> newNotesAtts;//（2-5）正常同步，第5个调用数据中第一调用的数据 不可使用全局，易错

    private Vector<TNNote> recoveryNotes;//(2-7)正常同步，第7个调用数据
//    Vector<TNNoteAtt> recoveryNotesAtts;//(2-7)正常同步，第7个调用数据中第一调用的数据 不可使用全局，易错

    Vector<TNNote> deleteNotes;//(2-8)正常同步，第8个调用数据
    Vector<TNNote> deleteRealNotes;//(2-9)正常同步，第9个调用数据
    Vector<TNNote> allNotes;//(2-10)正常同步，第10个调用数据
    Vector<TNNote> editNotes;//(2-11-1)正常同步，第11个调用数据
    Vector<TNNote> allLocalNotes;//(2-11-2)正常同步，第12个调用数据
    Vector<TNNote> catNotes;//2-12同步使用

    private List<AllNotesIdsBean.NoteIdItemBean> cloudIds;//2-10接口返回

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_layout);
        //p
        presenter = new PagerPresenterImpl(this, this);

        initAct();
        //
        initFrag();
    }

    private void initAct() {

        findViewById(R.id.table_home).setOnClickListener(this);
        findViewById(R.id.table_notes_newnote).setOnClickListener(this);
        findViewById(R.id.table_notes_search).setOnClickListener(this);
        findViewById(R.id.table_notes_sort).setOnClickListener(this);
        findViewById(R.id.table_cats_newfolder).setOnClickListener(this);
        findViewById(R.id.table_cats_newnote).setOnClickListener(this);
        findViewById(R.id.table_cats_serch).setOnClickListener(this);
        findViewById(R.id.table_cats_sort).setOnClickListener(this);
        findViewById(R.id.table_tags_newtag).setOnClickListener(this);
        findViewById(R.id.tablelayout_btn_page1).setOnClickListener(this);
        findViewById(R.id.tablelayout_btn_page2).setOnClickListener(this);
        findViewById(R.id.tablelayout_btn_page3).setOnClickListener(this);
        //
        mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);

    }

    private void initFrag() {
        //frag--title
        ((RadioButton) findViewById(R.id.tablelayout_btn_page1)).setText(R.string.table_notes);
        ((RadioButton) findViewById(R.id.tablelayout_btn_page2)).setText(R.string.table_cats);
        ((RadioButton) findViewById(R.id.tablelayout_btn_page3)).setText(R.string.table_tags);

        mPager = (HorizontalPager) findViewById(R.id.tablelayout_horizontalPager);
        mPager.setOnScreenSwitchListener(this);

        mChildPages = new Vector<TNChildViewBase>();
        //frag--pager1
        TNPageNotes notesView = new TNPageNotes(this);
        mChildPages.add(notesView);
        //frag--pager2
        TNPageCats catsView = new TNPageCats(this);
        mChildPages.add(catsView);
        //frag--pager3
        TNPageTags tagsView = new TNPageTags(this);
        mChildPages.add(tagsView);

        //显示
        for (int i = 0; i < mChildPages.size(); i++) {
            mPager.addView(mChildPages.get(i).mChildView);
        }
        int screen = 0;
        mPager.setCurrentScreen(screen, false);
        mCurrChild = mChildPages.get(screen);
        changeViewForScreen(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //================================布局相关的点击事件================================
            case R.id.table_notes_newnote: {//新建 标签
                TNNote note = TNNote.newNote();
                Bundle b = new Bundle();
                b.putLong("NoteForEdit", note.noteLocalId);
                b.putSerializable("NOTE", note);
                startActivity(TNNoteEditAct.class, b);
                break;
            }
            case R.id.table_notes_search: {//搜索
                Bundle b = new Bundle();
                b.putInt("SearchType", 1);
                startActivity(TNSearchAct.class, b);
                break;
            }
            case R.id.table_notes_sort: {//排序
                //笔记排序
                if (mSettings.sort == TNConst.CREATETIME) {
                    mSettings.sort = TNConst.UPDATETIME;
                    TNUtilsUi.showShortToast("按更新时间排序");
                } else {
                    mSettings.sort = TNConst.CREATETIME;
                    TNUtilsUi.showShortToast("按创建时间排序");
                }
                mSettings.savePref(false);
                ((TNPageNotes) mCurrChild).isNewSortord = true;
                configView();
                break;
            }
            case R.id.table_cats_newfolder: {//新建 文件夹
                ((TNPageCats) mCurrChild).newFolder();
                break;
            }
            case R.id.table_cats_newnote://新建 标签
                ((TNPageCats) mCurrChild).newNote();
                break;
            case R.id.table_cats_serch: {//搜索
                Bundle b = new Bundle();
                b.putInt("SearchType", 1);
                startActivity(TNSearchAct.class, b);
                break;
            }
            case R.id.table_cats_sort: {//排序
                //文件夹排序
                if (mSettings.sort == TNConst.CREATETIME) {
                    mSettings.sort = TNConst.UPDATETIME;
                    TNUtilsUi.showShortToast("按更新时间排序");
                } else {
                    mSettings.sort = TNConst.CREATETIME;
                    TNUtilsUi.showShortToast("按创建时间排序");
                }
                mSettings.savePref(false);
                configView();
                break;
            }
            case R.id.table_tags_newtag: {//添加 一个标签
                Bundle b = new Bundle();
                b.putString("TextType", "tag_add");
                b.putString("TextHint", getString(R.string.textedit_tag));
                b.putString("OriginalText", "");
                startActivity(TNTextEditAct.class, b);
                break;
            }
            case R.id.table_home://返回
                finish();
                break;
            case R.id.tablelayout_btn_page1://pager 1
                if (mPager.getCurrentScreen() != 0) {
                    mPager.setCurrentScreen(0, true);
                }
                break;
            case R.id.tablelayout_btn_page2://pager 2
                if (mPager.getCurrentScreen() != 1) {
                    mPager.setCurrentScreen(1, true);
                }
                break;
            case R.id.tablelayout_btn_page3://pager 3
                if (mPager.getCurrentScreen() != 2) {
                    mPager.setCurrentScreen(2, true);
                }
                break;

            //================================01 笔记相关的点击事件================================
            case R.id.notelistitem_menu_view: {// 查看
                mMenuBuilder.destroy();
                if (mCurrNote == null)
                    break;
                Bundle b = new Bundle();
                int type = 0;
                if (mCurrChild.pageId == R.id.page_sharenotes) {
                    type = 1;
                }
                b.putInt("Type", type);
                b.putLong("NoteLocalId", mCurrNote.noteLocalId);
                startActivity(TNNoteViewAct.class, b);
                break;
            }
            case R.id.notelistitem_menu_changetag: {//更换标签
                mMenuBuilder.destroy();
                if (mCurrNote == null)
                    break;
                TNNote note = TNDbUtils.getNoteByNoteLocalId(mCurrNote.noteLocalId);
                if (note.syncState != 2) {
                    Toast.makeText(this,
                            R.string.alert_NoteList_NotCompleted_ChangTag,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                Bundle b = new Bundle();
                b.putString("TagStrForEdit", note.tagStr);
                b.putLong("ChangeTagForNoteList", note.noteLocalId);
                startActivity(TNTagListAct.class, b);
                break;
            }

            case R.id.notelistitem_menu_edit: {//标签 编辑
                mMenuBuilder.destroy();
                if (mCurrNote == null)
                    break;
                if (mCurrNote.syncState == 2) {
                    Bundle b = new Bundle();
                    b.putLong("NoteForEdit", mCurrNote.noteLocalId);
                    b.putLong("NoteLocalId", mCurrNote.noteLocalId);
                    startActivity(TNNoteEditAct.class, b);
                } else {
                    TNHandleError.handleErrorCode(this,
                            this.getResources().getString(R.string.alert_NoteView_NotCompleted));
                }
                break;
            }

            case R.id.notelistitem_menu_moveto: {//标签 移动到文件夹
                mMenuBuilder.destroy();
                if (mCurrNote == null)
                    break;
                TNNote note = TNDbUtils.getNoteByNoteLocalId(mCurrNote.noteLocalId);
                if (note.syncState == 1) {
                    Toast.makeText(this, R.string.alert_NoteList_NotCompleted_Move,
                            Toast.LENGTH_SHORT).show();
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
                MLog.d("TNPagerAct--笔记相关--完全同步");
                mMenuBuilder.destroy();
                if (mCurrNote == null || mCurrNote.noteId == -1)
                    break;
                showSyncDialog(mCurrNote.noteId);

                break;
            }

            case R.id.notelistitem_menu_info: {//属性
                mMenuBuilder.destroy();
                if (mCurrNote == null)
                    break;
                Bundle b = new Bundle();
                b.putLong("NoteLocalId", mCurrNote.noteLocalId);
                startActivity(TNNoteInfoAct.class, b);
                break;
            }

            case R.id.notelistitem_menu_delete: {// 删除
                mMenuBuilder.destroy();
                if (mCurrNote == null)
                    break;
                TNNote note = TNDbUtils.getNoteByNoteLocalId(mCurrNote.noteLocalId);
                //
                showDeleteDialog(note.noteLocalId);
                break;
            }

            case R.id.notelistitem_menu_cancel: {//取消
                mMenuBuilder.destroy();
                break;
            }

            //================================02 文件夹相关的点击事件================================
            case R.id.folder_menu_sync: {
                MLog.d("TNPagerAct--文件夹--完全同步");
                mMenuBuilder.destroy();
                if (mCurrCat == null)
                    break;
                showSyncCatDialog(mCurrCat.catId);

                break;
            }

            case R.id.folder_menu_rename: {//文件 重命名
                mMenuBuilder.destroy();
                if (mCurrCat == null)
                    break;
                Bundle b = new Bundle();
                b.putString("TextType", "cat_rename");
                b.putString("TextHint", getString(R.string.textedit_folder));
                b.putString("OriginalText", mCurrCat.catName);
                b.putLong("ParentId", mCurrCat.catId);
                startActivity(TNTextEditAct.class, b);
                break;
            }

            case R.id.folder_menu_moveto: {//移动到文件夹
                mMenuBuilder.destroy();
                if (mCurrCat == null)
                    break;
                Bundle b = new Bundle();
                b.putLong("OriginalCatId", mCurrCat.pCatId);
                b.putInt("Type", 0);
                b.putLong("ChangeFolderForFolderList", mCurrCat.catId);
                startActivity(TNCatListAct.class, b);
                break;
            }

            case R.id.folder_menu_delete: {//文件 删除
                mMenuBuilder.destroy();
                if (mCurrCat == null)
                    break;
                mProgressDialog.show();
                showCatDeleteDialog(mCurrCat);


                break;
            }

            case R.id.folder_menu_info: {//属性
                mMenuBuilder.destroy();
                if (mCurrCat == null)
                    break;
                Bundle b = new Bundle();
                b.putLong("CatId", mCurrCat.catId);
                startActivity(TNCatInfoAct.class, b);
                break;
            }

            case R.id.folder_menu_setdefault: {//设为默认文件夹
                mMenuBuilder.destroy();
                if (mCurrCat == null)
                    break;
                setDefaultCatDialog(mCurrCat);
                break;
            }

            case R.id.folder_menu_recycle: {//清空回收站
                mMenuBuilder.destroy();
                clearrecyclerDialog();

                break;
            }

            case R.id.folder_menu_cancel: {//取消
                mMenuBuilder.destroy();
                break;
            }

            //================================03 标签相关的点击事件================================
            case R.id.tag_menu_display: {// 详情
                mMenuBuilder.destroy();
                if (mCurTag == null)
                    break;
                Bundle b = new Bundle();
                b.putLong("UserId", mSettings.userId);
                b.putInt("ListType", 4);
                b.putLong("ListDetail", mCurTag.tagId);
                b.putInt("count", mCurTag.noteCounts);
                startActivity(TNNoteListAct.class, b);
                break;
            }

            case R.id.tag_menu_info: {//属性
                mMenuBuilder.destroy();
                if (mCurTag == null)
                    break;
                Bundle b = new Bundle();
                b.putLong("TagId", mCurTag.tagId);
                startActivity(TNTagInfoAct.class, b);
                break;
            }

            case R.id.tag_menu_delete: {//删除
                mMenuBuilder.destroy();
                if (mCurTag == null)
                    break;
                deleteTag(mCurTag);
                break;
            }

            case R.id.tag_menu_cancel: {//取消
                mMenuBuilder.destroy();
                break;
            }
        }
    }

    @Override
    public void onScreenSwitched(int screen) {
        mCurrChild = mChildPages.get(screen);
        changeViewForScreen(screen);
    }

    // ---------------------------------------弹窗----------------------------------------

    /**
     * clearrecycler 弹窗
     */
    private void clearrecyclerDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //title
        LayoutInflater lf1 = LayoutInflater.from(this);
        View title = lf1.inflate(R.layout.dialog, null);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_layout, R.drawable.page_color);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_top_bar, R.drawable.dialog_top_bg);
        TNUtilsSkin.setImageViewDrawable(this, title, R.id.dialog_icon, R.drawable.dialog_icon);
        builder.setCustomTitle(title);

        ((TextView) title.findViewById(R.id.dialog_title)).setText(R.string.alert_Title);//title

        ((TextView) title.findViewById(R.id.dialog_msg)).setText((Integer) R.string.alert_NoteList_ClearRecycle);//content

        //
        final DialogInterface.OnClickListener posListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TNActionUtils.isSynchronizing()) {
                    //具体执行
                    ExecutorService service = Executors.newSingleThreadExecutor();
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

                            handler.sendEmptyMessage(CC_DELETE);
                        }
                    });

                }
            }
        };
        builder.setPositiveButton(R.string.alert_OK, posListener);

        //
        DialogInterface.OnClickListener negListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                dialog.dismiss();
            }
        };
        builder.setNegativeButton(R.string.alert_Cancel, negListener);
        dialog = builder.create();
        dialog.show();
    }

    /**
     * 文件删除 弹窗
     *
     * @param cat
     */
    private void showCatDeleteDialog(final TNCat cat) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //title
        LayoutInflater lf1 = LayoutInflater.from(this);
        View title = lf1.inflate(R.layout.dialog, null);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_layout, R.drawable.page_color);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_top_bar, R.drawable.dialog_top_bg);
        TNUtilsSkin.setImageViewDrawable(this, title, R.id.dialog_icon, R.drawable.dialog_icon);
        builder.setCustomTitle(title);

        ((TextView) title.findViewById(R.id.dialog_title)).setText(R.string.alert_Title);//title

        ((TextView) title.findViewById(R.id.dialog_msg)).setText((Integer) R.string.alert_CatInfo_Delete_HasChild);//content

        //
        final DialogInterface.OnClickListener posListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TNActionUtils.isSynchronizing()) {
                    TNUtilsUi.showNotification(TNPagerAct.this, R.string.alert_NoteView_Synchronizing, false);
                    //具体执行
                    pCatDelete(cat);
                }
            }
        };
        builder.setPositiveButton(R.string.alert_OK, posListener);

        //
        DialogInterface.OnClickListener negListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                dialog.dismiss();
            }
        };
        builder.setNegativeButton(R.string.alert_Cancel, negListener);
        dialog = builder.create();
        dialog.show();
    }

    /**
     * 删除 弹窗
     *
     * @param noteLocalId
     */
    private void showDeleteDialog(final long noteLocalId) {

        TNNote note = TNDbUtils.getNoteByNoteLocalId(noteLocalId);
        MLog.d("删除笔记--noteLocalId=" + noteLocalId + "--TNNote：" + note.toString());
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //title
        LayoutInflater lf1 = LayoutInflater.from(this);
        View title = lf1.inflate(R.layout.dialog, null);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_layout, R.drawable.page_color);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_top_bar, R.drawable.dialog_top_bg);
        TNUtilsSkin.setImageViewDrawable(this, title, R.id.dialog_icon, R.drawable.dialog_icon);
        builder.setCustomTitle(title);

        ((TextView) title.findViewById(R.id.dialog_title)).setText(R.string.alert_Title);//title
        //content
        int msg = R.string.alert_NoteView_DeleteNoteMsg;
        if (TNSettings.getInstance().isInProject()) {
            msg = R.string.alert_NoteView_DeleteNoteMsg_InGroup;
        }
        ((TextView) title.findViewById(R.id.dialog_msg)).setText((Integer) msg);//content

        //
        final DialogInterface.OnClickListener posListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TNActionUtils.isSynchronizing()) {
                    TNUtilsUi.showNotification(TNPagerAct.this, R.string.alert_NoteView_Synchronizing, false);
                    //具体执行
                    pDialogDelete(noteLocalId);

                }
            }
        };
        builder.setPositiveButton(R.string.alert_OK, posListener);

        //
        DialogInterface.OnClickListener negListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                dialog.dismiss();
            }
        };
        builder.setNegativeButton(R.string.alert_Cancel, negListener);
        dialog = builder.create();
        dialog.show();
    }

    /**
     * 完全同步SynceDataByNoteId
     */
    private void showSyncDialog(final long noteId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //title
        LayoutInflater lf1 = LayoutInflater.from(this);
        View title = lf1.inflate(R.layout.dialog, null);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_layout, R.drawable.page_color);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_top_bar, R.drawable.dialog_top_bg);
        TNUtilsSkin.setImageViewDrawable(this, title, R.id.dialog_icon, R.drawable.dialog_icon);
        builder.setCustomTitle(title);
        ((TextView) title.findViewById(R.id.dialog_title)).setText(R.string.alert_Title);//title
        ((TextView) title.findViewById(R.id.dialog_msg)).setText((Integer) R.string.alert_MainCats_SynchronizeNoteAll);//content

        //
        final DialogInterface.OnClickListener posListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TNActionUtils.isSynchronizing()) {
                    TNUtilsUi.showNotification(TNPagerAct.this, R.string.alert_NoteView_Synchronizing, false);
                    //监听
                    MLog.d("同步GetDataByNoteId");
                    pSynceDataByNoteId(noteId, -1, false);
                }
            }
        };
        builder.setPositiveButton(R.string.maincats_menu_syncall, posListener);

        //
        DialogInterface.OnClickListener negListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                dialog.dismiss();
            }
        };
        builder.setNegativeButton(R.string.alert_Cancel, negListener);
        dialog = builder.create();
        dialog.show();
    }

    /**
     * 完全同步syncCats
     */
    private void showSyncCatDialog(final long noteId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //title
        LayoutInflater lf1 = LayoutInflater.from(this);
        View title = lf1.inflate(R.layout.dialog, null);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_layout, R.drawable.page_color);
        TNUtilsSkin.setViewBackground(this, title, R.id.dialog_top_bar, R.drawable.dialog_top_bg);
        TNUtilsSkin.setImageViewDrawable(this, title, R.id.dialog_icon, R.drawable.dialog_icon);
        builder.setCustomTitle(title);
        ((TextView) title.findViewById(R.id.dialog_title)).setText(R.string.alert_Title);//title
        ((TextView) title.findViewById(R.id.dialog_msg)).setText((Integer) R.string.alert_MainCats_SynchronizeNoteAll);//content

        //
        final DialogInterface.OnClickListener posListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TNActionUtils.isSynchronizing()) {
                    TNUtilsUi.showNotification(TNPagerAct.this, R.string.alert_NoteView_Synchronizing, false);
                    //监听
                    MLog.d("同步Cats");
                    pSynceCat(noteId);
                }
            }
        };
        builder.setPositiveButton(R.string.maincats_menu_syncall, posListener);

        //
        DialogInterface.OnClickListener negListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                dialog.dismiss();
            }
        };
        builder.setNegativeButton(R.string.alert_Cancel, negListener);
        dialog = builder.create();
        dialog.show();
    }

    private void changeViewForScreen(int screen) {
        switch (mCurrChild.pageId) {
            case R.id.page_notes: {
                findViewById(R.id.table_toolbar_layout_notes).setVisibility(
                        View.VISIBLE);
                findViewById(R.id.table_toolbar_layout_cats).setVisibility(
                        View.GONE);
                findViewById(R.id.table_toolbar_layout_tags).setVisibility(
                        View.GONE);

                RadioButton rb = (RadioButton) findViewById(R.id.tablelayout_btn_page1);
                rb.setChecked(true);
                break;
            }

            case R.id.page_cats: {
                findViewById(R.id.table_toolbar_layout_notes).setVisibility(
                        View.GONE);
                findViewById(R.id.table_toolbar_layout_cats).setVisibility(
                        View.VISIBLE);
                findViewById(R.id.table_toolbar_layout_tags).setVisibility(
                        View.GONE);
                RadioButton rb = (RadioButton) findViewById(R.id.tablelayout_btn_page2);
                rb.setChecked(true);
                configView();
                break;
            }

            case R.id.page_tags: {
                findViewById(R.id.table_toolbar_layout_notes).setVisibility(
                        View.GONE);
                findViewById(R.id.table_toolbar_layout_cats).setVisibility(
                        View.GONE);
                findViewById(R.id.table_toolbar_layout_tags).setVisibility(
                        View.VISIBLE);
                RadioButton rb = (RadioButton) findViewById(R.id.tablelayout_btn_page3);
                rb.setChecked(true);
                configView();
                break;
            }
        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        i(TAG, "onRestoreInstanceState");
        int screen = savedInstanceState.getInt("CurrentScreen");
        mPager.setCurrentScreen(screen, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        i(TAG, "onSaveInstanceState");
        outState.putInt("CurrentScreen", mPager.getCurrentScreen());
        super.onSaveInstanceState(outState);
    }

    public void addNoteMenu(int resource) {
        mCurrNote = (TNNote) mCurrChild.mBundle.get("currentNote");
        View view = addMenu(resource);
        view.findViewById(R.id.notelistitem_menu_view).setOnClickListener(this);
        view.findViewById(R.id.notelistitem_menu_edit).setOnClickListener(this);
        view.findViewById(R.id.notelistitem_menu_sync).setOnClickListener(this);
        view.findViewById(R.id.notelistitem_menu_delete).setOnClickListener(this);
        view.findViewById(R.id.notelistitem_menu_moveto).setOnClickListener(this);
        view.findViewById(R.id.notelistitem_menu_changetag).setOnClickListener(this);
        view.findViewById(R.id.notelistitem_menu_info).setOnClickListener(this);
        view.findViewById(R.id.notelistitem_menu_cancel).setOnClickListener(this);
    }

    public void addCatMenu(int resource) {
        mCurrCat = (TNCat) mCurrChild.mBundle.get("currentCat");
        View view = addMenu(resource);
        if (mCurrCat.catId == -1002) {
            view.findViewById(R.id.folder_menu_recycle).setOnClickListener(this);
        } else {
            view.findViewById(R.id.folder_menu_sync).setOnClickListener(this);
            view.findViewById(R.id.folder_menu_rename).setOnClickListener(this);
            if (mCurrCat.catId != mSettings.defaultCatId) {
                view.findViewById(R.id.folder_menu_moveto).setOnClickListener(this);
                view.findViewById(R.id.folder_menu_delete).setOnClickListener(this);
                view.findViewById(R.id.folder_menu_setdefault).setOnClickListener(this);
            }
            view.findViewById(R.id.folder_menu_info).setOnClickListener(this);
        }
        view.findViewById(R.id.folder_menu_cancel).setOnClickListener(this);
    }

    public void addTagMenu(int resource) {
        mCurTag = (TNTag) mCurrChild.mBundle.get("currentTag");
        View view = addMenu(resource);
        view.findViewById(R.id.tag_menu_display).setOnClickListener(this);
        view.findViewById(R.id.tag_menu_info).setOnClickListener(this);
        view.findViewById(R.id.tag_menu_delete).setOnClickListener(this);
        view.findViewById(R.id.tag_menu_cancel).setOnClickListener(this);
    }

    private void deleteTag(final TNTag tag) {
        DialogInterface.OnClickListener pbtn_Click =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgressDialog.show();
                        pDeleteTag(tag.tagId);

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

    private void setDefaultCatDialog(final TNCat cat) {
        DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                setDefaultFolder(cat.catId);
                configView();
            }
        };

        JSONObject jsonData = TNUtils.makeJSON("CONTEXT", this, "TITLE",
                R.string.alert_Title, "MESSAGE",
                R.string.alert_CatInfo_SetDefaultMsg, "POS_BTN",
                R.string.alert_OK, "POS_BTN_CLICK", pbtn_Click, "NEG_BTN",
                R.string.alert_Cancel);
        TNUtilsUi.alertDialogBuilder(jsonData).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mCurrChild.pageId == R.id.page_cats) {
                if (((TNPageCats) mCurrChild).onKeyDown()) {
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);

    }

    public void dialogCB() {
//		mProgressDialog.show();
        configView();
    }

    public void deleteNoteCallBack() {
        if (isInFront)
            configView();
    }

    @Override
    protected void configView() {
        for (TNChildViewBase child : mChildPages) {
//			if (child.pageId == mCurrChild.pageId)
            child.configView(createStatus);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finish();
    }

    /**
     * GetAllDataByNoteId调用结束
     *
     * @param type 0=正常结束 1= cancel 2= stop
     * @param type
     */
    private void endGetAllDataByNoteId(int type) {
        MLog.d("GetDataByNoteId--同步-->完成");
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }

        TNUtilsUi.showToast("同步完成");

        if (type == 1) {
            TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell, true);
        } else if (type == 0) {
            TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
        } else {
            TNUtilsUi.showNotification(this, R.string.alert_Synchronize_Stoped, true);
        }
        configView();
    }

    /**
     * syncCats结束调用
     *
     * @param type 0=正常结束 1= cancel 2= stop
     */
    private void endSyncCats(int type) {

        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        TNUtilsUi.showToast("同步完成");
        if (type == 0) {
            TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell, true);
            mSettings.originalSyncTime = System.currentTimeMillis();
            mSettings.savePref(false);
        } else if (type == 1) {
            TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
        } else {
            TNUtilsUi.showNotification(this,
                    R.string.alert_Synchronize_Stoped, true);
        }
        configView();
    }


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
            case CAT:
                //执行下一个pos
                pGetAllDataByNoteId(((Integer) msg.obj + 1));
                break;
            case DIALOG_DELETE:
                configView();
                break;
            case CAT_DELETE:
                mProgressDialog.hide();
                configView();
                break;
            case CC_DELETE:
                mProgressDialog.hide();
                TNUtilsUi.showToast("回收站已清空");
                configView();
                break;
            case SYNC_DATA_BY_NOTEID:
                //关闭弹窗
                endGetAllDataByNoteId(0);
                break;
            case UI_HANDLER_2_11:
                Bundle bundle = msg.getData();
                int position = bundle.getInt("int");
                boolean is13 = bundle.getBoolean("boolean");

                pUpdataNote(position + 1, false);
                break;
        }

    }

    HandlerThread handlerThread2_11 = new HandlerThread("main_sjy2-11");
    Handler chlidHanlder2_11;

    //-------------------------------------GetDataByNoteId数据库操作----------------------------------------

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

    private void syncGetDataByNoteIdSQL(final long noteId, final int catPos, final boolean isCat) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                TNNote note = TNDbUtils.getNoteByNoteId(noteId);
                note.syncState = 2;
                if (note.attCounts > 0) {
                    for (int i = 0; i < note.atts.size(); i++) {
                        TNNoteAtt tempAtt = note.atts.get(i);
                        if (i == 0 && tempAtt.type > 10000 && tempAtt.type < 20000) {
                            TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, tempAtt.path, note.noteLocalId);
                        }
                        if (TextUtils.isEmpty(tempAtt.path) || "null".equals(tempAtt.path)) {
                            note.syncState = 1;
                        }
                    }
                }
                TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, note.syncState, note.noteLocalId);

                if (isCat) {
                    //下一个catpos
                    Message msg = Message.obtain();
                    msg.what = CAT;
                    msg.obj = catPos;
                    handler.sendMessage(msg);
                } else {

                    //下一个接口
                    handler.sendEmptyMessage(SYNC_DATA_BY_NOTEID);

                }
                ;
            }
        });

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
                    TNDb.getInstance().execSQL(TNSQLString.NOTE_DELETE_BY_NOTEID, nonteLocalID);
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
    //getDataBynoteId 调用 2-11-2

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

    /**
     * 2-11-1 更新日记时间
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
    //--------------------------------p层调用--------------------------------------

    private void setDefaultFolder(long catId) {
        presenter.pSetDefaultFolder(catId);
    }

    private void pDeleteTag(long tagId) {
        presenter.pDeleteTag(tagId);
    }

    private void pCatDelete(TNCat cat) {
        presenter.pDeleteCat(cat.catId);
    }


    // 弹窗触发删除
    private void pDialogDelete(final long noteLocalId) {
        ExecutorService service = Executors.newSingleThreadExecutor();
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

    /**
     * 2-12-1/dialog共用，如果是2-12-1调用，isCats = true
     * TNActionType.GetAllDataByNoteId的顺序是：
     * GetNoteById-->SyncNoteAtt的循环下载--->数据库
     *
     * @param noteId
     * @param catPos dialog不用,2-12使用,标记catNotes的位置
     * @param isCats dialog不用，2-12使用，
     */

    private void pSynceDataByNoteId(long noteId, int catPos, boolean isCats) {
        MLog.d("GetDataByNoteId-->pSynceDataByNoteId");
        presenter.pGetDataByNoteId(noteId, catPos, isCats);

    }

    /**
     * 2-12-2
     * 2-12-2/dialog共用，如果是2-12-2调用，isCats = true
     *
     * @param position atts的位置标记
     * @param atts
     * @param noteId
     */
    private void pSynceNoteAttr(int position, Vector<TNNoteAtt> atts, long noteId, int catPos, boolean isCats) {
        if (position < atts.size()) {
            MLog.d("GetDataByNoteId-->pSynceNoteAttr");
            presenter.pSynceNoteAttr(position, atts.get(position), atts, noteId, catPos, isCats);
        } else {
            //下一个catPos/下一个接口
            syncGetDataByNoteIdSQL(noteId, catPos, isCats);
        }
    }

    /**
     * @param catId
     */

    private void pSynceCat(long catId) {
        this.catId = catId;
        pAddNewNote();
    }


    //==================================syncCats p层调用=======================================

    /**
     * (二.5+二.6)正常同步 添加新笔记 pAddNewNote
     * 说明：同(二.2+二.3)的执行顺序，先处理notepos的图片，处理完就上传notepos的文本，然后再处理notepos+1的图片，如此循环
     * 接口个数：addNewNotes.size * addNewNotes.size
     */

    private void pAddNewNote() {
        MLog.d("sync---2-5-pAddNewNote");
        addNewNotes = TNDbUtils.getNoteListBySyncStateByCatId(TNSettings.getInstance().userId, 3, catId);//说明：和main的2-5此处不同


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
        MLog.d("sync---2-5-pAddNewNote--pNewNotePic");
        presenter.pNewNotePic(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.6)正常同步 第2个执行的接口 循环调用
     * 和（二.5组成双层for循环，该处是最外层for执行）
     */

    private void pNewNote(int position, int arraySize, TNNote tnNoteAtt, boolean isNewDb, String content) {
        MLog.d("sync---2-6-pAddNewNote--pNewNote");

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
        MLog.d("sync---2-7-recoveryNote");
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
        MLog.d("sync---2-7-1-pRecoveryNote");
        presenter.pRecoveryNote(noteID, position, arrySize);
    }

    /**
     * (二.7)02
     */
    private void pRecoveryNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        MLog.d("sync---2-7-2-pRecoveryNotePic");
        presenter.pRecoveryNotePic(picPos, picArrySize, notePos, noteArrySize, tnNoteAtt);
    }

    /**
     * (二.7)03
     */
    private void pRecoveryNoteAdd(int position, int arraySize, TNNote tnNoteAtt, boolean isNewDb, String content) {
        MLog.d("sync---2-7-3-pRecoveryNoteAdd");
        presenter.pRecoveryNoteAdd(position, arraySize, tnNoteAtt, isNewDb, content);
    }


    /**
     * (二.8)
     *
     * @param position
     */
    private void pDelete(int position) {
        MLog.d("sync---2-8-pDelete");

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
        presenter.pDeleteNote(noteId, postion);
        MLog.d("sync---2-8-pDelete--pNoteDelete");
    }

    /**
     * (二.8)删除本地数据 （不调接口）
     */
    private void pNoteLocalDelete(final int position, final long noteLocalId) {
        MLog.d("sync---2-8-pDelete--pNoteLocalDelete");
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
        MLog.d("sync---2-9-pRealDelete");
        if (deleteRealNotes.size() > 0 && position < deleteRealNotes.size()) {
            if (deleteRealNotes.get(position).noteId == -1) {
                //
                pDeleteReadNotesSql(deleteRealNotes.get(position).noteLocalId, position);
            } else {
                //2个接口
                pDeleteRealNotes(deleteRealNotes.get(position).noteId, position);
            }
        } else {
            //  下一个接口
            pGetFolderNoteIds();
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
        MLog.d("sync---2-9-pRealDelete--pDeleteReadNotesSql");
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
        MLog.d("sync---2-9-pRealDelete--pDeleteRealNotes");
        //
        presenter.pDeleteRealNotes(noteId, postion);

    }

    /**
     * 2-10 说明：2-5---2-9同main，2-10之后有区别
     */
    private void pGetFolderNoteIds() {
        presenter.pGetFolderNoteIds(catId);
    }

    /**
     * (二.10)-1 editNote上传图片
     * 说明：
     * 2-10-1和2-11-1图片上传和2-5/2-6相同
     *
     * @param position cloudIds数据的其实操作位置
     */
    private void pEditNotePic(int position) {
        MLog.d("sync---2-10-pEditNotePic");
        if (cloudIds.size() > 0 && position < (cloudIds.size())) {
            long id = cloudIds.get(position).getId();
            int lastUpdate = cloudIds.get(position).getUpdate_at();
            if (editNotes != null && editNotes.size() > 0) {
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
            allLocalNotes = TNDbUtils.getNoteListByCatId(TNSettings.getInstance().userId, catId, TNSettings.getInstance().sort, TNConst.MAX_PAGE_SIZE);
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
        MLog.d("sync---2-10-1-pEditNotePic");
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
            allLocalNotes = TNDbUtils.getNoteListByCatId(TNSettings.getInstance().userId, catId, TNSettings.getInstance().sort, TNConst.MAX_PAGE_SIZE);

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
        MLog.d("sync---2-11-1-pEditNotes");
        if (cloudIds.size() > 0 && cloudsPos < (cloudIds.size() - 1)) {
            presenter.pEditNote(cloudsPos, note);
        } else {
            //执行下一个接口
            allLocalNotes = TNDbUtils.getNoteListByCatId(TNSettings.getInstance().userId, catId, TNSettings.getInstance().sort, TNConst.MAX_PAGE_SIZE);
            pUpdataNote(0, false);
        }
    }

    /**
     * (二.11)-2 更新云端的笔记 与main不同
     *
     * @param position 执行的位置
     * @param is13     本类中没有main的2-13接口，设置false
     */
    private void pUpdataNote(int position, boolean is13) {
        MLog.d("sync---2-11-2-pUpdataNote");
        //为2-11-2接口返回，做预处理
        setChildHandler2_11(position);

        if (cloudIds.size() > 0 && position < (cloudIds.size() - 1)) {
            boolean isExit = false;
            long id = cloudIds.get(position).getId();
            int lastUpdate = cloudIds.get(position).getUpdate_at();

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
                pUpdataNote(position, id, false);
            } else {
                //
                //下一个position
                pUpdataNote(position + 1, is13);
            }
        } else {
            //下一个接口
            catNotes = TNDbUtils.getNoteListByCatId(TNSettings.getInstance().userId, catId, TNSettings.getInstance().sort, TNConst.MAX_PAGE_SIZE);
            pGetAllDataByNoteId(0);

        }
    }

    /**
     * (二.11)-2/(二.13) 更新云端的笔记
     * <p>
     * p层
     */
    private void pUpdataNote(int position, long noteId, boolean is13) {
        MLog.d("sync---2-11-3-pUpdataNote");
        presenter.pGetNoteByNoteId(position, noteId, false);
    }


    /**
     * 2-12
     * TNActionType.GetAllDataByNoteId的顺序是：
     * GetNoteById-->SyncNoteAtt的循环下载--->数据库
     *
     * @param catPos 标记catNotes循环的位置
     */
    private void pGetAllDataByNoteId(int catPos) {
        if (catNotes.size() > 0 && catPos < catNotes.size()) {
            if (catNotes.get(catPos).syncState == 1) {
                //2-12调用
                pSynceDataByNoteId(catNotes.get(catPos).noteId, catPos, true);
            } else {
                //下一个循环
                pGetAllDataByNoteId(catPos + 1);
            }
        } else {
            endSyncCats(0);
        }
    }


    //==================================接口结果回调==================================

    @Override
    public void onDefaultFolderSuccess(Object obj) {
        mProgressDialog.hide();
        if (isInFront) {
            configView();
        }
    }

    @Override
    public void onDefaultFolderFailed(String msg, Exception e) {
        mProgressDialog.hide();
        TNUtilsUi.showToast(msg);
    }


    @Override
    public void onFolderDeleteSuccess(Object obj) {
        mProgressDialog.hide();
    }

    @Override
    public void onFolderDeleteFailed(String msg, Exception e) {
        mProgressDialog.hide();
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onTagDeleteSuccess(Object obj, long tagID) {
        TNDb.getInstance().execSQL(TNSQLString.TAG_REAL_DELETE, tagID);
        mProgressDialog.hide();
        if (isInFront) {
            configView();
        }
    }

    @Override
    public void onTagDeleteFailed(String msg, Exception e) {
        mProgressDialog.hide();
        TNUtilsUi.showToast(msg);
    }

    //
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
        mProgressDialog.hide();
        MLog.d(msg);
    }

    //==================================GetDataByNoteId接口返回,与2-12共用，isCats用于区分=======================================

    //GetDataByNoteId
    @Override
    public void onGetDataByNoteIdSuccess(Object obj, long noteId, int catPos, boolean isCats) {

        //更新数据库
        updateNote((GetNoteByNoteIdBean) obj);
        //GetDataByNoteId的下一个接口
        TNNote note = TNDbUtils.getNoteByNoteId(noteId);
        Vector<TNNoteAtt> atts = note.atts;
        //执行该接口的循环调用
        if (atts.size() > 0) {
            pSynceNoteAttr(0, atts, noteId, catPos, isCats);
        } else {
            //下一个catPos/下一个接口
            syncGetDataByNoteIdSQL(noteId, catPos, isCats);

        }
    }

    @Override
    public void onGetDataByNoteIdFailed(String msg, Exception e) {
        MLog.d(msg);
    }

    @Override
    public void onSyncNoteAttrSuccess(Object obj, int position, Vector<TNNoteAtt> atts, long noteId, int catPos, boolean isCats) {
        //执行下一个循环
        pSynceNoteAttr(position + 1, atts, noteId, catPos, isCats);
    }

    @Override
    public void onSyncNoteAttrFailed(String msg, Exception e) {
        MLog.d(msg);
        TNUtilsUi.showToast(msg);
    }

    //==================================SyncCats 接口返回=======================================

    //2-5
    @Override
    public void onSyncNewNotePicSuccess(Object obj, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        MLog.d("sync----2-5-->Success");

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
        MLog.e("sync----2-5-->Failed");
    }

    //2-6
    @Override
    public void onSyncNewNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {
        MLog.d("sync----2-6-->Success");
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
        MLog.e("sync----2-6-->Failed");
    }

    //2-7-1
    @Override
    public void onSyncRecoverySuccess(Object obj, long noteId, int position) {
        MLog.d("sync----2-7-1-->Success");
        //更新数据库
        recoveryNoteSQL(noteId);

        //执行循环的下一个position+1数据/下一个接口
        recoveryNote(position + 1);
    }

    @Override
    public void onSyncRecoveryFailed(String msg, Exception e) {

        MLog.e("sync----2-7-1-->Failed");
        MLog.e(msg);
    }

    //2-7-2
    @Override
    public void onSyncRecoveryNotePicSuccess(Object obj, int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt) {
        MLog.d("sync----2-7-2-->Success");
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
        MLog.e("sync----2-7-2-->Failed");
    }

    //2-7-3
    @Override
    public void onSyncRecoveryNoteAddSuccess(Object obj, int position, int arraySize, boolean isNewDb) {
        MLog.d("sync----2-7-3-->Success");
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
        MLog.e("sync----2-7-3-->Failed");
    }

    //2-8
    @Override
    public void onSyncDeleteNoteSuccess(Object obj, long noteId, int position) {
        MLog.d("sync----2-8-->Success");
        //更新数据
        updataDeleteNoteSQL(noteId);

        //执行下一个
        pDelete(position + 1);
    }

    @Override
    public void onSyncDeleteNoteFailed(String msg, Exception e) {

        MLog.e("sync----2-8-->Failed");
        MLog.e(msg);
    }

    //2-9-1
    @Override
    public void onSyncpDeleteRealNotes1Success(Object obj, long noteId, int position) {
        MLog.d("sync----2-9-->Success");
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
        MLog.e("sync----2-9-1-->Failed");
    }

    //2-9-2
    @Override
    public void onSyncDeleteRealNotes2Success(Object obj, long noteId, int position) {
        MLog.d("sync----2-9-2-->Success");
        isRealDelete2 = true;
        //更新数据库
        deleteRealSQL(noteId, position);
    }

    @Override
    public void onSyncDeleteRealNotes2Failed(String msg, Exception e, int position) {
        isRealDelete2 = true;
        MLog.e(msg);
        MLog.e("sync----2-9-2-->Failed");
    }

    //2-10 与main不同
    @Override
    public void onpGetFolderNoteIdsSuccess(Object obj) {
        MLog.d("sync----2-10-->Success");
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
    public void onpGetFolderNoteIdsFailed(String msg, Exception e) {
        MLog.e("sync----2-10-->Failed");
        MLog.e(msg);
    }

    //2-10-1
    @Override
    public void onSyncEditNotePicSuccess(Object obj, int cloudsPos, int attsPos, TNNote tnNote) {
        MLog.d("sync----2-10-1-->Success");
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
        MLog.e("sync----2-10-1-->Failed");
    }


    //2-11-1
    @Override
    public void onSyncEditNoteSuccess(Object obj, int cloudsPos, TNNote note) {
        MLog.d("sync----2-11-1--->Success");
        //更新下一个cloudsPos位置的数据
        updataEditNotes(cloudsPos, note);
    }

    @Override
    public void onSyncEditNoteAddFailed(String msg, Exception e) {
        MLog.e("sync----2-11-1-->Failed");
        MLog.e(msg);
    }

    //2-11-2
    @Override
    public void onSyncpGetNoteByNoteIdSuccess(Object obj, int position, boolean is13) {
        MLog.d("sync----2-11-2-->Success");
        handleNote((GetNoteByNoteIdBean) obj, position, false);

    }

    @Override
    public void onSyncpGetNoteByNoteIdFailed(String msg, Exception e) {
        MLog.e("sync----2-11-2-->Failed");
        MLog.e(msg);
    }

}
