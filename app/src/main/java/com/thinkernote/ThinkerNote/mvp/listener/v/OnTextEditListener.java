package com.thinkernote.ThinkerNote.mvp.listener.v;

/**
 * v层
 */
public interface OnTextEditListener {
    void onFolderAddSuccess();

    void onFolderAddFailed(String msg, Exception e);

    void onFolderRenameSuccess();

    void onFolderRenameFailed(String msg, Exception e);

    void onTagAddSuccess();

    void onTagAddFailed(String msg, Exception e);

    void onTagRenameSuccess();

    void onTagRenameFailed(Exception e, String msg);
}
