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
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._constructer.module.NoteViewDownloadModuleImpl;
import com.thinkernote.ThinkerNote._interface.m.INoteViewDownloadModule;
import com.thinkernote.ThinkerNote._interface.v.OnNoteViewDownloadListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * 说明：无法修改老版本bug，路径报错
 */
public class NoteViewDownloadPresenter implements OnNoteViewDownloadListener {
    private static final String TAG = "NoteViewDownloadPresenter";
    private static final long ATT_MAX_DOWNLOAD_SIZE = 50 * 1024;

    private static NoteViewDownloadPresenter singleton = null;

    private OnDownloadStartListener startListener;
    private OnDownloadEndListener endListener;

    private List<TNNoteAtt> downAtts;

    private TNNote mNote;
    private Activity act;
    private INoteViewDownloadModule module;

    private NoteViewDownloadPresenter() {
        downAtts = new ArrayList<>();
        module = new NoteViewDownloadModuleImpl(act, this);
    }

    public static NoteViewDownloadPresenter getInstance() {
        if (singleton == null) {
            synchronized (NoteViewDownloadPresenter.class) {
                if (singleton == null) {
                    singleton = new NoteViewDownloadPresenter();
                }
            }
        }
        return singleton;
    }

    public NoteViewDownloadPresenter init(Activity act, TNNote note) {
        this.act = act;
        this.mNote = note;
        this.downAtts.clear();
        this.downAtts.addAll(note.atts);

        return this;
    }

    public void updateNote(TNNote note) {
        this.mNote = note;
        this.downAtts.addAll(note.atts);
    }

    /**
     * 多图下载（自动下载）
     */
    public void start() {
        MLog.e("start（）");
        startPosition(0, null);
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
            MLog.d(TAG, "start(attId)--TNNoteAtt:" + att.toString());

            if (!TNActionUtils.isDownloadingAtt(att.attId)) {
                //开始下载回调
                if (startListener != null)
                    startListener.onStart(att);
                MLog.d(TAG, "start(attId)--singledownload 下载文件");
                //下载
                singledownload(att, mNote);
            }
        }
    }

    /**
     * 测试 两个图片
     * TODO bug
     *
     * @param position
     * @param onAtt    结束一个position时使用，用于回调
     */
    private void startPosition(int position, TNNoteAtt onAtt) {
        /**
         * 新版本
         *
         *
         * 老版本
         * （2）
         * TNNote{noteLocalId=1, title='2018年7月26日  11:59 图片', syncState=1, creatorUserId=2483045, creatorNick='asdf456', catId=32302287,
         * content='<p><tn-media hash="4564F31BA492B799BE884563B9D3316E"></tn-media></p><p><tn-media hash="AAB5329272633CAD63A2F53236A87C0C"></tn-media></p>',
         * shortContent='  ', contentDigest='026180998DF4670605167D7D926D9E05', trash=0, source='android', createTime=1532577592,
         * lastUpdate=1532577592, thumbnail='', thmDrawable=null, lbsLongitude=0, lbsLatitude=0, lbsRadius=0, lbsAddress='null', tags=null, tagStr='', attCounts=2,
         * atts=[
         * TNNoteAtt{attLocalId=1, noteLocalId=1, attId=28519271, attName='1532577582546.jpg', type=10002, path='null', syncState=1, size=138470, digest='4564F31BA492B799BE884563B9D3316E', thumbnail='null', width=0, height=0},
         * TNNoteAtt{attLocalId=2, noteLocalId=1, attId=28519272, attName='1532577589229.jpg', type=10002, path='null', syncState=1, size=157804, digest='AAB5329272633CAD63A2F53236A87C0C', thumbnail='null', width=0, height=0}],
         * currentAtt=null, noteId=37840200, revision=0, originalNote=null, richText='null', mapping=null}
         *
         */
        MLog.e(TAG, "startPosition：" + position + "----" + mNote.toString());
        if (downAtts != null && downAtts.size() > 0 && position < downAtts.size()) {
            //获取当前position的TNNoteAtt
            TNNoteAtt att = downAtts.get(position);
            File file = null;

            if (!TextUtils.isEmpty(att.path)) {
                file = new File(att.path);
            }

            //如果att已经下载，执行下一个position
            if (file.length() != 0 && att.syncState == 2) {
                MLog.e("startPosition--文件已下载--回调主界面，执行下一个att循环");
                if (downAtts.size() == 1) {
                    endOneAttCallback(att, true);
                } else {
                    if (position == downAtts.size() - 1) {
                        endOneAttCallback(att, true);
                    } else {
                        endOneAttCallback(att, true);
                        //执行下一个循环
                        startPosition(position + 1, att);
                    }
                }
                return;
            }

            if (TNUtils.isNetWork() && att.attId != -1) {
                //开始下载回调
                if (startListener != null) {
                    startListener.onStart(att);
                }
                //下载
                MLog.d(TAG, "下载：position=" + position);
                listDownload(att, mNote, position);
            } else {//执行下一个att循环
                MLog.d(TAG, "startPosition--网络差 无法下载");
                if (downAtts.size() == 1) {
                    endOneAttCallback(att, false);
                } else {
                    if (position == downAtts.size() - 1) {
                        endOneAttCallback(att, false);
                    } else {
                        endOneAttCallback(att, false);
                        //执行下一个循环
                        startPosition(position + 1, att);
                    }
                }
            }

        } else {
            //循环结束，回调
            endOneAttCallback(onAtt, true);
        }
    }

    /**
     * 结束一个att下载回调
     */
    private void endOneAttCallback(TNNoteAtt att, boolean isSuccess) {
        if (att != null) {
            MLog.d(TAG, "回调act endListener attName=" + att.attName);
            //回调act
            if (endListener != null) {
                if (mNote.getAttDataById(att.attId) != null) {
                    MLog.d(TAG, "回调act endListener attName=" + att.attName);
                    if (isSuccess) {
                        endListener.onEnd(att, true, null);
                    } else {
                        endListener.onEnd(null, false, null);
                    }
                } else {
                    MLog.i(TAG, "att:" + att.attId + " not in the note:" + mNote.noteId);
                }
            }
        } else {
            if (!isSuccess) {
                endListener.onEnd(att, false, null);
            }
        }

    }


    //===========================接口调用=================================
    //调用接口
    private void singledownload(TNNoteAtt tnNoteAtt, TNNote tnNote) {
        MLog.d("download", "singledownload下载文件");
        module.singleDownload(tnNoteAtt, tnNote);
    }

    //调用接口
    private void listDownload(TNNoteAtt tnNoteAtt, TNNote tnNote, int position) {
        MLog.d("download", "listDownload下载文件");
        module.listDownload(tnNoteAtt, tnNote, position);
    }
    //===========================接口返回=================================


    /**
     * 下载结束后数据变化：
     * 新版本：
     * TNNote{noteLocalId=1, title='2018年7月26日  11:59 图片', syncState=1, creatorUserId=2483045, creatorNick='asdf456', catId=32302287,
     * content='<p><tn-media hash="4564F31BA492B799BE884563B9D3316E"></tn-media></p><p><tn-media hash="AAB5329272633CAD63A2F53236A87C0C"></tn-media></p>',
     * shortContent='  ', contentDigest='026180998DF4670605167D7D926D9E05', trash=0, source='android', createTime=1532577592,
     * lastUpdate=1532577592, thumbnail='', thmDrawable=null, lbsLongitude=0, lbsLatitude=0, lbsRadius=0, lbsAddress='null', tags=null, tagStr='', attCounts=2,
     * atts=[
     * TNNoteAtt{attLocalId=1, noteLocalId=1, attId=28519271, attName='1532577582546.jpg', type=10002, path='null', syncState=1, size=138470, digest='4564F31BA492B799BE884563B9D3316E', thumbnail='null', width=0, height=0},
     * TNNoteAtt{attLocalId=2, noteLocalId=1, attId=28519272, attName='1532577589229.jpg', type=10002, path='null', syncState=1, size=157804, digest='AAB5329272633CAD63A2F53236A87C0C', thumbnail='null', width=0, height=0}],
     * currentAtt=null, noteId=37840200, revision=0, originalNote=null, richText='null', mapping=null}
     * <p>
     * 老版本：
     * 下载第一个图片响应
     * <p>
     * TNNote{noteLocalId=1, title='2018年7月26日  11:59 图片', syncState=1, creatorUserId=2483045, creatorNick='asdf456', catId=32302287,
     * content='<p><tn-media hash="4564F31BA492B799BE884563B9D3316E"></tn-media></p><p><tn-media hash="AAB5329272633CAD63A2F53236A87C0C"></tn-media></p>',
     * shortContent='  ', contentDigest='026180998DF4670605167D7D926D9E05', trash=0, source='android', createTime=1532577592,
     * lastUpdate=1532577592, thumbnail='null', thmDrawable=null, lbsLongitude=0, lbsLatitude=0, lbsRadius=0, lbsAddress='null', tags=null, tagStr='', attCounts=2,
     * atts=[TNNoteAtt{attLocalId=1, noteLocalId=1, attId=28519271, attName='1532577582546.jpg', type=10002,
     * path='/storage/emulated/0/Android/data/com.thinkernote.ThinkerNote/files/Attachment/28/28519/28519271.jpeg',
     * syncState=2, size=138470, digest='4564F31BA492B799BE884563B9D3316E',
     * thumbnail='null', width=432, height=576},
     * TNNoteAtt{attLocalId=2, noteLocalId=1, attId=28519272, attName='1532577589229.jpg', type=10002, path='null', syncState=1, size=157804, digest='AAB5329272633CAD63A2F53236A87C0C', thumbnail='null', width=0, height=0}],
     * currentAtt=null, noteId=37840200, revision=0, originalNote=null, richText='null', mapping=null}
     * <p>
     * endListener
     * attr:TNNoteAtt{attLocalId=1, noteLocalId=1, attId=28519271, attName='1532577582546.jpg', type=10002,
     * path='/storage/emulated/0/Android/data/com.thinkernote.ThinkerNote/files/Attachment/28/28519/28519271.jpeg',
     * syncState=1, size=138470, digest='4564F31BA492B799BE884563B9D3316E',
     * thumbnail='/storage/emulated/0/Android/data/com.thinkernote.ThinkerNote/files/Attachment/28/28519/28519271.jpeg.thm',
     * width=0, height=0}
     *
     * @param att      已经拿到下载图片的路径,用于返回act
     * @param position
     */
    @Override
    public void onListDownloadSuccess(TNNote note, TNNoteAtt att, int position) {
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

        //更新mNote
        mNote = TNDbUtils.getNoteByNoteLocalId(mNote.noteLocalId);
        MLog.e("更新mNote:" + mNote.toString());

        //最后一个att处理（所有图片下载完成）
        if (position == mNote.attCounts - 1) {
            //数据库操作
            if (mNote.attCounts > 0) {
                for (int i = 0; i < mNote.atts.size(); i++) {
                    TNNoteAtt tempAtt = mNote.atts.get(i);
                    MLog.e("数据库操作：" + "mNote.syncState=" + mNote.syncState + "position=" + i + "--内容=" + tempAtt.toString());
                    if (i == 0 && tempAtt.type > 10000 && tempAtt.type < 20000) {
                        TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, tempAtt.path, mNote.noteLocalId);
                    }
                    if (TextUtils.isEmpty(tempAtt.path) || "null".equals(tempAtt.path)) {
                        mNote.syncState = 1;
                    }
                }
            }
        } else {
            //更新for循环的数据
            downAtts.clear();
            downAtts.addAll(mNote.atts);
        }
        //
        TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, mNote.syncState, mNote.noteLocalId);

        //下载成功的一个position的att展示
        endOneAttCallback(newAtt, true);
        //开始下一个position
        startPosition(position + 1, newAtt);
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
        if (!TextUtils.isEmpty(att.path)) {
            MLog.d("single文件下载成功：", "原状态att.syncState=" + att.syncState + "文件路径" + file.toString() + "文件大小" + file.length());
        }

        //将图片路径保存到本地数据库
        try {
            TNDb.beginTransaction();
            if (att.type > 10000 && att.type < 20000) {
                MLog.e("Single-更新数据库");
                newAtt.syncState = mNote.syncState > 2 ? mNote.syncState : 2;
                TNDb.getInstance().execSQL(TNSQLString.ATT_UPDATE_ATTLOCALID, att.attName, att.type, att.path, att.noteLocalId, file.length(), mNote.syncState > 2 ? mNote.syncState : 2, att.digest, att.attId, att.width, att.height, att.noteLocalId);
                TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_THUMBNAIL, att.path, mNote.noteLocalId);
                TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_SYNCSTATE, mNote.syncState > 2 ? mNote.syncState : 2, mNote.noteLocalId);

            } else {
                MLog.e("Bug 无法更新其他position数据");
                //结束一个att下载回调
                endOneAttCallback(att, false);
            }

            TNDb.setTransactionSuccessful();
        } finally {
            TNDb.endTransaction();
        }
        //结束一个att下载回调
        endOneAttCallback(newAtt, true);
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
