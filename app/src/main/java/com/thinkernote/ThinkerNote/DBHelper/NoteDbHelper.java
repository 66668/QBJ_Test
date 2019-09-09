package com.thinkernote.ThinkerNote.DBHelper;

import java.util.Vector;

import org.json.JSONObject;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDb2;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;

public class NoteDbHelper {
    public static void addNote(Object title, Object userId, Object catId, Object trash, Object content, Object source,
                               Object createTime, Object lastUpdate, Object syncState, Object noteId,
                               Object shortContent, Object tagStr, Object lbsLongitude, Object lbsLatitude, Object lbsRadius,
                               Object lbsAddress, Object nickName, Object thumbnail, Object contentDigest) {
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().execSQL(TNSQLString.NOTE_INSERT, title, userId, catId, trash, content, source,
                    createTime, lastUpdate, syncState, noteId,
                    shortContent, tagStr, lbsLongitude, lbsLatitude, lbsRadius,
                    lbsAddress, nickName, thumbnail, contentDigest);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
    }

    public static void updateNoteForList(Object title, Object catId,
                                         Object createTime, Object lastUpdate, Object syncState, Object shortContent, Object tagStr, Object contentDigest, Object noteId) {
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE_FOR_LIST, title, catId,
                    createTime, lastUpdate, syncState, shortContent, tagStr, contentDigest, noteId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
    }

    public static void updateNote(Object title, Object catId, Object content, Object syncState,
                                  Object createTime, Object lastUpdate, Object shortContent, Object tagStr, Object contentDigest, Object noteId) {
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().execSQL(TNSQLString.NOTE_UPDATE, title, catId, content,
                    createTime, lastUpdate, shortContent, tagStr, contentDigest, syncState, noteId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
    }

    public static void updateLocalNote(Object title, Object catId, Object content, Object trash, Object syncState,
                                       Object createTime, Object lastUpdate, Object shortContent, Object tagStr, Object contentDigest, Object noteLocalId) {
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().execSQL(TNSQLString.NOTE_LOCAL_UPDATE, title, catId, content,
                    createTime, lastUpdate, shortContent, tagStr, contentDigest, syncState, noteLocalId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
    }

    public static void clearNotes() {
        TNDb.beginTransaction();
        try {
            TNDb.getInstance().execSQL(TNSQLString.NOTE_CLEAR, TNSettings.getInstance().userId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
    }

    public static String getNotesCountByAll() {
        @SuppressWarnings("unchecked")
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb.beginTransaction();
        try {
            s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                    TNSQLString.NOTE_COUNT_BYALL, TNSettings.getInstance().userId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
        return s.size() > 0 ? s.get(0).get(0) : "0";
    }

    public static String getNotesCountByCatId(Object catId) {
        @SuppressWarnings("unchecked")
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb.beginTransaction();
        try {
            s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                    TNSQLString.NOTE_COUNT_BYCAT, catId, TNSettings.getInstance().userId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
        return s.size() > 0 ? s.get(0).get(0) : "0";

    }

    public static String getNotesCountByTag(Object tagName) {
        @SuppressWarnings("unchecked")
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb.beginTransaction();
        try {
            s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                    TNSQLString.NOTE_COUNT_BYTAGNAME, "%" + tagName + "%", TNSettings.getInstance().userId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
        return s.size() > 0 ? s.get(0).get(0) : "0";
    }

    /**
     * 通过noteLocalId获取笔记
     *
     * @param noteLocalId
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Vector<Vector<String>> getNoteByLocalId(Object noteLocalId) {
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb.beginTransaction();
        try {
            s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                    TNSQLString.NOTE_CHECK_LOCALID, TNSettings.getInstance().userId, noteLocalId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
        return s;
    }

    /**
     * 通过noteId获取本地LocalId
     *
     * @param noteId
     * @return
     */
    public static Object getNoteLocalId(Object noteId) {
        @SuppressWarnings("unchecked")
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb.beginTransaction();
        try {
            s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                    TNSQLString.NOTELOCALID_BY_NOTEID, noteId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
        return s.get(0).get(0);
    }

    @SuppressWarnings("unchecked")
    public static Vector<Vector<String>> getNote(Object noteId) {
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb.beginTransaction();
        try {
            s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                    TNSQLString.NOTE_CHECK_ID, noteId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
        return s;
    }

    public static boolean isNoteExist(Object noteId) {
        return getNote(noteId).size() > 0;
    }

    public static boolean isNeedUpdate(Object noteId, String lastUpdate) {
        return !getUpdateTimeByNoteId(noteId).equals(lastUpdate);
    }

    @SuppressWarnings("unchecked")
    public static String getUpdateTimeByNoteId(Object noteId) {
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb.beginTransaction();
        try {
            s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                    TNSQLString.LASTUPDATE_BY_NOTEID, noteId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
        return s.size() == 0 ? "" : s.get(0).get(0);
    }

    public static void addNote(JSONObject Note) {
        addNote(TNUtils.getFromJSON(Note, "title"),
                TNUtils.getFromJSON(Note, "userId"),
                TNUtils.getFromJSON(Note, "catId"),
                TNUtils.getFromJSON(Note, "trash"),
                TNUtils.getFromJSON(Note, "content"),
                TNUtils.getFromJSON(Note, "source"),
                TNUtils.getFromJSON(Note, "createTime"),
                TNUtils.getFromJSON(Note, "lastUpdate"),
                TNUtils.getFromJSON(Note, "syncState"),
                TNUtils.getFromJSON(Note, "noteId"),
                TNUtils.getFromJSON(Note, "shortContent"),
                TNUtils.getFromJSON(Note, "tagStr"),
                TNUtils.getFromJSON(Note, "lbsLongitude"),
                TNUtils.getFromJSON(Note, "lbsLatitude"),
                TNUtils.getFromJSON(Note, "lbsRadius"),
                TNUtils.getFromJSON(Note, "lbsAddress"),
                TNUtils.getFromJSON(Note, "nickName"),
                TNUtils.getFromJSON(Note, "thumbnail"),
                TNUtils.getFromJSON(Note, "contentDigest"));
    }

    public static void updateNote(JSONObject Note) {
        updateNote(TNUtils.getFromJSON(Note, "title"),
                TNUtils.getFromJSON(Note, "catId"),
                TNUtils.getFromJSON(Note, "content"),
                TNUtils.getFromJSON(Note, "syncState"),
                TNUtils.getFromJSON(Note, "createTime"),
                TNUtils.getFromJSON(Note, "lastUpdate"),
                TNUtils.getFromJSON(Note, "shortContent"),
                TNUtils.getFromJSON(Note, "tagStr"),
                TNUtils.getFromJSON(Note, "contentDigest"),
                TNUtils.getFromJSON(Note, "noteId"));
    }

    public static void addOrUpdateNote(JSONObject Note) {
        Object noteId = TNUtils.getFromJSON(Note, "noteId");
        if (isNoteExist(noteId)) {
            TNNote note = TNDbUtils.getNoteByNoteId((Long.valueOf(noteId.toString())));
            if (note.lastUpdate < Integer.valueOf(TNUtils.getFromJSON(Note, "lastUpdate").toString())) {
                note.syncState = 1;
            }
            updateNoteForList(TNUtils.getFromJSON(Note, "title"),
                    TNUtils.getFromJSON(Note, "catId"),
                    TNUtils.getFromJSON(Note, "createTime"),
                    TNUtils.getFromJSON(Note, "lastUpdate"),
                    note.syncState,
                    TNUtils.getFromJSON(Note, "shortContent"),
                    TNUtils.getFromJSON(Note, "tagStr"),
                    TNUtils.getFromJSON(Note, "contentDigest"),
                    noteId);
        } else {
            addNote(TNUtils.getFromJSON(Note, "title"),
                    TNUtils.getFromJSON(Note, "userId"),
                    TNUtils.getFromJSON(Note, "catId"),
                    TNUtils.getFromJSON(Note, "trash"),
                    TNUtils.getFromJSON(Note, "content"),
                    TNUtils.getFromJSON(Note, "source"),
                    TNUtils.getFromJSON(Note, "createTime"),
                    TNUtils.getFromJSON(Note, "lastUpdate"),
                    TNUtils.getFromJSON(Note, "syncState"),
                    noteId,
                    TNUtils.getFromJSON(Note, "shortContent"),
                    TNUtils.getFromJSON(Note, "tagStr"),
                    TNUtils.getFromJSON(Note, "lbsLongitude"),
                    TNUtils.getFromJSON(Note, "lbsLatitude"),
                    TNUtils.getFromJSON(Note, "lbsRadius"),
                    TNUtils.getFromJSON(Note, "lbsAddress"),
                    TNUtils.getFromJSON(Note, "nickName"),
                    TNUtils.getFromJSON(Note, "thumbnail"),
                    TNUtils.getFromJSON(Note, "contentDigest"));
        }
    }

    @SuppressWarnings("unchecked")
    public static Vector<Vector<String>> getAllNoteList(Object userId) {
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb.beginTransaction();
        try {
            s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                    TNSQLString.NOTE_GET_ALL, userId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    public static Vector<Vector<String>> getFolderAllNoteList(Object userId, Object folderId) {
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb.beginTransaction();
        try {
            s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                    TNSQLString.NOTE_GET_ALL_BY_FOLDER, userId, folderId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    public static Vector<Vector<String>> getNoteListByCount(Object userId, Object count, String type) {
        String sql = "";
        if (TNConst.CREATETIME.equals(type)) {
            sql = TNSQLString.NOTE_GET_COUNT_CREATE;
        } else {
            sql = TNSQLString.NOTE_GET_COUNT_UPDATE;
        }
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb.beginTransaction();
        try {
            s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                    sql, userId, count);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    public static Vector<Vector<String>> getNoteListByCatId(Object userId, Object catId, String type, int count) {
        String sql = "";
        if (count == TNConst.MAX_PAGE_SIZE) {
            if (TNConst.CREATETIME.equals(type)) {
                sql = TNSQLString.NOTE_GET_BY_CATID_CREATE;
            } else {
                sql = TNSQLString.NOTE_GET_BY_CATID_UPDATE;
            }
            Vector<Vector<String>> s = new Vector<Vector<String>>();
            TNDb.beginTransaction();
            try {
                s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                        sql, userId, catId);
                TNDb.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                TNDb.endTransaction();
            }
            return s;
        } else {
            if (TNConst.CREATETIME.equals(type)) {
                sql = TNSQLString.NOTE_GET_COUNT_BY_CATID_CREATE;
            } else {
                sql = TNSQLString.NOTE_GET_COUNT_BY_CATID_UPDATE;
            }
            Vector<Vector<String>> s = new Vector<Vector<String>>();
            TNDb.beginTransaction();
            try {
                s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                        sql, userId, catId, count);
                TNDb.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                TNDb.endTransaction();
            }
            return s;
        }
    }

    @SuppressWarnings("unchecked")
    public static Vector<Vector<String>> getNoteListByTagName(Object userId, Object tagName, String type, int count) {
        String sql = "";
        if (TNConst.CREATETIME.equals(type)) {
            sql = TNSQLString.NOTE_GET_COUNT_BY_TAGID_CREATE;
        } else {
            sql = TNSQLString.NOTE_GET_COUNT_BY_TAGID_UPDATE;
        }
        tagName = "%" + tagName + "%";
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb.beginTransaction();
        try {
            s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                    sql, userId, tagName, count);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    public static Vector<Vector<String>> getNoteListByTrash(Object userId, Object type) {
        String sql = "";
        if (TNConst.CREATETIME.equals(type)) {
            sql = TNSQLString.NOTE_GET_All_BY_TRASH_CREATE;
        } else {
            sql = TNSQLString.NOTE_GET_All_BY_TRASH_UPDATE;
        }
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb.beginTransaction();
        try {
            s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                    sql, userId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    public static Vector<Vector<String>> getNoteListBySearch(Object userId, Object keyWord, Object type) {
        String sql = "";
        if (TNConst.CREATETIME.equals(type)) {
            sql = TNSQLString.NOTELIST_BYKEYWORDS_CREATE;
        } else {
            sql = TNSQLString.NOTELIST_BYKEYWORDS_UPDATE;
        }
        keyWord = "%" + keyWord + "%";
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb.beginTransaction();
        try {
            s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                    sql, userId, keyWord, keyWord);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    public static Vector<Vector<String>> getNoteListBySyncState(Object userId, Object syncState) {
        String sql = TNSQLString.NOTELIST_BYSYNCSTATE;
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb.beginTransaction();
        try {
            s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                    sql, userId, syncState);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    public static Vector<Vector<String>> getNoteListBySyncStateByCatId(Object userId, Object syncState, Object catId) {
        String sql = TNSQLString.NOTELIST_BYSYNCSTATE_BYCATID;
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb.beginTransaction();
        try {
            s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
                    sql, userId, syncState, catId);
            TNDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb.endTransaction();
        }
        return s;
    }

    //获取老数据库笔记==============================================================
    @SuppressWarnings("unchecked")
    public static Vector<Vector<String>> getNoteListByOldDB(Object userId) {
        Vector<Vector<String>> s = new Vector<Vector<String>>();
        TNDb2.beginTransaction();
        try {
            Vector<Vector<String>> userLocalIds = (Vector<Vector<String>>) TNDb2.getInstance().execSQL(TNSQLString.USER_CHECK_OLD_DB_USERID, userId);
            String userLocalId = "";
            if (userLocalIds.size() > 0) {
                userLocalId = userLocalIds.get(0).get(0);
            } else {
                return null;
            }
            String sql = TNSQLString.NOTE_GET_OLD_DB;
            s = (Vector<Vector<String>>) TNDb2.getInstance().execSQL(
                    sql, userLocalId);
            TNDb2.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TNDb2.endTransaction();
        }
        return s;
    }

}
