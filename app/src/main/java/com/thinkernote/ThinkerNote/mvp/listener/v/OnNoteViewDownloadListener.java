package com.thinkernote.ThinkerNote.mvp.listener.v;

import com.thinkernote.ThinkerNote.bean.localdata.TNNote;
import com.thinkernote.ThinkerNote.bean.localdata.TNNoteAtt;

/**
 * 笔记详情 v层
 */
public interface OnNoteViewDownloadListener {
    void onSingleDownloadSuccess(TNNote tnNote, TNNoteAtt att);

    void onSingleDownloadFailed(String msg, Exception e);

    void onListDownloadSuccess(TNNote tnNote, TNNoteAtt att, int position);

    void onListDownloadFailed(String msg, Exception e, TNNoteAtt att, int position);

}
