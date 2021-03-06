package com.thinkernote.ThinkerNote._interface.p;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.bean.main.AllFolderItemBean;

import java.util.List;

/**
 * p层interface
 */
public interface ICatFragPresenter {

    //其他
    void pGetParentFolder();

    void pGetFolderByFolderId(long catId);

    void pGetNoteListByTrash(int pagerSize,int pagenum,String sortType);

    void pGetNoteListByFolderId(long folderId,int pagerSize,int pagenum,String sortType);

    //syncData
    void folderAdd(int position, int arraySize, String folderName);

    void pGetFolder();

    void pProfile();

    void pGetFoldersByFolderId(long id, int position, List<AllFolderItemBean> beans);

    void pFirstFolderAdd(int workPos, int workSize, long catID, String name, int catPos, int flag);

    void tagAdd(int position, int arraySize, String tagName);

    void pUploadOldNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void pOldNoteAdd(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void pGetTagList();

    void pNewNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void pNewNote(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void pRecoveryNote(long noteID, int position, int arrySize);

    void pRecoveryNotePic(int picPos, int picArrySize, int notePos, int noteArrySize, TNNoteAtt tnNoteAtt);

    void pRecoveryNoteAdd(int position, int arraySize, TNNote tnNote, boolean isNewDb, String content);

    void pDeleteNote(long noteId, int position);

    void pDeleteRealNotes(long noteId, int position);

    void pGetAllNotesId();

    void pEditNotePic(int cloudsPos, int attrPos, TNNote note);

    void pEditNote(int position, TNNote note);

    void pGetNoteByNoteId(int position, long noteId, boolean is12);

    void pGetAllTrashNoteIds();
}
