package com.thinkernote.ThinkerNote.Activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.Toast;

import com.thinkernote.ThinkerNote.Activity.fragment.FolderFragment;
import com.thinkernote.ThinkerNote.Activity.fragment.NoteFragment;
import com.thinkernote.ThinkerNote.Activity.fragment.TagFragment;
import com.thinkernote.ThinkerNote.bean.localdata.TNCat;
import com.thinkernote.ThinkerNote.bean.localdata.TNNote;
import com.thinkernote.ThinkerNote.bean.localdata.TNTag;
import com.thinkernote.ThinkerNote.db.Database.TNDb;
import com.thinkernote.ThinkerNote.db.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.db.Database.TNSQLString;
import com.thinkernote.ThinkerNote.base.TNConst;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.TNUtils;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.utils.MLog;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.base.TNChildViewBase;
import com.thinkernote.ThinkerNote.views.dialog.CommonDialog;
import com.thinkernote.ThinkerNote.mvp.MyRxManager;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnPagerListener;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnSyncListener;
import com.thinkernote.ThinkerNote.mvp.p.MainFragPresenter;
import com.thinkernote.ThinkerNote.mvp.p.SyncFolderPresenter;
import com.thinkernote.ThinkerNote.views.HorizontalPager;
import com.thinkernote.ThinkerNote.views.HorizontalPager.OnScreenSwitchListener;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.thinkernote.ThinkerNote.utils.MLog.i;

/**
 * 主页--我的笔记
 * 样式：act+3个frag
 * <p>
 * sjy 0704
 */
public class TNMainFragAct extends TNActBase implements OnScreenSwitchListener, OnClickListener,
        OnPagerListener, OnSyncListener {
    //正常登录的同步常量
    public static final int DIALOG_DELETE = 106;//
    public static final int CAT_DELETE = 107;//
    public static final int CC_DELETE = 108;//清空回收站
    public static final int SYNC_DATA_BY_NOTEID = 110;//
    //
    private HorizontalPager mPager;
    private Vector<TNChildViewBase> mChildPages;
    private TNChildViewBase mCurrChild;
    private ProgressDialog mProgressDialog;
    private TNSettings mSettings = TNSettings.getInstance();
    private TNNote mCurrNote;
    private TNCat mCurrCat;
    private TNTag mCurTag;
    private CommonDialog dialog;//GetDataByNoteId的弹窗；
    //p
    MainFragPresenter presenter;
    SyncFolderPresenter folderPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_layout);
        //p
        presenter = new MainFragPresenter(this, this);
        folderPresenter = new SyncFolderPresenter(this, this);

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
        NoteFragment notesView = new NoteFragment(this);
        mChildPages.add(notesView);
        //frag--pager2
        FolderFragment catsView = new FolderFragment(this);
        mChildPages.add(catsView);
        //frag--pager3
        TagFragment tagsView = new TagFragment(this);
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
//                TNNote note = TNNote.newNote();
//                Bundle b = new Bundle();
//                b.putLong("NoteForEdit", note.noteLocalId);
//                b.putSerializable("NOTE", note);//设置后，无法获取
//                startActivity(TNNoteEditAct.class, b);

                //sjy 编辑界面自己创建，
                startActivity(TNNoteEditAct.class);
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
                ((NoteFragment) mCurrChild).isNewSortord = true;
                configView();
                break;
            }
            case R.id.table_cats_newfolder: {//新建 文件夹
                ((FolderFragment) mCurrChild).newFolder();
                break;
            }
            case R.id.table_cats_newnote://新建 标签
                ((FolderFragment) mCurrChild).newNote();
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
                    Toast.makeText(this,
                            R.string.alert_NoteView_NotCompleted,
                            Toast.LENGTH_SHORT).show();
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
                MLog.d("TNMainFragAct--笔记相关--完全同步");
                mMenuBuilder.destroy();
                if (mCurrNote == null || mCurrNote.noteId == -1)
                    break;
                showSyncNoteDialog(mCurrNote.noteId);

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
                MLog.d("TNMainFragAct--文件夹--完全同步--" + mCurrCat.catName + "--" + mCurrCat.catId);
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

    /**
     * 全部笔记/文件夹/标签三个表的切换
     *
     * @param screen The new screen index.
     */
    private int currentPage = 0;

    @Override
    public void onScreenSwitched(int screen) {
        currentPage = screen;
        mCurrChild = mChildPages.get(screen);
        changeViewForScreen(screen);
    }

    // ---------------------------------------弹窗----------------------------------------

    /**
     * clearrecycler 弹窗
     */
    private void clearrecyclerDialog() {
        dialog = new CommonDialog(this, R.string.alert_NoteList_ClearRecycle,
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        if (!MyRxManager.getInstance().isSyncing()) {
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

                    @Override
                    public void cancelBack() {
                        mProgressDialog.hide();
                    }

                });
        dialog.show();
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
                            TNUtilsUi.showNotification(TNMainFragAct.this, R.string.alert_NoteView_Synchronizing, false);
                            //具体执行
                            pCatDelete(cat);
                        }
                    }

                    @Override
                    public void cancelBack() {
                        mProgressDialog.hide();
                    }

                });
        dialog.show();
    }

    /**
     * 删除 弹窗
     *
     * @param noteLocalId
     */
    private void showDeleteDialog(final long noteLocalId) {
        int msg = R.string.alert_NoteView_DeleteNoteMsg;
        if (TNSettings.getInstance().isInProject()) {
            msg = R.string.alert_NoteView_DeleteNoteMsg_InGroup;
        }
        dialog = new CommonDialog(this,
                msg,
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        if (!MyRxManager.getInstance().isSyncing()) {
                            TNUtilsUi.showNotification(TNMainFragAct.this, R.string.alert_NoteView_Synchronizing, false);
                            //具体执行
                            pDialogDelete(noteLocalId);

                        }
                    }

                    @Override
                    public void cancelBack() {
                        mProgressDialog.hide();
                    }

                });
        dialog.show();
    }

    /**
     * 内容完全同步
     */
    private void showSyncNoteDialog(final long noteId) {
        dialog = new CommonDialog(this, R.string.alert_MainCats_SynchronizeNote,
                "完全同步",
                "取消",
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        if (!MyRxManager.getInstance().isSyncing()) {
                            TNUtilsUi.showNotification(TNMainFragAct.this, R.string.alert_NoteView_Synchronizing, false);
                            //监听
                            MLog.d("同步GetDataByNoteId");
                            getDetailByNoteId(noteId);
                        }
                    }

                    @Override
                    public void cancelBack() {
                        mProgressDialog.hide();
                    }

                });
        dialog.show();
    }

    /**
     * 完全同步syncCats
     */
    private void showSyncCatDialog(final long folderId) {
        dialog = new CommonDialog(this, R.string.alert_MainCats_SynchronizeNoteAll,
                "完全同步",
                "取消",
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        MLog.d("同步Cats下所有笔记");
                        synceCat(folderId);
                    }

                    @Override
                    public void cancelBack() {
                        mProgressDialog.hide();
                    }

                });
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
        dialog = new CommonDialog(this, R.string.alert_TagInfo_DeleteMsg,
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        mProgressDialog.show();
                        pDeleteTag(tag.tagId);
                    }

                    @Override
                    public void cancelBack() {
                        mProgressDialog.hide();
                    }

                });
        dialog.show();
    }

    private void setDefaultCatDialog(final TNCat cat) {
        dialog = new CommonDialog(this, R.string.alert_CatInfo_SetDefaultMsg,
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        setDefaultFolder(cat.catId);
                        configView();
                    }

                    @Override
                    public void cancelBack() {
                        mProgressDialog.hide();
                    }

                });
        dialog.show();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mCurrChild.pageId == R.id.page_cats) {
                if (((FolderFragment) mCurrChild).onKeyDown()) {
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (TNChildViewBase child : mChildPages) {
//			if (child.pageId == mCurrChild.pageId)
                    child.configView(createStatus);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //关闭所有的资源
        for (int i = 0; i < mChildPages.size(); i++) {
            switch (i) {
                case 0://全部笔记
                    ((NoteFragment) mChildPages.get(i)).noteDestroy();
                    break;
                case 1:
                    ((FolderFragment) mChildPages.get(i)).folderDestory();
                    break;
                case 2:
                    ((TagFragment) mChildPages.get(i)).tagDestory();
                    break;
            }
        }
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }

        //关闭文件夹同步
        if (MyRxManager.getInstance().isFolderSyncing()) {
            folderPresenter.cancelSync();
        }
        //
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
        mProgressDialog.hide();


        if (type == 1) {
            TNUtilsUi.showNotification(this, R.string.alert_SynchronizeCancell, true);
        } else if (type == 0) {
            TNUtilsUi.showNotification(this, R.string.alert_MainCats_Synchronized, true);
            TNUtilsUi.showToast("同步完成");
        } else {
            TNUtilsUi.showNotification(this, R.string.alert_Synchronize_Stoped, true);
            TNUtilsUi.showToast("同步终止");
        }
        configView();
    }

    /**
     * syncCats结束调用
     *
     * @param type 0=正常结束 1= cancel 2= stop
     */
    private void endSyncCats(final int type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.hide();
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }

                if (type == 0) {
                    TNUtilsUi.showNotification(TNMainFragAct.this, R.string.alert_SynchronizeCancell, true);
                    mSettings.originalSyncTime = System.currentTimeMillis();
                    mSettings.savePref(false);
                } else if (type == 1) {
                    TNUtilsUi.showNotification(TNMainFragAct.this, R.string.alert_MainCats_Synchronized, true);
                    TNUtilsUi.showToast("同步完成");
                } else {
                    TNUtilsUi.showNotification(TNMainFragAct.this,
                            R.string.alert_Synchronize_Stoped, true);
                    TNUtilsUi.showToast("同步失败");
                }
                configView();
            }
        });

    }


    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {

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
        }

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


    //--------------------------------p层调用--------------------------------------

    private void setDefaultFolder(long catId) {
        if (TNUtils.isNetWork()) {
            presenter.setDefaultFolder(catId);
        } else {
            TNUtilsUi.showToast(R.string.alert_Net_NotWork);
        }

    }

    private void getDetailByNoteId(long noteId) {
        if (TNUtils.isNetWork()) {
            mProgressDialog.hide();
            presenter.getDetailByNoteId(noteId);
        } else {
            TNUtilsUi.showToast(R.string.alert_Net_NotWork);
            //结束同步按钮动作
            mProgressDialog.hide();
        }

    }

    private void pDeleteTag(long tagId) {
        presenter.deleteTag(tagId);
    }

    private void pCatDelete(TNCat cat) {
        MLog.d("TNMainFragAct删除文件夹");
        presenter.deleteFolder(cat.catId);
    }


    /**
     * @param catId
     */
    private void synceCat(long catId) {
        if (TNUtils.isNetWork()) {
            if (MyRxManager.getInstance().isSyncing()) {
                TNUtilsUi.showToast("主页正在同步，请稍后");
                return;
            }
            mProgressDialog.show();
            folderPresenter.SynchronizeFolder(catId);
        } else {
            //结束同步按钮动作
            endSyncCats(2);
            TNUtilsUi.showToast(R.string.alert_Net_NotWork);
        }


    }


    //==================================接口结果回调==================================

    //同步SyncFolder回调
    @Override
    public void onSyncSuccess(String obj) {
        MyRxManager.getInstance().setFolderSyncing(false);
        endSyncCats(1);
    }

    @Override
    public void onSyncFailed(Exception e, String msg) {
        MyRxManager.getInstance().setFolderSyncing(false);
        endSyncCats(2);
    }

    @Override
    public void onSyncEditSuccess() {
        endSyncCats(1);
    }

    //
    @Override
    public void onDefaultFolderSuccess() {
        mProgressDialog.hide();
        if (isInFront) {
            configView();
        }
    }

    @Override
    public void onDefaultFolderFailed(final String msg, Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.hide();
                TNUtilsUi.showToast(msg);
            }
        });

    }

    // 同步一条笔记详情
    @Override
    public void onDownloadNoteSuccess() {
        endGetAllDataByNoteId(0);
    }

    @Override
    public void onDownloadNoteFailed(String msg, Exception e) {
        endGetAllDataByNoteId(0);
    }

    //
    @Override
    public void onTagDeleteSuccess() {
        mProgressDialog.hide();
        if (isInFront) {
            configView();
        }
    }

    @Override
    public void onTagDeleteFailed(final String msg, Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.hide();
                TNUtilsUi.showToast(msg);
            }
        });
    }

    //删除文件夹
    @Override
    public void onDeleteFolderSuccess() {
        mProgressDialog.hide();
        configView();
    }

    @Override
    public void onDeleteFolderFailed(final String msg, Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.hide();
                TNUtilsUi.showToast(msg);
            }
        });

    }
}
