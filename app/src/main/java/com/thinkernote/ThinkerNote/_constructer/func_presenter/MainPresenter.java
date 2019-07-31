package com.thinkernote.ThinkerNote._constructer.func_presenter;

import android.content.Context;

import com.thinkernote.ThinkerNote.DBHelper.UserDbHelper;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Data.TNTag;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.func_module.FolderModule;
import com.thinkernote.ThinkerNote._constructer.func_module.NoteModule;
import com.thinkernote.ThinkerNote._constructer.func_module.TagsModule;
import com.thinkernote.ThinkerNote._constructer.func_module.UpgradeModule;
import com.thinkernote.ThinkerNote._interface.listener_m.IFolderModuleListener;
import com.thinkernote.ThinkerNote._interface.listener_m.IMainModuleListener;
import com.thinkernote.ThinkerNote._interface.listener_m.INoteModuleListener;
import com.thinkernote.ThinkerNote._interface.listener_m.ITagModuleListener;
import com.thinkernote.ThinkerNote._interface.listener_v.OnMainViewListener;
import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.main.AllFolderBean;
import com.thinkernote.ThinkerNote.bean.main.AllNotesIdsBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;
import com.thinkernote.ThinkerNote.http.fileprogress.FileProgressListener;

import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Vector;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 注册 p层 具体实现
 * 调用顺讯按编号执行
 */
public class MainPresenter implements IFolderModuleListener, IMainModuleListener, ITagModuleListener, INoteModuleListener {
    private static final String TAG = "MainPresenter";
    private Context context;
    private OnMainViewListener onView;
    private TNSettings settings;
    //p层调用M层方法
    private UpgradeModule upgradeModule;
    private FolderModule folderModule;
    private TagsModule tagsModule;
    private NoteModule noteModule;

    //具体操作所需参数
    private String[] arrayFolderName;//第一次登录，要同步的数据，（1-1）
    private String[] arrayTagName;//第一次登录，要同步的数据，（1-2）
    private List<AllNotesIdsBean.NoteIdItemBean> all_note_ids;//获取所有笔记id（12）
    private List<AllNotesIdsBean.NoteIdItemBean> trash_note_ids;//获取所有笔记id（15）

    public MainPresenter(Context context, OnMainViewListener logListener) {
        this.context = context;
        this.onView = logListener;
        settings = TNSettings.getInstance();
        upgradeModule = new UpgradeModule(context);
        folderModule = new FolderModule(context);
        tagsModule = new TagsModule(context);
        noteModule = new NoteModule(context);
    }
    //============================p层============================


    /**
     * 同步按钮 同步块顺序执行
     * 同步执行后，（1）--（16）会自动执行，除非跑异常终止
     */
    public void synchronizeData() {
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

    //（1）默认文件夹
    private void createFolderByFirstLaunch() {
        MLog.d(TAG, "同步--默认文件夹");
        folderModule.createFolderByFirstLaunch(arrayFolderName, -1L, this);
    }

    //(2) 默认Tag
    private void createTagByFirstLaunch() {
        MLog.d(TAG, "同步--默认Tag");
        tagsModule.createTagByFirstLaunch(arrayTagName, this);
    }

    /**
     * (3)同步老数据
     */
    private void getOldNote() {
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
        MLog.d(TAG, "同步--预先获取用户所有数据");
        folderModule.getProfiles(this);
    }

    /**
     * （5）获取所有文件夹数据
     * 条件：主界面||文件夹列表||文件夹数据为空
     */
    private void getAllFolder() {
        MLog.d(TAG, "同步--获取所有文件夹数据");
        folderModule.getAllFolder(this);
    }


    /**
     * （6）更新默认子文件夹
     */
    private void updateDefaultFolder() {
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
        Vector<TNTag> tags = TNDbUtils.getTagList(settings.userId);
        if (tags.size() == 0) {
            MLog.d(TAG, "同步--标签");
            tagsModule.getAllTags(tags, this);
        } else {
            //(8)执行下一个接口
            updateLocalNotes();
        }
    }

    /**
     * （8）笔记更新：上传本地新增笔记
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     */
    private void updateLocalNotes() {
        MLog.d(TAG, "同步--上传本地新增笔记");
        Vector<TNNote> localNewNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 3);
        if (localNewNotes != null && localNewNotes.size() > 0) {
            noteModule.updateLocalNewNotes(localNewNotes, this);
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
        MLog.d(TAG, "同步--还原回收站笔记");
        Vector<TNNote> recoveryNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 7);
        if (recoveryNotes != null && recoveryNotes.size() > 0) {
            noteModule.updateRecoveryNotes(recoveryNotes, this);
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
        MLog.d(TAG, "同步--删除到回收站");
        Vector<TNNote> mDeleteNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 6);
        if (mDeleteNotes != null && mDeleteNotes.size() > 0) {
            noteModule.deleteNotes(mDeleteNotes, this);
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
        MLog.d(TAG, "同步--彻底删除");
        Vector<TNNote> mClaerNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 5);
        if (mClaerNotes != null && mClaerNotes.size() > 0) {
            noteModule.clearNotes(mClaerNotes, this);
        } else {
            //（12）
            getAllNotsId();
        }
    }

    /**
     * （12）获取所有笔记id,用于同步相关
     */
    private void getAllNotsId() {
        MLog.d(TAG, "同步--获取所有笔记id");
        noteModule.getAllNotesId(this);
    }


    /**
     * (13)编辑笔记 同步
     * <p>
     * syncState ：1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原
     */
    private void updateEditNote() {
        MLog.d(TAG, "同步--编辑笔记");
        Vector<TNNote> editNotes = TNDbUtils.getNoteListBySyncState(TNSettings.getInstance().userId, 4);
        if (editNotes != null && editNotes.size() > 0 && all_note_ids != null && all_note_ids.size() > 0) {
            noteModule.updateEditNotes(all_note_ids, editNotes, this);
        } else {
            //(14)
            updateCloudNote();
        }
    }

    /**
     * （14）云端笔记同步到本地
     */
    private void updateCloudNote() {
        MLog.d(TAG, "同步--云端笔记同步到本地");
        noteModule.getCloudNote(all_note_ids, this);
    }

    /**
     * （15）获取所有回收站笔记
     */
    private void getTrashNotesId() {
        MLog.d(TAG, "同步--获取所有回收站笔记id");
        noteModule.getTrashNotesId(this);
    }

    /**
     * （16）回收站笔记根据id的处理
     */
    private void updateTrashNotes() {
        MLog.d(TAG, "同步--回收站笔记根据id的处理");
        Vector<TNNote> trashNotes = TNDbUtils.getNoteListByTrash(settings.userId, TNConst.CREATETIME);
        if (trashNotes != null && trashNotes.size() > 0 && trash_note_ids != null && trash_note_ids.size() > 0) {
            noteModule.upateTrashNotes(trash_note_ids, trashNotes, this);
        }

    }

    //===========================p层，非同步块的数据=================================
    //更新检查
    public void pUpgrade(String home) {
        upgradeModule.mUpgrade(this);
    }

    //下载
    public void pDownload(String url, FileProgressListener progressListener) {
        upgradeModule.mDownload(this, url, progressListener);
    }

    //============================================================================
    //==========================接口结果回调,再传递给UI==============================
    //============================================================================

    //===========================更新相关=================================
    //===========非同步块的回调=========

    @Override
    public void onUpgradeSuccess(Object obj) {
        onView.onUpgradeSuccess(obj);
    }

    @Override
    public void onUpgradeFailed(String msg, Exception e) {
        onView.onUpgradeFailed(msg, e);
    }

    //下载
    @Override
    public void onDownloadSuccess(File file) {
        onView.onDownloadSuccess(file);
    }

    @Override
    public void onDownloadFailed(String msg, Exception e) {
        onView.onDownloadFailed(msg, e);
    }

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

    //
    @Override
    public void onAddDefaultFolderIdSuccess() {

    }

    @Override
    public void onAddFolderSuccess() {

    }

    @Override
    public void onAddFolderFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
    }


    //（4）预先获取所有数据（主线程）
    @Override
    public void onProfileSuccess(ProfileBean profileBean) {
        //(5)获取所有文件夹数据
        getAllFolder();
    }

    @Override
    public void onProfileFailed(String msg, Exception e) {

    }

    //（5）获取所有文件夹数据
    @Override
    public void onGetFolderSuccess() {
        //（6）更新默认子文件夹
        updateDefaultFolder();
    }


    @Override
    public void onGetFolderFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
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

    // main 不用
    @Override
    public void onAddTagSuccess() {

    }

    @Override
    public void onAddTagFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
    }

    // （7）获取标签
    @Override
    public void onGetTagSuccess() {
        //（8）
        updateRecoveryNotes();
    }

    @Override
    public void onGetTagFailed(Exception e, String msg) {

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
        onView.onSyncFailed(e, msg);
    }

    //（8）同步本地数据 3
    @Override
    public void onUpdateLocalNoteSuccess() {
        //(9)
        updateRecoveryNotes();
    }

    @Override
    public void onUpdateLocalNoteFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
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
        onView.onSyncFailed(e, msg);
    }

    //(10)删除笔记
    @Override
    public void onDeleteNoteSuccess() {
        //(11) 彻底删除的笔记
        clearNotes();
    }

    @Override
    public void onDeleteNoteFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
    }


    //（11）彻底删除的笔记
    @Override
    public void onClearNoteSuccess() {
        //（12）
        getAllNotsId();
    }

    @Override
    public void onClearNoteFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
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
        onView.onSyncFailed(e, msg);
    }


    //(13)同步编辑笔记
    @Override
    public void onUpdateEditNoteSuccess() {
        //（14）加载云端笔记
        updateCloudNote();
    }

    @Override
    public void onUpdateEditNoteFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
    }

    //(14)加载云端笔记
    @Override
    public void onCloudNoteSuccess() {
        //(15) 同步回收站笔记
        getTrashNotesId();

    }

    @Override
    public void onCloudNoteFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
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
        onView.onSyncFailed(e, msg);
    }

    //（16）同步的最后一个接口
    @Override
    public void onGetTrashNoteSuccess() {
        onView.onSyncSuccess("数据同步成功");
    }

    @Override
    public void onGetTrashNoteFailed(Exception e, String msg) {
        onView.onSyncFailed(e, msg);
    }


}
