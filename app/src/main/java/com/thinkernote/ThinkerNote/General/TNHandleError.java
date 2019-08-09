package com.thinkernote.ThinkerNote.General;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNActionResult;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.Views.CommonDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

/**
 * 错误的返回处理
 */
public class TNHandleError {
    private static final String TAG = "TNHandleError";
    private static Activity currentAct = null;
    private static boolean isAlert = true;

    public static boolean handleResult(Activity aAct, TNAction aAction, boolean aAlert) {
        if (aAction.result != TNActionResult.Failed && aAction.result != TNActionResult.Cancelled) {
            return false;
        }

        currentAct = aAct;
        isAlert = aAlert;
        String errCode = (String) aAction.outputs.get(0);

        if ("login required".equals(errCode)) {
            TNUtilsUi.showToast(errCode);
            TNUtils.goToLogin(aAct.getApplicationContext());
            return true;
        }

        if ("笔记不存在".equals(errCode)) {
            try {
                long noteId = (Long) aAction.inputs.get(0);
                TNDb.getInstance().execSQL(TNSQLString.NOTE_DELETE_BY_NOTEID, noteId);
            } catch (Exception e) {
                return true;
            }
        }

        handle_NormalError(errCode);

        if ("标签不存在".equals(errCode)) {
            try {
                long tagId = (Long) aAction.inputs.get(0);
                TNDb.getInstance().execSQL(TNSQLString.TAG_REAL_DELETE, tagId);
            } catch (Exception e) {
                return true;
            }
        }

        if ("文件夹不存在".equals(errCode)) {
            try {
                long folderId = (Long) aAction.inputs.get(0);
                TNDb.getInstance().execSQL(TNSQLString.CAT_DELETE_CAT, folderId);
            } catch (Exception e) {
                return true;
            }
        }

        return true;
    }

    public static boolean handleResult(Activity aAct, TNAction aAction) {
        return handleResult(aAct, aAction, true);
    }

    public static void handleErrorCode(Activity aAct, String aCode, boolean aAlert) {
        currentAct = aAct;
        isAlert = aAlert;

        handle_NormalError(aCode);

    }

    public static void handleErrorCode(Activity aAct, String aCode) {
        handleErrorCode(aAct, aCode, false);
    }

    private static void showAlertMsg(String aMsg) {
        if (isAlert && !currentAct.isFinishing())
            TNUtilsUi.alert(currentAct, aMsg);
        else
            TNUtilsUi.showToast(aMsg);
    }

    // ErrorCode handle
    public static void handle_NormalError(String aCode) {
        showAlertMsg(aCode);
    }

    public static void handle_Protocol_Mismatch(TNAction aAction) {
        CommonDialog dialog = new CommonDialog(currentAct, R.string.alert_Protocol_Mismatch,
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                    }

                    @Override
                    public void cancelBack() {
                    }

                });
        dialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                System.exit(0);
            }
        });
        dialog.show();
    }

}
