package com.thinkernote.ThinkerNote.db.Database;

import java.util.Vector;

import com.thinkernote.ThinkerNote.db.CatDbHelper;
import com.thinkernote.ThinkerNote.db.NoteAttrDbHelper;
import com.thinkernote.ThinkerNote.db.NoteDbHelper;
import com.thinkernote.ThinkerNote.db.TagDbHelper;
import com.thinkernote.ThinkerNote.db.UserDbHelper;
import com.thinkernote.ThinkerNote.bean.localdata.TNCat;
import com.thinkernote.ThinkerNote.bean.localdata.TNNote;
import com.thinkernote.ThinkerNote.bean.localdata.TNNoteAtt;
import com.thinkernote.ThinkerNote.bean.localdata.TNTag;
import com.thinkernote.ThinkerNote.bean.localdata.TNUser;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsHtml;
import com.thinkernote.ThinkerNote.utils.MLog;

/**
 * 本地数据库
 */
public class TNDbUtils {
    private static final String TAG = "TNDbUtils";

    /**
     * name = phone/email/username
     *
     * @param name
     * @return
     */
    public static long getUserId(Object name) {
        Vector<Vector<String>> data = UserDbHelper.getUserId(name);
        if (data.size() > 0) {
            return Long.valueOf(data.get(0).get(0));
        } else {
            return -1;
        }
    }

    public static TNUser getUser(long userId) {
        Vector<Vector<String>> data = UserDbHelper.getUser(userId);
        if (data.size() > 0) {
//			`username`, `password`, `userEmail`, `phone`, `userId`, `emailVerify`
            TNUser user = new TNUser();
            user.username = data.get(0).get(0);
            user.password = data.get(0).get(1);
            user.userEmail = data.get(0).get(2);
            user.phone = data.get(0).get(3);
            user.userId = Long.valueOf(data.get(0).get(4));
            user.emailVerify = Integer.valueOf(data.get(0).get(5));
            user.totalSpace = Long.valueOf(data.get(0).get(6));
            user.usedSpace = Long.valueOf(data.get(0).get(7));
            return user;
        }
        return null;
    }

    public static Vector<TNCat> getAllCatList(long userId) {
        Vector<Vector<String>> data = CatDbHelper.getAllCatList(userId);
        return getCats(data);
    }

    public static Vector<TNCat> getCatsByCatId(long userId, long pCatId) {
        Vector<Vector<String>> data = CatDbHelper.getCatsByCatId(userId, pCatId);
        return getCats(data);
    }

    public static TNCat getCat(long catId) {
        Vector<Vector<String>> data = CatDbHelper.getCat(catId);
        if (data.size() > 0) {
            return getCats(data).get(0);
        }
        return null;
    }

    public static Vector<TNCat> getCats(Vector<Vector<String>> data) {
        Vector<TNCat> cats = new Vector<TNCat>();
        for (int i = 0; i < data.size(); i++) {
            TNCat cat = new TNCat();
            cat.catLocalId = Long.valueOf(data.get(i).get(0));
            cat.catName = data.get(i).get(1);
            cat.userId = Long.valueOf(data.get(i).get(2));
            cat.trash = Integer.valueOf(data.get(i).get(3));
            cat.catId = Long.valueOf(data.get(i).get(4));
            int noteCounts = Integer.valueOf(NoteDbHelper.getNotesCountByCatId(cat.catId));
            cat.noteCounts = noteCounts;
            int catCounts = Integer.valueOf(CatDbHelper.getCatCountByCatId(cat.catId));
            cat.catCounts = catCounts;
            cat.deep = Integer.valueOf(data.get(i).get(7));
            cat.pCatId = Long.valueOf(data.get(i).get(8));
            cat.isNew = Integer.valueOf(data.get(i).get(9));
            cat.createTime = Long.valueOf(data.get(i).get(10));
            cat.lastUpdateTime = Long.valueOf(data.get(i).get(11));

            cats.add(cat);
        }
        return cats;
    }

    public static Vector<TNTag> getTagList(long userId) {
        Vector<Vector<String>> data = TagDbHelper.getTagList(userId);
        return getTags(data);
    }

    public static TNTag getTag(long tagId) {
        Vector<Vector<String>> data = TagDbHelper.getTag(tagId);
        Vector<TNTag> tags = getTags(data);
        if (tags.size() > 0) {
            return tags.get(0);
        }
        return null;
    }

    public static TNTag getTagByText(String text) {
        Vector<Vector<String>> data = TagDbHelper.getTagByText(text);
        Vector<TNTag> tags = getTags(data);
        if (tags.size() > 0) {
            return tags.get(0);
        }
        return null;
    }

    public static Vector<TNTag> getTags(Vector<Vector<String>> data) {
        Vector<TNTag> tags = new Vector<TNTag>();
        for (int i = 0; i < data.size(); i++) {
            TNTag tag = new TNTag();
            tag.tagLocalId = Long.valueOf(data.get(i).get(0));
            tag.tagName = data.get(i).get(1);
            tag.userId = Long.valueOf(data.get(i).get(2));
            tag.trash = Integer.valueOf(data.get(i).get(3));
            tag.tagId = Long.valueOf(data.get(i).get(4));
            int noteCounts = Integer.valueOf(NoteDbHelper.getNotesCountByTag(tag.tagName));
            tag.noteCounts = noteCounts;
            tag.strIndex = data.get(i).get(6);

            tags.add(tag);
        }
        return tags;
    }

    public static Vector<TNNote> getAllNoteList(long userId) {
        Vector<TNNote> notes = new Vector<TNNote>();
        Vector<Vector<String>> data = NoteDbHelper.getAllNoteList(userId);
        for (int i = 0; i < data.size(); i++) {
            TNNote note = getNote(data.get(i));
            notes.add(note);
        }
        return notes;
    }

    public static Vector<TNNote> getFolderAllNoteList(long userId, long folderId) {
        Vector<TNNote> notes = new Vector<TNNote>();
        Vector<Vector<String>> data = NoteDbHelper.getFolderAllNoteList(userId, folderId);
        for (int i = 0; i < data.size(); i++) {
            TNNote note = getNote(data.get(i));
            notes.add(note);
        }
        return notes;
    }

    //	getNoteListByCount
    public static Vector<TNNote> getNoteListByCount(long userId, int count, String type) {
        Vector<Vector<String>> data = NoteDbHelper.getNoteListByCount(userId, count, type);
        return getNotes(data);
    }

    public static Vector<TNNote> getNoteListByCatId(long userId, long catId, String type, int count) {
        Vector<Vector<String>> data = NoteDbHelper.getNoteListByCatId(userId, catId, type, count);
        return getNotes(data);
    }

    public static Vector<TNNote> getNoteListByTagName(long userId, String tagName, String type, int count) {
        Vector<Vector<String>> data = NoteDbHelper.getNoteListByTagName(userId, tagName, type, count);
        return getNotes(data);
    }

    public static Vector<TNNote> getNoteListByTrash(long userId, String type) {
        Vector<Vector<String>> data = NoteDbHelper.getNoteListByTrash(userId, type);
        return getNotes(data);
    }

    public static Vector<TNNote> getNoteListBySearch(long userId, String keyWord, String type) {
        Vector<Vector<String>> data = NoteDbHelper.getNoteListBySearch(userId, keyWord, type);
        return getNotes(data);
    }

    public static Vector<TNNote> getNoteListBySyncState(long userId, int syncState) {
        Vector<Vector<String>> data = NoteDbHelper.getNoteListBySyncState(userId, syncState);
        return getNotes(data);
    }

    public static Vector<TNNote> getNoteListBySyncStateByCatId(long userId, int syncState, long catId) {
        Vector<Vector<String>> data = NoteDbHelper.getNoteListBySyncStateByCatId(userId, syncState, catId);
        return getNotes(data);
    }

    public static TNNote getNoteByNoteLocalId(long noteLocalId) {
        TNNote note = null;
        Vector<Vector<String>> data = NoteDbHelper.getNoteByLocalId(noteLocalId);
        if (data.size() > 0) {
            note = getNotes(data).get(0);
        }
        return note;
    }

    public static TNNote getNoteByNoteId(long noteId) {
        Vector<Vector<String>> data = NoteDbHelper.getNote(noteId);
        return data.size() > 0 ? getNotes(data).get(0) : null;
    }

    public static Vector<TNNote> getNotes(Vector<Vector<String>> data) {
        Vector<TNNote> notes = new Vector<TNNote>();
        for (int i = 0; i < data.size(); i++) {
            TNNote note = getNote(data.get(i));
            notes.add(note);
        }
        return notes;
    }

    public static TNNote getNote(Vector<String> data) {
        TNNote note = new TNNote();
        note.noteLocalId = Long.valueOf(data.get(0));
        note.title = data.get(1);
        note.creatorUserId = Long.valueOf(data.get(2));
        note.catId = Long.valueOf(data.get(3)) == 0 ? TNSettings.getInstance().defaultCatId : Long.valueOf(data.get(3));
        note.content = data.get(4);
        note.trash = Integer.valueOf(data.get(5));
        note.source = data.get(6);
        try {
            note.createTime = Integer.valueOf(data.get(7));
        } catch (Exception e) {
            note.createTime = Integer.valueOf(data.get(8));
        }
        try {
            note.lastUpdate = Integer.valueOf(data.get(8));
        } catch (Exception e) {
            note.lastUpdate = Integer.valueOf(data.get(7));
        }
        note.syncState = Integer.valueOf(data.get(9));
        note.noteId = Long.valueOf(data.get(10));
        note.shortContent = data.get(11);
        note.tagStr = data.get(12);
        note.lbsLongitude = Integer.valueOf(data.get(13));
        note.lbsLatitude = Integer.valueOf(data.get(14));
        note.lbsRadius = Integer.valueOf(data.get(15));
        note.lbsAddress = data.get(16);
        note.creatorNick = data.get(17);
        note.thumbnail = data.get(18);
        note.contentDigest = data.get(19);

        Vector<TNNoteAtt> atts = getAttrsByNoteLocalId(note.noteLocalId);

        note.atts = atts;
        note.attCounts = atts.size();

        return note;
    }

    public static TNNoteAtt getAttrById(long attId) {
        TNNoteAtt noteAtt = new TNNoteAtt();
        Vector<Vector<String>> data = NoteAttrDbHelper.getAttr(attId);
        if (data.size() > 0) {
            noteAtt.attLocalId = Long.valueOf(data.get(0).get(0));
            noteAtt.attName = data.get(0).get(1);
            noteAtt.type = Integer.valueOf(data.get(0).get(2));
            noteAtt.path = data.get(0).get(3);
            noteAtt.noteLocalId = Long.valueOf(data.get(0).get(4));
            noteAtt.size = Integer.valueOf(data.get(0).get(5));
            noteAtt.syncState = Integer.valueOf(data.get(0).get(6));
            noteAtt.digest = data.get(0).get(7);
            noteAtt.attId = Long.valueOf(data.get(0).get(8));
            noteAtt.width = Integer.valueOf(data.get(0).get(9));
            noteAtt.height = Integer.valueOf(data.get(0).get(10));
        }

        return noteAtt;
    }

    public static Vector<TNNoteAtt> getAttrsByNoteLocalId(long noteLocalId) {
        Vector<Vector<String>> data = NoteAttrDbHelper.getAttrsByNoteLocalId(noteLocalId);
        Vector<TNNoteAtt> noteAtts = new Vector<TNNoteAtt>();
        for (int i = 0; i < data.size(); i++) {
            TNNoteAtt noteAtt = new TNNoteAtt();
            noteAtt.attLocalId = Long.valueOf(data.get(i).get(0));
            noteAtt.attName = data.get(i).get(1);
            noteAtt.type = Integer.valueOf(data.get(i).get(2));
            noteAtt.path = data.get(i).get(3);
            noteAtt.noteLocalId = Long.valueOf(data.get(i).get(4));
            noteAtt.size = Integer.valueOf(data.get(i).get(5));
            noteAtt.syncState = Integer.valueOf(data.get(i).get(6));
            noteAtt.digest = data.get(i).get(7);
            noteAtt.attId = Long.valueOf(data.get(i).get(8));
            noteAtt.width = Integer.valueOf(data.get(i).get(9));
            noteAtt.height = Integer.valueOf(data.get(i).get(10));

            noteAtts.add(noteAtt);
        }
        return noteAtts;
    }

    /**
     * 清除缓存
     */
    public static void clearCache() {
        CatDbHelper.clearCats();
//		UserDbHelper.clearUsers();清缓存不清空user信息
        NoteDbHelper.clearNotes();
        TagDbHelper.clearTags();
        NoteAttrDbHelper.clearAttrs();
    }

    //获取老数据库未上传的笔记====================================================
    public static Vector<TNNote> getOldDbNotesByUserId(long userId) {
        Vector<Vector<String>> data = NoteDbHelper.getNoteListByOldDB(userId);
        return getNotesByOldDb(data);
    }

    public static Vector<TNNote> getNotesByOldDb(Vector<Vector<String>> data) {
        Vector<TNNote> notes = new Vector<TNNote>();
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                TNNote note = getNoteByOldDb(data.get(i));
                notes.add(note);
            }
        }
        return notes;
    }

    public static TNNote getNoteByOldDb(Vector<String> data) {
//		`noteLocalId`, `title`, `content`, `createTime`, `lastUpdate`, `lbsLongitude`, `lbsLatitude`, `lbsRadius`, `lbsAddress`
        TNNote note = TNNote.newNote();
        try {
            note.noteLocalId = Long.valueOf(data.get(0));
            note.title = data.get(1);

            note.content = TNUtilsHtml.codeHtmlContent(TNUtilsHtml.replaceBlank(data.get(2)), true);
            note.createTime = Integer.valueOf(data.get(3));
            note.lastUpdate = Integer.valueOf(data.get(4));
            note.lbsLongitude = Integer.valueOf(data.get(5));
            note.lbsLatitude = Integer.valueOf(data.get(6));
            note.lbsRadius = Integer.valueOf(data.get(7));
            note.lbsAddress = data.get(8);
        } catch (Exception e) {
            e.printStackTrace();
            //该异常只在TNMainAct的2-11-2的数据处理函数使用，这里只使用try catch 不处理即可 sjy
            MLog.e("TNDbUtils--getNoteByOldDb:" + e.toString());
        }
        Vector<TNNoteAtt> atts = getAttrsByNoteLocalIdByOldDb(note.noteLocalId);

        note.atts = atts;
        note.attCounts = atts.size();

        return note;
    }

    public static Vector<TNNoteAtt> getAttrsByNoteLocalIdByOldDb(long noteLocalId) {
        Vector<Vector<String>> data = NoteAttrDbHelper.getAttrsByNoteLocalIdByOldDb(noteLocalId);
        Vector<TNNoteAtt> noteAtts = new Vector<TNNoteAtt>();
        for (int i = 0; i < data.size(); i++) {
//			`attLocalId`, `attName`, `type`, `path`
            TNNoteAtt noteAtt = new TNNoteAtt();
            noteAtt.attLocalId = Long.valueOf(data.get(i).get(0));
            noteAtt.attName = data.get(i).get(1);
            noteAtt.type = Integer.valueOf(data.get(i).get(2));
            noteAtt.path = data.get(i).get(3);
            noteAtt.noteLocalId = noteLocalId;
            noteAtt.size = 0;
            noteAtt.syncState = 0;
            noteAtt.digest = "";
            noteAtt.attId = -1;
            noteAtt.width = 0;
            noteAtt.height = 0;

            noteAtts.add(noteAtt);
        }
        return noteAtts;
    }
}
