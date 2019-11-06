package com.thinkernote.ThinkerNote.db;

import java.util.Vector;

import org.json.JSONObject;

import com.thinkernote.ThinkerNote.db.Database.TNDb;
import com.thinkernote.ThinkerNote.db.Database.TNDb2;
import com.thinkernote.ThinkerNote.db.Database.TNSQLString;
import com.thinkernote.ThinkerNote.utils.TNUtils;

public class UserDbHelper {
	public static void addUser(Object username, Object password,
			Object userEmail, Object phone, Object userId, Object emailVerify, Object totalSpace, Object usedSpace) {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.USER_INSERT, username,
					password, userEmail, phone, userId, emailVerify, totalSpace, usedSpace);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}
	
	public static void updateUser(Object username, Object password,
			Object userEmail, Object phone, Object emailVerify, Object totalSpace, Object usedSpace, Object userId) {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.USER_UPDATE, username,
					password, userEmail, phone, emailVerify, totalSpace, usedSpace, userId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}

	public static void clearUsers() {
		TNDb.beginTransaction();
		try {
			TNDb.getInstance().execSQL(TNSQLString.USER_CLEAR);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
	}

	@SuppressWarnings("unchecked")
	public static Vector<Vector<String>> getUser(Object userId) {
		Vector<Vector<String>> s = new Vector<Vector<String>>();
		TNDb.beginTransaction();
		try {
			s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
					TNSQLString.USER_SELECT_BY_ID, userId);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
		return s;
	}
	
	@SuppressWarnings("unchecked")
	public static Vector<Vector<String>> getUserId(Object name) {
		Vector<Vector<String>> s = new Vector<Vector<String>>();
		TNDb.beginTransaction();
		try {
			s = (Vector<Vector<String>>) TNDb.getInstance().execSQL(
					TNSQLString.USER_CHECK_USERNAME, name);
			TNDb.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb.endTransaction();
		}
		return s;
	}

	public static boolean isUserExist(Object userId) {
		return getUser(userId).size() > 0;
	}

	public static void addUser(JSONObject user) {
		addUser(TNUtils.getFromJSON(user, "username"),
				TNUtils.getFromJSON(user, "password"),
				TNUtils.getFromJSON(user, "userEmail"),
				TNUtils.getFromJSON(user, "phone"),
				TNUtils.getFromJSON(user, "userId"),
				TNUtils.getFromJSON(user, "emailVerify"),
				TNUtils.getFromJSON(user, "totalSpace"),
				TNUtils.getFromJSON(user, "usedSpace"));
	}

	public static void addOrUpdateUser(JSONObject user) {
		Object userId = TNUtils.getFromJSON(user, "userId");
		if (isUserExist(userId)) {
			updateUser(TNUtils.getFromJSON(user, "username"),
					TNUtils.getFromJSON(user, "password"),
					TNUtils.getFromJSON(user, "userEmail"),
					TNUtils.getFromJSON(user, "phone"),
					TNUtils.getFromJSON(user, "emailVerify"),
					TNUtils.getFromJSON(user, "totalSpace"),
					TNUtils.getFromJSON(user, "usedSpace"),
					userId);
		} else {
			addUser(TNUtils.getFromJSON(user, "username"),
					TNUtils.getFromJSON(user, "password"),
					TNUtils.getFromJSON(user, "userEmail"),
					TNUtils.getFromJSON(user, "phone"),
					userId,
					TNUtils.getFromJSON(user, "emailVerify"),
					TNUtils.getFromJSON(user, "totalSpace"),
					TNUtils.getFromJSON(user, "usedSpace"));
		}
	}
	
	//获取老数据库的username=======================================================
	public static String getOldDbUserName(Object userId) {
		@SuppressWarnings("unchecked")
		Vector<Vector<String>> userNames = new Vector<Vector<String>>();
		TNDb2.beginTransaction();
		try {
			userNames = (Vector<Vector<String>>) TNDb2.getInstance().execSQL(
					TNSQLString.DLDDB_USERNAME_SELECT_BY_ID, userId);
			TNDb2.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb2.endTransaction();
		}

		return userNames.size() > 0 ? userNames.get(0).get(0) : "";
	}
	
	public static String getOldDbUserNameByAll() {
		@SuppressWarnings("unchecked")
		Vector<Vector<String>> userNames = new Vector<Vector<String>>();
		TNDb2.beginTransaction();
		try {
			userNames = (Vector<Vector<String>>) TNDb2.getInstance().execSQL(
					TNSQLString.DLDDB_USERNAME_SELECT_ALL);
			TNDb2.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TNDb2.endTransaction();
		}

		String userName = "";
		int size = userNames.size();
		for (int i = 0; i < size; i++) {
			if (size == 1) {
				userName += userNames.get(i).get(0);
			} else {
				if (i == size-1) {
					userName += userNames.get(i).get(0);
				} else {
					userName += userNames.get(i).get(0) + ",";
				}
			}
		}
		
		return userName;
	}
}
