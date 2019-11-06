package com.thinkernote.ThinkerNote.db;

import java.util.Vector;

import org.json.JSONObject;

import com.thinkernote.ThinkerNote.db.Database.TNDb;
import com.thinkernote.ThinkerNote.db.Database.TNSQLString;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.TNUtils;

public class TagDbHelper {

	public static void addTag(Object tagName, Object userId,
			Object trash, Object tagId, Object noteCount, Object strIndex) {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.TAG_INSERT, tagName,
					userId, trash, tagId, noteCount, strIndex);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}
	
	public static void updateTag(Object tagName, Object userId,
			Object trash, Object tagId, Object noteCount, Object strIndex) {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.TAG_UPDATE, tagName,
					userId, trash, tagId, noteCount, strIndex, tagId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}

	public static void clearTags() {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.TAG_CLEAR, TNSettings.getInstance().userId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}
	
	public static void deleteTag(Object tagId) {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.TAG_REAL_DELETE, tagId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}

	@SuppressWarnings("unchecked")
	public static Vector<Vector<String>> getTag(Object tagId) {
		Vector<Vector<String>> s = new Vector<Vector<String>>();
		TNDb.beginTransaction();
		try {
			s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
					TNSQLString.TAG_GET, tagId, TNSettings.getInstance().userId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
		return s;
	}
	
	@SuppressWarnings("unchecked")
	public static Vector<Vector<String>> getTagByText(Object text) {
		Vector<Vector<String>> s = new Vector<Vector<String>>();
		TNDb.beginTransaction();
		try {
			s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
					TNSQLString.TAG_GET_BY_TAGNAME, text, TNSettings.getInstance().userId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
		return s;
	}

	public static boolean isTagExist(Object tagId) {
		return getTag(tagId).size() > 0;
	}

	public static void addTag(JSONObject tag) {
//		`tagName`, `userId`, `trash`, `tagId`, `revision`, `strIndex`
		addTag(TNUtils.getFromJSON(tag, "tagName"),
				TNUtils.getFromJSON(tag, "userId"),
				TNUtils.getFromJSON(tag, "trash"),
				TNUtils.getFromJSON(tag, "tagId"),
				TNUtils.getFromJSON(tag, "count"),
				TNUtils.getFromJSON(tag, "strIndex"));
	}

	public static void addOrUpdateTag(JSONObject tag) {
		Object tagId = TNUtils.getFromJSON(tag, "tagId");
		if (isTagExist(tagId)) {
			updateTag(TNUtils.getFromJSON(tag, "tagName"),
					TNUtils.getFromJSON(tag, "userId"),
					TNUtils.getFromJSON(tag, "trash"),
					tagId,
					TNUtils.getFromJSON(tag, "count"),
					TNUtils.getFromJSON(tag, "strIndex"));
		} else {
			addTag(TNUtils.getFromJSON(tag, "tagName"),
					TNUtils.getFromJSON(tag, "userId"),
					TNUtils.getFromJSON(tag, "trash"),
					tagId,
					TNUtils.getFromJSON(tag, "count"),
					TNUtils.getFromJSON(tag, "strIndex"));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Vector<Vector<String>> getTagList(Object useId) {
		Vector<Vector<String>> s = new Vector<Vector<String>>();
		TNDb.beginTransaction();
		try {
			s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
					TNSQLString.TAG_GET_ALL, useId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
		return s;
	}
}
