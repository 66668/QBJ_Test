package com.thinkernote.ThinkerNote.DBHelper;

import java.util.Vector;

import org.json.JSONObject;

import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.base.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;

public class CatDbHelper {
	public static void addCat(Object catName, Object userId,
			Object trash, Object catId, Object noteCounts, Object catCounts,
		  	Object deep, Object pCatId, Object isNew, Object createTime, Object lastUpdateTime, Object strIndex) {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.CAT_INSERT, catName,
					userId, trash, catId, noteCounts, catCounts,
					deep, pCatId, isNew, createTime, lastUpdateTime, strIndex);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}
	
	public static void updateCat(Object catName, Object userId,
			Object trash, Object catId, Object noteCounts, Object catCounts, Object deep,
		 	Object pCatId, Object isNew, Object createTime, Object lastUpdateTime, Object strIndex) {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE, catName,
					userId, trash, catId, noteCounts, catCounts, deep,
					pCatId, isNew, createTime, lastUpdateTime, strIndex, catId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}

	public static void clearCats() {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.CAT_CLEAR, TNSettings.getInstance().userId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}
	
	public static void clearCatsByParentId(Object pCatId) {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.CAT_CLEAR_PCATID, TNSettings.getInstance().userId, pCatId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}

	@SuppressWarnings("unchecked")
	public static Vector<Vector<String>> getCat(Object catId) {
		Vector<Vector<String>> s = new Vector<Vector<String>>();
		TNDb.beginTransaction();
		try {
			s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
					TNSQLString.CAT_CHECK_CATID, catId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
		return s;
	}
	
	public static String getCatCountByCatId(Object catId) {
		@SuppressWarnings("unchecked")
		Vector<Vector<String>> s = new Vector<Vector<String>>();
		TNDb.beginTransaction();
		try {
			s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
					TNSQLString.CAT_COUNT_PCATID, catId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
		return s.size() > 0 ? s.get(0).get(0) : "0";
	}
	
	public static boolean isCatExist(Object catId) {
		return getCat(catId).size() > 0;
	}

	public static void addCat(JSONObject cat) {
		addCat(TNUtils.getFromJSON(cat, "catName"),
				TNUtils.getFromJSON(cat, "userId"),
				TNUtils.getFromJSON(cat, "trash"),
				TNUtils.getFromJSON(cat, "catId"),
				TNUtils.getFromJSON(cat, "noteCounts"),
				TNUtils.getFromJSON(cat, "catCounts"),
				TNUtils.getFromJSON(cat, "deep"),
				TNUtils.getFromJSON(cat, "pCatId"),
				TNUtils.getFromJSON(cat, "isNew"),
				TNUtils.getFromJSON(cat, "createTime"),
				TNUtils.getFromJSON(cat, "lastUpdateTime"),
				TNUtils.getFromJSON(cat, "strIndex"));
	}

	public static void addOrUpdateCat(JSONObject cat) {
		Object catId = TNUtils.getFromJSON(cat, "catId");
		if (isCatExist(catId)) {
			updateCat(TNUtils.getFromJSON(cat, "catName"),
					TNUtils.getFromJSON(cat, "userId"),
					TNUtils.getFromJSON(cat, "trash"),
					catId,
					TNUtils.getFromJSON(cat, "noteCounts"),
					TNUtils.getFromJSON(cat, "catCounts"),
					TNUtils.getFromJSON(cat, "deep"),
					TNUtils.getFromJSON(cat, "pCatId"),
					TNUtils.getFromJSON(cat, "isNew"),
					TNUtils.getFromJSON(cat, "createTime"),
					TNUtils.getFromJSON(cat, "lastUpdateTime"),
					TNUtils.getFromJSON(cat, "strIndex"));
		} else {
			addCat(TNUtils.getFromJSON(cat, "catName"),
					TNUtils.getFromJSON(cat, "userId"),
					TNUtils.getFromJSON(cat, "trash"),
					catId,
					TNUtils.getFromJSON(cat, "noteCounts"),
					TNUtils.getFromJSON(cat, "catCounts"),
					TNUtils.getFromJSON(cat, "deep"),
					TNUtils.getFromJSON(cat, "pCatId"),
					TNUtils.getFromJSON(cat, "isNew"),
					TNUtils.getFromJSON(cat, "createTime"),
					TNUtils.getFromJSON(cat, "lastUpdateTime"),
					TNUtils.getFromJSON(cat, "strIndex"));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static  Vector<Vector<String>> getAllCatList(Object userId) {
		Vector<Vector<String>> s = new Vector<Vector<String>>();
		TNDb.beginTransaction();
		try {
			s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
					TNSQLString.CAT_SELECT_ALL, userId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
		return s;
	}
	
	@SuppressWarnings("unchecked")
	public static  Vector<Vector<String>> getCatsByCatId(Object userId, Object catId) {
		String sql = TNSQLString.CAT_SELECT_BY_PCATID_UPDATETIME;
		if (TNSettings.getInstance().sort.equals(TNConst.CREATETIME)) {
			sql = TNSQLString.CAT_SELECT_BY_PCATID_CREATETIME;
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
	}
}
