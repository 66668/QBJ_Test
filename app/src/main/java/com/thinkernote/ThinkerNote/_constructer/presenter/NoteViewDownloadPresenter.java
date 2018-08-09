package com.thinkernote.ThinkerNote._constructer.presenter;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNActionUtils;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.module.NoteViewDownloadModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.INoteViewDownloadModule;
import com.thinkernote.ThinkerNote._interface.v.OnNoteViewDownloadListener;

import java.io.File;
import java.util.Vector;

/**
 * 笔记详情--下载文件的类
 * <p>
 * 说明：使用老版本代码，打开图片文件没有问题，但是无法打开其他文件
 */
public class NoteViewDownloadPresenter implements OnNoteViewDownloadListener {
    private static final String TAG = "AAA";
    private static final long ATT_MAX_DOWNLOAD_SIZE = 50 * 1024;

    private static NoteViewDownloadPresenter singleton = null;

    private OnDownloadStartListener startListener;
    private OnDownloadEndListener endListener;

    private Vector<TNNoteAtt> downloadingAtts;
    private Vector<TNNoteAtt> readyDownloadAtts;

    private TNNote mNote;
    private Activity act;
    private INoteViewDownloadModule module;


    public NoteViewDownloadPresenter(Activity act) {
        this.act = act;
        readyDownloadAtts = new Vector<TNNoteAtt>();
        downloadingAtts = new Vector<TNNoteAtt>();

        module = new NoteViewDownloadModuleImpl(act, this);
    }

    public void setNewNote(TNNote note) {
        mNote = note;
        this.readyDownloadAtts.clear();
        this.readyDownloadAtts.addAll(note.atts);
    }


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
                    MLog.e("开始下载att:" + att.toString());
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
                MLog.e("更新--TNNoteAtt:" + i + "--" + tempAtt.toString());
                //保存路径
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

    /**
     * 多文件下载返回
     *
     * @param att 已经拿到下载图片的路径
     */
    @Override
    public void onListDownloadSuccess(TNNote note, TNNoteAtt att, int position) {
        MLog.e(TAG, "下载文件成功--onListDownloadSuccess--att：" + att.toString());
        TNNoteAtt newAtt = att;
        //判断是否下载成功
        File file = new File(att.path);
        if (!file.exists()) {
            MLog.e("--------------------下载图片路径--------------失败");
            //结束下载，返回
            if (endListener != null) {
                if (mNote.getAttDataById(newAtt.attId) != null) {
                    MLog.e(TAG, "下载文件失败--start2--endListener");
                    endListener.onEnd(newAtt, true, "");
                }
            }
            return;
        }

        //下载结束，保存文件路径
        int width = 0;
        int height = 0;
        try {
            TNDb.beginTransaction();
            if (att.type > 10000 && att.type < 20000) {
                BitmapFactory.Options bfo = TNUtilsAtt.getImageSize(newAtt.path);
                width = bfo.outWidth;
                height = bfo.outHeight;
                newAtt.width = width;
                newAtt.height = height;
                TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, att.path, mNote.noteLocalId);
            }
            //保存下载路径
            TNDb.getInstance().execSQL(TNSQLString.ATT_SET_DOWNLOADED, att.path, width, height, 2, att.attLocalId);
            TNDb.getInstance().execSQL(TNSQLString.ATT_UPDATE_ATTLOCALID, att.attName, newAtt.type, att.path, att.noteLocalId, file.length(), att.syncState > 2 ? mNote.syncState : 2, att.digest, att.attId, att.width, att.height, att.attLocalId);
            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }

        downloadingAtts.remove(att);
        MLog.e(TAG, "onEnd前再次start（）");
        start();
        if (endListener != null) {
            if (mNote.getAttDataById(att.attId) != null) {
                MLog.e(TAG, "下载文件成功--start2--endListener");
                endListener.onEnd(newAtt, true, "");
            }
        }
    }

    @Override
    public void onListDownloadFailed(String msg, Exception e, TNNoteAtt att, int position) {
        MLog.d(TAG, msg);
    }


    /**
     * 单文件点击下载返回
     *
     * @param note
     * @param att
     */
    @Override
    public void onSingleDownloadSuccess(TNNote note, TNNoteAtt att) {
        MLog.e(TAG, "下载文件成功--onSingleDownloadSuccess--att：" + att.toString());
        TNNoteAtt newAtt = att;

        //判断是否下载成功
        File file = new File(att.path);
        if (!file.exists()) {
            //结束下载，返回
            if (endListener != null) {
                if (mNote.getAttDataById(newAtt.attId) != null) {
                    MLog.e(TAG, "下载文件成功--start2--endListener");
                    endListener.onEnd(newAtt, true, "");
                } else {
                    MLog.e(TAG, "att:" + att.attId + " not in the note:" + mNote.noteId);
                }
            }
            return;
        }

        //下载结束，保存文件路径
        int width = 0;
        int height = 0;

        try {
            TNDb.beginTransaction();
            if (att.type > 10000 && att.type < 20000) {
                BitmapFactory.Options bfo = TNUtilsAtt.getImageSize(newAtt.path);
                width = bfo.outWidth;
                height = bfo.outHeight;
                newAtt.width = width;
                newAtt.height = height;

                TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, newAtt.path, mNote.noteLocalId);
            }
            //保存下载路径
            TNDb.getInstance().execSQL(TNSQLString.ATT_SET_DOWNLOADED, att.path, width, height, 2, newAtt.attLocalId);
            TNDb.getInstance().execSQL(TNSQLString.ATT_UPDATE_ATTLOCALID, att.attName, newAtt.type, att.path, att.noteLocalId, file.length(), att.syncState > 2 ? mNote.syncState : 2, att.digest, att.attId, att.width, att.height, att.attLocalId);
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
