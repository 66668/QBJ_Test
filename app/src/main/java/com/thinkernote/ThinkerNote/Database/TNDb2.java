package com.thinkernote.ThinkerNote.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNActionResult;
import com.thinkernote.ThinkerNote.General.TNActionType2;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.Utils.MLog;

import java.util.Vector;

public class TNDb2 extends SQLiteOpenHelper {
    private static final String TAG = "TNDatabase";
    private static TNDb2 singleton = null;
    private final static int DB_VER = 27;
    private final static String DB_NAME = "ThinkerNote.db";
    private SQLiteDatabase db = null;

    private int changeBits;
    public static int DB_NOTES_CHANGED = 1; // 必需要重读数据库并刷新界面
    public static int DB_CATS_CHANGED = 2;
    public static int DB_TAGS_CHANGED = 4;
    public static int DB_USER_CHANGED = 8;
    public static int DB_PROJECTS_CHANGED = 16;
    public static int DB_COMMENT_CHANGED = 32;
    public static int DB_UNREADNOTE_CHANGED = 64;

    public TNDb2() {
        super(TNUtils.getAppContext(), DB_NAME, null, DB_VER);

        db = getWritableDatabase();

        TNAction.regRunner(TNActionType2.Db_Execute, this, "executeSQL");
        TNAction.regRunner(TNActionType2.DBReset, this, "DBReset");
    }

    public static TNDb2 getInstance() {
        if (singleton == null) {
            synchronized (TNDb.class) {
                if (singleton == null) {
                    singleton = new TNDb2();
                }
            }
        }

        return singleton;
    }

    @Override
    public void onCreate(SQLiteDatabase aDB) {
        MLog.d(TAG, "onCreate");
        aDB.execSQL(TNSQLString2.SETTING_CREATE_TABLE);
        aDB.execSQL(TNSQLString2.USER_CREATE_TABLE);
        aDB.execSQL(TNSQLString2.CAT_CREATE_TABLE);
        aDB.execSQL(TNSQLString2.TAG_CREATE_TABLE);
        aDB.execSQL(TNSQLString2.NOTE_CREATE_TABLE);
        aDB.execSQL(TNSQLString2.NOTETAG_CREATE_TABLE);
        aDB.execSQL(TNSQLString2.ATT_CREATE_TABLE);
        aDB.execSQL(TNSQLString2.BINDING_CREATE_TABLE);
        aDB.execSQL(TNSQLString2.PROJECT_CREATE_TABLE);
        aDB.execSQL(TNSQLString2.COMMENT_CREATE_TABLE_NEW);
        aDB.execSQL(TNSQLString2.UNREADNOTE_CREATE_TABLE_NEW);
        aDB.execSQL(TNSQLString2.NOTESHARE_CREATE_NEWTABLE);

        // remove all att files
        TNUtilsAtt.deleteAllAtts();
        TNUtilsAtt.createNomedia();
    }

    @Override
    public void onUpgrade(SQLiteDatabase aDB, int oldVer, int newVer) {

    }

    private void setShortContentToDB(SQLiteDatabase aDB) {
        Cursor cursor = aDB.rawQuery(TNSQLString2.NOTE_GET_ALL, null);
        MLog.i(TAG, "setShortContentToDB" + cursor.getCount());

        while (cursor.moveToNext()) {
            long noteLocalId = cursor.getLong(0);
            String content = cursor.getString(1);
            Object args[] = new Object[2];
            args[0] = TNUtils.getBriefContent(content);
            args[1] = noteLocalId;
            aDB.execSQL(TNSQLString2.NOTE_UPDATE_SHORTCONTENT, args);
            MLog.i(TAG, noteLocalId + "," + args[0]);
        }

        cursor.close();
    }

    private void setPingYingIndexToDB(SQLiteDatabase aDB) {
        Cursor cursor = aDB.rawQuery(TNSQLString2.NOTE_GET_ALL, null);
//        MLog.i(TAG, "setPingYingIndexToDB" + cursor.getCount());
        while (cursor.moveToNext()) {
            long noteLocalId = cursor.getLong(0);
            String title = cursor.getString(2);
            Object args[] = new Object[2];
            args[0] = TNUtils.getPingYinIndex(title);
            args[1] = noteLocalId;
            aDB.execSQL(TNSQLString2.NOTE_UPDATE_PINGYININDEX, args);
//            MLog.i(TAG, noteLocalId + "," + args[0]);
        }

        cursor.close();
    }

    private void setStrIndexToTagDB(SQLiteDatabase aDB) {
        Cursor cursor = aDB.rawQuery(TNSQLString2.TAG_GET_ALL, null);
//        MLog.i(TAG, "setStrIndexToTagDB:" + cursor.getCount());
        while (cursor.moveToNext()) {
            long tagLocalId = cursor.getLong(0);
            String tagName = cursor.getString(1);
            Object args[] = new Object[2];
            args[0] = TNUtils.getPingYinIndex(tagName);
            args[1] = tagLocalId;
            aDB.execSQL(TNSQLString2.TAG_UPDATA_INDEX, args);
//            MLog.i(TAG, tagLocalId + "," + args[0]);
        }

        cursor.close();
    }

    private long insert(String sql, String[] args) {
        int start = 0, end = 0;
        String tableName = "";
        ContentValues values = new ContentValues();

        start = sql.indexOf("`");
        end = sql.indexOf("`", start + 1);
        tableName = sql.substring(start, end + 1);

        for (int i = 0; i < args.length; i++) {
            start = sql.indexOf("`", end + 1);
            end = sql.indexOf("`", start + 1);
            values.put(sql.substring(start, end + 1), args[i]);
        }
        return db.insertOrThrow(tableName, null, values);
    }

    private Vector<Vector<String>> select(String sql, String[] args) {
        Cursor cursor = db.rawQuery(sql, args);

        Vector<Vector<String>> allData = new Vector<Vector<String>>();
        while (cursor.moveToNext()) {
            Vector<String> rowData = new Vector<String>();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                String value = cursor.getString(i);
                if (value != null)
                    rowData.add(value);
                else
                    rowData.add("0");
            }
            allData.add(rowData);
        }
        cursor.close();
//        MLog.d(TAG, allData.toString());

        return allData;
    }

    private void execute(String sql, String[] args) {
        db.execSQL(sql, args);
    }

    public Object execSQL(String sql, Object... args) {
        String[] valus = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            valus[i] = String.valueOf(args[i]);
        }
        printSql(sql, valus);
        if (sql.startsWith("SELECT")) {
            return select(sql, valus);
        } else if (sql.startsWith("INSERT")) {
            return insert(sql, valus);
        } else {
            execute(sql, valus);
        }

        return null;
    }

    public void executeSQL(TNAction aAction) {
//        MLog.d(TAG, aAction.inputs.toString());
        try {
            String sql = (String) aAction.inputs.get(0);
            if (sql.startsWith("SELECT")) {
                String[] args = new String[aAction.inputs.size() - 1];
                for (int i = 1; i < aAction.inputs.size(); i++) {
                    args[i - 1] = aAction.inputs.get(i).toString();
                }
                Cursor cursor = db.rawQuery(sql, args);

                Vector<Vector<String>> allData = new Vector<Vector<String>>();
                while (cursor.moveToNext()) {
                    Vector<String> rowData = new Vector<String>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        String value = cursor.getString(i);
                        if (value != null)
                            rowData.add(value);
                        else
                            rowData.add("0");
                    }
                    allData.add(rowData);
                }
                aAction.outputs.add(allData);
                cursor.close();
            } else if (sql.startsWith("INSERT")) {
                int start = 0, end = 0;
                String tableName = "";
                ContentValues values = new ContentValues();

                start = sql.indexOf("`");
                end = sql.indexOf("`", start + 1);
                tableName = sql.substring(start, end + 1);
                //Log.i(TAG, "tableName:" + tableName + start + end);

                for (int i = 1; i < aAction.inputs.size(); i++) {
                    start = sql.indexOf("`", end + 1);
                    end = sql.indexOf("`", start + 1);
                    values.put(sql.substring(start, end + 1), aAction.inputs.get(i).toString());
                }
                //Log.i(TAG, "values:" + values);
                long id = db.insertOrThrow(tableName, null, values);
                aAction.outputs.add(id);
            } else {
                Object[] args = new Object[aAction.inputs.size() - 1];
                for (int i = 1; i < aAction.inputs.size(); i++) {
                    args[i - 1] = aAction.inputs.get(i);
                }
                db.execSQL(sql, args);

            }
            aAction.result = TNActionResult.Finished;
        } catch (SQLiteException e) {
            e.printStackTrace();
            MLog.e("数据库2异常："+e.toString());
        }
    }

    public static void beginTransaction() {
        getInstance().db.beginTransaction();
    }

    public static void setTransactionSuccessful() {
        getInstance().db.setTransactionSuccessful();
    }

    public static void endTransaction() {
        getInstance().db.endTransaction();
    }

    public static String getData(TNAction aAction, int row, int col) {
        @SuppressWarnings("unchecked")
        Vector<Vector<String>> allData = (Vector<Vector<String>>) aAction.outputs.get(0);
        return allData.get(row).get(col);
    }

    public static int getSize(TNAction aAction) {
        @SuppressWarnings("unchecked")
        Vector<Vector<String>> allData = (Vector<Vector<String>>) aAction.outputs.get(0);
        return allData.size();
    }

    public static boolean isChanges(int aChange) {
        return (getInstance().changeBits & aChange) != 0;
    }

    public static void addChange(int aChange) {
        if ((getInstance().changeBits & aChange) == 0)
            getInstance().changeBits += aChange;
    }

    public static void removeChange(int aChange) {
        if ((getInstance().changeBits & aChange) != 0)
            getInstance().changeBits -= aChange;
    }

    public void DBReset(TNAction aAction) {
        beginTransaction();
        try {
            //drop tables
            getInstance().db.execSQL(TNSQLString2.SETTING_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.USER_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.CAT_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.TAG_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.NOTE_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.NOTETAG_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.ATT_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.BINDING_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.PROJECT_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.COMMENT_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.UNREADNOTE_DROP_TABLE);

            //create tables
            getInstance().db.execSQL(TNSQLString2.SETTING_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.USER_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.CAT_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.TAG_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.NOTE_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.NOTETAG_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.ATT_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.BINDING_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.PROJECT_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.COMMENT_CREATE_TABLE_NEW);
            getInstance().db.execSQL(TNSQLString2.UNREADNOTE_CREATE_TABLE_NEW);

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    /**
     * 新方式 sjy 0620
     */
    public void DBReset() {
        beginTransaction();
        try {
            //drop tables
            getInstance().db.execSQL(TNSQLString2.SETTING_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.USER_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.CAT_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.TAG_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.NOTE_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.NOTETAG_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.ATT_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.BINDING_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.PROJECT_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.COMMENT_DROP_TABLE);
            getInstance().db.execSQL(TNSQLString2.UNREADNOTE_DROP_TABLE);

            //create tables
            getInstance().db.execSQL(TNSQLString2.SETTING_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.USER_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.CAT_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.TAG_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.NOTE_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.NOTETAG_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.ATT_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.BINDING_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.PROJECT_CREATE_TABLE);
            getInstance().db.execSQL(TNSQLString2.COMMENT_CREATE_TABLE_NEW);
            getInstance().db.execSQL(TNSQLString2.UNREADNOTE_CREATE_TABLE_NEW);

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    private void printSql(String sql, String[] args) {
        String values = "";
        for (String arg : args) {
            arg = "`" + arg + "` ";
            values = values + arg;
        }
//        MLog.d(TAG, sql + "\r\n" + values);
    }
}
