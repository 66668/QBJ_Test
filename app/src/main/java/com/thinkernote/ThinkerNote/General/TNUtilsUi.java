package com.thinkernote.ThinkerNote.General;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.thinkernote.ThinkerNote.Action.TNAction.TNRunner;
import com.thinkernote.ThinkerNote.BuildConfig;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.base.TNApplication;

import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TNUtilsUi {
    private static final String TAG = "TNUtilsUi";

    public static View addListHelpInfoFootView(Activity activity, ListView lv,
                                               String title, String info) {
        LayoutInflater layoutInflater = (LayoutInflater) (activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        LinearLayout v = (LinearLayout) layoutInflater.inflate(
                R.layout.listfootview_helpinfo, null);
        ((TextView) v.findViewById(R.id.listfootview_title)).setText(title);
        ((TextView) v.findViewById(R.id.listfootview_info)).setText(info);
        lv.addFooterView(v, null, false);
        return ((LinearLayout) v.findViewById(R.id.listfootview_loading));
    }

    public static String getFootViewTitle(Activity act, int mListType) {
        if (mListType == 1) {// 全部笔记
            return act.getString(R.string.listfootview_title_notelist_allnote);
        } else if (mListType == 3) {// 回收站
            return act.getString(R.string.listfootview_title_notelist_recycle);
        } else if (mListType == 4) {// 标签
            return act.getString(R.string.listfootview_title_notelist_tagnote);
        } else if (mListType == 5) {// 搜索结果
            return act.getString(R.string.listfootview_title_notelist_searchresult);
        } else if (mListType == 6) {
            return act.getString(R.string.listfootview_title_notelist_mypublicnote);
        } else if (mListType == 7) {
            return act.getString(R.string.listfootview_title_tags);
        } else
            return act.getString(R.string.listfootview_title_notelist_allnote);
    }

    public static String getFootViewInfo(Activity act, int mListType) {
        if (mListType == 1) {// 全部笔记
            return act.getString(R.string.listfootview_info_notelist_allnote);
        } else if (mListType == 3) {// 回收站
            return act.getString(R.string.listfootview_info_notelist_recycle);
        } else if (mListType == 4) {// 标签
            return act.getString(R.string.listfootview_info_notelist_tagnote);
        } else if (mListType == 5) {// 搜索结果
            return act.getString(R.string.listfootview_info_notelist_searchresult);
        } else if (mListType == 6) {
            return act.getString(R.string.listfootview_info_notelist_mypublicnote);
        } else if (mListType == 7) {
            return act.getString(R.string.listfootview_info_tags);
        } else
            return act.getString(R.string.listfootview_info_notelist_allnote);
    }

    public static long getMonth(long milliseconds) {
        Calendar c = Calendar.getInstance(Locale.CHINA);
        c.setTimeInMillis(milliseconds);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        c.set(year, month, 0, 0, 0, 0);
        return c.getTimeInMillis() / 1000;
    }

    public static ProgressDialog progressDialog(Context aContext, int msgId) {
        ProgressDialog dialog = new ProgressDialog(aContext);
        dialog.setTitle("");
        dialog.setIndeterminate(true);
        dialog.setMessage(TNUtils.getAppContext().getString(msgId));
        dialog.setCancelable(false);
        dialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH)// Search键
                {
                    return true;
                    // 如果true，说明此事件已被处理，将不会冒泡提交OnDismissListener ，这时将不会看到google
                    // search 界面
                    // return false;
                    // 如果是这样，如果注册的有OnDismissListener ，可以看到onDismiss事件会被触发
                }
                return false;
            }
        });
        return dialog;
    }

    public static void alert(Context context, Object str) {
        if (((Activity) context).isFinishing())
            return;
        JSONObject jsonData = TNUtils.makeJSON("CONTEXT", context, "TITLE",
                R.string.alert_Title, "MESSAGE", str, "POS_BTN",
                R.string.alert_OK);
        TNUtilsUi.alertDialogBuilder(jsonData).show();
    }

    public static void alert(final Activity act, Object str,
                             final boolean finish, boolean canCancel) {
        if (act.isFinishing())
            return;

        DialogInterface.OnClickListener pbtn_Click = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (finish) {
                    act.finish();
                }
            }
        };
        JSONObject jsonData = TNUtils.makeJSON("CONTEXT", act, "TITLE",
                R.string.alert_Title, "MESSAGE", str, "POS_BTN",
                R.string.alert_OK, "POS_BTN_CLICK", pbtn_Click);
        AlertDialog ad = TNUtilsUi.alertDialogBuilder(jsonData);
        ad.show();
        if (!canCancel) {
            ad.setCancelable(false);
            ad.setCanceledOnTouchOutside(false);
        }
    }

    public static AlertDialog.Builder alertDialogBuilder1(JSONObject aJson) {
        Context context = (Context) TNUtils.getFromJSON(aJson, "CONTEXT");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // 如果context已有Alert，则不成功，暂未处理

        // Title
        Object title = TNUtils.getFromJSON(aJson, "TITLE");

        View v;
        if (View.class.isInstance(title)) {
            v = (View) title;
        } else {
            LayoutInflater lf = LayoutInflater
                    .from(TNSettings.getInstance().appContext);
            v = lf.inflate(R.layout.dialog, null);
        }

        TNUtilsSkin.setViewBackground((Activity) context, v,
                R.id.dialog_layout, R.drawable.page_color);
        TNUtilsSkin.setViewBackground((Activity) context, v,
                R.id.dialog_top_bar, R.drawable.dialog_top_bg);
        TNUtilsSkin.setImageViewDrawable((Activity) context, v,
                R.id.dialog_icon, R.drawable.dialog_icon);

        builder.setCustomTitle(v);
        ((TextView) v.findViewById(R.id.dialog_title))
                .setText(R.string.alert_Title);

        Object msg = TNUtils.getFromJSON(aJson, "MESSAGE");
        if (msg == null)
            v.findViewById(R.id.dialog_msg).setVisibility(View.GONE);
        else if (Integer.class.isInstance(msg))
            ((TextView) v.findViewById(R.id.dialog_msg)).setText((Integer) msg);
        else if (CharSequence.class.isInstance(msg))
            ((TextView) v.findViewById(R.id.dialog_msg))
                    .setText((CharSequence) msg);
        // View
        Object view = TNUtils.getFromJSON(aJson, "VIEW");
        if (View.class.isInstance(view))
            builder.setView((View) view);

        // PositiveButton
        OnClickListener posListener = (OnClickListener) TNUtils.getFromJSON(
                aJson, "POS_BTN_CLICK");
        Object posBtn = TNUtils.getFromJSON(aJson, "POS_BTN");
        if (Integer.class.isInstance(posBtn)) {
            builder.setPositiveButton((Integer) posBtn, posListener);
        } else if (CharSequence.class.isInstance(posBtn)) {
            builder.setPositiveButton((CharSequence) posBtn, posListener);
        }

        // NeutralButton
        OnClickListener neuListener = (OnClickListener) TNUtils.getFromJSON(
                aJson, "NEU_BTN_CLICK");
        Object neuBtn = TNUtils.getFromJSON(aJson, "NEU_BTN");
        if (Integer.class.isInstance(neuBtn)) {
            builder.setNeutralButton((Integer) neuBtn, neuListener);
        } else if (CharSequence.class.isInstance(neuBtn)) {
            builder.setNeutralButton((CharSequence) neuBtn, neuListener);
        }

        // NegativeButton
        OnClickListener negListener = (OnClickListener) TNUtils.getFromJSON(
                aJson, "NEG_BTN_CLICK");
        Object negBtn = TNUtils.getFromJSON(aJson, "NEG_BTN");
        if (Integer.class.isInstance(negBtn)) {
            builder.setNegativeButton((Integer) negBtn, negListener);
        } else if (CharSequence.class.isInstance(negBtn)) {
            builder.setNegativeButton((CharSequence) negBtn, negListener);
        }

        return builder;
    }

    //TODO 可修改优化 delete
    public static AlertDialog alertDialogBuilder(JSONObject aJson) {
        Context context = (Context) TNUtils.getFromJSON(aJson, "CONTEXT");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // 如果context已有Alert，则不成功，暂未处理

        // Title
        Object title = TNUtils.getFromJSON(aJson, "TITLE");

        View v;
        if (View.class.isInstance(title)) {
            v = (View) title;
        } else {
            LayoutInflater lf = LayoutInflater
                    .from(TNSettings.getInstance().appContext);
            v = lf.inflate(R.layout.dialog, null);
        }

        TNUtilsSkin.setViewBackground((Activity) context, v, R.id.dialog_layout, R.drawable.page_color);
        TNUtilsSkin.setViewBackground((Activity) context, v, R.id.dialog_top_bar, R.drawable.dialog_top_bg);
        TNUtilsSkin.setImageViewDrawable((Activity) context, v, R.id.dialog_icon, R.drawable.dialog_icon);

        builder.setCustomTitle(v);
        ((TextView) v.findViewById(R.id.dialog_title))
                .setText(R.string.alert_Title);

        Object msg = TNUtils.getFromJSON(aJson, "MESSAGE");
        if (msg == null)
            v.findViewById(R.id.dialog_msg).setVisibility(View.GONE);
        else if (Integer.class.isInstance(msg))
            ((TextView) v.findViewById(R.id.dialog_msg)).setText((Integer) msg);
        else if (CharSequence.class.isInstance(msg))
            ((TextView) v.findViewById(R.id.dialog_msg))
                    .setText((CharSequence) msg);
        // View
        Object view = TNUtils.getFromJSON(aJson, "VIEW");
        if (View.class.isInstance(view))
            builder.setView((View) view);

        // PositiveButton
        OnClickListener posListener = (OnClickListener) TNUtils.getFromJSON(
                aJson, "POS_BTN_CLICK");
        Object posBtn = TNUtils.getFromJSON(aJson, "POS_BTN");
        if (Integer.class.isInstance(posBtn)) {
            builder.setPositiveButton((Integer) posBtn, posListener);
        } else if (CharSequence.class.isInstance(posBtn)) {
            builder.setPositiveButton((CharSequence) posBtn, posListener);
        }

        // NeutralButton
        OnClickListener neuListener = (OnClickListener) TNUtils.getFromJSON(
                aJson, "NEU_BTN_CLICK");
        Object neuBtn = TNUtils.getFromJSON(aJson, "NEU_BTN");
        if (Integer.class.isInstance(neuBtn)) {
            builder.setNeutralButton((Integer) neuBtn, neuListener);
        } else if (CharSequence.class.isInstance(neuBtn)) {
            builder.setNeutralButton((CharSequence) neuBtn, neuListener);
        }

        // NegativeButton
        OnClickListener negListener = (OnClickListener) TNUtils.getFromJSON(
                aJson, "NEG_BTN_CLICK");
        Object negBtn = TNUtils.getFromJSON(aJson, "NEG_BTN");
        if (Integer.class.isInstance(negBtn)) {
            builder.setNegativeButton((Integer) negBtn, negListener);
        } else if (CharSequence.class.isInstance(negBtn)) {
            builder.setNegativeButton((CharSequence) negBtn, negListener);
        }
        AlertDialog ad = builder.create();
        TNRunner run = new TNRunner(context, "addDialog", AlertDialog.class);
        run.run(ad);
        return ad;
    }

    public static void showToast(Object msg) {
        if (Integer.class.isInstance(msg)) {
            Toast t1 = Toast.makeText(TNUtils.getAppContext(), (Integer) msg,
                    Toast.LENGTH_LONG);
            t1.show();
        } else if (String.class.isInstance(msg)) {
            Toast t1 = Toast.makeText(TNUtils.getAppContext(), (String) msg,
                    Toast.LENGTH_LONG);
            t1.show();
        }
    }

    public static void showShortToast(Object msg) {
        if (Integer.class.isInstance(msg)) {
            Toast t1 = Toast.makeText(TNUtils.getAppContext(), (Integer) msg,
                    Toast.LENGTH_SHORT);
            t1.show();
        } else if (String.class.isInstance(msg)) {
            Toast t1 = Toast.makeText(TNUtils.getAppContext(), (String) msg,
                    Toast.LENGTH_SHORT);
            t1.show();
        }
    }

    public static void openAppForStore(Activity act, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + packageName));
        TNUtilsDialog.startIntent(act, intent,
                R.string.alert_About_CantOpenComment);
    }

    public static void inviteFriend(Activity act, long userId, String username) {
//		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
//		intent.setType("text/plain");
        String[] emailReciver = new String[]{};
        String emailSubject = act.getString(R.string.userinfo_invite_subject);
        String emailBody = null;

        emailBody = String.format(
                act.getString(R.string.userinfo_invite_body4),
                TNUtils.toInviteCode(userId));

        sendToEmail(act, emailReciver, emailSubject, emailBody);

        // 设置邮件默认地址
//		intent.putExtra(android.content.Intent.EXTRA_EMAIL, emailReciver);
//		// 设置邮件默认标题
//		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, emailSubject);
//		// 设置要默认发送的内容
//		intent.putExtra(android.content.Intent.EXTRA_TEXT, emailBody);
//
//		TNUtilsDialog.startIntent(act, intent,
//				R.string.alert_About_CantSendEmail);
    }

    public static void sendToEmail(Activity act, String[] reciver, String subject, String body) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");

        // 设置邮件默认地址
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, reciver);
        // 设置邮件默认标题
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        // 设置要默认发送的内容
        intent.putExtra(android.content.Intent.EXTRA_TEXT, body);

        TNUtilsDialog.startIntent(act, intent,
                R.string.alert_About_CantSendEmail);
    }

    public static void shareNote(Activity act, TNNote note) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        String[] emailReciver = new String[]{};

        // //设置邮件默认地址
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, emailReciver);
        // 设置邮件默认标题
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, note.title);
        // 设置要默认发送的内容
        String text = note.getPlainText();
        text += act.getString(R.string.noteview_share_from);
        intent.putExtra(android.content.Intent.EXTRA_TEXT, text);

        TNUtilsDialog.startIntent(act, intent,
                R.string.alert_About_CantSendEmail);
    }

    public static void shareContent(Activity act, String content, String title) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
        intent.putExtra(android.content.Intent.EXTRA_TEXT, content);

        TNUtilsDialog.startIntent(act, intent,
                R.string.alert_About_CantSendEmail);
    }

    public static void sendToSMS(Activity act, TNNote note) {
        String text = note.getPlainText();
        text += act.getString(R.string.noteview_share_from);
        sendToSMS(act, text);
    }

    public static void sendToSMS(Activity act, String content) {
        Uri smsToUri = Uri.parse("smsto:");
        Intent intent = new Intent(android.content.Intent.ACTION_SENDTO,
                smsToUri);
        intent.putExtra("sms_body", content);
        TNUtilsDialog
                .startIntent(act, intent, R.string.alert_About_CantSendSMS);
    }

    /**
     * 分享到微信好友和朋友圈
     *
     * @param act
     * @param note
     * @param isCycle true表示朋友圈，false表示好友
     */
    public static void sendToWX(Activity act, TNNote note, boolean isCycle) {
        IWXAPI api = WXAPIFactory.createWXAPI(act, TNConst.WX_APP_ID, true);
        api.registerApp(TNConst.WX_APP_ID);

        String text = note.getPlainText();
        text = TextUtils.isEmpty(text) ? "笔记无内容" : text;
        if (text.length() > 140) {
            text = text.substring(0, 140);
        }

        WXTextObject textObject = new WXTextObject();
        textObject.text = text;
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObject;
        msg.description = text;
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text");
        req.message = msg;
        req.scene = isCycle ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }

    /**
     * 分享到QQ好友和QQ空间
     *
     * @param act
     * @param note
     */
    public static void sendToQQ(Activity act, TNNote note, Tencent tencent, IUiListener listener) {
        String text = note.getPlainText();
        text = TextUtils.isEmpty(text) ? "笔记无内容" : text;
        if (text.length() > 50) {
            text = text.substring(0, 50);
        }
        Bundle bundle = new Bundle();
        //这条分享消息被好友点击后的跳转URL。
        bundle.putString(Constants.PARAM_TARGET_URL, "https://www.qingbiji.cn/showNote/" + note.noteId);
        //分享的标题。注：PARAM_TITLE、PARAM_IMAGE_URL、PARAM_SUMMARY不能全为空，最少必须有一个是有值的。
        bundle.putString(Constants.PARAM_TITLE, "");
        //分享的图片URL
        bundle.putString(Constants.PARAM_IMAGE_URL, "");
        //分享的消息摘要，最长50个字
        bundle.putString(Constants.PARAM_SUMMARY, text);
        //手Q客户端顶部，替换“返回”按钮文字，如果为空，用返回代替
        bundle.putString(Constants.PARAM_APPNAME, "");
        //标识该消息的来源应用，值为应用名称+AppId。
        bundle.putString(Constants.PARAM_APP_SOURCE, "轻笔记手机端" + TNConst.QQ_APP_ID);

        tencent.shareToQQ(act, bundle, listener);
    }

    public static String buildTransaction(String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis())
                : type + System.currentTimeMillis();
    }

    public static void showNotification(Activity act, int msgId, boolean isCancel) {
        MLog.d(TAG, "showNotification:" + act.getString(msgId));
        String service = Context.NOTIFICATION_SERVICE;
        final NotificationManager nm = (NotificationManager) act
                .getSystemService(service); // 获得系统级服务，用于管理消息
        Builder builder = new Notification.Builder(act);
        PendingIntent pi = PendingIntent.getActivity(act, 0, new Intent(), 0); // 消息触发后调用
        builder.setContentIntent(pi);
        Notification n = builder.setContentTitle("轻笔记")
                .setContentText(act.getString(msgId))
                .setSmallIcon(R.drawable.icon)
                .build();
        n.icon = R.drawable.icon; // 设置图标
        n.tickerText = act.getString(msgId); // 设置消息
        n.when = System.currentTimeMillis(); // 设置时间

        nm.notify(1, n); // 发送通知

        if (isCancel) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    nm.cancel(1);
                }
            }, 1000);
        }

    }

    public static void showNotification(Activity act, String msgStr, boolean isCancel) {
        MLog.d(TAG, "showNotification:" + msgStr);
        String service = Context.NOTIFICATION_SERVICE;
        final NotificationManager nm = (NotificationManager) act
                .getSystemService(service); // 获得系统级服务，用于管理消息
        Builder builder = new Notification.Builder(act);
        PendingIntent pi = PendingIntent.getActivity(act, 0, new Intent(), 0); // 消息触发后调用
        builder.setContentIntent(pi);
        Notification n = builder.setContentTitle("轻笔记")
                .setContentText(msgStr)
                .setSmallIcon(R.drawable.icon)
                .build();
        n.icon = R.drawable.icon; // 设置图标
        n.tickerText = msgStr; // 设置消息
        n.when = System.currentTimeMillis(); // 设置时间

        nm.notify(1, n); // 发送通知

        if (isCancel) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    nm.cancel(1);
                }
            }, 1000);
        }

    }

    public static void clearNotification(Activity act) {
        MLog.i(TAG, "clearNotification");
        String service = Context.NOTIFICATION_SERVICE;
        NotificationManager nm = (NotificationManager) act
                .getSystemService(service); // 获得系统级服务，用于管理消息
        nm.cancel(1);
    }

    public static void hideKeyboard(Activity act, int viewId) {
        InputMethodManager imm = (InputMethodManager) act
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(act.findViewById(viewId).getWindowToken(),
                0); // 隐藏软键盘
    }

    public static void showKeyBoard(Activity act, int viewId) {
        InputMethodManager imm = ((InputMethodManager) act
                .getSystemService(Context.INPUT_METHOD_SERVICE));
        imm.showSoftInput(act.findViewById(viewId), 0);
    }

    public static void showKeyBoard(Activity act, View v, int viewId) {
        InputMethodManager imm = ((InputMethodManager) act
                .getSystemService(Context.INPUT_METHOD_SERVICE));
        imm.showSoftInput(v.findViewById(viewId), 0);
    }

    public static String formatDate(Activity act, long milliseconds) {
        long now = System.currentTimeMillis() / 1000;
        long tzOffset = Calendar.getInstance().getTimeZone().getRawOffset() / 1000;
        long secToday = (now + tzOffset) / 86400 * 86400 - tzOffset;
        float hours = (secToday - milliseconds) / 3600f;

        // Log.i(TAG, "now=" + now + "tzOffset=" + tzOffset
        // + "secToday=" + secToday + "hours=" + hours);
        Date date = new Date(milliseconds * 1000L);
        String formated = null;
        if (hours < 144 && hours > -24) {
            if (hours <= 0) { // today
                formated = String.format(
                        act.getString(R.string.notelist_sformat),
                        act.getString(R.string.notelist_today),
                        date.getHours(), date.getMinutes());
            } else if (hours < 24) { // yestoday
                formated = String.format(
                        act.getString(R.string.notelist_sformat),
                        act.getString(R.string.notelist_yestoday),
                        date.getHours(), date.getMinutes());
            } else { // this week
                formated = String.format(act
                                .getString(R.string.notelist_mformat),
                        date.getMonth() + 1, date.getDate(), act.getResources()
                                .getTextArray(R.array.weeks)[date.getDay()]);
            }
        } else {
            formated = String.format(act.getString(R.string.notelist_lformat),
                    date.getYear() + 1900, date.getMonth() + 1, date.getDate());
        }

        MLog.i(TAG, "milliseconds=" + milliseconds + "now=" + now + "tzOffset="
                + tzOffset + "secToday=" + secToday + "hours=" + hours
                + "formated=" + formated);

        return formated;
    }

    public static String formatHighPrecisionDate(Activity act, long milliseconds) {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setTimeInMillis(milliseconds);
        return String.format(act.getString(R.string.noteedit_current_time),
                calendar.get(Calendar.YEAR),
                (calendar.get(Calendar.MONTH) + 1),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
    }

    public static void setMenuBackground(final Activity aAct) {
        MLog.d(TAG, "Enterting setMenuBackGround");
        aAct.getLayoutInflater().setFactory(new Factory() {

            @Override
            public View onCreateView(String name, Context context,
                                     AttributeSet attrs) {

                if (name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuItemView")) {

                    try { // Ask our inflater to create the view
                        LayoutInflater f = aAct.getLayoutInflater();
                        final View view = f.createView(name, null, attrs);
                        /*
                         * The background gets refreshed each time a new item is
                         * added the options menu. So each time Android applies
                         * the default background we need to set our own
                         * background. This is done using a thread giving the
                         * background change as runnable object
                         */
                        new Handler().post(new Runnable() {
                            public void run() {
                                view.setBackgroundResource(R.drawable.menu_selector);
                            }
                        });
                        return view;
                    } catch (InflateException e) {
                    } catch (ClassNotFoundException e) {
                    }
                }
                return null;
            }
        });
    }

    public static void openFile(String filePath) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);

        /* 设置intent的file与MimeType */
        Uri contentUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//7.0+版本安全设置
            contentUri = FileProvider.getUriForFile(TNApplication.getInstance(), BuildConfig.APPLICATION_ID + ".FileProvider", new File(filePath));
        } else {//7.0-正常调用
            contentUri = Uri.fromFile(new File(filePath));
        }
        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        TNSettings.getInstance().topAct.startActivity(intent);
    }

    public static boolean isCallFromOutside(Activity act) {
        ActivityManager am = (ActivityManager) act
                .getSystemService(Activity.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = am.getRunningTasks(5);
        for (RunningTaskInfo task : tasks) {
            if (task.id == act.getTaskId()) {
                String baseActName = task.baseActivity.getClassName();
                MLog.i(TAG, "taskId=" + act.getTaskId());
                MLog.i(TAG, "baseActName=" + baseActName);
                MLog.i(TAG, "topActName" + task.topActivity.getClassName());
                return !baseActName
                        .startsWith("com.thinkernote.ThinkerNote.Activity");
            }
        }
        return true;
    }

    public static void setEnabledViews(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (Button.class.isInstance(view) || ImageButton.class.isInstance(view))
            view.setFocusable(enabled);

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int idx = 0; idx < group.getChildCount(); idx++) {
                setEnabledViews(group.getChildAt(idx), enabled);
            }
        }
    }

    // private static int taskId = 0;
    private static String baseName = "";
    private static boolean screenOff = true;

    public static void checkLockScreen(Context context) {
        final ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        final KeyguardManager kg = (KeyguardManager) context
                .getSystemService(Context.KEYGUARD_SERVICE);

        RunningTaskInfo task = null;
        try {
            task = am.getRunningTasks(1).get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String baseActName = task.baseActivity.getClassName();
        boolean screenStatus = kg.inKeyguardRestrictedInputMode();
        if ((!baseActName.equals(baseName) && !baseActName
                .equals("com.thinkernote.ThinkerNote.Activity.TNMainAct"))
                || screenOff != screenStatus) {
            MLog.i(TAG, " screenOff:" + screenStatus + " topAct:"
                    + task.topActivity.getClassName() + " baseAct:"
                    + task.baseActivity.getClassName());
            // taskId = task.id;
            baseName = baseActName;
            screenOff = screenStatus;
            TNSettings settings = TNSettings.getInstance();
            if (!settings.needShowLock) {
                MLog.i(TAG, "set needShowLock = true");
                settings.needShowLock = true;
                settings.savePref(false);
            }
        }
    }

    public static void addHelpView(final Activity activity, int id) {
        ImageView iv = new ImageView(activity);
        iv.setImageResource(id);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewGroup rootView = (ViewGroup) activity
                        .findViewById(android.R.id.content);
                rootView.removeView(v);
            }
        });

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        activity.addContentView(iv, params);
    }
}
