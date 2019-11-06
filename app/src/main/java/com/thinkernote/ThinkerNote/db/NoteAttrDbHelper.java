package com.thinkernote.ThinkerNote.db;

import java.util.Vector;

import org.json.JSONObject;

import com.thinkernote.ThinkerNote.db.Database.TNDb;
import com.thinkernote.ThinkerNote.db.Database.TNDb2;
import com.thinkernote.ThinkerNote.db.Database.TNSQLString;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.TNUtils;

public class NoteAttrDbHelper {

	public static void addAttr(Object attName, Object type, Object path, Object noteLocalId, Object size, 
			Object syncState, Object digest, Object attId, Object width, Object height) {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.ATT_INSERT, attName, type, path, noteLocalId, size,
					syncState, digest, attId, width, height);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}
	
	public static void updateAttr(Object attName, Object type, Object path, Object noteLocalId, Object size, 
			 Object digest, Object width, Object height, Object attId) {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.ATT_UPDATE, attName,
					type, path, noteLocalId, size, digest, width, height, attId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}

	public static void clearAttrs() {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.ATT_CLEAR, TNSettings.getInstance().userId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}
	
	public static void deleteAttById(Object attId) {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.ATT_DELETE_ATT_ID, attId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}
	
	public static void deleteAttByAttLocalId(Object attLocalId) {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.ATT_DELETE_ATTLOCALID, attLocalId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}
	
	public static void clearAttrsByNoteId(Object noteLocalId) {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.ATT_DELETE_NOTE, noteLocalId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}

	@SuppressWarnings("unchecked")
	public static Vector<Vector<String>> getAttr(Object attId) {
		Vector<Vector<String>> s = new Vector<Vector<String>>();
		TNDb.beginTransaction();
		try {
			s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
					TNSQLString.ATT_SELECT_BYID, attId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
		return s;
	}
	
	public static boolean isAttExist(Object attId) {
		return getAttr(attId).size() > 0;
	}

	public static void addAttr(JSONObject attr) {
//		",`attName` TEXT(100) NOT NULL" +
//		",`type` INTEGER NOT NULL" +
//		",`path` TEXT(100) NOT NULL" +
//		",`noteId` LONG NOT NULL" +
//		",`size` INTEGER NOT NULL" +
//		",`syncState` INTEGER NOT NULL" +
//		",`digest` TEXT(32) NOT NULL" +
//		",`attId` LONG NOT NULL" +
//		",`width` INTEGER NOT NULL" +
//		",`height` INTEGER NOT NULL" +
		addAttr(TNUtils.getFromJSON(attr, "attName"),
				TNUtils.getFromJSON(attr, "type"),
				TNUtils.getFromJSON(attr, "path"),
				TNUtils.getFromJSON(attr, "noteLocalId"),
				TNUtils.getFromJSON(attr, "size"),
				TNUtils.getFromJSON(attr, "syncState"),
				TNUtils.getFromJSON(attr, "digest"),
				TNUtils.getFromJSON(attr, "attId"),
				TNUtils.getFromJSON(attr, "width"),
				TNUtils.getFromJSON(attr, "height"));
	}

	public static void addOrUpdateAttr(JSONObject attr) {
		Object attId = TNUtils.getFromJSON(attr, "attId");
		if (isAttExist(attId)) {
//			Object attName, Object type,
//			Object size, Object digest, Object attId
			updateAttr(TNUtils.getFromJSON(attr, "attName"),
					TNUtils.getFromJSON(attr, "type"),
					TNUtils.getFromJSON(attr, "path"),
					TNUtils.getFromJSON(attr, "noteLocalId"),
					TNUtils.getFromJSON(attr, "size"),
					TNUtils.getFromJSON(attr, "digest"),
					TNUtils.getFromJSON(attr, "width"),
					TNUtils.getFromJSON(attr, "height"),
					attId);
		} else {
			addAttr(TNUtils.getFromJSON(attr, "attName"),
					TNUtils.getFromJSON(attr, "type"),
					TNUtils.getFromJSON(attr, "path"),
					TNUtils.getFromJSON(attr, "noteLocalId"),
					TNUtils.getFromJSON(attr, "size"),
					TNUtils.getFromJSON(attr, "syncState"),
					TNUtils.getFromJSON(attr, "digest"),
					attId,
					TNUtils.getFromJSON(attr, "width"),
					TNUtils.getFromJSON(attr, "height"));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static  Vector<Vector<String>> getAttrsByNoteLocalId(Object noteId) {
		Vector<Vector<String>> s = new Vector<Vector<String>>();
		TNDb.beginTransaction();
		try {
			s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
					TNSQLString.ATT_SELECT_BY_NOTEID, noteId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
		return s;
	}
	
	//操作老数据库的方法================================================
	@SuppressWarnings("unchecked")
	public static  Vector<Vector<String>> getAttrsByNoteLocalIdByOldDb(Object noteLocalId) {
		Vector<Vector<String>> s = new Vector<Vector<String>>();
		TNDb2.beginTransaction();
		try {
			s = (Vector<Vector<String>>) TNDb2.getInstance().execSQL(
					TNSQLString.ATT_SELECT_BY_NOTEID_BY_OLD_DB, noteLocalId);
			TNDb2.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb2.endTransaction();
		}
		return s;
	}
}
