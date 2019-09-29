package com.thinkernote.ThinkerNote.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Database.TNDb;
import com.thinkernote.ThinkerNote.Database.TNDbUtils;
import com.thinkernote.ThinkerNote.Database.TNSQLString;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsDialog;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.Views.ArrayWheelAdapter;
import com.thinkernote.ThinkerNote.Views.OnWheelChangedListener;
import com.thinkernote.ThinkerNote.Views.WheelView;
import com.thinkernote.ThinkerNote.base.TNActBase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * menu 属性
 * sjy 0615
 */
public class TNNoteInfoAct extends TNActBase implements OnClickListener, OnGroupClickListener {
    public static final int TAGINFO = 101;//1

    /*
     * Bundle: NoteLocalId
     */

    private Dialog mProgressDialog = null;
    private long mCreateTime;


    private long mNoteLocalId;
    private TNNote mNote;

    private WheelView mYearWheel, mMonthWheel, mDayWheel, mHourWheel, mMinuteWheel;
    private LinearLayout mWheelLayout;
    private int mYear;
    //
    LinearLayout ly_createTime, ly_creater;
    TextView tv_catName, tv_sync, tv_creater, tv_createTime, tv_lastTime, tv_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_noteinfo);
        mNoteLocalId = getIntent().getExtras().getLong("NoteLocalId");
        mNote = TNDbUtils.getNoteByNoteLocalId(mNoteLocalId);
        initMyView();
        initOther();
        setViews();
    }

    private void initOther() {

        mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);

        mYearWheel = (WheelView) findViewById(R.id.ak_fram_editalarm_year);
        mMonthWheel = (WheelView) findViewById(R.id.ak_fram_editalarm_month);
        mDayWheel = (WheelView) findViewById(R.id.ak_fram_editalarm_day);
        mHourWheel = (WheelView) findViewById(R.id.ak_fram_editalarm_hour);
        mMinuteWheel = (WheelView) findViewById(R.id.ak_fram_editalarm_minute);
        mWheelLayout = (LinearLayout) findViewById(R.id.ak_fram_editalarm_wheellayout);
        findViewById(R.id.ak_fram_editalarm_wheel_cancel).setOnClickListener(this);
        findViewById(R.id.ak_fram_editalarm_wheel_ok).setOnClickListener(this);
    }

    /**
     *
     */
    private void initMyView() {
        ly_createTime = findViewById(R.id.ly_createTime);
        ly_creater = findViewById(R.id.ly_creater);

        ly_createTime.setOnClickListener(this);
        findViewById(R.id.noteinfo_back).setOnClickListener(this);

        tv_catName = findViewById(R.id.tv_catName);
        tv_sync = findViewById(R.id.tv_sync);
        tv_creater = findViewById(R.id.tv_creater);
        tv_createTime = findViewById(R.id.tv_createTime);
        tv_lastTime = findViewById(R.id.tv_lastTime);
        tv_count = findViewById(R.id.tv_count);
        registerForContextMenu(findViewById(R.id.share_url_menu));

    }

    /**
     * 显示内容
     */
    private void showView() {
        TNSettings settings = TNSettings.getInstance();
        TNCat cat = TNDbUtils.getCat(mNote.catId);
        String catName = cat == null ? "" : cat.catName;
        tv_catName.setText(catName);

        //
        String sync = getString(R.string.noteinfo_no);
        if (mNote != null && mNote.syncState == 2) {
            sync = getString(R.string.noteinfo_yes);
        }
        tv_sync.setText(sync);

        //
        if (settings.isInProject()) {
            ly_creater.setVisibility(View.VISIBLE);
            tv_creater.setText(mNote.creatorNick);
        } else {
            ly_creater.setVisibility(View.GONE);
        }

        //
        tv_createTime.setText(formatDate(mNote.createTime));
        tv_count.setText(String.valueOf(mNote.content.length()));
    }

    @Override
    protected void configView() {
        showView();

    }

    // ContextMenu
    // -------------------------------------------------------------------------------
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        switch (v.getId()) {
            case R.id.share_url_menu:
                getMenuInflater().inflate(R.menu.share_url_menu, menu);
                break;

            default:
                MLog.d(TAG, "onCreateContextMenu default");
                break;
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share_url_menu_copy:
                ClipboardManager c = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                c.setText("http://www.qingbiji.cn/note/" + TNUtils.Hash17(mNote.noteId));
                break;

            case R.id.share_url_menu_send: {
                String msg = getString(R.string.shareinfo_publicnote_url, mNote.title, TNUtils.Hash17(mNote.noteId));
                String email = String.format("mailto:?subject=%s&body=%s", mNote.title, msg);
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(email));
                TNUtilsDialog.startIntent(this, intent, R.string.alert_About_CantSendEmail);
                break;
            }

            case R.id.share_url_menu_open: {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.qingbiji.cn/note/" + TNUtils.Hash17(mNote.noteId)));
                TNUtilsDialog.startIntent(this, intent, R.string.alert_About_CantOpenWeb);
                break;
            }

            case R.id.share_url_menu_sms: {
                String msg = getString(R.string.shareinfo_publicnote_url, mNote.title, TNUtils.Hash17(mNote.noteId));
                TNUtilsUi.sendToSMS(this, msg);
                break;
            }
            case R.id.share_url_menu_other: {
                String msg = getString(R.string.shareinfo_publicnote_url, mNote.title, TNUtils.Hash17(mNote.noteId));
                TNUtilsUi.shareContent(this, msg, "轻笔记分享");
                break;
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.noteinfo_toolbar_layout, R.drawable.toolbg);
        TNUtilsSkin.setViewBackground(this, null, R.id.noteinfo_page, R.drawable.page_bg);
    }

    @Override
    public void onDestroy() {
        mProgressDialog.dismiss();
        super.onDestroy();
    }


    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.noteinfo_back:
                finish();
                break;
            case R.id.ak_fram_editalarm_wheel_cancel:
                hidWheelView(true);
                break;
            case R.id.ak_fram_editalarm_wheel_ok:
                setSelectTime();
                hidWheelView(true);
                break;
            case R.id.ly_createTime:
                showWheelView();
                break;
        }
    }

    // Private methods
    // -------------------------------------------------------------------------------
    private String formatDate(long milliseconds) {
        Date date = new Date(milliseconds * 1000L);
        String formated = String.format(getString(R.string.noteinfo_lformat), date.getYear() + 1900,
                date.getMonth() + 1, date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds());

        return formated;
    }

    // ----------------------------------------------------------------------------------

    // new add time selector
    private void showWheelView() {
        if (mWheelLayout.getVisibility() == View.VISIBLE) {
            return;
        }

        mCreateTime = mNote.createTime * 1000L;
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setTimeInMillis(mCreateTime);

        mYear = calendar.get(Calendar.YEAR);
        final String yearValues[] = new String[60];
        for (int i = 0; i < 60; i++) {
            yearValues[i] = String.valueOf(1970 + i);
        }
        mYearWheel.setAdapter(new ArrayWheelAdapter<String>(yearValues));
        mYearWheel.setVisibleItems(5);
        mYearWheel.setCurrentItem(mYear - 1970);

        int month = calendar.get(Calendar.MONTH);
        final String monthValues[] = new String[12];
        for (int i = 0; i < 12; i++) {
            monthValues[i] = String.valueOf(i + 1);
            if (monthValues[i].length() == 1) {
                monthValues[i] = "0" + monthValues[i];
            }
        }
        mMonthWheel.setAdapter(new ArrayWheelAdapter<String>(monthValues));
        mMonthWheel.setVisibleItems(5);
        mMonthWheel.setCurrentItem(month);
        mMonthWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                Calendar c = Calendar.getInstance(Locale.CHINA);
                c.setTimeInMillis(System.currentTimeMillis());
                c.set(Calendar.MONTH, mMonthWheel.getCurrentItem());
                int maxDay = c.getActualMaximum(Calendar.DATE);
                String dayValues[] = new String[maxDay];
                for (int i = 0; i < maxDay; i++) {
                    dayValues[i] = String.valueOf(i + 1);
                    if (dayValues[i].length() == 1) {
                        dayValues[i] = "0" + dayValues[i];
                    }
                }
                mDayWheel.setAdapter(new ArrayWheelAdapter<String>(dayValues));
                if ((mDayWheel.getCurrentItem() + 1) > maxDay) {
                    mDayWheel.setCurrentItem(getPosition(dayValues, String.valueOf(maxDay)));
                }
            }
        });

        int maxDay = calendar.getActualMaximum(Calendar.DATE);
        final String dayValues[] = new String[maxDay];
        for (int i = 0; i < maxDay; i++) {
            dayValues[i] = String.valueOf(i + 1);
            if (dayValues[i].length() == 1) {
                dayValues[i] = "0" + dayValues[i];
            }
        }
        mDayWheel.setAdapter(new ArrayWheelAdapter<String>(dayValues));
        mDayWheel.setVisibleItems(5);
        mDayWheel.setCurrentItem((calendar.get(Calendar.DAY_OF_MONTH) - 1));

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final String hourValues[] = new String[24];
        for (int i = 0; i < 24; i++) {
            hourValues[i] = String.valueOf(i + 1);
            if (hourValues[i].length() == 1) {
                hourValues[i] = "0" + hourValues[i];
            }
        }
        mHourWheel.setAdapter(new ArrayWheelAdapter<String>(hourValues));
        mHourWheel.setVisibleItems(5);
        mHourWheel.setCurrentItem(hour - 1);

        int minute = calendar.get(Calendar.MINUTE);
        final String minValues[] = new String[60];
        for (int i = 0; i < 60; i++) {
            minValues[i] = String.valueOf(i + 1);
            if (minValues[i].length() == 1) {
                minValues[i] = "0" + minValues[i];
            }
        }
        mMinuteWheel.setAdapter(new ArrayWheelAdapter<String>(minValues));
        mMinuteWheel.setVisibleItems(5);
        mMinuteWheel.setCurrentItem(minute - 1);

        showWheelView(true);
    }

    private void setSelectTime() {
        int year = 1970 + mYearWheel.getCurrentItem();
        int month = mMonthWheel.getCurrentItem() + 1;
        int day = mDayWheel.getCurrentItem() + 1;
        int hour = mHourWheel.getCurrentItem() + 1;
        int minute = mMinuteWheel.getCurrentItem() + 1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date dt = sdf.parse(year + "/" + month + "/" + day + " " + hour + ":" + minute + ":" + "00");
            mNote.createTime = (int) (dt.getTime() / 1000);
            NoteLocalChangeCreateTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void NoteLocalChangeCreateTime() {
        final long noteLocalId = mNote.noteLocalId;
        final int createTime = mNote.createTime;
        final int lastUpdate = (int) (System.currentTimeMillis() / 1000);
        final TNNote note = TNDbUtils.getNoteByNoteLocalId(noteLocalId);
        final int syncState = note.noteId == -1 ? 3 : 4;

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                TNDb.beginTransaction();
                try {
                    //
                    TNDb.getInstance().execSQL(TNSQLString.NOTE_CHANGE_CREATETIME, createTime, syncState, lastUpdate, noteLocalId);
                    TNDb.getInstance().execSQL(TNSQLString.CAT_UPDATE_LASTUPDATETIME, System.currentTimeMillis() / 1000, note.catId);
                    TNDb.setTransactionSuccessful();
                } finally {
                    TNDb.endTransaction();
                }
                //
                handler.sendEmptyMessage(TAGINFO);
            }
        });
    }

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case TAGINFO://2-8-2的调用
                configView();
                break;
        }
    }

    private void showWheelView(boolean animate) {
        if (animate)
            mWheelLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.ak_translate_in_bottom));
        mWheelLayout.setVisibility(View.VISIBLE);
    }

    private void hidWheelView(boolean animate) {
        if (mWheelLayout.getVisibility() == View.GONE) {
            return;
        }
        if (animate)
            mWheelLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.ak_translate_out_bottom));
        mWheelLayout.setVisibility(View.GONE);
    }

    private int getPosition(String[] values, String str) {
        for (int i = 0; i < values.length; i++) {
            if (str.equals(values[i]))
                return i;
        }
        return -1;
    }

}
