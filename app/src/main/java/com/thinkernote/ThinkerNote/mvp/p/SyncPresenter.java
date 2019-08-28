package com.thinkernote.ThinkerNote.mvp.p;

import android.content.Context;

import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNTag;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.mvp.listener.m.IFolderModuleListener;
import com.thinkernote.ThinkerNote.mvp.listener.m.INoteModuleListener;
import com.thinkernote.ThinkerNote.mvp.listener.m.ITagModuleListener;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnSyncListener;
import com.thinkernote.ThinkerNote.mvp.m.FolderModule;
import com.thinkernote.ThinkerNote.mvp.m.NoteModule;
import com.thinkernote.ThinkerNote.mvp.m.TagModule;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.bean.main.NoteListBean;
import com.thinkernote.ThinkerNote.mvp.http.MyRxManager;

import java.util.List;
import java.util.Vector;

/**
 * 同步块(包好同步取消操作)
 */
public class SyncPresenter implements IFolderModuleListener, ITagModuleListener, INoteModuleListener {
    private static final String TAG = "SyncPresenter";
    private Context context;
    private OnSyncListener onView;
    private TNSettings settings;

    /**
     * 流程控制：
     * <p>
     * type = HOME   全执行
     * type = TRASH  不执行（4）（5）（6）（7）
     * type = FOLDER 不执行（7）
     * type = NOTE   不执行（4）（5）（6）(7)
     * type = TAG    不执行（4）（5）（6）
     * type = EDIT   从（8）开始,不执行（15）（16）
     */
    private String type;//判断是那个模块的同步，用于大块同步的某一步骤省略

    //p层调用M层方法
    private FolderModule folderModule;
    private TagModule tagsModule;
    private NoteModule noteModule;

    //具体操作所需参数
    private String[] arrayFolderName;//第一次登录，要同步的数据，（1-1）
    private String[] arrayTagName;//第一次登录，要同步的数据，（1-2）
    private List<AllNotesIdsBean.NoteIdItemBean> all_note_ids;//获取所有笔记的id（12）
    private List<AllNotesIdsBean.NoteIdItemBean> trash_note_ids;//获取所有回收站笔记id（15）

    public SyncPresenter(Context context, OnSyncListener logListener) {
        this.context = context;
        this.onView = logListener;
        settings = TNSettings.getInstance();
        folderModule = new FolderModule(context);
        tagsModule = new TagModule(context);
        noteModule = new NoteModule(context);
    }

    /**
     * 是否从（1）开始同步，否则从（8）开始
     */
    private boolean isAllSync = true; //有重叠的同步块，在notelist界面中，isAllSync =true,同步所有，isAllSync =false,从本地新笔记开始同步，

    public boolean isAllSync() {
        return isAllSync;
    }

    //============================p层============================


    /**
     * 同步按钮 同步块顺序执行
     * 同步执行后，（1）--（16）会自动执行，除非跑异常终止
     */
    public void synchronizeData(String type) {
        MyRxManager.getInstance().setSyncing();
        this.type = type;
        if (type.equals("EDIT")) {
            isAllSync = false;
            // 从（8）开始,不执行（15）（16）
            updateLocalNotes();
        } else {
            isAllSync = true;
            MLog.d(TAG, "主界面同步开始");
            if (settings.firstLaunch) {//第一次安装登陆情况
                //创建默认的文件夹名称
                arrayFolderName = new String[]{TNConst.FOLDER_DEFAULT, TNConst.FOLDER_MEMO, TNConst.GROUP_FUN, TNConst.GROUP_WORK, TNConst.GROUP_LIFE};
                //创建默认的标签
                arrayTagName = new String[]{TNConst.TAG_IMPORTANT, TNConst.TAG_TODO, TNConst.TAG_GOODSOFT};
                createFolderByFirstLaunch();
            } else {
                //(3)同步老数据
                getOldNote();
            }
        }
    }

    public void synchronizeEdit() {
        synchronizeData("EDIT");
    }


    //（1）默认文件夹
    private void createFolderByFirstLaunch() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--createFolderByFirstLaunch");
            backSuccess("同步取消");
            return;
        }
        MLog.d(TAG, "同步--默认文件夹");
        folderModule.createFolderByFirstLaunch(arrayFolderName, -1L, this);
    }

    //(2) 默认Tag
    private void createTagByFirstLaunch() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--createTagByFirstLaunch");
            backSuccess("同步取消");
            return;
        }
        MLog.d(TAG, "同步--默认Tag");
        tagsModule.createTagByFirstLaunch(arrayTagName, this);
    }

    /**
     * (3)同步老数据
     */
    private void getOldNote() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--getOldNote");
            backSuccess("同步取消");
            return;
        }
        MLog.d(TAG, "同步--老数据");
        Vector<TNNote> oldNotes = TNDbUtils.getOldDbNotesByUserId(TNSettings.getInstance().userId);
        if (!settings.syncOldDb && oldNotes != null && oldNotes.size() > 0) {
            //
            noteModule.updateOldNote(oldNotes, false, this);
        } else {
            //下个执行接口
            getProFiles();
        }
    }

    /**
     * (4)获取所有数据
     */
    private void getProFiles() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--getProFiles");
            backSuccess("同步取消");
            return;
        }
        Vector<TNCat> cats = TNDbUtils.getAllCatList(settings.userId);
        if (cats != null && cats.size() > 0 && (type.equals("FLODER") || type.equals("NOTE") || type.equals("TAG"))) {//不执行的type
            getTags();
        } else {
            MLog.d(TAG, "同步--预先获取用户所有数据");
            folderModule.getProfiles(this);
        }
    }

    /**
     * （5）获取所有文件夹数据
     * 条件：主界面||文件夹列表||文件夹数据为空
     */
    private void getAllFolder() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--getAllFolder");
            backSuccess("同步取消");
            return;
        }
        MLog.d(TAG, "同步--所有文件夹");
        folderModule.getAllFolder(this);
    }


    /**
     * （6）更新默认子文件夹
     */
    private void updateDefaultFolder() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--updateDefaultFolder");
            backSuccess("同步取消");
            return;
        }
        MLog.d(TAG, "同步----更新默认子文件夹");
        Vector<TNCat> cats = TNDbUtils.getAllCatList(settings.userId);
        if (settings.firstLaunch) {
            if (cats != null && cats.size() > 0) {
                String[] works = new String[]{TNConst.FOLDER_WORK_NOTE, TNConst.FOLDER_WORK_UNFINISHED, TNConst.FOLDER_WORK_FINISHED};
                String[] life = new String[]{TNConst.FOLDER_LIFE_DIARY, TNConst.FOLDER_LIFE_KNOWLEDGE, TNConst.FOLDER_LIFE_PHOTO};
                String[] funs = new String[]{TNConst.FOLDER_FUN_TRAVEL, TNConst.FOLDER_FUN_MOVIE, TNConst.FOLDER_FUN_GAME};
                folderModule.createFolderByIdByFirstLaunch(cats, works, life, funs, this);
            } else {
                //（7）
                getTags();
            }
        } else {
            //（7）
            getTags();
        }
    }

    /**
     * (7)更新 标签
     */
    private void getTags() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--getTags");
            backSuccess("同步取消");
            return;
        }
        MLog.d(TAG, "同步--标签");
        Vector<TNTag> tags = TNDbUtils.getTagList(settings.userId);
        if (tags != null && tags.size() > 0 && (type.equals("NOTE") || type.equals("TRASH") || type.equals("FOLDER"))) {//不执行
            //(8)执行下一个接口
            updateLocalNotes();
        } else {
            tagsModule.getAllTags(this);
        }
    }

    /**
     * （8）笔记更新：上传本地新增笔记
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     */
    private void updateLocalNotes() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--updateLocalNotes");
            backSuccess("同步取消");
            return;
        }
        MLog.d(TAG, "同步--上传本地新增笔记");
        Vector<TNNote> localNewNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 3);
        if (localNewNotes != null && localNewNotes.size() > 0) {
            noteModule.updateLocalNewNotes(localNewNotes, this, true);
        } else {
            //(9)
            updateRecoveryNotes();
        }
    }

    /**
     * （9）笔记更新：还原回收站笔记
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     */
    private void updateRecoveryNotes() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--updateRecoveryNotes");
            backSuccess("同步取消");
            return;
        }
        MLog.d(TAG, "同步--还原回收站笔记");
        Vector<TNNote> recoveryNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 7);
        if (recoveryNotes != null && recoveryNotes.size() > 0) {
            noteModule.updateRecoveryNotes(recoveryNotes, this, true);
        } else {
            //（10）
            deleteNotes();
        }
    }

    /**
     * （10）笔记更新：删除到回收站/完全删除
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     */
    private void deleteNotes() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--deleteNotes");
            backSuccess("同步取消");
            return;
        }
        MLog.d(TAG, "同步--删除到回收站");
        Vector<TNNote> mDeleteNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 6);
        if (mDeleteNotes != null && mDeleteNotes.size() > 0) {
            noteModule.deleteNotes(mDeleteNotes, this, true);
        } else {
            //（11）
            clearNotes();
        }
    }

    /**
     * （11）笔记更新：彻底删除
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     */
    private void clearNotes() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--clearNotes");
            backSuccess("同步取消");
            return;
        }
        MLog.d(TAG, "同步--彻底删除");
        Vector<TNNote> mClaerNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 5);
        if (mClaerNotes != null && mClaerNotes.size() > 0) {
            noteModule.clearNotes(mClaerNotes, this,true);
        } else {
            //（12）
            getAllNotsId();
        }
    }

    /**
     * （12）获取所有笔记id,用于同步相关
     */
    private void getAllNotsId() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--getAllNotsId");
            backSuccess("同步取消");
            return;
        }
        MLog.d(TAG, "同步--获取所有笔记id");
        noteModule.getAllNotesId(this);
    }


    /**
     * (13)云端的编辑笔记 同步（12的子步骤）
     * <p>
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     */
    private void updateEditNote() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--updateEditNote");
            backSuccess("同步取消");
            return;
        }
        MLog.d(TAG, "同步--编辑笔记");
        Vector<TNNote> editNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 4);
        if (editNotes != null && editNotes.size() > 0 && all_note_ids != null && all_note_ids.size() > 0) {
            noteModule.updateEditNotes(all_note_ids, editNotes, this,true);
        } else {
            //(14)
            updateCloudNote();
        }
    }

    /**
     * （14）云端新笔记同步到本地（12的子步骤）
     */
    private void updateCloudNote() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--updateCloudNote");
            backSuccess("同步取消");
            return;
        }
        MLog.d(TAG, "同步--云端新笔记同步到本地");
        final Vector<TNNote> localAllNotes = TNDbUtils.getAllNoteList(TNSettings.getInstance().userId);
        if (all_note_ids != null && all_note_ids.size() > 0) {
            noteModule.getCloudNote(all_note_ids, localAllNotes, this);
        } else {
            //（15）
            getTrashNotesId();
        }

    }

    /**
     * （15）获取所有回收站笔记
     */
    private void getTrashNotesId() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--getTrashNotesId");
            backSuccess("同步取消");
            return;
        }
        if (type.equals("EDIT")) {
            //结束
            MyRxManager.getInstance().syncOver();
            onView.onSyncEditSuccess();
        } else {
            MLog.d(TAG, "同步--获取所有回收站笔记id");
            noteModule.getTrashNotesId(this);
        }

    }

    /**
     * （16）回收站笔记根据id的处理
     */
    private void updateTrashNotes() {
        if (!MyRxManager.getInstance().isSyncing) {
            MLog.d(TAG, "终止同步--updateTrashNotes");
            backSuccess("同步取消");
            return;
        }
        MLog.d(TAG, "同步--回收站笔记根据id的处理");
        Vector<TNNote> trashNotes = TNDbUtils.getNoteListByTrash(settings.userId, TNConst.CREATETIME);
        if (trashNotes != null && trashNotes.size() > 0 && trash_note_ids != null && trash_note_ids.size() > 0) {
            noteModule.upateTrashNotes(trash_note_ids, trashNotes, this);
        } else {
            //返回成功
            backSuccess("数据同步成功");
        }

    }

    //============================================================================
    //==========================接口结果回调,再传递给UI==============================
    //============================================================================

    //===========同步块的回调,如下会按编号都调用=========

    //===========================folder相关回调=================================

    /**
     * （1）第一次登陆的同步
     */
    @Override
    public void onAddDefaultFolderSuccess() {
        //（2）处理tag标签
        createTagByFirstLaunch();
    }

    //(6)更新默认子文件夹
    @Override
    public void onAddDefaultFolderIdSuccess() {
        //(7)
        getTags();
    }

    //（1）（7）失败回调
    @Override
    public void onAddFolderFailed(Exception e, String msg) {
        backFailed(e, msg);
    }


    //（4）预先获取所有数据（主线程）
    @Override
    public void onProfileSuccess(ProfileBean profileBean) {
        //(5)获取所有文件夹数据
        getAllFolder();
    }


    //（5）获取所有文件夹数据
    @Override
    public void onGetFolderSuccess() {
        //（6）更新默认子文件夹
        updateDefaultFolder();
    }


    @Override
    public void onGetFolderFailed(Exception e, String msg) {
        backFailed(e, msg);
    }


    //===============================tag相关回调=============================

    //（2）
    @Override
    public void onAddDefaultTagSuccess() {
        //第一次登陆，创建 文件夹和标签，完成
        settings.firstLaunch = false;
        settings.savePref(false);
        //(3) 同步老数据
        getOldNote();
    }


    @Override
    public void onAddTagFailed(Exception e, String msg) {
        backFailed(e, msg);
    }

    @Override
    public void onTagRenameSuccess() {

    }

    @Override
    public void onTagRenameFailed(Exception e, String msg) {

    }

    // （7）获取标签
    @Override
    public void onGetTagSuccess() {
        //（8）
        updateLocalNotes();
    }

    @Override
    public void onGetTagFailed(Exception e, String msg) {
        backFailed(e, msg);
    }


    //===============================note相关回调=============================
    // (3)同步老数据
    @Override
    public void onUpdateOldNoteSuccess() {
        //(4)获取所有文件夹数据
        getProFiles();
    }

    @Override
    public void onUpdateOldNoteFailed(Exception e, String msg) {
        backFailed(e, msg);
    }

    //（8）同步本地数据 3
    @Override
    public void onUpdateLocalNoteSuccess() {
        //(9)
        updateRecoveryNotes();
    }

    @Override
    public void onUpdateLocalNoteFailed(Exception e, String msg) {
        backFailed(e, msg);
    }

    //（9）还原回收站笔记结果
    @Override
    public void onUpdateRecoveryNoteSuccess() {
        //10
        MLog.d(TAG, "还原回收站笔记-成功");
        deleteNotes();
    }

    @Override
    public void onUpdateRecoveryNoteFailed(Exception e, String msg) {
        backFailed(e, msg);
    }

    //(10)删除笔记
    @Override
    public void onDeleteNoteSuccess() {
        //(11) 彻底删除的笔记
        clearNotes();
    }

    @Override
    public void onDeleteNoteFailed(Exception e, String msg) {
        backFailed(e, msg);
    }


    //（11）彻底删除的笔记
    @Override
    public void onClearNoteSuccess() {
        //（12）
        getAllNotsId();
    }

    @Override
    public void onClearNoteFailed(Exception e, String msg) {
        backFailed(e, msg);
    }

    // （12）获取所有笔记ID，用于同步
    @Override
    public void onGetAllNoteIdSuccess() {
        //(13) 更新编辑的笔记
        updateEditNote();
    }

    //(12)
    @Override
    public void onGetAllNoteIdNext(AllNotesIdsBean bean) {
        //(可为空)
        all_note_ids = bean.getNote_ids();
    }

    @Override
    public void onGetAllNoteIdFailed(Exception e, String msg) {
        backFailed(e, msg);
    }


    //(13)同步编辑笔记
    @Override
    public void onUpdateEditNoteSuccess() {
        //（14）加载云端笔记
        updateCloudNote();
    }

    @Override
    public void onUpdateEditNoteFailed(Exception e, String msg) {
        backFailed(e, msg);
    }

    //(14)加载云端笔记
    @Override
    public void onCloudNoteSuccess() {
        //(15) 同步回收站笔记
        getTrashNotesId();

    }

    @Override
    public void onCloudNoteFailed(Exception e, String msg) {
        backFailed(e, msg);
    }


    //(15)获取回收站笔记id
    @Override
    public void onGetTrashNoteIdSuccess() {
        //（16）对回收站笔记的处理
        updateTrashNotes();
    }

    @Override
    public void onGetTrashNoteIdNext(AllNotesIdsBean bean) {
        //拿到中间数据(可为空)
        trash_note_ids = bean.getNote_ids();
    }

    @Override
    public void onGetTrashNoteIdFailed(Exception e, String msg) {
        backFailed(e, msg);
    }

    //（16）同步的最后一个接口
    @Override
    public void onGetTrashNoteSuccess() {
        backSuccess("数据同步成功");
    }

    @Override
    public void onGetTrashNoteFailed(Exception e, String msg) {
        backFailed(e, msg);
    }

    private void backFailed(Exception e, String msg) {
        MyRxManager.getInstance().syncOver();
        onView.onSyncFailed(e, msg);
    }

    private void backSuccess(String msg) {
        MyRxManager.getInstance().syncOver();
        onView.onSyncSuccess(msg);
    }


    //=====================同步块不走如下回调============================

    @Override
    public void onProfileFailed(String msg, Exception e) {

    }

    @Override
    public void onDeleteFolderSuccess() {

    }

    @Override
    public void onDeleteFolderFailed(String msg, Exception e) {

    }

    @Override
    public void onDefaultFolderSuccess() {

    }

    @Override
    public void onDefaultFolderFailed(String msg, Exception e) {

    }

    @Override
    public void onRenameFolderSuccess() {

    }

    @Override
    public void onRenameFolderFailed(Exception e, String msg) {

    }

    @Override
    public void onAddFolderSuccess() {

    }

    // main 不用
    @Override
    public void onAddTagSuccess() {

    }


    //tagFragment单独使用，此处不使用
    @Override
    public void onGetTagListSuccess() {

    }

    @Override
    public void onGetTagListFailed(Exception e, String msg) {

    }

    @Override
    public void onDeleteTagSuccess() {

    }

    @Override
    public void onDeleteTagFailed(Exception e, String msg) {

    }

    @Override
    public void onNoteListByIdSuccess() {

    }

    @Override
    public void onNoteListByIdNext(NoteListBean bean) {

    }

    @Override
    public void onNoteListByIdFailed(Exception e, String msg) {

    }

    @Override
    public void onDownloadNoteSuccess() {

    }

    @Override
    public void onDownloadNoteFailed(Exception e, String msg) {

    }

}
