package com.thinkernote.ThinkerNote._constructer.presenter;

import android.app.Activity;
import android.text.TextUtils;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.module.NoteViewDownloadModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.INoteViewDownloadModule;
import com.thinkernote.ThinkerNote._interface.v.OnNoteViewDownloadListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * 说明：此处bug严重，使用老版本代码，后续修改
 */
public class NoteViewDownloadPresenter2 implements OnNoteViewDownloadListener {
    private static final String TAG = "NoteViewDownloadPresenter";
    private static final long ATT_MAX_DOWNLOAD_SIZE = 50 * 1024;

    private static NoteViewDownloadPresenter2 singleton = null;

    private OnDownloadStartListener startListener;
    private OnDownloadEndListener endListener;

    private Vector<TNNoteAtt> downloadingAtts;
    private Vector<TNNoteAtt> readyDownloadAtts;

    private TNNote mNote;
    private Activity act;
    private INoteViewDownloadModule module;


    public NoteViewDownloadPresenter2(Activity act) {
        this.act = act;
    }

//    private NoteViewDownloadPresenter2() {
//        readyDownloadAtts = new Vector<TNNoteAtt>();
//        downloadingAtts = new Vector<TNNoteAtt>();
//
//        module = new NoteViewDownloadModuleImpl(act, this);
//    }

    //不可用单例
//    public static NoteViewDownloadPresenter2 getInstance() {
//        if (singleton == null) {
//            synchronized (NoteViewDownloadPresenter2.class) {
//                if (singleton == null) {
//                    singleton = new NoteViewDownloadPresenter2();
//                }
//            }
//        }
//        return singleton;
//    }

    public void setNewNote(TNNote note){
        mNote = note;
        this.readyDownloadAtts.clear();
        this.readyDownloadAtts.addAll(note.atts);
    }

//    public NoteViewDownloadPresenter2 init(Activity act, TNNote note) {
//        this.act = act;
//        mNote = note;
//        this.readyDownloadAtts.clear();
//        this.readyDownloadAtts.addAll(note.atts);
//
//        return this;
//    }

    public void updateNote(TNNote note) {
        this.mNote = note;
        this.readyDownloadAtts.clear();
        this.readyDownloadAtts.addAll(note.atts);
    }

    /**
     * 多图下载（自动下载）
     */
    public void start() {
        Vector<TNNoteAtt> tmpList = new Vector<TNNoteAtt>();
        //for循环
        for (TNNoteAtt att : readyDownloadAtts) {
            File file = null;
            if (!TextUtils.isEmpty(att.path)) {
                file = new File(att.path);
            }
            if (att.syncState == 2) {
                continue;
            }
            if (TNUtils.isNetWork() && att.attId != -1) {
                if (!TNActionUtils.isDownloadingAtt(att.attId)) {
                    if (startListener != null) {
                        startListener.onStart(att);
                    }
                    MLog.e("下载图片的att:" + att.toString());
                    listDownload(att, mNote, 0);
                    downloadingAtts.add(att);
                    tmpList.add(att);
                }
            }
        }

        //att下载结束，更新mNote
        readyDownloadAtts.removeAll(tmpList);
        mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);
        MLog.e(TAG, "更新mNote：" + mNote.toString());
        mNote.syncState = mNote.syncState > 2 ? mNote.syncState : 2;
        //数据库操作
        if (mNote.attCounts > 0) {
            for (int i = 0; i < mNote.atts.size(); i++) {
                TNNoteAtt tempAtt = mNote.atts.get(i);

                if (i == 0 && tempAtt.type > 10000 && tempAtt.type < 20000) {
                    TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, tempAtt.path, mNote.noteLocalId);
                }
                if (TextUtils.isEmpty(tempAtt.path) || "null".equals(tempAtt.path)) {
                    mNote.syncState = 1;
                }
            }
        }
        TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, mNote.syncState, mNote.noteLocalId);
    }

    /**
     * 点击下载
     *
     * @param attId
     */
    public void start(long attId) {
        MLog.d(TAG, "start(attId):" + attId);
        if (act == null || act.isFinishing()) {
            return;
        }
        if (TNUtils.checkNetwork(act)) {
            TNNoteAtt att = mNote.getAttDataById(attId);

            if (!TNActionUtils.isDownloadingAtt(att.attId)) {
                //开始下载回调
                if (startListener != null)
                    startListener.onStart(att);
                //下载
                singledownload(att, mNote);
            }
        }

    }


    //===========================接口调用=================================
    //调用接口
    private void singledownload(TNNoteAtt tnNoteAtt, TNNote tnNote) {
        module.singleDownload(tnNoteAtt, tnNote);
    }

    //调用接口
    private void listDownload(TNNoteAtt tnNoteAtt, TNNote tnNote, int position) {
        module.listDownload(tnNoteAtt, tnNote, position);
    }
    //===========================接口返回=================================


    @Override
    public void onListDownloadSuccess(TNNote note, TNNoteAtt att, int position) {
        MLog.e("下载文件成功");
        TNNoteAtt newAtt = att;

        //更改att path 和  syncState
        File file = new File(att.path);
        try {
            TNDb.beginTransaction();
            if (att.type > 10000 && att.type < 20000) {
                TNDb.getInstance().execSQL(TNSQLString.ATT_UPDATE_ATTLOCALID, att.attName, att.type, att.path, att.noteLocalId, file.length(), mNote.syncState > 2 ? mNote.syncState : 2, att.digest, att.attId, att.width, att.height, att.attLocalId);
                TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, att.path, mNote.noteLocalId);
            }
            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }

        downloadingAtts.remove(att);
        MLog.e("下载文件成功--start1");
        start();
        if (endListener != null) {
            if (mNote.getAttDataById(att.attId) != null) {
                MLog.e("下载文件成功--start2--endListener");
                endListener.onEnd(newAtt, true, "");
            } else {
                MLog.i(TAG, "att:" + att.attId + " not in the note:" + mNote.noteId);
            }
        }
    }

    @Override
    public void onListDownloadFailed(String msg, Exception e, TNNoteAtt att, int position) {
        MLog.d(TAG, msg);
    }

    /**
     * @param att 已经拿到下载图片的路径
     */
    @Override
    public void onSingleDownloadSuccess(TNNote note, TNNoteAtt att) {
        TNNoteAtt newAtt = att;

        File file = new File(att.path);
        //更改att path 和  syncState
        try {
            TNDb.beginTransaction();
            if (att.type > 10000 && att.type < 20000) {
                TNDb.getInstance().execSQL(TNSQLString.ATT_UPDATE_ATTLOCALID, att.attName, att.type, att.path, att.noteLocalId, file.length(), mNote.syncState > 2 ? mNote.syncState : 2, att.digest, att.attId, att.width, att.height, att.noteLocalId);
                TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, att.path, mNote.noteLocalId);
            }
            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }

        downloadingAtts.remove(att);
        start();
        if (endListener != null) {
            if (mNote.getAttDataById(att.attId) != null)
                endListener.onEnd(newAtt, true, "");
            else
                MLog.i(TAG, "att:" + att.attId + " not in the note:" + mNote.noteId);
        }
    }

    @Override
    public void onSingleDownloadFailed(String msg, Exception e) {
        MLog.d(TAG, msg);
    }


    //===========================回调act的自定义回调===========================
    public void setOnDownloadStartListener(OnDownloadStartListener startListener) {
        this.startListener = startListener;
    }

    public void setOnDownloadEndListener(OnDownloadEndListener endListener) {
        this.endListener = endListener;
    }

    /**
     * 点击下载 或自动下载 ，完成一个 回调一个
     */
    public interface OnDownloadStartListener {

        public void onStart(TNNoteAtt att);
    }

    /**
     * 点击下载 或自动下载 ，完成一个 回调一个
     */
    public interface OnDownloadEndListener {
        public void onEnd(TNNoteAtt att, boolean isSucess, String msg);
    }

}
